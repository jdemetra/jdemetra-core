/*
 * Copyright 2015 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.tsp.extra.sdmx;

import demetra.data.AggregationType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.timeseries.util.TsDataBuilder;
import demetra.tsprovider.cube.CubeConnection;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSeries;
import demetra.tsprovider.cube.CubeSeriesWithData;
import nbbrd.design.VisibleForTesting;
import sdmxdl.*;
import sdmxdl.util.SdmxCubeUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SdmxCubeConnection implements CubeConnection {

    public static SdmxCubeConnection of(Connection connection, DataflowRef ref, List<String> dimensions, String labelAttribute, String sourceLabel) throws IOException {
        Dataflow flow = connection.getFlow(ref);
        DataStructure dsd = connection.getStructure(ref);
        CubeId root = getOrLoadRoot(dimensions, dsd);
        return new SdmxCubeConnection(connection, flow, dsd, root, labelAttribute, sourceLabel);
    }

    private final Connection connection;
    private final Dataflow flow;
    private final DataStructure dsd;
    private final CubeId root;
    private final String labelAttribute;
    private final String sourceLabel;

    @Override
    public Optional<IOException> testConnection() {
        try {
            connection.testConnection();
            return Optional.empty();
        } catch (IOException ex) {
            return Optional.of(ex);
        }
    }

    @Override
    public CubeId getRoot() {
        return root;
    }

    @Override
    public Stream<CubeSeries> getAllSeries(CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, ref);
        return SdmxCubeUtil
                .getAllSeries(connection, flow.getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, ref);
        return SdmxCubeUtil
                .getAllSeriesWithData(connection, flow.getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    @Override
    public Optional<CubeSeries> getSeries(CubeId id) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, id);
        return SdmxCubeUtil
                .getSeries(connection, flow.getRef(), converter.toKey(id))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    @Override
    public Optional<CubeSeriesWithData> getSeriesWithData(CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, ref);
        return SdmxCubeUtil
                .getSeriesWithData(connection, flow.getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    @Override
    public Stream<CubeId> getChildren(CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, ref);
        String dimensionId = ref.getDimensionId(ref.getLevel());
        int dimensionIndex = SdmxCubeUtil.getDimensionIndexById(dsd, dimensionId).orElseThrow(RuntimeException::new);
        return SdmxCubeUtil
                .getChildren(connection, flow.getRef(), converter.toKey(ref), dimensionIndex)
                .sorted()
                .map(ref::child);
    }

    @Override
    public String getDisplayName() {
        return String.format("%s ~ %s", sourceLabel, flow.getLabel());
    }

    @Override
    public String getDisplayName(CubeId id) {
        if (id.isVoid()) {
            return "All";
        }
        return getKey(dsd, id).toString();
    }

    @Override
    public String getDisplayNodeName(CubeId id) {
        if (id.isVoid()) {
            return "All";
        }
        return getDisplayNodeName(dsd, id);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static CubeSeries cubeSeriesOf(KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeries(converter.fromKey(series.getKey()), series.getMeta().get(labelAttribute), series.getMeta());
    }

    private static CubeSeriesWithData cubeSeriesWithDataOf(KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeriesWithData(converter.fromKey(series.getKey()), series.getMeta().get(labelAttribute), series.getMeta(), getData(series));
    }

    private static String getDisplayNodeName(DataStructure dsd, CubeId ref) {
        if (ref.isRoot()) {
            return "Invalid reference '" + dump(ref) + "'";
        }
        int index = ref.getLevel() - 1;
        Map<String, String> codes = SdmxCubeUtil.getDimensionById(dsd, ref.getDimensionId(index)).orElseThrow(RuntimeException::new).getCodes();
        String codeId = ref.getDimensionValue(index);
        return codes.getOrDefault(codeId, codeId);
    }

    private static String dump(CubeId ref) {
        return IntStream.range(0, ref.getMaxLevel())
                .mapToObj(o -> ref.getDimensionId(o) + "=" + (o < ref.getLevel() ? ref.getDimensionValue(0) : "null"))
                .collect(Collectors.joining(", "));
    }

    @VisibleForTesting
    static Key getKey(DataStructure dsd, CubeId ref) {
        if (ref.isRoot()) {
            return Key.ALL;
        }
        String[] result = new String[ref.getMaxLevel()];
        for (int i = 0; i < result.length; i++) {
            String id = ref.getDimensionId(i);
            String value = i < ref.getLevel() ? ref.getDimensionValue(i) : "";
            int index = SdmxCubeUtil.getDimensionIndexById(dsd, id).orElseThrow(RuntimeException::new);
            result[index] = value;
        }
        return Key.of(result);
    }

    @lombok.RequiredArgsConstructor
    static final class KeyConverter {

        static KeyConverter of(DataStructure dsd, CubeId ref) {
            return new KeyConverter(dsd, new CubeIdBuilder(dsd, ref));
        }

        final DataStructure dsd;
        final CubeIdBuilder builder;

        public Key toKey(CubeId a) {
            return getKey(dsd, a);
        }

        public CubeId fromKey(Key b) {
            return builder.getId(b);
        }
    }

    private static final class CubeIdBuilder {

        private final CubeId ref;
        private final int[] indices;
        private final String[] dimValues;

        CubeIdBuilder(DataStructure dsd, CubeId ref) {
            this.ref = ref;
            this.indices = new int[ref.getDepth()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = SdmxCubeUtil.getDimensionIndexById(dsd, ref.getDimensionId(ref.getLevel() + i)).orElseThrow(RuntimeException::new);
            }
            this.dimValues = new String[indices.length];
        }

        public CubeId getId(Key o) {
            for (int i = 0; i < indices.length; i++) {
                dimValues[i] = o.get(indices[i]);
            }
            return ref.child(dimValues);
        }
    }

    private static TsData getData(Series series) {
        switch (series.getObs().size()) {
            case 0:
                return TsData.empty("No data");
            case 1:
                return TsDataBuilder
                        .byDateTime(SINGLE_GATHERING)
                        .addAll(series.getObs().stream(), Obs::getPeriod, Obs::getValue)
                        .build();
            default:
                return TsDataBuilder
                        .byDateTime(DEFAULT_GATHERING)
                        .addAll(series.getObs().stream(), Obs::getPeriod, Obs::getValue)
                        .build();
        }
    }

    private static final ObsGathering DEFAULT_GATHERING = ObsGathering
            .builder()
            .includeMissingValues(true)
            .unit(TsUnit.UNDEFINED)
            .aggregationType(AggregationType.None)
            .build();

    private static final ObsGathering SINGLE_GATHERING = ObsGathering
            .builder()
            .includeMissingValues(true)
            .unit(TsUnit.YEAR)
            .aggregationType(AggregationType.None)
            .build();

    private static CubeId getOrLoadRoot(List<String> dimensions, DataStructure dsd) {
        return dimensions.isEmpty()
                ? CubeId.root(loadDefaultDimIds(dsd))
                : CubeId.root(dimensions);
    }

    private static List<String> loadDefaultDimIds(DataStructure dsd) {
        return dsd
                .getDimensions()
                .stream()
                .map(Dimension::getId)
                .collect(Collectors.toList());
    }
    //</editor-fold>
}

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
import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import sdmxdl.*;
import sdmxdl.util.SdmxCubeUtil;

import java.io.IOException;
import java.util.EnumMap;
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

    public static SdmxCubeConnection of(IOSupplier<SdmxConnection> supplier, DataflowRef ref, List<String> dimensions, String labelAttribute, String sourceLabel) throws IOException {
        try (SdmxConnection conn = supplier.getWithIO()) {
            Dataflow flow = conn.getFlow(ref);
            DataStructure dsd = conn.getStructure(ref);
            CubeId root = getOrLoadRoot(dimensions, dsd);
            return new SdmxCubeConnection(supplier, flow, dsd, root, labelAttribute, sourceLabel);
        }
    }

    private final IOSupplier<SdmxConnection> supplier;
    private final Dataflow flow;
    private final DataStructure dsd;
    private final CubeId root;
    private final String labelAttribute;
    private final String sourceLabel;

    @Override
    public Optional<IOException> testConnection() {
        return Optional.empty();
    }

    @Override
    public CubeId getRoot() {
        return root;
    }

    @Override
    public Stream<CubeSeries> getAllSeries(CubeId ref) throws IOException {
        SdmxConnection conn = supplier.getWithIO();
        try {
            return getAllSeries(conn, flow, dsd, ref, labelAttribute).onClose(IORunnable.unchecked(conn::close));
        } catch (IOException ex) {
            throw close(conn, ex);
        }
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId ref) throws IOException {
        SdmxConnection conn = supplier.getWithIO();
        try {
            return getAllSeriesWithData(conn, flow, dsd, ref, labelAttribute).onClose(IORunnable.unchecked(conn::close));
        } catch (IOException ex) {
            throw close(conn, ex);
        }
    }

    @Override
    public Optional<CubeSeries> getSeries(CubeId id) throws IOException {
        try (SdmxConnection conn = supplier.getWithIO()) {
            return getSeries(conn, flow, dsd, id, labelAttribute);
        }
    }

    @Override
    public Optional<CubeSeriesWithData> getSeriesWithData(CubeId ref) throws IOException {
        try (SdmxConnection conn = supplier.getWithIO()) {
            return getSeriesWithData(conn, flow, dsd, ref, labelAttribute);
        }
    }

    @Override
    public Stream<CubeId> getChildren(CubeId ref) throws IOException {
        SdmxConnection conn = supplier.getWithIO();
        try {
            return getChildren(conn, flow, dsd, ref).onClose(IORunnable.unchecked(conn::close));
        } catch (IOException ex) {
            throw close(conn, ex);
        }
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
    public void close() {
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static Stream<CubeSeries> getAllSeries(SdmxConnection conn, Dataflow flow, DataStructure dsd, CubeId node, String labelAttribute) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, node);
        return SdmxCubeUtil
                .getAllSeries(conn, flow.getRef(), converter.toKey(node))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    private static Stream<CubeSeriesWithData> getAllSeriesWithData(SdmxConnection conn, Dataflow flow, DataStructure dsd, CubeId node, String labelAttribute) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, node);
        return SdmxCubeUtil
                .getAllSeriesWithData(conn, flow.getRef(), converter.toKey(node))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    private static Optional<CubeSeries> getSeries(SdmxConnection conn, Dataflow flow, DataStructure dsd, CubeId leaf, String labelAttribute) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, leaf);
        return SdmxCubeUtil
                .getSeries(conn, flow.getRef(), converter.toKey(leaf))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    private static Optional<CubeSeriesWithData> getSeriesWithData(SdmxConnection conn, Dataflow flow, DataStructure dsd, CubeId leaf, String labelAttribute) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, leaf);
        return SdmxCubeUtil
                .getSeriesWithData(conn, flow.getRef(), converter.toKey(leaf))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    private static Stream<CubeId> getChildren(SdmxConnection conn, Dataflow flow, DataStructure dsd, CubeId node) throws IOException {
        KeyConverter converter = KeyConverter.of(dsd, node);
        String dimensionId = node.getDimensionId(node.getLevel());
        int dimensionIndex = SdmxCubeUtil.getDimensionIndexById(dsd, dimensionId).orElseThrow(RuntimeException::new);
        return SdmxCubeUtil
                .getChildren(conn, flow.getRef(), converter.toKey(node), dimensionIndex)
                .map(node::child);
    }

    private static CubeSeries cubeSeriesOf(KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeries(converter.fromKey(series.getKey()), series.getMeta().get(labelAttribute), series.getMeta());
    }

    private static CubeSeriesWithData cubeSeriesWithDataOf(KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeriesWithData(converter.fromKey(series.getKey()), series.getMeta().get(labelAttribute), series.getMeta(), getData(series));
    }

    private static <EX extends Throwable> EX close(SdmxConnection conn, EX ex) {
        try {
            conn.close();
        } catch (IOException other) {
            ex.addSuppressed(other);
        }
        return ex;
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
        return TsDataBuilder
                .byDateTime(GATHERINGS.get(series.getFreq()))
                .addAll(series.getObs().stream(), Obs::getPeriod, Obs::getValue)
                .build();
    }

    private static final Map<Frequency, ObsGathering> GATHERINGS = initGatherings();

    private static Map<Frequency, ObsGathering> initGatherings() {
        Map<Frequency, ObsGathering> result = new EnumMap<>(Frequency.class);
        result.put(Frequency.ANNUAL, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.YEAR).aggregationType(AggregationType.None).build());
        result.put(Frequency.HALF_YEARLY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.HALF_YEAR).aggregationType(AggregationType.None).build());
        result.put(Frequency.QUARTERLY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.QUARTER).aggregationType(AggregationType.None).build());
        result.put(Frequency.MONTHLY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.MONTH).aggregationType(AggregationType.None).build());
        result.put(Frequency.WEEKLY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.WEEK).aggregationType(AggregationType.Last).build());
        result.put(Frequency.DAILY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.DAY).aggregationType(AggregationType.Last).build());
        result.put(Frequency.HOURLY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.HOUR).aggregationType(AggregationType.Last).build());
        result.put(Frequency.DAILY_BUSINESS, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.DAY).aggregationType(AggregationType.Last).build());
        result.put(Frequency.MINUTELY, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.MINUTE).aggregationType(AggregationType.Last).build());
        result.put(Frequency.UNDEFINED, ObsGathering.builder().includeMissingValues(true).unit(TsUnit.UNDEFINED).aggregationType(AggregationType.None).build());
        return result;
    }

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

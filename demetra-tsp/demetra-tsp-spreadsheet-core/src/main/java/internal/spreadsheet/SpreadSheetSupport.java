/*
 * Copyright 2016 National Bank of Belgium
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
package internal.spreadsheet;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsData;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.stream.DataSetTs;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.MultiLineNameUtil;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class SpreadSheetSupport implements HasDataHierarchy, HasTsStream {

    @ThreadSafe
    public interface Resource {

        @NonNull
        SpreadSheetAccessor getAccessor(@NonNull DataSource dataSource) throws IOException;

        @NonNull
        IParam<DataSet, String> getSheetParam(@NonNull DataSource dataSource);

        @NonNull
        IParam<DataSet, String> getSeriesParam(@NonNull DataSource dataSource);
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final Resource resource;

    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        SpreadSheetAccessor accessor = resource.getAccessor(dataSource);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);

        return accessor
                .getSheetNames()
                .stream()
                .map(childrenMapper(dataSource, sheetParam))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);

        SpreadSheetAccessor accessor = resource.getAccessor(parent.getDataSource());
        IParam<DataSet, String> sheetParam = resource.getSheetParam(parent.getDataSource());
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(parent.getDataSource());

        return accessor
                .getSheetByName(sheetParam.get(parent))
                .orElseThrow(() -> sheetNotFound(parent))
                .stream()
                .map(childrenMapper(parent, seriesParam))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<DataSetTs> getData(DataSource dataSource, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        SpreadSheetAccessor accessor = resource.getAccessor(dataSource);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSource);

        Stream<SheetTs> data = accessor
                .getSheets()
                .stream()
                .flatMap(SheetTs::allOf);

        return streamOf(data, dataSource, sheetParam, seriesParam);
    }

    @Override
    public Stream<DataSetTs> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        SpreadSheetAccessor accessor = resource.getAccessor(dataSet.getDataSource());
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSet.getDataSource());
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSet.getDataSource());

        Stream<SheetTs> data = accessor
                .getSheetByName(sheetParam.get(dataSet))
                .map(SheetTs::allOf)
                .orElseThrow(() -> sheetNotFound(dataSet))
                .filter(getSeriesFilter(dataSet, seriesParam));

        return streamOf(data, dataSet.getDataSource(), sheetParam, seriesParam);
    }

    private static IOException sheetNotFound(DataSet dataSet) {
        return new IOException("Sheet not found: " + dataSet.toString());
    }

    private static Function<String, DataSet> childrenMapper(DataSource dataSource, IParam<DataSet, String> sheetParam) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        return o -> builder.put(sheetParam, o).build();
    }

    private static Function<Ts, DataSet> childrenMapper(DataSet parent, IParam<DataSet, String> seriesParam) {
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        return o -> builder.put(seriesParam, o.getName()).build();
    }

    private static Function<SheetTs, DataSet> dataMapper(DataSource dataSource, IParam<DataSet, String> sheetParam, IParam<DataSet, String> seriesParam) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        return o -> builder.put(sheetParam, o.sheetName).put(seriesParam, o.seriesName).build();
    }

    private Predicate<SheetTs> getSeriesFilter(DataSet dataSet, IParam<DataSet, String> seriesParam) {
        switch (dataSet.getKind()) {
            case COLLECTION:
                return o -> true;
            case SERIES:
                return o -> o.seriesName.equals(seriesParam.get(dataSet));
            default:
                throw new IllegalArgumentException(dataSet.getKind().name());
        }
    }

    private static Stream<DataSetTs> streamOf(Stream<SheetTs> data, DataSource dataSource, IParam<DataSet, String> sheetParam, IParam<DataSet, String> seriesParam) {
        Function<SheetTs, DataSet> mapper = dataMapper(dataSource, sheetParam, seriesParam);
        return data.map(sheetTs -> new DataSetTs(mapper.apply(sheetTs), sheetTs.getLabel(), sheetTs.getMeta(), sheetTs.getData()));
    }

    @lombok.AllArgsConstructor
    private static final class SheetTs {

        static Stream<SheetTs> allOf(TsCollection sheet) {
            return sheet.stream().map(series -> SheetTs.of(sheet, series));
        }

        static SheetTs of(TsCollection sheet, Ts series) {
            return new SheetTs(sheet.getName(), sheet.getMeta().getOrDefault(GridLayout.PROPERTY, ""), series.getName(), series.getData());
        }

        private final String sheetName;
        private final String sheetLayout;
        private final String seriesName;
        private final TsData seriesData;

        TsData getData() {
            return seriesData;
        }

        Map<String, String> getMeta() {
            Map<String, String> result = new HashMap<>();
            result.put(SHEET_NAME_META, sheetName);
            result.put(SHEET_GRID_LAYOUT_META, sheetLayout);
            result.put(SERIES_NAME_META, seriesName);
            return result;
        }

        String getLabel() {
            return sheetName + MultiLineNameUtil.SEPARATOR + seriesName;
        }
    }

    public static final String SHEET_NAME_META = "sheet.name";
    public static final String SHEET_GRID_LAYOUT_META = "sheet.gridLayout";
    public static final String SERIES_NAME_META = "series.name";
}

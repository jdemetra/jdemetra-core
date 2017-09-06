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

import internal.spreadsheet.grid.GridSheet;
import internal.spreadsheet.grid.GridSeries;
import internal.spreadsheet.grid.GridBook;
import demetra.io.FunctionWithIO;
import demetra.tsprovider.DataSet;
import static demetra.tsprovider.DataSet.Kind.COLLECTION;
import static demetra.tsprovider.DataSet.Kind.SERIES;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursor;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.MultiLineNameUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class SpreadSheetSupport implements HasDataHierarchy, HasTsCursor {

    @ThreadSafe
    public interface Resource {

        @Nonnull
        GridBook getAccessor(@Nonnull DataSource dataSource) throws IOException;

        @Nonnull
        IParam<DataSet, String> getSheetParam(@Nonnull DataSource dataSource);

        @Nonnull
        IParam<DataSet, String> getSeriesParam(@Nonnull DataSource dataSource);
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final Resource resource;

    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        Collection<GridSheet> sheets = getBook(dataSource).getSheets().values();
        return sheets.stream()
                .sorted(Comparator.comparing(GridSheet::getOrdering))
                .map(childrenMapper(dataSource))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);
        GridSheet sheet = lookupSheet(parent);
        if (sheet == null) {
            throw dataNotFound(parent);
        }
        return sheet.getRanges().stream()
                .sorted(Comparator.comparing(GridSeries::getOrdering))
                .map(childrenMapper(parent))
                .collect(Collectors.toList());
    }

    @Override
    public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        Stream<TsRecord> data = getDataStream(dataSource);
        return cursorOf(data, dataSource);
    }

    @Override
    public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Stream<TsRecord> data = getDataStream(dataSet);
        return cursorOf(data, dataSet.getDataSource());
    }

    private static IOException dataNotFound(DataSet dataSet) {
        return new IOException("Data not found: " + dataSet.toString());
    }

    private Function<GridSheet, DataSet> childrenMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        return o -> builder.put(sheetParam, o.getSheetName()).build();
    }

    private Function<GridSeries, DataSet> childrenMapper(DataSet parent) {
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(parent.getDataSource());
        return o -> builder.put(seriesParam, o.getSeriesName()).build();
    }

    private FunctionWithIO<TsRecord, DataSet> dataMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSource);
        return o -> builder.put(sheetParam, o.sheet.getSheetName()).put(seriesParam, o.series.getSeriesName()).build();
    }

    private Stream<TsRecord> getDataStream(DataSource dataSource) throws IOException {
        Collection<GridSheet> sheets = getBook(dataSource).getSheets().values();
        return sheets.stream().flatMap(sheet -> sheet.getRanges().stream().map(series -> new TsRecord(sheet, series)));
    }

    private Stream<TsRecord> getDataStream(DataSet dataSet) throws IOException {
        switch (dataSet.getKind()) {
            case COLLECTION: {
                GridSheet sheet = lookupSheet(dataSet);
                if (sheet == null) {
                    throw dataNotFound(dataSet);
                }
                return sheet.getRanges().stream().map(series -> new TsRecord(sheet, series));
            }
            case SERIES: {
                GridSheet sheet = lookupSheet(dataSet);
                if (sheet == null) {
                    throw dataNotFound(dataSet);
                }
                GridSeries series = lookupSeries(dataSet);
                if (series == null) {
                    throw dataNotFound(dataSet);
                }
                return Stream.of(new TsRecord(sheet, series));
            }
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    private TsCursor<DataSet> cursorOf(Stream<TsRecord> data, DataSource dataSource) {
        return TsCursor.from(data.iterator(), TsRecord::getData, TsRecord::getMeta, TsRecord::getLabel).map(dataMapper(dataSource));
    }

    private GridBook getBook(DataSource dataSource) throws IOException {
        return resource.getAccessor(dataSource);
    }

    private GridSheet lookupSheet(DataSet dataSet) throws IOException {
        GridBook book = getBook(dataSet.getDataSource());
        IParam<DataSet, String> param = resource.getSheetParam(dataSet.getDataSource());
        return book.getSheetByName(param.get(dataSet));
    }

    private GridSeries lookupSeries(DataSet dataSet) throws IOException {
        GridSheet sheet = lookupSheet(dataSet);
        if (sheet == null) {
            return null;
        }
        IParam<DataSet, String> param = resource.getSeriesParam(dataSet.getDataSource());
        return sheet.getSeriesByName(param.get(dataSet));
    }

    @lombok.AllArgsConstructor
    private static final class TsRecord {

        private final GridSheet sheet;
        private final GridSeries series;

        OptionalTsData getData() {
            return series.getData();
        }

        Map<String, String> getMeta() {
            Map<String, String> result = new HashMap<>();
            result.put(SHEET_NAME_META, sheet.getSheetName());
            result.put(SERIES_NAME_META, series.getSeriesName());
            result.put(SERIES_GRID_TYPE_META, series.getGridType().name());
            result.put(SERIES_ORDERING_META, Integer.toString(series.getOrdering()));
            return result;
        }

        String getLabel() {
            return sheet.getSheetName() + MultiLineNameUtil.SEPARATOR + series.getSeriesName();
        }
    }

    public static final String SHEET_NAME_META = "sheet.name";
    public static final String SERIES_NAME_META = "series.name";
    public static final String SERIES_GRID_TYPE_META = "series.gridType";
    public static final String SERIES_ORDERING_META = "series.ordering";
}

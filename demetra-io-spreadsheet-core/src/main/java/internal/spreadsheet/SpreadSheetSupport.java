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

import demetra.timeseries.TsData;
import demetra.tsprovider.DataSet;
import static demetra.tsprovider.DataSet.Kind.COLLECTION;
import static demetra.tsprovider.DataSet.Kind.SERIES;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursor;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.MultiLineNameUtil;
import internal.spreadsheet.grid.BookData;
import ioutil.IO;
import java.io.IOException;
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
        BookData getAccessor(@Nonnull DataSource dataSource) throws IOException;

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
        List<TsCollection> sheets = getBook(dataSource).getSheets();
        return sheets.stream()
                .map(childrenMapper(dataSource))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);
        TsCollection sheet = lookupSheet(parent);
        if (sheet == null) {
            throw dataNotFound(parent);
        }
        return sheet.getItems().stream()
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

    private Function<TsCollection, DataSet> childrenMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        return o -> builder.put(sheetParam, o.getName()).build();
    }

    private Function<Ts, DataSet> childrenMapper(DataSet parent) {
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(parent.getDataSource());
        return o -> builder.put(seriesParam, o.getName()).build();
    }

    private IO.Function<TsRecord, DataSet> dataMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSource);
        return o -> builder.put(sheetParam, o.sheet.getName()).put(seriesParam, o.series.getName()).build();
    }

    private Stream<TsRecord> getDataStream(DataSource dataSource) throws IOException {
        List<TsCollection> sheets = getBook(dataSource).getSheets();
        return sheets.stream().flatMap(sheet -> sheet.getItems().stream().map(series -> new TsRecord(sheet, series)));
    }

    private Stream<TsRecord> getDataStream(DataSet dataSet) throws IOException {
        switch (dataSet.getKind()) {
            case COLLECTION: {
                TsCollection sheet = lookupSheet(dataSet);
                if (sheet == null) {
                    throw dataNotFound(dataSet);
                }
                return sheet.getItems().stream().map(series -> new TsRecord(sheet, series));
            }
            case SERIES: {
                TsCollection sheet = lookupSheet(dataSet);
                if (sheet == null) {
                    throw dataNotFound(dataSet);
                }
                Ts series = lookupSeries(dataSet);
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

    private BookData getBook(DataSource dataSource) throws IOException {
        return resource.getAccessor(dataSource);
    }

    private TsCollection lookupSheet(DataSet dataSet) throws IOException {
        BookData book = getBook(dataSet.getDataSource());
        IParam<DataSet, String> param = resource.getSheetParam(dataSet.getDataSource());
        return book.getSheetByName(param.get(dataSet));
    }

    private Ts lookupSeries(DataSet dataSet) throws IOException {
        TsCollection sheet = lookupSheet(dataSet);
        if (sheet == null) {
            return null;
        }
        IParam<DataSet, String> param = resource.getSeriesParam(dataSet.getDataSource());
        return sheet
                .getItems()
                .stream()
                .filter(o -> o.getName().equals(param.get(dataSet)))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    @lombok.AllArgsConstructor
    private static final class TsRecord {

        private final TsCollection sheet;
        private final Ts series;

        TsData getData() {
            return series.getData();
        }

        Map<String, String> getMeta() {
            Map<String, String> result = new HashMap<>();
            result.put(SHEET_NAME_META, sheet.getName());
            result.put(SHEET_GRID_LAYOUT_META, sheet.getMetaData().getOrDefault("gridLayout", ""));
            result.put(SERIES_NAME_META, series.getName());
            return result;
        }

        String getLabel() {
            return sheet.getName() + MultiLineNameUtil.SEPARATOR + series.getName();
        }
    }

    public static final String SHEET_NAME_META = "sheet.name";
    public static final String SHEET_GRID_LAYOUT_META = "sheet.gridLayout";
    public static final String SERIES_NAME_META = "series.name";
}

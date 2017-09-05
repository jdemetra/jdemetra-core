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
package demetra.spreadsheet;

import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataHierarchy;
import internal.spreadsheet.SpreadSheetCollection;
import internal.spreadsheet.SpreadSheetSeries;
import internal.spreadsheet.SpreadSheetSource;
import ec.tss.tsproviders.utils.DataSourcePreconditions;
import ec.tss.tsproviders.cursor.HasTsCursor;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.cursor.TsCursor;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.MetaData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
final class SpreadSheetSupport implements HasDataHierarchy, HasTsCursor {

    @ThreadSafe
    public interface Resource {

        @Nonnull
        SpreadSheetSource getAccessor(@Nonnull DataSource dataSource) throws IOException;

        @Nonnull
        IParam<DataSet, String> getSheetParam(@Nonnull DataSource dataSource);

        @Nonnull
        IParam<DataSet, String> getSeriesParam(@Nonnull DataSource dataSource);
    }

    @Nonnull
    public static SpreadSheetSupport of(@Nonnull String providerName, @Nonnull Resource resource) {
        return new SpreadSheetSupport(providerName, resource);
    }

    private final String providerName;
    private final Resource resource;

    private SpreadSheetSupport(String providerName, Resource resource) {
        this.providerName = providerName;
        this.resource = resource;
    }

    //<editor-fold defaultstate="collapsed" desc="HasDataHierarchy">
    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        Collection<SpreadSheetCollection> data = getSource(dataSource).getCollections().values();
        return data.stream()
                .sorted()
                .map(childrenMapper(dataSource))
                .collect(Collectors.toList());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);
        Collection<SpreadSheetSeries> data = getCollection(parent).orElseThrow(() -> dataNotFound(parent)).getSeries();
        return data.stream()
                .sorted()
                .map(childrenMapper(parent))
                .collect(Collectors.toList());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasTsCursor">
    @Override
    public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        Stream<Tuple> data = getDataStream(dataSource);
        return cursorOf(data, dataSource);
    }

    @Override
    public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Stream<Tuple> data = getDataStream(dataSet);
        return cursorOf(data, dataSet.getDataSource());
    }
    //</editor-fold>

    private static IOException dataNotFound(DataSet dataSet) {
        return new IOException("Data not found: " + dataSet.toString());
    }

    private Function<SpreadSheetCollection, DataSet> childrenMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        return o -> {
            sheetParam.set(builder, o.getSheetName());
            return builder.build();
        };
    }

    private Function<SpreadSheetSeries, DataSet> childrenMapper(DataSet parent) {
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(parent.getDataSource());
        return o -> {
            seriesParam.set(builder, o.getSeriesName());
            return builder.build();
        };
    }

    private Function<Tuple, DataSet> dataMapper(DataSource dataSource) {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        IParam<DataSet, String> sheetParam = resource.getSheetParam(dataSource);
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSource);
        return o -> {
            sheetParam.set(builder, o.col.getSheetName());
            seriesParam.set(builder, o.series.getSeriesName());
            return builder.build();
        };
    }

    private Stream<Tuple> getDataStream(DataSource dataSource) throws IOException {
        Collection<SpreadSheetCollection> data = getSource(dataSource).getCollections().values();
        return data.stream().flatMap(col -> col.getSeries().stream().map(series -> new Tuple(col, series)));
    }

    private Stream<Tuple> getDataStream(DataSet dataSet) throws IOException {
        switch (dataSet.getKind()) {
            case COLLECTION: {
                SpreadSheetCollection data = getCollection(dataSet).orElseThrow(() -> dataNotFound(dataSet));
                return Stream.of(data).flatMap(col -> col.getSeries().stream().map(series -> new Tuple(col, series)));
            }
            case SERIES: {
                SpreadSheetCollection data = getCollection(dataSet).orElseThrow(() -> dataNotFound(dataSet));
                SpreadSheetSeries series = getSeries(dataSet).orElseThrow(() -> dataNotFound(dataSet));
                return Stream.of(new Tuple(data, series));
            }
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    private TsCursor<DataSet> cursorOf(Stream<Tuple> data, DataSource dataSource) {
        return TsCursor.from(data.iterator(), Tuple::getData, Tuple::getMeta, Tuple::getLabel).transform(dataMapper(dataSource));
    }

    private static final class Tuple {

        private final SpreadSheetCollection col;
        private final SpreadSheetSeries series;

        public Tuple(SpreadSheetCollection col, SpreadSheetSeries series) {
            this.col = col;
            this.series = series;
        }

        OptionalTsData getData() {
            return series.getData();
        }

        MetaData getMeta() {
            MetaData result = new MetaData();
            result.put("sheet.name", col.getSheetName());
            result.put("series.name", series.getSeriesName());
            result.put("series.alignType", series.getAlignType().name());
            result.put("series.ordering", Integer.toString(series.getOrdering()));
            return result;
        }

        String getLabel() {
            return col.getSheetName() + MultiLineNameUtil.SEPARATOR + series.getSeriesName();
        }
    }

    private SpreadSheetSource getSource(DataSource dataSource) throws IOException {
        return resource.getAccessor(dataSource);
    }

    private Optional<SpreadSheetCollection> getCollection(DataSet dataSet) throws IOException {
        SpreadSheetSource ws = getSource(dataSet.getDataSource());
        return getCollectionByName(ws, resource.getSheetParam(dataSet.getDataSource()).get(dataSet));
    }

    private Optional<SpreadSheetSeries> getSeries(DataSet dataSet) throws IOException {
        IParam<DataSet, String> seriesParam = resource.getSeriesParam(dataSet.getDataSource());
        return getCollection(dataSet)
                .map(worksheet -> {
                    String s = seriesParam.get(dataSet);
                    return getSeriesByName(worksheet, s);
                });
    }

    private static Optional<SpreadSheetCollection> getCollectionByName(SpreadSheetSource ws, String name) {
        return Optional.ofNullable(ws.getCollections().get(clean(name)));
    }

    private static SpreadSheetSeries getSeriesByName(SpreadSheetCollection col, String name) {
        for (SpreadSheetSeries o : col.getSeries()) {
            if (o.getSeriesName().equals(name)) {
                return o;
            }
        }
        String s = clean(name);
        for (SpreadSheetSeries o : col.getSeries()) {
            if (o.getSeriesName().equals(s)) {
                return o;
            }
        }
        return null;
    }

    private static String clean(String s) {
        // probably we should change the CharSet, but it is not very clear how and which one
        int l = s.lastIndexOf('$');
        if (l < 0) {
            return s;
        }
        s = s.substring(0, l);
        if (s.charAt(0) == '\'') {
            s = s.substring(1);
        }
        return s.replace('#', '.');
    }
}

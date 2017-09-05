/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.spreadsheet;

import internal.spreadsheet.SpreadSheetLegacy;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import static ec.tss.tsproviders.spreadsheet.SpreadSheetBean.X_CLEAN_MISSING;
import internal.spreadsheet.SpreadSheetFactory;
import internal.spreadsheet.SpreadSheetCollection;
import internal.spreadsheet.SpreadSheetSeries;
import internal.spreadsheet.SpreadSheetSource;
import internal.spreadsheet.TsImportOptions;
import ec.tss.tsproviders.utils.*;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Demortier Jeremy
 */
@ServiceProvider(service = ITsProvider.class)
public final class SpreadSheetProvider extends AbstractFileLoader<SpreadSheetSource, SpreadSheetBean> {

    private static final String SOURCE = "XCLPRVDR";
    private static final String VERSION = "20111201";
    private static final IParam<DataSet, String> Y_SHEETNAME = Params.onString("", "sheetName");
    private static final IParam<DataSet, String> Z_SERIESNAME = Params.onString("", "seriesName");
    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadSheetProvider.class);
    private final IParser<DataSource> legacyDataSourceParser;
    private final IParser<DataSet> legacyDataSetParser;

    public SpreadSheetProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.Once);
        this.legacyDataSourceParser = o -> SpreadSheetLegacy.parseLegacyDataSource(o, LegacyConverterImpl.INSTANCE);
        this.legacyDataSetParser = o -> SpreadSheetLegacy.parseLegacyDataSet(o, LegacyConverterImpl.INSTANCE);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSet result = super.toDataSet(moniker);
        if (result != null) {
            return result;
        }
        synchronized (legacyDataSetParser) {
            return legacyDataSetParser.parse(moniker.getId());
        }
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSource result = super.toDataSource(moniker);
        if (result != null) {
            return result;
        }
        synchronized (legacyDataSourceParser) {
            return legacyDataSourceParser.parse(moniker.getId());
        }
    }

    @Override
    protected SpreadSheetSource loadFromBean(SpreadSheetBean bean) throws Exception {
        File file = getRealFile(bean.getFile());
        Book.Factory factory = getFactoryByFile(file);
        if (factory != null) {
            try (Book book = factory.load(file)) {
                ObsGathering gathering = bean.isCleanMissing()
                        ? ObsGathering.excludingMissingValues(bean.getFrequency(), bean.getAggregationType())
                        : ObsGathering.includingMissingValues(bean.getFrequency(), bean.getAggregationType());
                TsImportOptions options = TsImportOptions.of(bean.getDataFormat(), gathering);
                return SpreadSheetFactory.getDefault().toSource(book, options);
            }
        }
        throw new RuntimeException("File type not supported");
    }

    @Override
    public String getDisplayName() {
        return "Spreadsheets";
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        SpreadSheetBean bean = decodeBean(dataSource);
        return TsFrequency.Undefined != bean.getFrequency()
                ? bean.getFile().getPath() + " " + OptionalTsData.Builder.toString(bean.getFrequency(), bean.getAggregationType())
                : bean.getFile().getPath();
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        support.check(dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return Y_SHEETNAME.get(dataSet);
            case SERIES:
                return Y_SHEETNAME.get(dataSet) + MultiLineNameUtil.SEPARATOR + Z_SERIESNAME.get(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        support.check(dataSource);
        SpreadSheetSource ws = getSource(dataSource);
        if (ws.getCollections().isEmpty()) {
            return Collections.emptyList();
        }

        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        return ws.getCollections().values().stream()
                .sorted()
                .map(o -> builder.put(Y_SHEETNAME, o.getSheetName()).build())
                .collect(Collectors.toList());
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        support.check(dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return Y_SHEETNAME.get(dataSet);
            case SERIES:
                return Z_SERIESNAME.get(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        support.check(parent, DataSet.Kind.COLLECTION);

        SpreadSheetCollection col = getCollection(parent);
        if (col == null || col.getSeries().isEmpty()) {
            return Collections.emptyList();
        }

        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        return col.getSeries().stream()
                .sorted()
                .map(o -> builder.put(Z_SERIESNAME, o.getSeriesName()).build())
                .collect(Collectors.toList());
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        SpreadSheetSource source = getSource(dataSource);
        //info.moniker.setName();
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        source.getCollections().values().stream()
                .sorted()
                .forEach(o -> {
                    DataSet child = builder.put(Y_SHEETNAME, o.getSheetName()).build();
                    info.items.addAll(getAll(child, o));
                });
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        SpreadSheetCollection collection = getCollection(dataSet);
        if (collection == null) {
            throw new IOException("null");
        }
        info.name = collection.getSheetName();
        info.type = TsInformationType.All;
        info.items.addAll(getAll(dataSet, collection));
    }

    List<TsInformation> getAll(DataSet dataSet, SpreadSheetCollection collection) {
        if (collection.getSeries().isEmpty()) {
            return Collections.emptyList();
        }
        DataSet.Builder builder = dataSet.toBuilder(DataSet.Kind.SERIES);
        return collection.getSeries().stream()
                .map(o -> {
                    DataSet child = builder.put(Z_SERIESNAME, o.getSeriesName()).build();
                    return support.fillSeries(newTsInformation(child, TsInformationType.All), o.getData(), X_CLEAN_MISSING.get(dataSet.getDataSource()));
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        SpreadSheetSeries series = getSeries(dataSet);
        if (series == null) {
            throw new IOException("null");
        }
        support.fillSeries(info, series.getData(), X_CLEAN_MISSING.get(dataSet.getDataSource()));
        info.name = getDisplayName(dataSet);
        info.type = TsInformationType.All;
    }

    private SpreadSheetSource getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    private SpreadSheetCollection getCollection(DataSet dataSet) throws IOException {
        SpreadSheetSource ws = getSource(dataSet.getDataSource());
        return search(ws, Y_SHEETNAME.get(dataSet));
    }

    private SpreadSheetSeries getSeries(DataSet dataSet) throws IOException {
        SpreadSheetCollection worksheet = getCollection(dataSet);
        if (worksheet == null) {
            return null;
        }
        String s = Z_SERIESNAME.get(dataSet);
        //       String s = clean(Z_SERIESNAME.get(dataSet));
        for (SpreadSheetSeries o : worksheet.getSeries()) {
            if (o.getSeriesName().equals(s)) {
                return o;
            }
        }
        s = clean(s);
        for (SpreadSheetSeries o : worksheet.getSeries()) {
            if (o.getSeriesName().equals(s)) {
                return o;
            }
        }
        return null;
    }

    private static SpreadSheetCollection search(SpreadSheetSource ws, String cname) {
        if (ws == null) {
            return null;
        }
        return ws.getCollections().get(clean(cname));
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

    @Override
    public SpreadSheetBean newBean() {
        return new SpreadSheetBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        try {
            return ((SpreadSheetBean) bean).toDataSource(SOURCE, VERSION);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public SpreadSheetBean decodeBean(DataSource dataSource) {
        return new SpreadSheetBean(support.check(dataSource));
    }

    @Override
    public boolean accept(File pathname) {
        return getFactoryByFile(pathname) != null;
    }

    @Override
    public String getFileDescription() {
        return "Spreadsheet file";
    }

    @Nullable
    private Book.Factory getFactoryByFile(@Nonnull File file) {
        for (Book.Factory o : ServiceLoader.load(Book.Factory.class)) {
            if (o.canLoad() && o.accept(file)) {
                return o;
            }
        }
        return null;
    }

    private enum LegacyConverterImpl implements SpreadSheetLegacy.Converter {

        INSTANCE;

        @Override
        public DataSource toSource(File file) {
            SpreadSheetBean bean = new SpreadSheetBean();
            bean.setFile(file);
            return bean.toDataSource(SpreadSheetProvider.SOURCE, SpreadSheetProvider.VERSION);
        }

        @Override
        public DataSet toCollection(DataSource dataSource, String sheetName) {
            return DataSet.builder(dataSource, DataSet.Kind.COLLECTION)
                    .put(SpreadSheetProvider.Y_SHEETNAME, sheetName)
                    .build();
        }

        @Override
        public DataSet toSeries(DataSource dataSource, String sheetName, String seriesName) {
            return DataSet.builder(dataSource, DataSet.Kind.SERIES)
                    .put(SpreadSheetProvider.Y_SHEETNAME, sheetName)
                    .put(SpreadSheetProvider.Z_SERIESNAME, seriesName)
                    .build();
        }

        @Override
        public DataSet toSeries(DataSource dataSource, String sheetName, int seriesIndex) {
            SpreadSheetProvider tmp = TsProviders.lookup(SpreadSheetProvider.class, dataSource).get();
            SpreadSheetSource col;
            try {
                col = tmp.getSource(dataSource);
            } catch (Exception ex) {
                return null;
            }
            SpreadSheetCollection cur = SpreadSheetProvider.search(col, sheetName);
            if (cur == null) {
                return null;
            }
            String seriesName = seriesIndex < cur.getSeries().size() ? cur.getSeries().get(seriesIndex).getSeriesName() : null;
            return toSeries(dataSource, sheetName, seriesName);
        }
    }
}

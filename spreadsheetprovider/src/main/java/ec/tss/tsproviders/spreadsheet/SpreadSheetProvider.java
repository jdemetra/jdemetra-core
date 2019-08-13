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

import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import static ec.tss.tsproviders.spreadsheet.SpreadSheetBean.X_CLEAN_MISSING;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSeries;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSource;
import ec.tss.tsproviders.spreadsheet.engine.TsImportOptions;
import ec.tss.tsproviders.utils.*;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import nbbrd.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Demortier Jeremy
 */
@ServiceProvider(ITsProvider.class)
public class SpreadSheetProvider extends AbstractFileLoader<SpreadSheetSource, SpreadSheetBean> {

    public static final String SOURCE = "XCLPRVDR";
    public static final String VERSION = "20111201";
    public static final IParam<DataSet, String> Y_SHEETNAME = Params.onString("", "sheetName");
    public static final IParam<DataSet, String> Z_SERIESNAME = Params.onString("", "seriesName");
    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadSheetProvider.class);
    protected final Parsers.Parser<DataSource> legacyDataSourceParser;
    protected final Parsers.Parser<DataSet> legacyDataSetParser;

    public SpreadSheetProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.Once);
        this.legacyDataSourceParser = SpreadSheetLegacy.legacyDataSourceParser();
        this.legacyDataSetParser = SpreadSheetLegacy.legacyDataSetParser();
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
                TsImportOptions options = TsImportOptions.create(bean.getDataFormat(), gathering);
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
        if (ws.collections.isEmpty()) {
            return Collections.emptyList();
        }

        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        return ws.collections.values().stream()
                .sorted()
                .map(o -> builder.put(Y_SHEETNAME, o.sheetName).build())
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
        if (col == null || col.series.isEmpty()) {
            return Collections.emptyList();
        }

        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        return col.series.stream()
                .sorted()
                .map(o -> builder.put(Z_SERIESNAME, o.seriesName).build())
                .collect(Collectors.toList());
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        SpreadSheetSource source = getSource(dataSource);
        //info.moniker.setName();
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        source.collections.values().stream()
                .sorted()
                .forEach(o -> {
                    DataSet child = builder.put(Y_SHEETNAME, o.sheetName).build();
                    info.items.addAll(getAll(child, o));
                });
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        SpreadSheetCollection collection = getCollection(dataSet);
        if (collection == null) {
            throw new IOException("null");
        }
        info.name = collection.sheetName;
        info.type = TsInformationType.All;
        info.items.addAll(getAll(dataSet, collection));
    }

    List<TsInformation> getAll(DataSet dataSet, SpreadSheetCollection collection) {
        if (collection.series.isEmpty()) {
            return Collections.emptyList();
        }
        DataSet.Builder builder = dataSet.toBuilder(DataSet.Kind.SERIES);
        return collection.series.stream()
                .map(o -> {
                    DataSet child = builder.put(Z_SERIESNAME, o.seriesName).build();
                    return support.fillSeries(newTsInformation(child, TsInformationType.All), o.data, X_CLEAN_MISSING.get(dataSet.getDataSource()));
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        SpreadSheetSeries series = getSeries(dataSet);
        if (series == null) {
            throw new IOException("null");
        }
        support.fillSeries(info, series.data, X_CLEAN_MISSING.get(dataSet.getDataSource()));
        info.name = getDisplayName(dataSet);
        info.type = TsInformationType.All;
    }

    @NonNull
    public SpreadSheetSource getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    public SpreadSheetCollection getCollection(DataSet dataSet) throws IOException {
        SpreadSheetSource ws = getSource(dataSet.getDataSource());
        return search(ws, Y_SHEETNAME.get(dataSet));
    }

    public SpreadSheetSeries getSeries(DataSet dataSet) throws IOException {
        SpreadSheetCollection worksheet = getCollection(dataSet);
        if (worksheet == null) {
            return null;
        }
        String s = Z_SERIESNAME.get(dataSet);
        //       String s = clean(Z_SERIESNAME.get(dataSet));
        for (SpreadSheetSeries o : worksheet.series) {
            if (o.seriesName.equals(s)) {
                return o;
            }
        }
        s = clean(s);
        for (SpreadSheetSeries o : worksheet.series) {
            if (o.seriesName.equals(s)) {
                return o;
            }
        }
        return null;
    }

    static SpreadSheetCollection search(SpreadSheetSource ws, String cname) {
        if (ws == null) {
            return null;
        }
        return ws.collections.get(clean(cname));
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

    private Book.@Nullable Factory getFactoryByFile(@NonNull File file) {
        for (Book.Factory o : ServiceLoader.load(Book.Factory.class)) {
            if (o.canLoad() && o.accept(file)) {
                return o;
            }
        }
        return null;
    }
}

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

import com.google.common.collect.Ordering;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import static ec.tss.tsproviders.spreadsheet.SpreadSheetBean.X_CLEAN_MISSING;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetParser;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSeries;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSource;
import ec.tss.tsproviders.utils.*;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Book;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Demortier Jeremy
 */
public class SpreadSheetProvider extends AbstractFileLoader<SpreadSheetSource, SpreadSheetBean> {

    public static final String SOURCE = "XCLPRVDR";
    public static final String VERSION = "20111201";
    public static final IParam<DataSet, String> Y_SHEETNAME = Params.onString("", "sheetName");
    public static final IParam<DataSet, String> Z_SERIESNAME = Params.onString("", "seriesName");
    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadSheetProvider.class);
    protected final Parsers.Parser<DataSource> legacyDataSourceParser;
    protected final Parsers.Parser<DataSet> legacyDataSetParser;

    public SpreadSheetProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.None);
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
                return SpreadSheetParser.getDefault().parse(book, bean.dataFormat.dateParser(), bean.dataFormat.numberParser(), bean.frequency, bean.aggregationType, bean.cleanMissing);
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

        List<SpreadSheetCollection> tmp = Ordering.natural().sortedCopy(ws.collections.values());

        DataSet[] children = new DataSet[tmp.size()];
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        for (int i = 0; i < children.length; i++) {
            Y_SHEETNAME.set(builder, tmp.get(i).sheetName);
            children[i] = builder.build();
        }
        return Arrays.asList(children);
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

        List<SpreadSheetSeries> tmp = Ordering.natural().sortedCopy(col.series);

        DataSet[] children = new DataSet[tmp.size()];
        DataSet.Builder builder = DataSet.builder(parent, DataSet.Kind.SERIES);
        for (int i = 0; i < children.length; i++) {
            Z_SERIESNAME.set(builder, tmp.get(i).seriesName);
            children[i] = builder.build();
        }
        return Arrays.asList(children);
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        SpreadSheetSource source = getSource(dataSource);
        //info.moniker.setName();
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        for (SpreadSheetCollection o : Ordering.natural().sortedCopy(source.collections.values())) {
            Y_SHEETNAME.set(builder, o.sheetName);
            info.items.addAll(getAll(builder.build(), o));
        }
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
        TsInformation[] result = new TsInformation[collection.series.size()];
        DataSet.Builder builder = DataSet.builder(dataSet, DataSet.Kind.SERIES);
        for (int i = 0; i < result.length; i++) {
            SpreadSheetSeries o = collection.series.get(i);
            Z_SERIESNAME.set(builder, o.seriesName);
            result[i] = support.fillSeries(newTsInformation(builder.build(), TsInformationType.All), o.data, X_CLEAN_MISSING.get(dataSet.getDataSource()));
        }
        return Arrays.asList(result);
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        SpreadSheetSeries series = getSeries(dataSet);
        if (series == null) {
            throw new IOException("null");
        }
        support.fillSeries(info, series.data, X_CLEAN_MISSING.get(dataSet.getDataSource()));
        info.type = TsInformationType.All;
    }

    @Nonnull
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
        return ((SpreadSheetBean) bean).toDataSource(SOURCE, VERSION);
    }

    @Override
    public SpreadSheetBean decodeBean(DataSource dataSource) {
        return new SpreadSheetBean(dataSource);
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
}

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
package ec.tss.tsproviders.common.xml;

import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import ec.tss.tsproviders.utils.AbstractFileLoader;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Params;
import ec.tss.tsproviders.utils.Parsers;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceProvider(service = ITsProvider.class)
public class XmlProvider extends AbstractFileLoader<wsTsWorkspace, XmlBean> {

    public static final String SOURCE = "Xml";
    public static final String VERSION = "20111201";
    static final IParam<DataSet, Integer> Y_COLLECTIONINDEX = Params.onInteger(-1, "collectionIndex");
    static final IParam<DataSet, Integer> Z_SERIESINDEX = Params.onInteger(-1, "seriesIndex");
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlProvider.class);

    protected final Parsers.Parser<DataSource> legacyDataSourceParser;
    protected final Parsers.Parser<DataSet> legacyDataSetParser;

    public XmlProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.Once);
        this.legacyDataSourceParser = XmlLegacy.dataSourceParser();
        this.legacyDataSetParser = XmlLegacy.dataSetParser();
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
    protected wsTsWorkspace loadFromBean(XmlBean bean) throws IOException {
        File file = getRealFile(bean.getFile());
        return wsTsWorkspace.parse(file);
    }

    @Override
    public String getDisplayName() {
        return "Xml files";
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        support.check(dataSet);
        wsTsWorkspace ws = cache.getIfPresent(dataSet.getDataSource());
        if (ws == null) {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return Y_COLLECTIONINDEX.get(dataSet).toString();
                case SERIES:
                    return Y_COLLECTIONINDEX.get(dataSet) + " - " + Z_SERIESINDEX.get(dataSet);
            }
        } else {
            wsTsCollection col = ws.tsclist[Y_COLLECTIONINDEX.get(dataSet)];
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.name;
                case SERIES:
                    return col.name + " - " + col.tslist[Z_SERIESINDEX.get(dataSet)].name;
            }
        }
        return "";
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        support.check(dataSource);
        wsTsWorkspace ws = getSource(dataSource);
        if (ws == null || ws.tsclist.length == 0) {
            return Collections.emptyList();
        }

        DataSet[] children = new DataSet[ws.tsclist.length];
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        for (int i = 0; i < children.length; i++) {
            children[i] = builder.put(Y_COLLECTIONINDEX, i).build();
        }
        return Arrays.asList(children);
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        support.check(dataSet);
        wsTsWorkspace ws = cache.getIfPresent(dataSet.getDataSource());
        if (ws == null) {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return Y_COLLECTIONINDEX.get(dataSet).toString();
                case SERIES:
                    return Z_SERIESINDEX.get(dataSet).toString();
            }
        } else {
            wsTsCollection col = ws.tsclist[Y_COLLECTIONINDEX.get(dataSet)];
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.name;
                case SERIES:
                    return col.tslist[Z_SERIESINDEX.get(dataSet)].name;
            }
        }
        return "";
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        support.check(parent, DataSet.Kind.COLLECTION);

        wsTsWorkspace ws = getSource(parent.getDataSource());
        int index = Y_COLLECTIONINDEX.get(parent);
        if (ws == null || index >= ws.tsclist.length || ws.tsclist[index].tslist.length == 0) {
            return Collections.emptyList();
        }

        DataSet[] children = new DataSet[ws.tsclist[index].tslist.length];
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        for (int i = 0; i < children.length; i++) {
            children[i] = builder.put(Z_SERIESINDEX, i).build();
        }
        return Arrays.asList(children);
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        wsTsWorkspace source = getSource(dataSource);
        if (source == null) {
            throw new IOException("null");
        }
        //info.moniker.setName(collection.name);
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        for (int i = 0; i < source.tsclist.length; i++) {
            DataSet child = builder.put(Y_COLLECTIONINDEX, i).build();
            info.items.addAll(getAll(child, source.tsclist[i]));
        }
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        wsTsCollection collection = getCollection(dataSet);
        if (collection == null) {
            throw new IOException("null");
        }
        // add the name...
        info.name = collection.name;
        // build information from the collection
        info.type = TsInformationType.All;
        info.items.addAll(getAll(dataSet, collection));
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        wsTs series = getSeries(dataSet);
        if (series == null) {
            throw new IOException("null");
        }
        info.data = series.tsdata.create();
        info.name = getDisplayName(dataSet);
        info.type = TsInformationType.All;
    }

    List<TsInformation> getAll(DataSet dataSet, wsTsCollection collection) {
        if (collection.tslist == null || collection.tslist.length == 0) {
            return Collections.emptyList();
        }
        TsInformation[] result = new TsInformation[collection.tslist.length];
        DataSet.Builder builder = dataSet.toBuilder(DataSet.Kind.SERIES);
        for (int i = 0; i < result.length; ++i) {
            wsTs s = collection.tslist[i];
            DataSet child = builder.put(Z_SERIESINDEX, i).build();
            OptionalTsData data = s.tsdata != null ? OptionalTsData.present(s.tsdata.create()) : OptionalTsData.absent("No data");
            result[i] = support.fillSeries(newTsInformation(child, TsInformationType.All), data, true);
        }
        return Arrays.asList(result);
    }

    private wsTsWorkspace getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    private wsTsCollection getCollection(DataSet dataSet) throws IOException {
        return search(getSource(dataSet.getDataSource()), Y_COLLECTIONINDEX.get(dataSet));
    }

    private wsTs getSeries(DataSet dataSet) throws IOException {
        return search(getSource(dataSet.getDataSource()), Y_COLLECTIONINDEX.get(dataSet), Z_SERIESINDEX.get(dataSet));
    }

    private wsTsCollection search(wsTsWorkspace ws, int coll) {
        if (ws == null || ws.tsclist == null || coll >= ws.tsclist.length) {
            return null;
        }
        return ws.tsclist[coll];
    }

    private wsTs search(wsTsWorkspace ws, int coll, int pos) {
        if (ws == null || ws.tsclist == null || coll >= ws.tsclist.length) {
            return null;
        }
        wsTsCollection cur = ws.tsclist[coll];
        if (cur.tslist != null && pos < cur.tslist.length) {
            return cur.tslist[pos];
        } else {
            return null;
        }
    }

    @Override
    public XmlBean newBean() {
        return new XmlBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        try {
            return ((XmlBean) bean).toDataSource(SOURCE, VERSION);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public XmlBean decodeBean(DataSource dataSource) {
        return new XmlBean(support.check(dataSource));
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.getPath().toLowerCase(Locale.ENGLISH).endsWith(".xml");
    }

    @Override
    public String getFileDescription() {
        return "Xml file";
    }
}

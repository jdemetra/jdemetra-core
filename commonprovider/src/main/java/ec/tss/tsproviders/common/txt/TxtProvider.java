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
package ec.tss.tsproviders.common.txt;

import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import static ec.tss.tsproviders.common.txt.TxtBean.X_CLEAN_MISSING;
import ec.tss.tsproviders.utils.AbstractFileLoader;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Params;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceProvider(service = ITsProvider.class)
public class TxtProvider extends AbstractFileLoader<TxtSource, TxtBean> {

    public static final String SOURCE = "Txt";
    public static final String VERSION = "20111201";
    static final IParam<DataSet, Integer> Z_SERIESINDEX = Params.onInteger(-1, "seriesIndex");
    private static final Logger LOGGER = LoggerFactory.getLogger(TxtProvider.class);

    private final TxtFileFilter fileFilter;
    protected final Parsers.Parser<DataSource> legacyDataSourceParser;
    protected final Parsers.Parser<DataSet> legacyDataSetParser;

    public TxtProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.Once);
        this.fileFilter = new TxtFileFilter();
        this.legacyDataSourceParser = TxtLegacy.dataSourceParser();
        this.legacyDataSetParser = TxtLegacy.dataSetParser();
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
    protected TxtSource loadFromBean(TxtBean bean) throws Exception {
        File realFile = getRealFile(bean.getFile());
        return TxtLoader.load(realFile, bean);
    }

    @Override
    public String getDisplayName() {
        return "Txt files";
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        TxtBean bean = decodeBean(dataSource);
        return TsFrequency.Undefined != bean.getFrequency()
                ? bean.getFile().getPath() + " " + OptionalTsData.Builder.toString(bean.getFrequency(), bean.getAggregationType())
                : bean.getFile().getPath();
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        support.check(dataSet);
        Integer index = Z_SERIESINDEX.get(dataSet);
        TxtSource tmp = cache.getIfPresent(dataSet.getDataSource());
        if (tmp == null) {
            return "Column " + index;
        }
        return tmp.items.get(index).name;
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        support.check(dataSource);
        TxtSource tmp = getSource(dataSource);
        if (tmp.items.isEmpty()) {
            return Collections.emptyList();
        }

        DataSet[] children = new DataSet[tmp.items.size()];
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (int i = 0; i < children.length; i++) {
            Z_SERIESINDEX.set(builder, i);
            children[i] = builder.build();
        }
        return Arrays.asList(children);
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        TxtSource source = getSource(dataSource);
        //info.moniker.setName(TxtBean.FILE.get(dataSource).getPath());
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (TxtSeries o : source.items) {
            Z_SERIESINDEX.set(builder, o.index);
            info.items.add(support.fillSeries(newTsInformation(builder.build(), TsInformationType.All), o.data, X_CLEAN_MISSING.get(dataSource)));
        }
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        TxtSeries series = getSeries(dataSet);
        support.fillSeries(info, series.data, X_CLEAN_MISSING.get(dataSet.getDataSource()));
        info.name = getDisplayName(dataSet);
        info.type = TsInformationType.All;
    }

    public TxtSource getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    public TxtSeries getSeries(DataSet dataSet) throws IOException {
        return getSource(dataSet.getDataSource()).items.get(Z_SERIESINDEX.get(dataSet));
    }

    @Override
    public TxtBean newBean() {
        return new TxtBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        try {
            return ((TxtBean) bean).toDataSource(SOURCE, VERSION);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public TxtBean decodeBean(DataSource dataSource) {
        return new TxtBean(support.check(dataSource));
    }

    @Override
    public boolean accept(File pathname) {
        return fileFilter.accept(pathname);
    }

    @Override
    public String getFileDescription() {
        return fileFilter.getDescription();
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        Objects.requireNonNull(parent);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        return getDisplayName(dataSet);
    }
}

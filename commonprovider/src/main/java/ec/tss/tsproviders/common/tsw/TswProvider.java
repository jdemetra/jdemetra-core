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
package ec.tss.tsproviders.common.tsw;

import com.google.common.collect.ImmutableList;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.utils.AbstractFileLoader;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.Params;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
public class TswProvider extends AbstractFileLoader<TswSource, TswBean> {

    public static final String SOURCE = "TSW";
    public static final String VERSION = "20111201";
    static final IParam<DataSet, String> Z_FILENAME = Params.onString("", "fileName");
    static final IParam<DataSet, String> Z_NAME = Params.onString("", "name");
    private static final Logger LOGGER = LoggerFactory.getLogger(TswProvider.class);

    public TswProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.None);
    }

    @Override
    public String getDisplayName() {
        return "TSW files";
    }

    @Override
    public TswBean newBean() {
        return new TswBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        return ((TswBean) bean).toDataSource(SOURCE, VERSION);
    }

    @Override
    public TswBean decodeBean(DataSource dataSource) {
        return new TswBean(dataSource);
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }

    @Override
    public String getFileDescription() {
        return "TSW file";
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TswSource loadFromBean(TswBean bean) throws Exception {
        return TswFactory.getDefault().load(getRealFile(bean.getFile()).toPath());
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (TswSeries o : getSource(dataSource).items) {
            Z_FILENAME.set(builder, o.fileName);
            Z_NAME.set(builder, o.name);
            info.items.add(support.fillSeries(newTsInformation(builder.build(), TsInformationType.All), o.data, true));
        }
        info.type = TsInformationType.All;
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        TswSeries series = getSeries(dataSet);
        support.fillSeries(info, series.data, true);
        info.type = TsInformationType.All;
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        support.check(dataSource);
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        ImmutableList.Builder<DataSet> result = ImmutableList.builder();
        for (TswSeries series : getSource(dataSource).items) {
            Z_FILENAME.set(builder, series.fileName);
            Z_NAME.set(builder, series.name);
            result.add(builder.build());
        }
        return result.build();
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        support.check(dataSet);
        try {
            return getSeries(dataSet).name;
        } catch (IOException ex) {
            return Z_FILENAME.get(dataSet);
        }
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        return getDisplayName(dataSet);
    }

    public TswSource getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    public TswSeries getSeries(DataSet dataSet) throws IOException {
        support.check(dataSet);
        String fileName = Z_FILENAME.get(dataSet);
        String name = Z_NAME.get(dataSet);
        List<TswSeries> items = getSource(dataSet.getDataSource()).items;
        if (!name.isEmpty()) {
            // NewFactory
            for (TswSeries s : items) {
                if (s.fileName.equals(fileName) && s.name.equals(name)) {
                    return s;
                }
            }
        } else {
            // OldFactory
            for (TswSeries s : items) {
                if (s.fileName.equals(fileName)) {
                    return s;
                }
            }
        }
        throw new FileNotFoundException(fileName);
    }
}

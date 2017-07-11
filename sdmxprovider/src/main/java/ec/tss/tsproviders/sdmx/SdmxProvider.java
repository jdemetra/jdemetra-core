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
package ec.tss.tsproviders.sdmx;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import static ec.tss.tsproviders.sdmx.SdmxBean.X_TITLE_ATTRIBUTE;
import ec.tss.tsproviders.sdmx.engine.CunningPlanFactory;
import ec.tss.tsproviders.sdmx.engine.ISdmxSourceFactory;
import ec.tss.tsproviders.sdmx.model.SdmxGroup;
import ec.tss.tsproviders.sdmx.model.SdmxItem;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tss.tsproviders.utils.AbstractFileLoader;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.Params;
import ec.tstoolkit.MetaData;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(service = ITsProvider.class)
public class SdmxProvider extends AbstractFileLoader<SdmxSource, SdmxBean> {

    public static final String SOURCE = "TSProviders.Sdmx.SdmxProvider";
    public static final String VERSION = "20120106";

    private static final IParam<DataSet, String> Y_GROUP_ID = Params.onString("", "group");
    private static final IParam<DataSet, String> Z_SERIES_ID = Params.onString("", "series");

    private static final Logger LOGGER = LoggerFactory.getLogger(SdmxProvider.class);

    private final ISdmxSourceFactory[] factories;
    private final IParser<DataSource> legacyDataSourceParser;
    private final IParser<DataSet> legacyDataSetParser;
    private final Splitter.MapSplitter keyValueSplitter;
    private final Joiner compactNamingJoiner;
    private boolean compactNaming;
    private boolean keysInMetaData;

    public SdmxProvider() {
        super(LOGGER, SOURCE, TsAsyncMode.Once);
        this.factories = new ISdmxSourceFactory[]{new CunningPlanFactory()};
        this.legacyDataSourceParser = SdmxLegacy.dataSourceParser();
        this.legacyDataSetParser = SdmxLegacy.dataSetParser();
        this.keyValueSplitter = Splitter.on(',').trimResults().withKeyValueSeparator('=');
        this.compactNamingJoiner = Joiner.on('.');
        this.compactNaming = false;
        this.keysInMetaData = false;
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        DataSet result = super.toDataSet(moniker);
        if (result != null) {
            return result;
        }
        String id = moniker.getId();
        return id != null ? legacyDataSetParser.parse(id) : null;
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        DataSource result = super.toDataSource(moniker);
        if (result != null) {
            return result;
        }
        String id = moniker.getId();
        return id != null ? legacyDataSourceParser.parse(id) : null;
    }

    @Override
    protected SdmxSource loadFromBean(SdmxBean bean) throws Exception {
        File file = getRealFile(bean.getFile());
        for (ISdmxSourceFactory o : factories) {
            if (o.getName().equals(bean.getFactory())) {
                return o.create(file);
            }
        }
        throw new Exception("Unknown factory '" + bean.factory + "'");
    }

    private MetaData getMetaData(SdmxSeries series) {
        MetaData result = new MetaData();
        if (compactNaming) {
            for (Entry<String, String> o : series.key) {
                result.put(o.getKey(), o.getValue());
            }
        }
        for (Entry<String, String> o : series.attributes) {
            result.put(o.getKey(), o.getValue());
        }
        return result;
    }

    private TsInformation newTsInformation(DataSet dataSet, SdmxSeries series) {
        TsInformation result = newTsInformation(dataSet, TsInformationType.All);
        result.metaData = getMetaData(series);
        support.fillSeries(result, series.data, true);
        return result;
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        SdmxSource source = getSource(dataSource);
        info.type = TsInformationType.All;
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (SdmxItem o : source.items) {
            if (o instanceof SdmxGroup) {
                SdmxGroup group = ((SdmxGroup) o);
                builder.put(Y_GROUP_ID, group.id);
                for (SdmxSeries series : group.series) {
                    builder.put(Z_SERIES_ID, series.id);
                    info.items.add(newTsInformation(builder.build(), series));
                }
            } else {
                SdmxSeries series = (SdmxSeries) o;
                DataSet child = builder.put(Y_GROUP_ID, "").put(Z_SERIES_ID, series.id).build();
                info.items.add(newTsInformation(child, series));
            }
        }
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        SdmxGroup group = getGroup(dataSet);
        info.type = TsInformationType.All;
        DataSet.Builder builder = dataSet.toBuilder(DataSet.Kind.SERIES);
        for (SdmxSeries series : group.series) {
            DataSet child = builder.put(Y_GROUP_ID, group.id).put(Z_SERIES_ID, series.id).build();
            info.items.add(newTsInformation(child, series));
        }
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        SdmxSeries series = getSeries(dataSet);
        info.name = getDisplayName(dataSet);
        info.type = TsInformationType.All;
        info.metaData = getMetaData(series);
        support.fillSeries(info, series.data, true);
    }

    @Override
    public String getDisplayName() {
        return "SDMX files";
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        support.check(dataSource);
        ImmutableList.Builder<DataSet> result = ImmutableList.builder();
        DataSet.Builder cBuilder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
        DataSet.Builder sBuilder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (SdmxItem o : getSource(dataSource).items) {
            if (o instanceof SdmxGroup) {
                result.add(cBuilder.put(Y_GROUP_ID, o.id).build());
            } else {
                result.add(sBuilder.put(Y_GROUP_ID, "").put(Z_SERIES_ID, o.id).build());
            }
        }
        return result.build();
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        support.check(dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getLabel(Y_GROUP_ID.get(dataSet), X_TITLE_ATTRIBUTE.get(dataSet.getDataSource()));
            case SERIES:
                String prefix = Y_GROUP_ID.get(dataSet).trim();
                String fullId = (prefix.isEmpty() ? "" : (prefix + ", ")) + Z_SERIES_ID.get(dataSet);
                return getLabel(fullId, X_TITLE_ATTRIBUTE.get(dataSet.getDataSource()));
        }
        return "";
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        support.check(parent, DataSet.Kind.COLLECTION);
        ImmutableList.Builder<DataSet> result = ImmutableList.builder();
        DataSet.Builder builder = parent.toBuilder(DataSet.Kind.SERIES);
        for (SdmxSeries o : getGroup(parent).series) {
            result.add(builder.put(Z_SERIES_ID, o.id).build());
        }
        return result.build();
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        support.check(dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getLabel(Y_GROUP_ID.get(dataSet), X_TITLE_ATTRIBUTE.get(dataSet.getDataSource()));
            case SERIES:
                return getLabel(Z_SERIES_ID.get(dataSet), X_TITLE_ATTRIBUTE.get(dataSet.getDataSource()));
        }
        return "";
    }

    @Override
    public SdmxBean newBean() {
        return new SdmxBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        try {
            return ((SdmxBean) bean).toDataSource(SOURCE, VERSION);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public SdmxBean decodeBean(DataSource dataSource) {
        return new SdmxBean(support.check(dataSource));
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.getPath().toLowerCase(Locale.ENGLISH).endsWith(".xml");
    }

    @Override
    public String getFileDescription() {
        return "Sdmx file";
    }

    public SdmxSource getSource(DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    public SdmxGroup getGroup(DataSet dataSet) throws IOException {
        String groupName = Y_GROUP_ID.get(dataSet);
        for (SdmxItem o : getSource(dataSet.getDataSource()).items) {
            if (o instanceof SdmxGroup && o.id.equals(groupName)) {
                return (SdmxGroup) o;
            }
        }
        throw new IOException("Can't find group");
    }

    public SdmxSeries getSeries(DataSet dataSet) throws IOException {
        String seriesName = Z_SERIES_ID.get(dataSet);
        for (SdmxItem o : getSource(dataSet.getDataSource()).items) {
            if (o instanceof SdmxGroup) {
                for (SdmxSeries s : ((SdmxGroup) o).series) {
                    if (s.id.equals(seriesName)) {
                        return s;
                    }
                }
            } else if (o instanceof SdmxSeries && o.id.equals(seriesName)) {
                return (SdmxSeries) o;
            }
        }
        throw new IOException("Can't find series");
    }

    public boolean isCompactNaming() {
        return compactNaming;
    }

    public void setCompactNaming(boolean compactNaming) {
        this.compactNaming = compactNaming;
    }

    public boolean isKeysInMetaData() {
        return keysInMetaData;
    }

    public void setKeysInMetaData(boolean keysInMetaData) {
        this.keysInMetaData = keysInMetaData;
    }

    private String getLabel(String id, String titleAttribute) {
        if (!titleAttribute.isEmpty()) {
            try {
                return Strings.nullToEmpty(keyValueSplitter.split(id).get(titleAttribute));
            } catch (IllegalArgumentException ex) {
            }
        }
        if (compactNaming) {
            try {
                return compactNamingJoiner.join(keyValueSplitter.split(id).values());
            } catch (IllegalArgumentException ex) {
            }
        }
        return id;
    }
}

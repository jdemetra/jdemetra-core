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
package ec.tss.tsproviders.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 * @param <DATA>
 */
public abstract class AbstractDataSourceProvider<DATA> extends AbstractTsProvider implements IDataSourceProvider {

    protected final LoadingCache<DataSource, DATA> cache;
    protected final DataSourceSupport support;

    public AbstractDataSourceProvider(Logger logger, String providerName, TsAsyncMode asyncMode) {
        super(logger, providerName, asyncMode);
        this.cache = createCache();
        this.support = DataSourceSupport.create(providerName, logger);
    }

    @Nonnull
    protected abstract DATA loadFromDataSource(@Nonnull DataSource key) throws Exception;

    @Nonnull
    protected CacheBuilder<Object, Object> createCacheBuilder() {
        return CacheBuilder.newBuilder().softValues();
    }

    private LoadingCache<DataSource, DATA> createCache() {
        return createCacheBuilder().build(new CacheLoader<DataSource, DATA>() {
            @Override
            public DATA load(DataSource key) throws Exception {
                return loadFromDataSource(key);
            }
        });
    }

    @Override
    public List<DataSource> getDataSources() {
        return support.getDataSources();
    }

    @Override
    public String getDisplayName(IOException exception) throws IllegalArgumentException {
        return support.getDisplayName(exception);
    }

    @Override
    public void addDataSourceListener(IDataSourceListener listener) {
        support.addDataSourceListener(listener);
    }

    @Override
    public void removeDataSourceListener(IDataSourceListener listener) {
        support.removeDataSourceListener(listener);
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        return support.toMoniker(dataSet);
    }

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        return support.toMoniker(dataSource);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        return support.toDataSet(moniker);
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        return support.toDataSource(moniker);
    }

    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

    @Override
    public boolean queryTsCollection(TsMoniker moniker, TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return support.checkQuietly(moniker) && super.queryTsCollection(moniker, type);
    }

    @Override
    public boolean queryTs(TsMoniker moniker, TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return support.checkQuietly(moniker) && super.queryTs(moniker, type);
    }

    @Override
    protected boolean process(TsCollectionInformation info) {
        {
            // case 0: moniker is a valid source
            DataSource dataSource = toDataSource(info.moniker);
            if (support.checkQuietly(dataSource)) {
                try {
                    fillCollection(info, dataSource);
                    return true;
                } catch (Exception ex) {
                    support.fillCollection(info, ex);
                    return false;
                }
            }
        }
        {
            // case 1: moniker is a valid collection
            DataSet dataSet = toDataSet(info.moniker);
            if (support.checkQuietly(dataSet, DataSet.Kind.COLLECTION)) {
                try {
                    fillCollection(info, dataSet);
                    return true;
                } catch (Exception ex) {
                    support.fillCollection(info, ex);
                    return false;
                }
            }
        }
        logger.warn("Invalid moniker '{}'", info.moniker.getId());
        return false;
    }

    @Override
    protected boolean process(TsInformation info) {
        {
            // case 2: moniker is a valid series
            DataSet dataSet = toDataSet(info.moniker);
            if (support.checkQuietly(dataSet, DataSet.Kind.SERIES)) {
                try {
                    fillSeries(info, dataSet);
                    return true;
                } catch (Exception ex) {
                    support.fillSeries(info, ex);
                    return false;
                }
            }
        }
        logger.warn("Invalid moniker '{}'", info.moniker.getId());
        return false;
    }

    abstract protected void fillCollection(@Nonnull TsCollectionInformation info, @Nonnull DataSource dataSource) throws IOException;

    abstract protected void fillCollection(@Nonnull TsCollectionInformation info, @Nonnull DataSet dataSet) throws IOException;

    abstract protected void fillSeries(@Nonnull TsInformation info, @Nonnull DataSet dataSet) throws IOException;

    @Nonnull
    protected TsInformation newTsInformation(@Nonnull DataSet dataSet, @Nonnull TsInformationType type) {
        return new TsInformation(getDisplayName(dataSet), toMoniker(dataSet), type);
    }

    @Deprecated
    @Nonnull
    protected TsInformation newTsInformation(@Nonnull DataSet dataSet, @Nullable TsData data, @Nullable MetaData metaData) {
        TsInformation result = newTsInformation(dataSet, data);
        result.metaData = metaData;
        return result;
    }

    @Deprecated
    @Nonnull
    protected TsInformation newTsInformation(@Nonnull DataSet dataSet, @Nullable TsData data) {
        TsInformation result = new TsInformation(getDisplayName(dataSet), toMoniker(dataSet), TsInformationType.All);
        result.data = data;
        return result;
    }
}

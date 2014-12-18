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
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 * @param <DATA>
 */
public abstract class AbstractDataSourceProvider<DATA> implements IDataSourceProvider {

    protected final Logger logger;
    protected final String providerName;
    protected final TsAsyncMode asyncMode;
    protected final LoadingCache<DataSource, DATA> cache;
    protected final DataSourceSupport support;
    protected final AsyncRequests asyncRequests;
    //protected final Service requestsHandler;
    protected final RequestsHandler2 requestsHandler;

    public AbstractDataSourceProvider(Logger logger, String providerName, TsAsyncMode asyncMode) {
        this.logger = logger;
        this.providerName = providerName;
        this.asyncMode = asyncMode;
        this.cache = createCache();
        this.support = DataSourceSupport.create(providerName, logger);
        this.asyncRequests = new AsyncRequests();
        if (this.asyncMode != TsAsyncMode.None) {
            this.requestsHandler = new RequestsHandler2();
            requestsHandler.start();
        } else {
            requestsHandler = null;
        }
    }

    @Nonnull
    protected abstract DATA loadFromDataSource(@Nonnull DataSource key) throws Exception;

    protected CacheBuilder<Object, Object> createCacheBuilder() {
        return CacheBuilder.newBuilder().softValues();
    }

    final protected LoadingCache<DataSource, DATA> createCache() {
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
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getSource() {
        return providerName;
    }

    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

    @Override
    public void dispose() {
        if (asyncMode != TsAsyncMode.None) {
            requestsHandler.stop();
            asyncRequests.clear();
        }
        clearCache();
    }

    @Override
    public TsAsyncMode getAsyncMode() {
        return asyncMode;
    }

    @Override
    public boolean queryTsCollection(TsMoniker moniker, TsInformationType type) {
        if (asyncMode == TsAsyncMode.None || !support.checkQuietly(moniker)) {
            return false;
        }
        if (type == TsInformationType.None) {
            asyncRequests.removeTsCollection(moniker, TsInformationType.All);
        } else {
            boolean empty = asyncRequests.isEmpty();
            asyncRequests.addTsCollection(moniker, type);
            if (empty) {
                requestsHandler.unpark();
            }
        }
        return true;
    }

    @Override
    public boolean queryTs(TsMoniker moniker, TsInformationType type) {
        if (asyncMode == TsAsyncMode.None || !support.checkQuietly(moniker)) {
            return false;
        }
        if (type == TsInformationType.None) {
            asyncRequests.removeTs(moniker, TsInformationType.All);
        } else {
            boolean empty = asyncRequests.isEmpty();
            asyncRequests.addTs(moniker, type);
            if (empty) {
                requestsHandler.unpark();
            }
        }
        return true;
    }

    @Override
    final public boolean get(TsCollectionInformation info) {
        if (asyncMode != TsAsyncMode.None) {
            // remove request that are encompassed by this one
            asyncRequests.removeTsCollection(info.moniker, info.type);
        }
        return process(info);
    }

    @Override
    final public boolean get(TsInformation info) {
        if (asyncMode != TsAsyncMode.None) {
            // remove request that are encompassed by this one
            asyncRequests.removeTs(info.moniker, info.type);
        }
        return process(info);
    }

    protected boolean process(TsCollectionInformation info) {
        {
            // case 0: moniker is a valid source
            DataSource dataSource = toDataSource(info.moniker);
            if (support.checkQuietly(dataSource)) {
                try {
                    fillCollection(info, dataSource);
                    return true;
                } catch (Exception ex) {
                    logger.error("While getting source", ex);
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
                    logger.error("While getting collection", ex);
                    return false;
                }
            }
        }
        logger.warn("Invalid moniker '{}'", info.moniker.getId());
        return false;
    }

    protected boolean process(TsInformation info) {
        {
            // case 2: moniker is a valid series
            DataSet dataSet = toDataSet(info.moniker);
            if (support.checkQuietly(dataSet, DataSet.Kind.SERIES)) {
                try {
                    fillSeries(info, dataSet);
                    return true;
                } catch (Exception ex) {
                    logger.error("While getting series", ex);
                    return false;
                }
            }
        }
        logger.warn("Invalid moniker '{}'", info.moniker.getId());
        return false;
    }

    protected abstract void fillCollection(@Nonnull TsCollectionInformation info, @Nonnull DataSource dataSource) throws IOException;

    protected abstract void fillCollection(@Nonnull TsCollectionInformation info, @Nonnull DataSet dataSet) throws IOException;

    protected abstract void fillSeries(@Nonnull TsInformation info, @Nonnull DataSet dataSet) throws IOException;

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

    protected class RequestsHandler extends AbstractExecutionThreadService {

        @Override
        protected Executor executor() {
            return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.MIN_PRIORITY).build());
        }

        @Override
        protected void run() throws Exception {
            while (isRunning()) {
                // step 1. process tsCollection
                TsCollectionInformation crequest = asyncRequests.nextTsCollection();
                if (crequest != null && TsFactory.instance.isTsCollectionAlive(crequest.moniker)) {
                    if (process(crequest)) {
                        TsFactory.instance.update(crequest);
                    }
                }
                // step 2. process ts
                TsInformation srequest = asyncRequests.nextTs();
                if (srequest != null && TsFactory.instance.isTsAlive(srequest.moniker)) {
                    if (process(srequest)) {
                        TsFactory.instance.update(srequest);
                    }
                }
                // step 3. sleep if queues are empty
                if (srequest == null && crequest == null) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        // continue
                    }
                }
            }
        }
    }

    protected class RequestsHandler2 implements Runnable {

        final Thread requestsThread;
        final AtomicBoolean end = new AtomicBoolean(false);

        RequestsHandler2() {
            requestsThread = new Thread(this);
            requestsThread.setDaemon(true);
        }

        @Override
        public void run() {
            while (!end.get()) {
                // step 1. process tsCollection
                TsCollectionInformation crequest = asyncRequests.nextTsCollection();
                if (crequest != null && TsFactory.instance.isTsCollectionAlive(crequest.moniker)) {
                    if (process(crequest)) {
                        TsFactory.instance.update(crequest);
                    }
                }
                // step 2. process ts
                TsInformation srequest = asyncRequests.nextTs();
                if (srequest != null && TsFactory.instance.isTsAlive(srequest.moniker)) {
                    if (process(srequest)) {
                        TsFactory.instance.update(srequest);
                    }
                }
                // step 3. sleep if queues are empty
                if (srequest == null && crequest == null) {
                    LockSupport.park();
                }
            }
        }

        void start() {
            requestsThread.start();
        }

        void stop() {
            if (asyncRequests.isEmpty()) {
                LockSupport.unpark(requestsThread);
            }
            end.set(true);
        }

        void unpark() {
            LockSupport.unpark(requestsThread); //To change body of generated methods, choose Tools | Templates.
        }

        void park() {
            LockSupport.park(requestsThread); //To change body of generated methods, choose Tools | Templates.
        }
    }
}

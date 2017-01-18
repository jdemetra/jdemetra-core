/*
 * Copyright 2015 National Bank of Belgium
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
package ec.tss.tsproviders.utils;

import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Implementation of ITsProvider that delegates the data retrieval to TsFiller.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public abstract class TsProviderSupport implements ITsProvider {

    @Nonnull
    public static ITsProvider of(@Nonnull String providerName, @Nonnull TsAsyncMode asyncMode, @Nonnull TsFiller filler) {
        return of(providerName, asyncMode, filler, NO_CACHE);
    }

    @Nonnull
    public static ITsProvider of(@Nonnull String providerName, @Nonnull TsAsyncMode asyncMode, @Nonnull TsFiller filler, @Nonnull Runnable cacheCleaner) {
        switch (asyncMode) {
            case None:
                return new SyncImpl(providerName, filler, cacheCleaner);
            case Dynamic:
            case Once:
                return new AsyncImpl(providerName, asyncMode, filler, cacheCleaner);
            default:
                throw new RuntimeException();
        }
    }

    private final String providerName;
    private final Runnable cacheCleaner;

    private TsProviderSupport(String providerName, Runnable cacheCleaner) {
        this.providerName = Objects.requireNonNull(providerName);
        this.cacheCleaner = Objects.requireNonNull(cacheCleaner);
    }

    @Override
    public String getSource() {
        return providerName;
    }

    @Override
    public void clearCache() {
        cacheCleaner.run();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final Runnable NO_CACHE = () -> {
    };

    private static final class SyncImpl extends TsProviderSupport {

        private final TsFiller filler;

        private SyncImpl(String providerName, TsFiller filler, Runnable cacheCleaner) {
            super(providerName, cacheCleaner);
            this.filler = Objects.requireNonNull(filler);
        }

        @Override
        public TsAsyncMode getAsyncMode() {
            return TsAsyncMode.None;
        }

        @Override
        public void dispose() {
            clearCache();
        }

        @Override
        public boolean get(TsCollectionInformation info) {
            DataSourcePreconditions.checkProvider(getSource(), info.moniker);
            return filler.fillCollection(info);
        }

        @Override
        public boolean get(TsInformation info) {
            DataSourcePreconditions.checkProvider(getSource(), info.moniker);
            return filler.fillSeries(info);
        }

        @Override
        public boolean queryTs(TsMoniker moniker, TsInformationType type) {
            Objects.requireNonNull(moniker, "Moniker cannot be null");
            Objects.requireNonNull(type, "Type cannot be null");
            throw new IllegalStateException("Not async provider");
        }

        @Override
        public boolean queryTsCollection(TsMoniker moniker, TsInformationType type) {
            Objects.requireNonNull(moniker, "Moniker cannot be null");
            Objects.requireNonNull(type, "Type cannot be null");
            throw new IllegalStateException("Not async provider");
        }
    }

    private static final class AsyncImpl extends TsProviderSupport {

        private final TsAsyncMode asyncMode;
        private final TsFiller filler;
        private final AsyncRequests asyncRequests;
        private final RequestsHandler2 requestsHandler;

        private AsyncImpl(String providerName, TsAsyncMode asyncMode, TsFiller filler, Runnable cacheCleaner) {
            super(providerName, cacheCleaner);
            this.asyncMode = Objects.requireNonNull(asyncMode);
            this.filler = Objects.requireNonNull(filler);
            this.asyncRequests = new AsyncRequests();
            this.requestsHandler = new RequestsHandler2();
            requestsHandler.start();
        }

        @Override
        public TsAsyncMode getAsyncMode() {
            return asyncMode;
        }

        @Override
        public void dispose() {
            requestsHandler.stop();
            asyncRequests.clear();
            clearCache();
        }

        @Override
        public boolean get(TsCollectionInformation info) {
            DataSourcePreconditions.checkProvider(getSource(), info.moniker);
            // remove request that are encompassed by this one
            asyncRequests.removeTsCollection(info.moniker, info.type);
            return filler.fillCollection(info);
        }

        @Override
        public boolean get(TsInformation info) {
            DataSourcePreconditions.checkProvider(getSource(), info.moniker);
            // remove request that are encompassed by this one
            asyncRequests.removeTs(info.moniker, info.type);
            return filler.fillSeries(info);
        }

        @Override
        public boolean queryTs(TsMoniker moniker, TsInformationType type) {
            Objects.requireNonNull(moniker, "Moniker cannot be null");
            Objects.requireNonNull(type, "Type cannot be null");
            DataSourcePreconditions.checkProvider(getSource(), moniker);
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
        public boolean queryTsCollection(TsMoniker moniker, TsInformationType type) {
            Objects.requireNonNull(moniker, "Moniker cannot be null");
            Objects.requireNonNull(type, "Type cannot be null");
            DataSourcePreconditions.checkProvider(getSource(), moniker);
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

        private final class RequestsHandler2 implements Runnable {

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
                        filler.fillCollection(crequest);
                        TsFactory.instance.update(crequest);
                    }
                    // step 2. process ts
                    TsInformation srequest = asyncRequests.nextTs();
                    if (srequest != null && TsFactory.instance.isTsAlive(srequest.moniker)) {
                        filler.fillSeries(srequest);
                        TsFactory.instance.update(srequest);
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
                LockSupport.unpark(requestsThread);
            }

            void park() {
                LockSupport.park(requestsThread);
            }
        }
    }
    //</editor-fold>
}

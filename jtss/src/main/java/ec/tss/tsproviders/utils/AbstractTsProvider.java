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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public abstract class AbstractTsProvider implements ITsProvider {

    protected final Logger logger;
    protected final String providerName;
    protected final TsAsyncMode asyncMode;
    protected final AsyncRequests asyncRequests;
    //protected final Service requestsHandler;
    protected final RequestsHandler2 requestsHandler;

    public AbstractTsProvider(Logger logger, String providerName, TsAsyncMode asyncMode) {
        this.logger = logger;
        this.providerName = providerName;
        this.asyncMode = asyncMode;
        this.asyncRequests = new AsyncRequests();
        if (this.asyncMode != TsAsyncMode.None) {
            this.requestsHandler = new RequestsHandler2();
            requestsHandler.start();
        } else {
            requestsHandler = null;
        }
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
        if (asyncMode == TsAsyncMode.None) {
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
        if (asyncMode == TsAsyncMode.None) {
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

    abstract protected boolean process(TsCollectionInformation info);

    abstract protected boolean process(TsInformation info);

//    protected class RequestsHandler extends AbstractExecutionThreadService {
//
//        @Override
//        protected Executor executor() {
//            return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.MIN_PRIORITY).build());
//        }
//
//        @Override
//        protected void run() throws Exception {
//            while (isRunning()) {
//                // step 1. process tsCollection
//                TsCollectionInformation crequest = asyncRequests.nextTsCollection();
//                if (crequest != null && TsFactory.instance.isTsCollectionAlive(crequest.moniker)) {
//                    if (process(crequest)) {
//                        TsFactory.instance.update(crequest);
//                    }
//                }
//                // step 2. process ts
//                TsInformation srequest = asyncRequests.nextTs();
//                if (srequest != null && TsFactory.instance.isTsAlive(srequest.moniker)) {
//                    if (process(srequest)) {
//                        TsFactory.instance.update(srequest);
//                    }
//                }
//                // step 3. sleep if queues are empty
//                if (srequest == null && crequest == null) {
//                    try {
//                        TimeUnit.SECONDS.sleep(3);
//                    } catch (InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                        // continue
//                    }
//                }
//            }
//        }
//    }
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
                    process(crequest);
                    TsFactory.instance.update(crequest);
                }
                // step 2. process ts
                TsInformation srequest = asyncRequests.nextTs();
                if (srequest != null && TsFactory.instance.isTsAlive(srequest.moniker)) {
                    process(srequest);
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

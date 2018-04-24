/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.utilities;

import demetra.design.ThreadSafe;
import demetra.design.VisibleForTesting;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @see https://docs.oracle.com/javase/tutorial/ext/basics/spi.html
 */
@ThreadSafe
@lombok.experimental.UtilityClass
public class ServiceLookup {

    @Nonnull
    public <S> S first(@Nonnull Class<S> type) throws ServiceException {
        return loadFirst(type, LOADER, LOGGER);
    }

    @Nonnull
    public <S> AtomicReference<S> firstMutable(@Nonnull Class<S> type) throws ServiceException {
        return new AtomicReference<>(loadFirst(type, LOADER, LOGGER));
    }

    @Nonnull
    public <S> Supplier<S> firstDynamic(@lombok.NonNull Class<S> type) throws ServiceException {
        return () -> loadFirst(type, LOADER, LOGGER);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private final Loader LOADER = new LoaderWithCache(ServiceLoader::load);
    private final Logger LOGGER = Logger.getLogger(ServiceLookup.class.getName());

    @VisibleForTesting
    <S> S loadFirst(Class<S> type, Loader loader, Logger logger) throws ServiceException {
        Iterator<S> iter = loader.load(type).iterator();
        if (!iter.hasNext()) {
            throw new ServiceException(type, "Not found");
        }
        S result = iter.next();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Using provider '%s' for service '%s'", new Object[]{result, type});
        }
        return result;
    }

    @VisibleForTesting
    interface Loader {

        <S> Iterable<S> load(Class<S> type) throws ServiceException;
    }

    @VisibleForTesting
    static final class LoaderWithCache implements Loader {

        private final Loader delegate;
        private final ConcurrentMap<Class, Iterable> cache;

        LoaderWithCache(Loader delegate) {
            this.delegate = delegate;
            this.cache = new ConcurrentHashMap<>();
        }

        @Override
        public <S> Iterable<S> load(Class<S> type) throws ServiceException {
            return cache.computeIfAbsent(type, o -> delegate.load(o));
        }
    }
    //</editor-fold>
}

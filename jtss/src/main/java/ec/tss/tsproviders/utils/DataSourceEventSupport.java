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

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceListener;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceEventSupport {

    @Nonnull
    public static DataSourceEventSupport create() {
        return create(NOPLogger.NOP_LOGGER);
    }

    /**
     * Creates a new DataSourceEventSupport that uses WeakReferences to allows
     * listeners to be garbage-collected and is thread-safe
     *
     * @param logger
     * @return
     */
    @Nonnull
    public static DataSourceEventSupport create(@Nonnull Logger logger) {
        Set<IDataSourceListener> weakHashSet = Collections.newSetFromMap(new WeakHashMap<IDataSourceListener, Boolean>());
        return new DataSourceEventSupport(logger, Collections.synchronizedSet(weakHashSet));
    }
    protected final Logger logger;
    protected final Set<IDataSourceListener> listeners;

    public DataSourceEventSupport(@Nonnull Logger logger, @Nonnull Set<IDataSourceListener> listeners) {
        this.logger = logger;
        this.listeners = listeners;
    }

    @Nonnull
    public Logger getLogger() {
        return logger;
    }

    public void add(@Nonnull IDataSourceListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void remove(@Nonnull IDataSourceListener listener) {
        listeners.remove(Objects.requireNonNull(listener));
    }

    public void fireOpened(@Nonnull DataSource dataSource) {
        for (IDataSourceListener o : listeners) {
            try {
                o.opened(dataSource);
            } catch (Exception ex) {
                logger.warn("While sending open event", ex);
            }
        }
    }

    public void fireClosed(@Nonnull DataSource dataSource) {
        for (IDataSourceListener o : listeners) {
            try {
                o.closed(dataSource);
            } catch (Exception ex) {
                logger.warn("While sending close event", ex);
            }
        }
    }

    public void fireAllClosed(@Nonnull String providerName) {
        for (IDataSourceListener o : listeners) {
            try {
                o.allClosed(providerName);
            } catch (Exception ex) {
                logger.warn("While sending closeall event", ex);
            }
        }
    }

    public void fireChanged(@Nonnull DataSource dataSource) {
        for (IDataSourceListener o : listeners) {
            try {
                o.changed(dataSource);
            } catch (Exception ex) {
                logger.warn("While sending change event", ex);
            }
        }
    }
}

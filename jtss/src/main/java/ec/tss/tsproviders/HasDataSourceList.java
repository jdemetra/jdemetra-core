/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.tsproviders;

import ec.tss.tsproviders.utils.DataSourceEventSupport;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;

/**
 * Defines the ability to watch a list of data sources. Note that the
 * implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataSourceList {

    void reload(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Gets the DataSources loaded by this provider.
     *
     * @return a list of DataSources; might be empty but never null.
     */
    @NonNull
    List<DataSource> getDataSources();

    /**
     * Adds a listener to the provider in order to receive change
     * notifications.<br>Note that the specified listener might be stored in a
     * {@link WeakHashMap} to avoid memory leak. It is up to you to keep it in a
     * strong ref to prevent garbage collection.
     *
     * @param listener
     */
    void addDataSourceListener(@NonNull IDataSourceListener listener);

    /**
     * Removes a listener from the provider if that listener has been added.
     * Does nothing otherwise.
     *
     * @param listener
     */
    void removeDataSourceListener(@NonNull IDataSourceListener listener);

    @NonNull
    public static HasDataSourceList of(
            @NonNull String providerName, @NonNull Logger logger,
            @NonNull Iterable<DataSource> dataSources,
            @NonNull Consumer<? super DataSource> cacheCleaner) {
        return new Util.DataSourceListSupport(providerName, dataSources, DataSourceEventSupport.create(logger), cacheCleaner);
    }

    @NonNull
    public static HasDataSourceList of(
            @NonNull String providerName, @NonNull Logger logger,
            @NonNull Iterable<DataSource> dataSources) {
        return of(providerName, logger, dataSources, Util.DO_NOTHING);
    }
}

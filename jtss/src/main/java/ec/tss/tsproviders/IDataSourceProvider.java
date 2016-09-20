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
package ec.tss.tsproviders;

import com.google.common.base.Strings;
import ec.tss.ITsProvider;
import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines a provider that is used to discover and browse DataSources and
 * DataSets. All the methods defined here are for consultation only. To allow
 * changes, a provider must implement {@link IDataSourceLoader}.
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 */
@ThreadSafe
public interface IDataSourceProvider extends ITsProvider, HasDataHierarchy, HasDataDisplayName, HasDataMoniker {

    /**
     * Gets a label for this provider.<br>Note that the result might change
     * according to the configuration of the provider.
     *
     * @return a non-empty label.
     */
    @Nonnull
    default String getDisplayName() {
        return getSource();
    }

    /**
     * Gets the DataSources loaded by this provider.
     *
     * @return a list of DataSources; might be empty but never null.
     */
    @Nonnull
    List<DataSource> getDataSources();

    /**
     * Adds a listener to the provider in order to receive change
     * notifications.<br>Note that the specified listener might be stored in a
     * {@link WeakHashMap} to avoid memory leak. It is up to you to keep it in a
     * strong ref to prevent garbage collection.
     *
     * @param listener
     */
    void addDataSourceListener(@Nonnull IDataSourceListener listener);

    /**
     * Removes a listener from the provider if that listener has been added.
     * Does nothing otherwise.
     *
     * @param listener
     */
    void removeDataSourceListener(@Nonnull IDataSourceListener listener);

    /**
     * Gets a label for an exception thrown by this provider.
     *
     * @param exception
     * @return a non-empty label
     * @throws IllegalArgumentException if the exception doesn't belong to this
     * provider.
     */
    @Nonnull
    default String getDisplayName(@Nonnull IOException exception) throws IllegalArgumentException {
        String message = exception.getMessage();
        return !Strings.isNullOrEmpty(message) ? message : exception.getClass().getSimpleName();
    }
}

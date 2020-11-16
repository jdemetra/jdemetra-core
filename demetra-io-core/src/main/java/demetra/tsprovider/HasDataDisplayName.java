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
package demetra.tsprovider;

import nbbrd.design.ThreadSafe;
import internal.tsprovider.InternalTsProvider;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the ability to get a label from a DataSource or a DataSet. Note that
 * the implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataDisplayName {

    /**
     * Gets a label for the specified DataSource.<br>Note that the result might
     * change according to the configuration of the provider.
     *
     * @param dataSource
     * @return a non-empty label.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     */
    @NonNull
    String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Gets a full label of the specified DataSet. Use this method in a
     * list.<br>Note that the result might change according to the configuration
     * of the provider.
     *
     * @param dataSet
     * @return a non-empty label.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     * provider.
     */
    @NonNull
    String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException;

    /**
     * Gets a short label of the specified DataSet. Use this method in a
     * tree.<br>Note that the result might change according to the configuration
     * of the provider.
     *
     * @param dataSet
     * @return a non-empty label.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     * provider.
     */
    @NonNull
    default String getDisplayNodeName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        return getDisplayName(dataSet);
    }

    /**
     * Gets a label for an exception thrown by this provider.
     *
     * @param exception
     * @return a non-empty label
     * @throws IllegalArgumentException if the exception doesn't belong to this
     * provider.
     */
    @NonNull
    default String getDisplayName(@NonNull IOException exception) throws IllegalArgumentException {
        return InternalTsProvider.getDisplayNameFromMessageOrClassName(exception);
    }

    /**
     * Creates a new instance of HasDataDisplayName using uri parser/formatter.
     *
     * @param providerName a non-null provider name
     * @return a non-null instance
     */
    @NonNull
    static HasDataDisplayName usingUri(@NonNull String providerName) {
        return new InternalTsProvider.DataDisplayNameSupport(providerName, DataSource.uriFormatter(), DataSet.uriFormatter());
    }
}

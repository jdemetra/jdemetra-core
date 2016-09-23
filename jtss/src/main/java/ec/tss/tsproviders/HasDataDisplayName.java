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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

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
    @Nonnull
    String getDisplayName(@Nonnull DataSource dataSource) throws IllegalArgumentException;

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
    @Nonnull
    String getDisplayName(@Nonnull DataSet dataSet) throws IllegalArgumentException;

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
    @Nonnull
    default String getDisplayNodeName(@Nonnull DataSet dataSet) throws IllegalArgumentException {
        return getDisplayName(dataSet);
    }
}

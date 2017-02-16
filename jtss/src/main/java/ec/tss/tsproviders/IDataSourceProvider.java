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
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines a provider that is used to discover and browse DataSources and
 * DataSets. All the methods defined here are for consultation only. To allow
 * changes, a provider must implement {@link IDataSourceLoader}.
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 * @since 1.0.0
 */
@ThreadSafe
public interface IDataSourceProvider extends ITsProvider, HasDataSourceList, HasDataHierarchy, HasDataDisplayName, HasDataMoniker {

    @Override
    default void reload(DataSource dataSource) {
        clearCache();
    }

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

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
package demetra.timeseries;

import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

/**
 * Generic interface for providers of time series.
 *
 * @author Jean Palate
 * @since 1.0.0
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
@ThreadSafe
public interface TsProvider extends AutoCloseable {

    /**
     * Requests to the provider to clear any cached information.
     */
    void clearCache();

    @Override
    void close();

    /**
     * @param moniker a non-null moniker that identifies a time series collection
     * @param type
     * @return a non-null time series
     * @throws IOException              if an internal exception prevented data retrieval.
     * @throws IllegalArgumentException if the moniker doesn't belong to this provider.
     */
    @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException;

    /**
     * @param moniker a non-null moniker that identifies a time series
     * @param type
     * @return a non-null time series collection
     * @throws IOException              if an internal exception prevented data retrieval.
     * @throws IllegalArgumentException if the moniker doesn't belong to this provider.
     */
    @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException;

    /**
     * Gets the identifier of the source.
     *
     * @return A non-empty unique identifier for the provider. The source is
     * used as first string in the moniker of a series provided by this
     * provider.
     */
    @NonNull
    String getSource();

    /**
     * Checks if the provider is able to provide information.
     *
     * @return True if the provider is available, false otherwise (missing
     * modules, missing or unavailable resources...)
     */
    @ServiceFilter
    boolean isAvailable();
}

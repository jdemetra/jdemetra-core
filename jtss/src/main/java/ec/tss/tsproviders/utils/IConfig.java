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

import ec.tstoolkit.design.IBuilder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Defines an immutable key-value store.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@Immutable
public interface IConfig {

    /**
     * Returns all the parameters of this config sorted by key.
     *
     * @return a non-null map
     */
    @Nonnull
    SortedMap<String, String> getParams();

    /**
     * Returns a parameter by its key.
     *
     * @param key non-null key of the requested parameter
     * @return a parameter if available, null otherwise
     */
    @Nullable
    default String get(@Nonnull String key) {
        return getParams().get(key);
    }

    /**
     * Returns an optional parameter by its key.
     *
     * @param key non-null of the requested parameter
     * @return an optional parameter
     * @since 2.2.0
     */
    @Nonnull
    default Optional<String> getParam(@Nonnull String key) {
        return Optional.of(getParams().get(key));
    }

    /**
     * Performs the given action for each parameter in this config until all
     * parameters have been processed or the action throws an exception.
     *
     * @param action The non-null action to be performed for each entry
     * @since 2.2.0
     */
    default void forEach(@Nonnull BiConsumer<? super String, ? super String> action) {
        getParams().forEach(action);
    }

    /**
     * Returns a sequential {@code Stream} with the parameters of this config as
     * its source.
     *
     * @return a non-null sequential {@code Stream} over the parameters in this
     * config
     * @since 2.2.0
     */
    @Nonnull
    default Stream<Entry<String, String>> stream() {
        return getParams().entrySet().stream();
    }

    public interface Builder<THIS, T extends IConfig> extends IBuilder<T> {

        /**
         * Put a key-value pair.
         *
         * @param key a non-null key
         * @param value a non-null value
         * @return itself
         */
        @Nonnull
        THIS put(@Nonnull String key, @Nonnull String value);

        @Nonnull
        default THIS put(@Nonnull String key, int value) {
            return put(key, String.valueOf(value));
        }

        @Nonnull
        default THIS put(@Nonnull String key, boolean value) {
            return put(key, String.valueOf(value));
        }

        @Nonnull
        default THIS put(@Nonnull Map.Entry<String, String> entry) {
            return put(entry.getKey(), entry.getValue());
        }

        @Nonnull
        default THIS putAll(@Nonnull Map<String, String> map) {
            map.forEach(this::put);
            return (THIS) this;
        }
    }
}

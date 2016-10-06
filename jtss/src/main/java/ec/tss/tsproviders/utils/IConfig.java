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
import java.util.SortedMap;
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

    @Nullable
    String get(@Nonnull String key);

    @Nonnull
    SortedMap<String, String> getParams();

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

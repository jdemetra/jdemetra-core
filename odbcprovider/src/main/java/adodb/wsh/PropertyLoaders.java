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
package adodb.wsh;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Package private supporting class for {@link PropertyLoader}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class PropertyLoaders {

    private PropertyLoaders() {
        // static class
    }

    static final class MemoizingPropertyLoader implements PropertyLoader {

        private final PropertyLoader loader;
        private Map<String, String> properties;

        MemoizingPropertyLoader(PropertyLoader loader) {
            this.loader = loader;
            this.properties = null;
        }

        @Override
        public Map<String, String> loadAll() throws IOException {
            if (properties == null) {
                properties = Collections.unmodifiableMap(loader.loadAll());
            }
            return properties;
        }
    }

    static final class ExpiringMemoizingPropertyLoader implements PropertyLoader {

        private final PropertyLoader loader;
        private final long durationNano;
        private final LongSupplier clock;
        private Map<String, String> properties;
        private long expiration;

        ExpiringMemoizingPropertyLoader(PropertyLoader loader, long durationNano, LongSupplier clock) {
            this.loader = loader;
            this.durationNano = durationNano;
            this.clock = clock;
            this.properties = null;
            this.expiration = 0;
        }

        @Override
        public Map<String, String> loadAll() throws IOException {
            if (properties == null) {
                properties = Collections.unmodifiableMap(loader.loadAll());
                expiration = clock.getAsLong() + durationNano;
            } else if (expiration <= clock.getAsLong()) {
                properties = null;
                expiration = 0;
                return loadAll();
            }
            return properties;
        }
    }
}

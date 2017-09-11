/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tsprovider.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface CacheProvider {

    @Nonnull
    <K, V> ConcurrentMap<K, V> softValuesCacheAsMap();

    static CacheProvider getDefault() {
        // FIXME: create real cache and improved API (JCache?)
        return new CacheProvider() {
            @Override
            public <K, V> ConcurrentMap<K, V> softValuesCacheAsMap() {
                return new ConcurrentHashMap<>();
            }
        };
    }
}

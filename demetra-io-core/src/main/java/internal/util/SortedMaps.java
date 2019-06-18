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
package internal.util;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SortedMaps {

    @NonNull
    public <K, V> SortedMap<K, V> immutableCopyOf(@NonNull Map<K, V> input) {
        switch (input.size()) {
            case 0:
                return Collections.emptySortedMap();
            default:
                return Collections.unmodifiableSortedMap(new TreeMap<>(input));
        }
    }

    @NonNull
    public <K, V> SortedMap<K, V> immutableOf(@NonNull K key, @NonNull V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        SortedMap<K, V> result = new TreeMap<>();
        result.put(key, value);
        return Collections.unmodifiableSortedMap(result);
    }
}

/*
 * Copyright 2018 National Bank of Belgium
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
public final class LombokHelper {

    public static <X> X getValue(X value, boolean set, Supplier<X> defaultValue) {
        return set ? value : defaultValue.get();
    }

    public static <K, V> Map<K, V> getMap(List<K> keys, List<V> values) {
        if (keys == null) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }
        return Collections.unmodifiableMap(result);
    }

    public static <X> List<X> getList(List<X> list) {
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }
}

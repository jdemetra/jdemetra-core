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

package ec.tss.tsproviders.sdmx.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author Kristof Bayens
 */
@Deprecated
public final class DimensionHelper {

    private DimensionHelper() {
        // static class
    }
    
    @Deprecated
    public static Map<String, String> exceptKeys(Map<String, String> map, Collection<String> keys) {
        Map<String, String> newmap = new HashMap<>();
        for (Entry<String, String> o : map.entrySet()) {
            if (!keys.contains(o.getKey())) {
                newmap.put(o.getKey(), o.getValue());
            }
        }
        return newmap;
    }

    @Deprecated
    public static Map<String, String> interKeys(Map<String, String> map, Collection<String> keys) {
        Map<String, String> newmap = new HashMap<>();
        for (Entry<String, String> o : map.entrySet()) {
            if (keys.contains(o.getKey())) {
                newmap.put(o.getKey(), o.getValue());
            }
        }
        return newmap;
    }

    @Deprecated
    public static String makeKey(Map<String, String> keys) {
        if (keys.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Iterator<Entry<String, String>> iterator = new TreeMap<>(keys).entrySet().iterator();
        Entry<String, String> first = iterator.next();
        result.append(first.getKey()).append("=").append(first.getValue());
        while (iterator.hasNext()) {
            Entry<String, String> o = iterator.next();
            result.append(", ").append(o.getKey()).append("=").append(o.getValue());
        }
        return result.toString();
    }
}

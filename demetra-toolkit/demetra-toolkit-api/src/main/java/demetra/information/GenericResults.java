/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.information;

import demetra.util.WildCards;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
@lombok.Builder
public class GenericResults implements Explorable {

    public static GenericResults of(Explorable explorable, List<String> items) {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (String item : items) {
            if (WildCards.hasWildCards(item)) {
                Map<String, Object> all = explorable.searchAll(item, Object.class);
                for (String s : all.keySet()) {
                    Object obj = explorable.getData(s);
                    if (obj != null) {
                        map.put(s, obj);
                    }
                }
            } else {
                Object obj = explorable.getData(item);
                if (obj != null) {
                    map.put(item, obj);
                }
            }
        }
        return new GenericResults(Collections.unmodifiableMap(map));
    }

    @lombok.NonNull
    @lombok.Singular("entry")
    private Map<String, Object> entries;

    @Override
    public boolean contains(String id) {
        return entries.containsKey(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        entries.forEach((key, obj) -> dic.put(key, obj.getClass()));
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        Object obj = entries.get(id);
        if (tclass.isInstance(obj)) {
            return (T) obj;
        } else {
            return null;
        }
    }

    @Override
    public <T> Map<String, T> searchAll(String pattern, Class<T> tclass) {
        Map<String, T> rslt = new LinkedHashMap<>();
        WildCards wc = new WildCards(pattern);
        entries.forEach((key, obj) -> {
            if (wc.match(key) && tclass.isInstance(obj)) {
                rslt.put(key, (T) obj);
            }
        }
        );
        return rslt;
    }
}

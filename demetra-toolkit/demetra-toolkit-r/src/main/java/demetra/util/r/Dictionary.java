/*
 * Copyright 2019 National Bank of Belgium.
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
package demetra.util.r;

import demetra.timeseries.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class Dictionary {

    public static Dictionary of(Map<String, TsData> rslt) {
        Dictionary dic = new Dictionary();
        dic.dictionary.putAll(rslt);
        return dic;
    }

    private final Map<String, TsData> dictionary = new LinkedHashMap<>();

    public void add(String name, TsData s) {
        dictionary.put(name, s);
    }

    public String[] names() {
        return dictionary.keySet().toArray(new String[dictionary.size()]);
    }

    public TsData get(String name) {
        return dictionary.get(name);
    }

    public Map<String, TsData> data() {
        return Collections.unmodifiableMap(dictionary);
    }
}

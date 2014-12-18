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

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
public abstract class AbstractConfigBuilder<THIS extends AbstractConfigBuilder, T extends IConfig> implements IConfig.Builder<THIS, T> {

    protected final Map<String, String> params;

    protected AbstractConfigBuilder() {
        this(new HashMap<String, String>());
    }

    protected AbstractConfigBuilder(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public THIS put(String key, String value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(value, "value");
        params.put(key, value);
        return (THIS) this;
    }

    public THIS put(String key, int value) {
        return put(key, String.valueOf(value));
    }

    public THIS put(String key, boolean value) {
        return put(key, String.valueOf(value));
    }

    public THIS put(Map.Entry<String, String> entry) {
        return put(entry.getKey(), entry.getValue());
    }

    public THIS putAll(Map<String, String> map) {
        for (Map.Entry<String, String> o : map.entrySet()) {
            put(o);
        }
        return (THIS) this;
    }

    public THIS clear() {
        params.clear();
        return (THIS) this;
    }
}

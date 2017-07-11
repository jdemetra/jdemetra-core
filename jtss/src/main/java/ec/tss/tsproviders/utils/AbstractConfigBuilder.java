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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 * @param <THIS>
 * @param <T>
 */
public abstract class AbstractConfigBuilder<THIS extends AbstractConfigBuilder, T extends IConfig> implements IConfig.Builder<THIS, T> {

    protected final Map<String, String> params;

    protected AbstractConfigBuilder() {
        this(new HashMap<>());
    }

    protected AbstractConfigBuilder(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public THIS put(String key, String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        params.put(key, value);
        return (THIS) this;
    }

    public THIS clear() {
        params.clear();
        return (THIS) this;
    }
}

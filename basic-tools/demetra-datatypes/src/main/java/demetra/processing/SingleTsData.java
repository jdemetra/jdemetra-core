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
package demetra.processing;

import demetra.timeseries.simplets.TsDataType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class SingleTsData implements IProcResults {

    String name;
    TsDataType ts;

    @Override
    public boolean contains(String id) {
        return id.equals(name);
    }

    @Override
    public Map<String, Class> getDictionary() {
        return Collections.singletonMap(name, (Class) TsDataType.class);
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (id.equals(name) && tclass.equals(TsDataType.class)) {
            return (T) ts;
        } else {
            return null;
        }
    }

}

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class MultiTsData implements IProcResults {

    String name;
    TsDataType[] ts;

    @Override
    public boolean contains(String id) {
        return decode(id) >= 0;
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        for (int i = 0; i < ts.length; ++i) {
            dic.put(encode(i), TsDataType.class);
        }
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (!tclass.equals(TsDataType.class)) {
            return null;
        }
        int i = decode(id);
        if (i < 0) {
            return null;
        }
        return (T) ts[i];
    }

    private int decode(String s) {
        if (!s.startsWith(name)) {
            return -1;
        }
        try {
            int i = Integer.parseInt(s.substring(name.length()));
            if (i <= 0 || i > ts.length) {
                return 0;
            }
            return i - 1;
        } catch (NumberFormatException err) {
            return -1;
        }
    }

    private String encode(int i) {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(i + 1);
        return builder.toString();
    }

}

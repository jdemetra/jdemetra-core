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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Philippe Charles
 */
public class ParamBean {

    @XmlAttribute(name = "key")
    public String key;
    @XmlAttribute(name = "value")
    public String value;

    @NonNull
    public static ImmutableSortedMap<String, String> toSortedMap(@Nullable ParamBean[] params) {
        if (params == null || params.length == 0) {
            return ImmutableSortedMap.of();
        }
        ImmutableSortedMap.Builder<String, String> b = ImmutableSortedMap.naturalOrder();
        for (ParamBean o : params) {
            b.put(Strings.nullToEmpty(o.key), Strings.nullToEmpty(o.value));
        }
        return b.build();
    }

    @Nullable
    public static ParamBean[] fromSortedMap(@NonNull SortedMap<String, String> sortedMap) {
        if (sortedMap.isEmpty()) {
            return null;
        }
        ParamBean[] result = new ParamBean[sortedMap.size()];
        int i = 0;
        for (Map.Entry<String, String> o : sortedMap.entrySet()) {
            ParamBean item = new ParamBean();
            item.key = o.getKey();
            item.value = o.getValue();
            result[i++] = item;
        }
        return result;
    }

    @NonNull
    public static Properties toProperties(@Nullable ParamBean[] params) {
        Properties result = new Properties();
        if (params != null) {
            for (ParamBean o : params) {
                result.put(o.key, o.value);
            }
        }
        return result;
    }

    @Nullable
    public static ParamBean[] fromProperties(@NonNull Properties properties) {
        SortedMap<String, String> result = new TreeMap<>();
        properties.stringPropertyNames().forEach((o) -> {
            result.put(o, properties.getProperty(o));
        });
        return fromSortedMap(result);
    }
}

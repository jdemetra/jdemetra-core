/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.information;

import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import ec.tstoolkit.utilities.WildCards;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <S>
 * @since 2.2.0
 * @author Jean Palate
 */
public class InformationMapping<S> {
    
    public static final String LSTART = "(", LEND = ")";
    
    public static String listKey(String prefix, int item) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        if (LSTART != null) {
            builder.append(LSTART);
        }
        builder.append(item);
        if (LEND != null) {
            builder.append(LEND);
        }
        return builder.toString();
    }
    
    public static int listItem(String prefix, String key) {
        if (!key.startsWith(prefix)) {
            return -1;
        }
        int start = prefix.length();
        if (LSTART != null) {
            start += LSTART.length();
        }
        int end = key.length();
        if (LEND != null) {
            end -= LEND.length();
        }
        if (end <= start) {
            return -1;
        }
        String s = key.substring(start, end);
        try {
            return Integer.parseUnsignedInt(s);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    
    private static class TListFunction<S, T> {
        
        final Class<T> targetClass;
        final BiFunction<S, Integer, T> extractor;
        final int start, end;
        
        TListFunction(Class<T> tclass, int start, int end, BiFunction<S, Integer, T> extractor) {
            this.targetClass = tclass;
            this.extractor = extractor;
            this.start = start;
            this.end = end;
        }
    }
    
    private static class TFunction<S, T> {
        
        final Class<T> targetClass;
        final Function<S, T> extractor;
        
        TFunction(Class<T> tclass, Function<S, T> extractor) {
            this.targetClass = tclass;
            this.extractor = extractor;
        }
    }
    
    private final LinkedHashMap<String, TFunction<S, ?>> map = new LinkedHashMap<>();
    private final LinkedHashMap<String, TListFunction<S, ?>> lmap = new LinkedHashMap<>();
    
    public <T> void set(String name, Class<T> tclass, Function<S, T> extractor) {
        synchronized (map) {
            map.put(name, new TFunction(tclass, extractor));
        }
    }
    
    public <T> void set(String name, Function<S, TsData> extractor) {
        synchronized (lmap) {
            map.put(name, new TFunction(TsData.class, extractor));
        }
    }
    
    public <T> void setList(String prefix, int start, int end, Class<T> tclass, BiFunction<S, Integer, T> extractor) {
        synchronized (lmap) {
            lmap.put(prefix, new TListFunction(tclass, start, end, extractor));
        }
    }
    
    public <T> void setList(String prefix, int start, int end, BiFunction<S, Integer, TsData> extractor) {
        synchronized (lmap) {
            lmap.put(prefix, new TListFunction(TsData.class, start, end, extractor));
        }
    }
    
    public void fillDictionary(String prefix, Map<String, Class> dic) {
        synchronized (map) {
            for (Entry<String, TFunction<S, ?>> entry : map.entrySet()) {
                dic.put(InformationSet.item(prefix, entry.getKey()), entry.getValue().targetClass);
            }
        }
        synchronized (lmap) {
            for (Entry<String, TListFunction<S, ?>> entry : lmap.entrySet()) {
                dic.put(InformationSet.item(prefix, entry.getKey() + "*"), entry.getValue().targetClass);
            }
        }
    }
    
    private int lmapsize() {
        return lmap.entrySet().stream().map(x -> 1 + x.getValue().end - x.getValue().start).reduce(0, Integer::sum);
    }
    
    public String[] keys() {
        List<String> k = new ArrayList<>();
        synchronized (map) {
            k.addAll(map.keySet());
        }
        synchronized (lmap) {
            for (Entry<String, TListFunction<S, ?>> entry : lmap.entrySet()) {
                for (int j = entry.getValue().start; j <= entry.getValue().end; ++j) {
                    k.add(listKey(entry.getKey(), j));
                }
            }
        }
        return k.toArray(new String[k.size()]);
    }
    
    public boolean contains(String id) {
        synchronized (map) {
            if (map.containsKey(id)) {
                return true;
            }
        }
        synchronized (lmap) {
            for (Entry<String, TListFunction<S, ?>> x : lmap.entrySet()) {
                int idx = listItem(x.getKey(), id);
                if (idx >= x.getValue().start && idx <= x.getValue().end) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public <T> T getData(S source, String id, Class<T> tclass) {
        synchronized (map) {
            TFunction<S, ?> fn = map.get(id);
            if (fn != null) {
                if (!tclass.isAssignableFrom(fn.targetClass)) {
                    return null;
                } else {
                    return (T) fn.extractor.apply(source);
                }
            }
        }
        // search in lists
        synchronized (lmap) {
            for (Entry<String, TListFunction<S, ?>> x : lmap.entrySet()) {
                TListFunction<S, ?> value = x.getValue();
                if (tclass.isAssignableFrom(value.targetClass)) {
                    int idx = listItem(x.getKey(), id);
                    if (idx >= value.start && idx <= value.end) {
                        return (T) value.extractor.apply(source, idx);
                    }
                }
            }
        }
        return null;
    }
    
    public <T> Map<String, T> searchAll(S source, String pattern, Class<T> tclass) {
        LinkedHashMap<String, T> list = new LinkedHashMap<>();
        WildCards wc = new WildCards(pattern);
        synchronized (map) {
            for (Entry<String, TFunction<S, ?>> x : map.entrySet()) {
                if (wc.match(x.getKey())) {
                    TFunction<S, ?> fn = x.getValue();
                    if (tclass.isAssignableFrom(fn.targetClass)) {
                        list.put(x.getKey(), (T) fn.extractor.apply(source));
                    }
                }
            }
        }
        // search in lists
        synchronized (lmap) {
            for (Entry<String, TListFunction<S, ?>> x : lmap.entrySet()) {
                TListFunction<S, ?> fn = x.getValue();
                if (tclass.isAssignableFrom(fn.targetClass)) {
                    // far to be optimal... TO IMPROVE
                    for (int i = fn.start; i <= fn.end; ++i) {
                        String key = listKey(x.getKey(), i);
                        if (wc.match(key)) {
                            list.put(key, (T) fn.extractor.apply(source, i));
                        }
                    }
                }
            }
        }
        return list;
    }
}

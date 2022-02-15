/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.processing;

import demetra.information.Explorable;
import demetra.util.WildCards;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
@lombok.Builder
public class GenericOutput implements Explorable {
       
    public static GenericOutput of(Explorable explorable, List<String> items) {

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
        return new GenericOutput(Collections.unmodifiableMap(map));
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

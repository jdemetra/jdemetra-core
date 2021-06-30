/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.information;

import demetra.util.WildCards;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class InformationExtractors {

    volatile Map<Class, List<InformationExtractor>> extractors;

    public void reloadExtractors() {
        List<InformationExtractor> load = InformationExtractorLoader.load();
        HashMap<Class, List<InformationExtractor>> x = new HashMap<>();
        load.forEach(cur -> {
            List<InformationExtractor> all = x.get(cur.getSourceClass());
            if (all == null) {
                List<InformationExtractor> list = new ArrayList<>(4);
                list.add(cur);
                x.put(cur.getSourceClass(), list);
            } else {
                all.add(cur);
            }
        });
        // read only collections
        Set<Class> keys = x.keySet();
        for (Class cl : keys) {
            List<InformationExtractor> cur = x.get(cl);
            cur.sort(new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    int p1 = ((InformationExtractor) o1).getPriority();
                    int p2 = ((InformationExtractor) o2).getPriority();
                    return Integer.compare(p2, p1);
                }
            });
            // last one
            InformationExtractor last = cur.get(cur.size() - 1);
            if (last.getPriority() == -1) {
                x.put(cl, Collections.singletonList(last));
            } else {
                x.put(cl, Collections.unmodifiableList(x.get(cl)));
            }
        }
        extractors = Collections.unmodifiableMap(x);
    }

    public List<InformationExtractor> extractors(Class D) {
        if (extractors == null) {
            reloadExtractors();
        }
        return extractors.get(D);
    }

    public void fillDictionary(Class D, String prefix, Map dic, boolean compact) {
        List<InformationExtractor> all = extractors(D);
        for (InformationExtractor x : all) {
            x.fillDictionary(prefix, dic, compact);
        }
    }

    public boolean contains(Class D, String id) {
        List<InformationExtractor> all = extractors(D);
        for (BasicInformationExtractor x : all) {
            if (x.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public <S, Q> Q getData(Class D, S source, String id, Class<Q> qclass) {
        List<InformationExtractor> all = extractors(D);
        for (BasicInformationExtractor<S> x : all) {
            Q obj = x.getData(source, id, qclass);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public <S, Q> void searchAll(Class D, S source, WildCards wc, Class<Q> qclass, Map<String, Q> map) {
        List<InformationExtractor> all = extractors(D);
        for (BasicInformationExtractor<S> x : all) {
            x.searchAll(source, wc, qclass, map);
        }
    }

}

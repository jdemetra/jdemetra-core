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
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author PALATEJ
 * @param <S>
 */
public abstract class DynamicMapping<S, M> implements InformationExtractor<S> {

    private final String prefix;
    private final Function<S, Map<String, M>> fn;

    public DynamicMapping(final String prefix, final Function<S, Map<String, M>> fn) {
        this.prefix = prefix;
        this.fn = fn;
    }

    @Override
    public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
    }

    @Override
    public boolean contains(String id) {
        return false;
    }

    @Override
    public <Q> Q getData(S source, String id, Class<Q> qclass) {
        if (source == null) {
            return null;
        }
        Map<String, M> map = fn.apply(source);
        String subitem = id;
        if (prefix != null) {
            if (id.length() <= prefix.length()) {
                return null;
            }
            if (!id.startsWith(prefix) || id.charAt(prefix.length()) != BasicInformationExtractor.SEP) {
                return null;
            }
            subitem = id.substring(prefix.length() + 1);
        }
        M rslt = map.get(subitem);
        if (rslt != null && qclass.isInstance(rslt)) {
            return (Q) rslt;
        } else {
            return null;
        }
    }

    @Override
    public <Q> void searchAll(S source, WildCards wc, Class<Q> tclass, Map<String, Q> map) {
        if (source == null) {
            return;
        }
        Map<String, M> cmap = fn.apply(source);
        cmap.forEach((key, value) -> {
            if (wc.match(key)) {
                if (tclass.isInstance(value)) {
                    map.put(BasicInformationExtractor.concatenate(prefix, key), (Q) value);
                }
            }
        }
        );
    }

}

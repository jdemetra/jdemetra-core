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
 */
public abstract class InformationDelegate<S, T> implements InformationExtractor<S> {

    private final Function<S, T> fn;

    public abstract Class<T> getDelegateClass();

    public InformationDelegate(final Function<S, T> fn) {
        this.fn = fn;
    }

    @Override
    public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        InformationExtractors.fillDictionary(getDelegateClass(), BasicInformationExtractor.concatenate(prefix, null), dic, compact);
    }

    @Override
    public boolean contains(String id) {
        return InformationExtractors.contains(getDelegateClass(), id);
    }

    @Override
    public <Q> Q getData(S source, String id, Class<Q> qclass) {
        if (source == null) {
            return null;
        }
        T t = fn.apply(source);
        if (t == null) {
            return null;
        } else {
            return InformationExtractors.getData(t, id, qclass);
        }
    }

    @Override
    public <Q> void searchAll(S source, WildCards wc, Class<Q> tclass, Map<String, Q> map) {
        InformationExtractors.searchAll(fn.apply(source), wc, tclass, map);
    }

}

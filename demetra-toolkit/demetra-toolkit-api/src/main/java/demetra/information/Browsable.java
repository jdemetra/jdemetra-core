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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author PALATEJ
 * @param <X>
 */
public class Browsable<X> implements Explorable {

    final InformationMapping<X> mapping;
    final Function<Browsable<X>, X> fn;

    Browsable(InformationMapping<X> mapping, Function<Browsable<X>, X> fn) {
        this.mapping = mapping;
        this.fn = fn;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return mapping.getData(fn.apply(this), id, tclass);
    }

    @Override
    public boolean contains(String id) {
        return mapping.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        mapping.fillDictionary(null, dic, true);
        return dic;
    }

}

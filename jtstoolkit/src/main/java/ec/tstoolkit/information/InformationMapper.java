/*
 * Copyright 2013 National Bank of Belgium
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

import ec.tstoolkit.utilities.InformationExtractor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Jean Palate
 */
@Deprecated
public class InformationMapper<S> {

    static public abstract class Mapper<S, T> implements InformationExtractor<S, T> {

        private final Class<T> tclass_;

        protected Mapper(Class<T> tclass) {
            tclass_ = tclass;
        }

        @Override
        public void flush(S source) {
        }

        public Class<T> target() {
            return tclass_;
        }
    }

    private final LinkedHashMap<String, Mapper<S, ?>> map_ = new LinkedHashMap<>();

    public void add(String name, Mapper<S, ?> mapper) {
        map_.put(name, mapper);
    }

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        for (Entry<String, Mapper<S, ?>> entry : map_.entrySet()) {
            dic.put(InformationSet.item(prefix, entry.getKey()), entry.getValue().target());
        }
    }
    
    public String[] keys(){
        String[] k=new String[map_.size()];
        int i=0;
        for (String s : map_.keySet()){
            k[i++]=s;
        }
        return k;
    }

    public boolean contains(String id) {
        return map_.containsKey(id);
    }

    public <T> T getData(S source, String id, Class<T> tclass) {
        Mapper<S, T> map = (Mapper<S, T>) map_.get(id);
        if (map == null || !tclass.isAssignableFrom(map.target())) {
            return null;
        } else {
            return map.retrieve(source);
        }
    }

}

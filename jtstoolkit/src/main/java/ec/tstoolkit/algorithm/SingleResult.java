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

package ec.tstoolkit.algorithm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SingleResult<T>  implements IProcResults {
    
    final String name;
    final T value;
    final Class<T> tclass;
    
    public SingleResult(String name, T val, Class<T> tclass) {
        this.name = name;
        this.value = val;
        this.tclass=tclass;
    }
    
    public SingleResult(T val, Class<T> tclass) {
        this.name = VALUE;
        this.value = val;
        this.tclass=tclass;
    }

    public String getName(){
        return name;
    }
    
    public T get(){
        return value;
    }
    
    @Override
    public boolean contains(String id) {
        return id.equals(name);
    }
    
    @Override
    public Map<String, Class> getDictionary() {
        return Collections.singletonMap(name, (Class)tclass);
    }
    
    @Override
    public <S> S getData(String id, Class<S> tclass) {
        if (this.name.equals(id) && this.tclass.equals(tclass)) {
            return (S)value;
        } else {
            return null;
        }
    }
    
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.emptyList();
    }

    public static final String VALUE="value";
}


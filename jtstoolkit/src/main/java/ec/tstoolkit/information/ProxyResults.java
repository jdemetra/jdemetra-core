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
package ec.tstoolkit.information;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class ProxyResults implements IProcResults {

    public static final String ALL = "all";

    private final String name_;
    private final InformationSet info_;
    private LinkedHashMap<String, Class> map = new LinkedHashMap<>();

    public ProxyResults(InformationSet info, String name) {
        name_ = name;
        info_ = info;
        info_.fillDictionary(name_, map);
        map.put(ALL, InformationSet.class);
    }

    public InformationSet getInformationSet() {
        return info_;
    }

    @Override
    public boolean contains(String id) {
        return map.containsKey(id);
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (id.equals(ALL) && tclass.equals(InformationSet.class)) {
            return (T) info_;
        } else {
            return info_.search(id, tclass);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        ArrayList<ProcessingInformation> infos = new ArrayList<>();
        List<String> errors = info_.errors();
        for (String e : errors){
            infos.add(ProcessingInformation.error(name_, e));
        }
        List<String> wrn = info_.warnings();
        for (String e : wrn){
            infos.add(ProcessingInformation.warning(name_, e));
        }
        return infos;
    }
}

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

package ec.tss.disaggregation.documents;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class BenchmarkingResults implements IProcResults {

    public static final String ORIGINAL = "original", TARGET="target",  BENCHMARKED = "benchmarked";

    public BenchmarkingResults() {
    }

    public void set(TsData original, TsData target, TsData benchmarked) {
        info.set(ORIGINAL, original);
        info.set(TARGET, original);
        info.set(BENCHMARKED, benchmarked);
    }
    
    public <T> void addInformation(String code, T t){
        info.add(InformationSet.split(code), info);
    }
    
    private InformationSet info = new InformationSet();

    @Override
    public boolean contains(String id) {
        return info.search(id, Object.class) != null;
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return info.search(id, tclass);
    }
    
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.emptyList();
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic, boolean compact ){
        dic.put(InformationSet.item(prefix, ORIGINAL), TsData.class);
        dic.put(InformationSet.item(prefix, TARGET), TsData.class);
        dic.put(InformationSet.item(prefix, BENCHMARKED), TsData.class);
    }
}

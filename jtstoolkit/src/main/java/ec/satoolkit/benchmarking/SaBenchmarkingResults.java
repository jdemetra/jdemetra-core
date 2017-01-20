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

package ec.satoolkit.benchmarking;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SaBenchmarkingResults implements IProcResults {
    
    private final TsData orig, target, bench;
    
    public SaBenchmarkingResults(TsData orig, TsData target, TsData bench){
        this.orig=orig;
        this.target=target;
        this.bench=bench;
    }
    
    public TsData getTarget(){
        return target;
    } 
    
    public TsData getBenchmarkedSeries(){
        return bench;
    } 

    public TsData getOriginalSeries(){
        return orig;
    } 

    public static final String ORIGINAL="original", TARGET="target", BENCHMARKED="result";
    
    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
       map.put(InformationSet.item(prefix, ORIGINAL), TsData.class);
       map.put(InformationSet.item(prefix, TARGET), TsData.class);
       map.put(InformationSet.item(prefix, BENCHMARKED), TsData.class);
    }
 
    @Override
    public boolean contains(String id) {
        return id.equals(BENCHMARKED) || id.equals(TARGET)|| id.equals(ORIGINAL);
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        LinkedHashMap<String, Class> dic=new LinkedHashMap<>();
        fillDictionary(null, dic, compact);
       return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (id.equals(ORIGINAL) && tclass.equals(TsData.class))
            return (T) orig;
        if (id.equals(BENCHMARKED) && tclass.equals(TsData.class))
            return (T) bench;
        if (id.equals(TARGET) && tclass.equals(TsData.class))
            return (T) target;
        return null;
    }
        
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }
}

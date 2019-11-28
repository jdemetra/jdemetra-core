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
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class MultiBenchmarkingResults implements IProcResults {

    public static final String INPUT="input", BENCHMARKED = "benchmarked";

    public MultiBenchmarkingResults() {
        info.add(INPUT, new InformationSet());
        info.add(BENCHMARKED, new InformationSet());
    }

    public void addBenchmarked(String name, TsData benchmarked) {
        info.getSubSet(BENCHMARKED).set(name, benchmarked);
    }
    
    public void addInput(String name, TsData original) {
        info.getSubSet(INPUT).set(name, original);
    }
    
    public <T> void addInformation(String code, T t) {
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
        info.fillDictionary(null, map);
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

    public List<Information<TsData>> getInputData() {
        return info.getSubSet(INPUT).select(TsData.class);
    }

    public List<Information<TsData>> getBenchmarkedData() {
        return info.getSubSet(BENCHMARKED).select(TsData.class);
    }

    public List<String> getInputItems() {
        ArrayList<String> items = new ArrayList<>();
        info.getSubSet(INPUT).fillDictionary(INPUT, items, TsData.class);
        return items;
    }

    public List<String> getBenchmarkedItems() {
        ArrayList<String> items = new ArrayList<>();
        info.getSubSet(BENCHMARKED).fillDictionary(BENCHMARKED, items, TsData.class);
        return items;
    }
}

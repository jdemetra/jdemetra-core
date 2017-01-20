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
package ec.tss.businesscycle.documents;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class BusinessCycleDecomposition implements IProcResults {

    private final TsData target, trend, cycle;
    private final boolean mul;
    public static final String TARGET = "target", TREND = "trend", CYCLE = "cycle", MODE = "mode";

    public BusinessCycleDecomposition(boolean mul, TsData target, TsData trend, TsData cycle) {
        this.mul = mul;
        this.target = target;
        this.trend = trend;
        this.cycle = cycle;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        MAPPING.fillDictionary(prefix, dic, compact);
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    // MAPPING
    public static InformationMapping<BusinessCycleDecomposition> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<BusinessCycleDecomposition, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    private static final InformationMapping<BusinessCycleDecomposition> MAPPING = new InformationMapping<>(BusinessCycleDecomposition.class);

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    static {
        MAPPING.set(TARGET, source -> source.target);
        MAPPING.set(TREND, source -> source.trend);
        MAPPING.set(CYCLE, source -> source.cycle);
        MAPPING.set(MODE, Boolean.class, source -> source.mul);
    }

    /**
     * @return the trend
     */
    public TsData getTrend() {
        return trend;
    }

    /**
     * @return the cycle
     */
    public TsData getCycle() {
        return cycle;
    }

    /**
     * @return the cycle
     */
    public TsData getTarget() {
        return target;
    }

    /**
     * @return the mul
     */
    public boolean isMultiplicative() {
        return mul;
    }
}

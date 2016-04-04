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
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class BusinessCycleDecomposition implements IProcResults {

    private final TsData target, trend, cycle;
    private final boolean mul;
    public static final String TARGET="target", TREND = "trend", CYCLE = "cycle", MODE = "mode";

    public BusinessCycleDecomposition(boolean mul, TsData target, TsData trend, TsData cycle) {
        this.mul = mul;
        this.target=target;
        this.trend = trend;
        this.cycle = cycle;
    }
    
    public static void fillDictionary(String prefix, Map<String, Class> dic){
        synchronized (mapper) {
            mapper.fillDictionary(prefix, dic);
        }
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            return mapper.getData(this, id, tclass);
        }
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<BusinessCycleDecomposition, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    
    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    private static final InformationMapper<BusinessCycleDecomposition> mapper = new InformationMapper<>();

    static {
       mapper.add(TARGET, new InformationMapper.Mapper<BusinessCycleDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(BusinessCycleDecomposition source) {
                return source.target;
            }
        });
        mapper.add(TREND, new InformationMapper.Mapper<BusinessCycleDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(BusinessCycleDecomposition source) {
                return source.trend;
            }
        });
        mapper.add(CYCLE, new InformationMapper.Mapper<BusinessCycleDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(BusinessCycleDecomposition source) {
                return source.cycle;
            }
        });
        mapper.add(MODE, new InformationMapper.Mapper<BusinessCycleDecomposition, Boolean>(Boolean.class) {

            @Override
            public Boolean retrieve(BusinessCycleDecomposition source) {
                return source.mul;
            }
        });
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

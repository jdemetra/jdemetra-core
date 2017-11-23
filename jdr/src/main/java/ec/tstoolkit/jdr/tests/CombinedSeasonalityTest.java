/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.tests;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.tstoolkit.jdr.mapping.TestInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class CombinedSeasonalityTest {
    
    @lombok.Builder
    @lombok.Value
    static public class Results implements IProcResults{

        ec.satoolkit.diagnostics.CombinedSeasonalityTest combinedTest;
        
        static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.set("kruskalwallis", ec.tstoolkit.information.StatisticalTest.class, 
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getCombinedTest().getNonParametricTestForStableSeasonality()));
            MAPPING.set("stable", ec.tstoolkit.information.StatisticalTest.class, 
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getCombinedTest().getStableSeasonality()));
            MAPPING.set("evolutive", ec.tstoolkit.information.StatisticalTest.class,
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getCombinedTest().getEvolutiveSeasonality()));
            MAPPING.set("summary", String.class, 
                    source->source.getCombinedTest().getSummary().name());
        }

        public InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }
    }
    
    
}

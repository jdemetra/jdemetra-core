/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.tests;

import demetra.information.InformationMapping;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CombinedSeasonalityTestInfo {

    
    final InformationMapping<CombinedSeasonalityTest> MAPPING = new InformationMapping<>(CombinedSeasonalityTest.class);
    
    public InformationMapping<CombinedSeasonalityTest> getMapping() {
        return MAPPING;
    }
    static {
            MAPPING.set("kruskalwallis", ec.tstoolkit.information.StatisticalTest.class, 
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getNonParametricTestForStableSeasonality()));
            MAPPING.set("stable", ec.tstoolkit.information.StatisticalTest.class, 
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getStableSeasonality()));
            MAPPING.set("evolutive", ec.tstoolkit.information.StatisticalTest.class,
                    source->ec.tstoolkit.information.StatisticalTest.create(source.getEvolutiveSeasonality()));
            MAPPING.set("summary", String.class, 
                    source->source.getSummary().name());
            MAPPING.set("stable.ssm", Double.class, 
                    source->source.getStableSeasonality().getSSM());
            MAPPING.set("stable.ssr", Double.class, 
                    source->source.getStableSeasonality().getSSR());
            MAPPING.set("stable.ssq", Double.class, 
                    source->source.getStableSeasonality().getSSQ());
            MAPPING.set("evolutive.ssm", Double.class, 
                    source->source.getEvolutiveSeasonality().getSSM());
            MAPPING.set("evolutive.ssr", Double.class, 
                    source->source.getEvolutiveSeasonality().getSSR());
            MAPPING.set("evolutive.ssq", Double.class, 
                    source->source.getEvolutiveSeasonality().getSSQ());
     }

}

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
package ec.tstoolkit.jdr.regarima;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.jdr.mapping.LikelihoodStatisticsInfo;
import ec.tstoolkit.jdr.mapping.ResidualsInfo;
import ec.tstoolkit.jdr.mapping.SarimaInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import jdr.spec.ts.Utility.Dictionary;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Processor {
    @lombok.Value
    public static class Results implements IProcResults{

        PreprocessingModel model;

        static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate("model", RegArimaInfo.getMapping(), source->source.getModel());
            MAPPING.delegate("arima", SarimaInfo.getMapping(), source->source.getModel().estimation.getArima());
            MAPPING.delegate("likelihood", LikelihoodStatisticsInfo.getMapping(), source->source.getModel().estimation.getStatistics());
            MAPPING.delegate("residuals", ResidualsInfo.getMapping(), source->source.getModel().estimation.getNiidTests());
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
    
    public static Results tramo(TsData s, TramoSpecification spec, Dictionary dic)  {
        ProcessingContext context=null;
        if (dic != null)
            context=dic.toContext();
        PreprocessingModel model = spec.build(context).process(s, null);
        return new Results(model);
    }

    public static Results x12(TsData s, RegArimaSpecification spec, Dictionary dic) {
        ProcessingContext context=null;
        if (dic != null)
            context=dic.toContext();
        PreprocessingModel model = spec.build(context).process(s, null);
        return new Results(model);
    }
    
}

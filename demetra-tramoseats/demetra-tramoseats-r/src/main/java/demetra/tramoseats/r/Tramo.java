/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.tramoseats.r;

import demetra.arima.SarimaModel;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.tramo.TramoSpec;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.regarima.ApiUtility;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.RegSarimaResults;
import jdplus.tramo.TramoProcessor;
import jdplus.tramoseats.extractors.TramoExtractor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Tramo {
    @lombok.Value
    public static class Results implements ProcResults{
        private ModelEstimation core;

        @Override
        public boolean contains(String id) {
            return TramoExtractor.getMapping().contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            TramoExtractor.getMapping().fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return TramoExtractor.getMapping().getData(core, id, tclass);
        }
    }
    
    public Results process(TsData series, String defSpec){
        TramoSpec spec=TramoSpec.fromString(defSpec);
        TramoProcessor tramo= TramoProcessor.of(spec, null);
        RegSarimaResults estimation = tramo.process(series);
        return new Results(estimation.getRegarima());
    }
    
}

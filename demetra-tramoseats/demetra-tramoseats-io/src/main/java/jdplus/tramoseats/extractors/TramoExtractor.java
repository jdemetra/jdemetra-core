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
package jdplus.tramoseats.extractors;

import jdplus.arima.extractors.SarimaExtractor;
import demetra.information.InformationMapping;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoExtractor {
    static final InformationMapping<ModelEstimation> MAPPING = new InformationMapping<>(ModelEstimation.class);

    static {
        
//        MAPPING.delegate(null, LinearModelEstimationDescriptor.getMapping(), x->x);
        MAPPING.set("model", SarimaModel.class, x->x.getModel().arima());
//        MAPPING.delegate("sarima", SarimaExtractor.getMapping(), x->((LinearModelEstimation<SarimaModel>)x).getStochasticComponent());
    }

    public InformationMapping<ModelEstimation> getMapping() {
        return MAPPING;
    }
    
}

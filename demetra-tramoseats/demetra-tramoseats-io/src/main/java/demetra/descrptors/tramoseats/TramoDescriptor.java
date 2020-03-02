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
package demetra.descrptors.tramoseats;

import demetra.arima.SarimaModel;
import demetra.descriptors.arima.SarimaDescriptor;
import demetra.descriptors.timeseries.regression.LinearModelEstimationDescriptor;
import demetra.information.InformationMapping;
import demetra.timeseries.regression.modelling.LinearModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoDescriptor {
    static final InformationMapping<LinearModelEstimation> MAPPING = new InformationMapping<>(LinearModelEstimation.class);

    static {
        
        MAPPING.delegate(null, LinearModelEstimationDescriptor.getMapping(), x->x);
        MAPPING.set("model", SarimaModel.class, x->((LinearModelEstimation<SarimaModel>)x).getStochasticComponent());
        MAPPING.delegate("sarima", SarimaDescriptor.getMapping(), x->((LinearModelEstimation<SarimaModel>)x).getStochasticComponent());
    }

    public InformationMapping<LinearModelEstimation> getMapping() {
        return MAPPING;
    }
    
}

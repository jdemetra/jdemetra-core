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
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ArimaInfo {
    static final InformationMapping<ArimaModel> MAPPING = new InformationMapping<>(ArimaModel.class);

    static {
        MAPPING.set("ar", double[].class, model->model.getStationaryAR().getCoefficients());
        MAPPING.set("diff", double[].class, model->model.getNonStationaryAR().getCoefficients());
        MAPPING.set("ma", double[].class, model->model.getMA().getCoefficients());
        MAPPING.set("fullar", double[].class, model->model.getAR().getCoefficients());
        MAPPING.set("innovationvariance", Double.class, model->model.getInnovationVariance());
    }

    public InformationMapping<ArimaModel> getMapping() {
        return MAPPING;
    }
    
}

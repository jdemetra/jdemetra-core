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
package demetra.regarima.regular;

import demetra.arima.IArimaModel;
import demetra.regarima.RegArimaModel;

/**
 *
 * @author Jean Palate
 */
public interface IOutliersDetectionModule {

    /**
     * Search outliers in the given RegArima model
     *
     * @param model Model being considered. The model will be augmented with the
     * new outliers. On exit, 
     * @param criticalValue Critical value for the detection of outliers
     * @return True if the model was changed, false otherwise
     */
    ProcessingResult process(RegArimaModelling model, double criticalValue);

}

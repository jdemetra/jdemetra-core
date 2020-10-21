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
package jdplus.ucarima;

import jdplus.arima.LinearProcess;
import nbbrd.design.Development;
import jdplus.math.linearfilters.RationalFilter;

/**
 * This class represents the wiener-Kolmogorov estimator of a component of an
 * Ucarima model.
 * It contains the Wiener-Kolmogorov filter (which is applied on the observations)
 * and the model of the estimator (which is related to the innovations)
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class WienerKolmogorovEstimator {

    private final RationalFilter wienerKolmogorovFilter;
    private final LinearProcess estimatorModel;

}

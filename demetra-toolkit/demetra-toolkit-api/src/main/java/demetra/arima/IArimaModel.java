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
package demetra.arima;

/**
 *
 * @author PALATEJ
 */
public interface IArimaModel {

    static final String NAME="arima";
    /**
     * Name of the model (optional); null by default
     */
    default String getName(){
        return NAME;
    }
    
    double getInnovationVariance();
    /**
     * Stationary auto-regressive polynomial (1+ar[0]B...); True signs. 
     * Doesn't contain the constant term (always 1)
     * All the roots of the stationary polynomial should be outside the unit circle (not checked)
     */
    double[] getAr();
    /**
     * Non-stationary auto-regressive polynomial (1, delta(1)...); True signs. 
     * Doesn't contain the constant term (always 1)
     * All the roots of the non-stationary polynomial should be on the unit circle (not checked)
     */
    double[] getDelta();
    /**
     * Moving-average polynomial (1, theta(1)...); True signs.
     * Doesn't contain the constant term (always 1)
     */
    double[] getMa();
   
}

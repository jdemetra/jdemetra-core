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
package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;

/**
 * This interface describes a generic Arima model, defined by 
 * AR(B) y(t) = MA(B) e(t),
 * where B is the back-shift operator,
 * AR(B) is the auto-regressive polynomial (with real coefficients)
 * and MA(B) is the moving average polynomial (with real coefficients).
 * e(t) is the noise (or innovation), often defined by a N(0,sig2) gaussian distribution 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IArimaModel extends ILinearModel
{

    /**
     * Gets the auto-regressive polynomial
     * @return The auto-regressive polynomial in the back-shift operator,
     * represented by a new BackFilter object
     * @see(ec.tstoolkit.maths.linearfilters.BackFilter).
     */
    BackFilter getAR();

    /**
     * Gets the degree of the AR polynomial. 0 if the auto-regressive polynomial
     * is missing.
     * @return The degree of the AR polynomial. >= 0.
     */
    int getARCount();

    /**
     * Gets the moving average polynomial
     * @return The moving average polynomial in the back-shift operator,
     * represented by a new BackFilter object
     * @see(ec.tstoolkit.maths.linearfilters.BackFilter).
     */
    BackFilter getMA();

    /**
     * Gets the degree of the MA polynomial. 0 if the moving average polynomial
     * is missing.
     * @return The degree of the MA polynomial. >= 0.
     */
    int getMACount();

    /**
     * Gets the non stationary (containing only unit roots) auto-regressive polynomial
     * @return The non stationary auto-regressive polynomial in the back-shift operator,
     * represented by a new BackFilter object
     * @see(ec.tstoolkit.maths.linearfilters.BackFilter).
     */
    BackFilter getNonStationaryAR();

    /**
     * Gets the degree of the non stationary AR polynomial (or, equivalently, the 
     * number of unit roots). 
     * 0 if there is no unit roots.
     * @return The number of unit roots. 0 if the model is stationary.
     */
    int getNonStationaryARCount();

    /**
     * 
     * @return
     */
    BackFilter getStationaryAR();

    /**
     * Gets the degree of the stationary (without unit roots) AR polynomial. 
     * @return The degree of the stationary AR polynomial.
     */
    int getStationaryARCount();

    /**
     * Gets the pi-weights of the model.
     * The pi-weights are defined by the rational polynomial AR(B)/MA(B).
     * 
     * @return The pi-weights are represented by a rational polynomial in the back-shift operator.
     */
    RationalBackFilter getPiWeights();

    /**
     * Gets the psi-weights of the model.
     * The psi-weights are defined by the rational polynomial MA(B)/AR(B).
     * 
     * @return The psi-weights are represented by a rational polynomial in the back-shift operator.
     */
    RationalBackFilter getPsiWeights();


    /**
     * Computes the stationary transformation of this model
     * @return The stationary transformation (containing the non-stationary AR polynomial
     * and the stationary model) is returned
     */
    StationaryTransformation stationaryTransformation();

}

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
package demetra.arima;

import demetra.design.Development;
import demetra.maths.linearfilters.IRationalFilter;


/**
 * This interface describes a generic Linear model, defined by 
 * Q(B, F) y(t) = e(t),
 * where B is the back-shift operator, F is the forward operator
 * and Q(B, F) is a rational polynomial (with real coefficients).
 * e(t) is the noise (or innovation), often defined by a N(0,sig2) gaussian distribution 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ILinearModel {
    /**
     * Return the auto-covariance function of the model, provided that the model is stationary
     * @return The auto-covariance function of the model.
     * @throws An ArimaException is thrown when the model is not stationary
     * (which means that the auto-covariance function is not defined)
     */
    AutoCovarianceFunction getAutoCovarianceFunction();

    /**
     * Returns the filter (Q(F,B)) that defines the model 
     * @return The rational filter (in B and F) 
     */
    IRationalFilter getFilter();

    /**
     * Gets the variance of the innovation of the model.
     * @return The variance of the innovation of the model (&>= 0)
     */
    double getInnovationVariance();

    /**
     * Returns the (pseudo-)spectrum of the model.
     * The pseudo-spectrum is defined as follows.
     * If Q(F,B) can be written (N1(B)N2(F))/(D1(B)D2(F)),
     * the auto-covariance generating function (ACGF) of the model
     * is defined by
     * (N1(B)N1(F)N2(B)N2(F))/(D1(B)D1(F)D2(B)D2(F))*sig, where sig is the innovation
     * variance.
     * The pseudo-spectrum is then obtained as the Fourier transform of the ACGF. 
     * See the Spectrum class for further information. 
     * @return The (pseudo-)spectrum of the model. It should be noted that the 
     * pseudo-spectrum is always defined. 
     */
    Spectrum getSpectrum();

    /**
     * Checks that the model is invertible (or that the numerator of its rational filter
     * doesn't contain unit roots).
     * @return True if the model is invertible, false otherwise.
     */
    boolean isInvertible();

    /**
     * Verifies that a model is null. A model is null if it generates always 0 values.
     * In the practice, it means that the model is stationary and that its innovation variance 
     * is 0.
     * @return True if the model is null, false otherwise.
     */
    boolean isNull();

    /**
     * Checks that the model is stationary (or that the denominator of its rational filter
     * doesn't contain unit roots).
     * @return True if the model is stationary, false otherwise.
     */
    boolean isStationary();
}

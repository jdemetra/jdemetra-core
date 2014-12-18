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

package ec.tstoolkit.maths.realfunctions.bfgs;

import ec.tstoolkit.design.Development;

/**
 * A line function is able to compute f(step) f'(step) for positive steps in the
 * range [stepMin, stepMax[
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface ILineFunction {
    /**
     * Gets the derivative corresponding to the given step
     * 
     * @return
     */
    double getDerivative();

    /**
     * Gets the current step of the line function
     * 
     * @return
     */
    double getStep();

    /**
     *
     * @return
     */
    double getStepMax();

    /**
     *
     * @return
     */
    double getStepMin();

    /**
     * Gets the value corresponding to the given step
     * 
     * @return
     */
    double getValue();

    /**
     * Sets the step of the line function
     * 
     * @param step
     */
    void setStep(double step);
}

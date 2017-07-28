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

package demetra.maths.functions;

import demetra.design.Development;
import demetra.data.DoubleSequence;


/**
 * Evaluation of a function for a given set of parameters
 * The evaluation contains the function, the parameters and the value of the function 
 * at that point
 * Formally, if we consider the function y=f(x1...xn), a IFunctionInstance
 * corresponds to a couple ({x1...xn}, f(x1...xn))
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IMFunctionPoint {
    /**
     * Gets the underlying function
     * @return 
     */
    IMFunction getFunction();

    /**
     * Gets the derivatives of the function at this point
     * @return Returns the derivatives of the function. May be numerical or
     * analytical derivatives
     */
    IMFunctionDerivatives getDerivatives();
    /**
     * Gets the parameters of the evaluation
     * @return A read only set of parameters.
     */
    DoubleSequence getParameters();

    /**
     * Gets the value of the function for the set of parameters of the
     * IFunctionInstance
     * @return A Double value. May be Double.Nan
     */
    DoubleSequence getValues();
}
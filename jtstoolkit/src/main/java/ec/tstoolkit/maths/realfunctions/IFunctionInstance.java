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

package ec.tstoolkit.maths.realfunctions;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 * Evaluation of a function for a given set of parameters
 * The evaluation contains the set of parameters and the value of the function 
 * for that set.
 * Formally, if we consider the function y=f(x1...xn), a IFunctionInstance
 * corresponds to a couple ({x1...xn}, f(x1...xn))
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFunctionInstance {
    /**
     * Gets the parameters of the evaluation
     * @return A read only set of parameters.
     */
    IReadDataBlock getParameters();

    /**
     * Gets the value of the function for the set of parameters of the
     * IFunctionInstance
     * @return A Double value. May be Double.Nan
     */
    double getValue();
}
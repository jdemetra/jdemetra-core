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
 * Generic interface for real functions
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFunction {

    /**
     * Evaluates the function for a given set of parameters
     * @param parameters The parameters (read only)
     * @return The evaluation of the function (may be null)
     */
    IFunctionInstance evaluate(IReadDataBlock parameters);

    /**
     * Gets the derivatives of the function at a given point
     * @param point The point (see IFunctionInstance for further information)
     * @return Returns the derivatives of the function. May be numerical or
     * analytical derivatives
     */
    IFunctionDerivatives getDerivatives(IFunctionInstance point);

    /**
     * Gets the domain of the function
     * @return The domain of the function
     */
    IParametersDomain getDomain();
}

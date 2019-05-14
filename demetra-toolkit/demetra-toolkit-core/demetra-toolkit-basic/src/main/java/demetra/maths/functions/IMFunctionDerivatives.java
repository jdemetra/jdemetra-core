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
import demetra.maths.matrices.Matrix;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IMFunctionDerivatives {
    /**
     * Gets the underlying function
     * @return 
     */
    IMFunction getFunction();
    /**
     * Computes dF(y)/d(x(var))
     * @param iy
     * @param ix
     * @return
     */
    double getPartialDerivative(int iy, int ix);

    /**
     * Computes the Jacobian
     * J(i, j)=partialDerivatives(i,j)
     * @param jacobian
     */
    void getJacobian(Matrix jacobian);
}

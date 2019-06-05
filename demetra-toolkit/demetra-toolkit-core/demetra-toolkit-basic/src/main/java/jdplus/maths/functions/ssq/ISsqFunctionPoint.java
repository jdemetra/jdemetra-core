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

package jdplus.maths.functions.ssq;

import demetra.design.Development;
import demetra.data.DoubleSeq;


/**
 * f(p) = sum(e(t,p)^2)
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsqFunctionPoint {

    /**
     * Gets the underlying function
     * @return 
     */
    ISsqFunction getSsqFunction();
    /**
     * Gets the derivatives of the function at this point
     * @return Returns the derivatives of the function. May be numerical or
     * analytical derivatives
     */
    default ISsqFunctionDerivatives ssqDerivatives(){
        return new SsqNumericalDerivatives(this, false);
    };
    
    default int getDim(){
        return getE().length();
    }
    /**
     * 
     * @return Returns e(t,p)
     */
    DoubleSeq getE();

    /**
     * 
     * @return Returns p
     */
    DoubleSeq getParameters();

    /**
     * Gets the value of the function
     * @return 
     */
    default double getSsqE(){
        return getE().ssq();
    };
}

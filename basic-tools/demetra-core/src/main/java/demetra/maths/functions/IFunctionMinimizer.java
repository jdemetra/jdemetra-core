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
import demetra.data.Doubles;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFunctionMinimizer {

    /**
     * 
     * @return
     */
    IFunctionMinimizer exemplar();

    /**
     * 
     * @return
     */
    double getFunctionPrecision();

    /**
     * 
     * @return
     */
    double getParametersPrecision();
    /**
     * 
     * @return
     */
    Matrix curvatureAtMinimum();
    
    Doubles gradientAtMinimum();

    /**
     *
     * @return
     */
    int getIterCount();

    /**
     *
     * @return
     */
    int getMaxIter();

    /**
     * 
     * @return
     */
    IFunctionPoint getResult();

    double getObjective();

    /**
     * 
     * @param function
     * @param start
     * @return
     */
    boolean minimize(IFunctionPoint start);

    /**
     * 
     * @param function
     * @return
     */
    default boolean minimize(IFunction function){
        Doubles start = function.getDomain().getDefault();
        return minimize(function.evaluate(start));
    }
    
    /**
     * 
     * @param value
     */
    void setFunctionPrecision(double value);

    /**
     * 
     * @param value
     */
    void setParametersPrecsion(double value);
    /**
     *
     * @param n
     */
    void setMaxIter(int n);
}

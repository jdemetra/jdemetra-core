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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsqFunctionMinimizer {

    /**
     * 
     * @return
     */
    ISsqFunctionMinimizer exemplar();

    /**
     * 
     * @return
     */
    double getConvergenceCriterion();

    /**
     * 
     * @return
     */
    Matrix getCurvature();

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
    ISsqFunctionInstance getResult();
    
    double[] getGradient();

    double getObjective();

    /**
     * 
     * @param function
     * @param start
     * @return
     */
    boolean minimize(ISsqFunction function, ISsqFunctionInstance start);

    /**
     * 
     * @param value
     */
    void setConvergenceCriterion(double value);

    /**
     *
     * @param n
     */
    void setMaxIter(int n);
}

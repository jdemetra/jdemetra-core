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
public class ProxyMinimizer implements IFunctionMinimizer {

    private ISsqFunctionMinimizer m_min;

    /**
     *
     * @param min
     */
    public ProxyMinimizer(ISsqFunctionMinimizer min) {
        m_min = min;
    }
    
    public ISsqFunctionMinimizer getCore(){
        return m_min;
    }

    @Override
    public IFunctionMinimizer exemplar() {
        return new ProxyMinimizer(m_min.exemplar());
    }

    /**
     *
     * @return
     */
    @Override
    public double getConvergenceCriterion() {
        return m_min.getConvergenceCriterion();
    }

    @Override
    public Matrix getCurvature() {
        return m_min.getCurvature();
    }

    @Override
    public double[] getGradient() {
        return m_min.getGradient();
    }

    @Override
    public double getObjective() {
        return m_min.getObjective();
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return m_min.getIterCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return m_min.getMaxIter();
    }

    @Override
    public IFunctionInstance getResult() {
        return (IFunctionInstance) m_min.getResult();
    }

    /**
     *
     * @param function
     * @param start
     * @return
     */
    @Override
    public boolean minimize(IFunction function, IFunctionInstance start) {
        ISsqFunction fn = (ISsqFunction) function;
        ISsqFunctionInstance s = fn.ssqEvaluate(start.getParameters());
        return m_min.minimize(fn, s);

    }

    /**
     *
     * @param value
     */
    @Override
    public void setConvergenceCriterion(double value) {
        m_min.setConvergenceCriterion(value);
    }

    /**
     *
     * @param n
     */
    @Override
    public void setMaxIter(int n) {
        m_min.setMaxIter(n);
    }
}

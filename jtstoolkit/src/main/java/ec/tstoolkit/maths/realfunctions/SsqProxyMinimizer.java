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
public class SsqProxyMinimizer implements ISsqFunctionMinimizer {

    private IFunctionMinimizer m_min;

    /**
     * 
     * @param min
     */
    public SsqProxyMinimizer(IFunctionMinimizer min) {
	m_min = min;
    }

    @Override
    public ISsqFunctionMinimizer exemplar() {
	return new SsqProxyMinimizer(m_min.exemplar());
    }

    /**
     * 
     * @return
     */
    @Override
    public double getConvergenceCriterion() {
	return m_min.getConvergenceCriterion();
    }

    /**
     * 
     * @return
     */
    @Override
    public Matrix getCurvature() {
	return m_min.getCurvature();
    }

     /**
     * 
     * @return
     */
    @Override
    public double[] getGradient() {
	return m_min.getGradient();
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
    public ISsqFunctionInstance getResult() {
	SsqProxyFunctionInstance rslt = (SsqProxyFunctionInstance) m_min
		.getResult();
	return rslt.m_f;
    }

      @Override
    public double getObjective() {
	return m_min.getObjective();
    }
  /**
     * 
     * @param function
     * @param start
     * @return
     */
    @Override
    public boolean minimize(ISsqFunction function, ISsqFunctionInstance start) {
	return m_min.minimize(new SsqProxyFunction(function),
		new SsqProxyFunctionInstance(start));
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

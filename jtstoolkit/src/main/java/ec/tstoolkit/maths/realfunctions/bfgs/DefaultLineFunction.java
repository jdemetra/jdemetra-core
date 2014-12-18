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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.FunctionException;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultLineFunction implements ILineFunction {

    private IFunction m_fn;

    private IFunctionInstance m_ftry;

    private DataBlock m_ptmp, m_origin;

    private DataBlock m_dir;

    private double m_step, m_max = 1, m_dstep1 = 8, m_dstep2 = 1.05, m_df;

    private int m_maxiter = 100;

    /** Creates a new instance of DefaultLineFunction
     * @param fn
     * @param grad
     * @param dir
     * @param point
     */
    public DefaultLineFunction(IFunction fn, IFunctionInstance point,
	    double[] dir, double[] grad) {
	m_fn = fn;
	m_ftry = point;
	m_ptmp = new DataBlock(point.getParameters());
	m_origin = new DataBlock(m_ptmp);
	m_dir = new DataBlock(dir.clone());
	if (grad != null)
	    m_df = m_dir.dot(new DataBlock(grad));
	calcMax();
    }

    private double calcEpsilon() {
	double eps = 0;
	for (int i = 0; i < m_dir.getLength(); ++i) {
	    double di = Math.abs(m_dir.get(i));
	    if (di != 0) {
		double e = m_fn.getDomain().epsilon(m_ftry.getParameters(), i)
			/ di;
		if (eps == 0 || eps > e)
		    eps = e;
	    }
	}
	return eps;
    }

    private void calcMax() {
	// m_max=1;
	m_max = 1;
	if (calcp(m_max)) {
	    // expand
	    m_max = expand(m_max, m_dstep1, false);
	    m_max = expand(m_max, 1 / m_dstep2, true);
	} else {
	    // shrink
	    m_max = expand(m_max, 1 / m_dstep1, true);
	    if (m_max != 0) {
		m_max = expand(m_max, m_dstep2, false);
		m_max /= m_dstep2;
	    }
	}
    }

    private boolean calcp(double alpha) {
	m_ptmp.copy(m_origin);
	m_ptmp.addAY(alpha, m_dir);
	return m_fn.getDomain().checkBoundaries(m_ptmp);
    }

    // expand val by factor.
    // if test == true, stop for the first acceptable val (!= val)(return value)
    // else stop for the first non acceptable value ( (return value)
    private double expand(double val, double factor, boolean test) {
	int iter = 0;
	if (test)
	    do
		val *= factor;
	    while (!calcp(val) && iter++ < m_maxiter);
	else
	    do
		val *= factor;
	    while (calcp(val * factor) && iter++ < m_maxiter);
	if (test && iter == m_maxiter)
	    if (factor <= 1)
		return 0;
	    else
		return Double.MAX_VALUE;
	else
	    return val;
    }

    @Override
    public double getDerivative() {
	if (m_df != 0)
	    return m_df;
	double eps = calcEpsilon();
	int n = 10;
	while (n >= 0) {
	    if (calcp(m_step + eps)) {
		m_df = (m_fn.evaluate(m_ptmp).getValue() - m_ftry.getValue())
			/ eps;
		return m_df;
	    } else if (calcp(m_step - eps)) {
		m_df = (m_ftry.getValue() - m_fn.evaluate(m_ptmp).getValue())
			/ eps;
		return m_df;
	    }
	    eps /= 2;
	    --n;
	}
	throw new FunctionException(FunctionException.D_ERR);
    }

    /**
     * 
     * @return
     */
    public IFunctionInstance getResult()
    {
	return m_ftry;
    }

    @Override
    public double getStep() {
	return m_step;
    }

    /**
     *
     * @return
     */
    @Override
    public double getStepMax() {
	return m_max;
    }

    /**
     *
     * @return
     */
    @Override
    public double getStepMin() {
	return 0;
    }

    @Override
    public double getValue() {
	return m_ftry.getValue();
    }

    @Override
    public void setStep(double value) {
	if (value == m_step)
	    return;
	if (value < 0 || value > m_max || !calcp(value))
	    throw new FunctionException(FunctionException.STEP_ERR);
	m_step = value;
	m_ftry = m_fn.evaluate(m_ptmp);
	m_df = 0;

    }
}

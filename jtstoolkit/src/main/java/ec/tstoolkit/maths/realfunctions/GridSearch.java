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
public class GridSearch implements IFunctionMinimizer {

    private double m_eps = 1e-7, m_deps = 1e-6;

    private double m_lbound = -1, m_ubound = 1;

    private double m_a, m_b, m_dfn, m_va, m_vb, m_x;

    private int m_nsteps0 = 30, m_nsteps1 = 4;

    private IFunction m_fn;

    private IFunctionInstance m_ftry;

    private int maxIter = 100, niter;

    /**
     *
     */
    public GridSearch() {
    }

    private void clear() {
        m_ftry = null;
        m_fn = null;
        m_dfn = Double.MAX_VALUE;
        m_va = Double.NaN;
        m_vb = Double.NaN;
        m_x = Double.NaN;
    }

    @Override
    public IFunctionMinimizer exemplar() {
        GridSearch search = new GridSearch();
        search.m_eps = m_eps;
        search.m_nsteps0 = m_nsteps0;
        search.m_nsteps1 = m_nsteps1;
        search.m_lbound = m_lbound;
        search.m_ubound = m_ubound;
        return search;
    }

    @Override
    public double getConvergenceCriterion() {
        return m_eps;
    }

    @Override
    public Matrix getCurvature() {
        return new NumericalDerivatives(m_fn, m_ftry, false).getHessian();
    }

    @Override
    public double[] getGradient() {
        return new NumericalDerivatives(m_fn, m_ftry, false).getGradient();
    }

    /**
     *
     * @return
     */
    public int getGridCount() {
        return m_nsteps1;
    }

    public int getInitialGridCount() {
        return m_nsteps0;
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return niter;
    }

    /**
     *
     * @return
     */
    public double getLBound() {
        return m_lbound;
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return maxIter;
    }

    @Override
    public IFunctionInstance getResult() {
        return m_ftry;
    }

    @Override
    public double getObjective() {
        return m_ftry == null ? Double.NaN : m_ftry.getValue();
    }

    /**
     *
     * @return
     */
    public double getUBound() {
        return m_ubound;
    }

    private boolean iterate(int nsteps) {
        // evaluate the function at the grid points
        double step = (m_b - m_a) / nsteps;
        double[] vals = new double[nsteps + 1];
        vals[0] = m_va;
        vals[nsteps] = m_vb;
        for (int i = 1; i < nsteps; ++i) {
            vals[i] = evaluate(m_a + i * step);
        }
        double min = Double.MAX_VALUE;
        int imin = -1;
        for (int i = 0; i <= nsteps; ++i) {
            double val = vals[i];
            if (!Double.isNaN(val) && val < min) {
                imin = i;
                min = val;
            }
        }
        if (imin == -1) {
            return false;
        }
        m_x = m_a + step * imin;
        if (imin == 0) {
            m_b = m_a + step;
            m_vb = vals[1];
        } else if (imin == nsteps) {
            m_a = m_b - step;
            m_va = vals[nsteps - 1];
        } else {
            m_a += step * (imin - 1);
            m_b = m_a + 2 * step;
            m_va = vals[imin - 1];
            m_vb = vals[imin + 1];
        }
        m_dfn = Math.abs(m_va - m_vb);
        return true;
    }

    // / <summary>
    // /
    // / </summary>
    // / <param name="function"></param>
    // / <param name="start">Unused. Should be null</param>
    // / <returns></returns>
    @Override
    public boolean minimize(IFunction function, IFunctionInstance start) {
        clear();
        if (function.getDomain().getDim() != 1 || m_lbound >= m_ubound
                || m_nsteps0 < 3 || m_nsteps1 < 3) {
            return false;
        }
        m_fn = function;
        m_a = m_lbound;
        m_b = m_ubound;
        m_va = evaluate(m_a);
        m_vb = evaluate(m_b);
        niter = 0;
        while (niter++ < maxIter && m_b - m_a > m_deps && (Double.isNaN(m_dfn )||m_dfn > m_eps) ){
            if (!iterate(niter == 1 ? m_nsteps0 : m_nsteps1)) {
                return false;
            }
        }
        m_ftry = m_fn.evaluate(new SingleParameter(m_x));
        return true;
    }

    private double evaluate(double x) {
        try {
            IFunctionInstance fn = m_fn.evaluate(new SingleParameter(x));
            return fn.getValue();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    /**
     *
     * @param lb
     * @param ub
     */
    public void setBounds(double lb, double ub) {
        if (lb >= ub) {
            throw new FunctionException("Invalid bounds");
        }
        m_lbound = lb;
        m_ubound = ub;
    }

    @Override
    public void setConvergenceCriterion(double value) {
        m_eps = value;
    }

    public void setPrecision(double value) {
        m_deps = value;
    }
    /**
     *
     * @param value
     */
    public void setGridCount(int value) {
        m_nsteps1 = value;
    }

    /**
     *
     * @param value
     */
    public void setInitialGridCount(int value) {
        m_nsteps0 = value;
    }

    /**
     *
     * @param n
     */
    @Override
    public void setMaxIter(int n) {
        maxIter = n;
    }
}

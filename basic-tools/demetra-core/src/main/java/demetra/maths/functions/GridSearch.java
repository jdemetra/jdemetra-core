/*
 * Copyright 2013 National Bank ofInternal Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofInternal the Licence at:
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

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GridSearch implements IFunctionMinimizer {

    private double epsilon = 1e-7, precision = 1e-6;

    private double lbound = -1, ubound = 1;

    private double a, b, dfn, va, vb, curX;

    private int m_nsteps0 = 30, m_nsteps1 = 4;

    private IFunction fn;

    private IFunctionPoint m_ftry;

    private int maxIter = 100, niter;

    /**
     *
     */
    public GridSearch() {
    }

    private void clear() {
        m_ftry = null;
        fn = null;
        dfn = Double.MAX_VALUE;
        va = Double.NaN;
        vb = Double.NaN;
        curX = Double.NaN;
    }

    @Override
    public IFunctionMinimizer exemplar() {
        GridSearch search = new GridSearch();
        search.epsilon = epsilon;
        search.m_nsteps0 = m_nsteps0;
        search.m_nsteps1 = m_nsteps1;
        search.lbound = lbound;
        search.ubound = ubound;
        return search;
    }

    @Override
    public double getFunctionPrecision() {
        return epsilon;
    }

    @Override
    public Matrix curvatureAtMinimum() {
        Matrix h = Matrix.square(1);
        new NumericalDerivatives(m_ftry, false).hessian(h);
        return h;
    }

    @Override
    public Doubles gradientAtMinimum() {
        return new NumericalDerivatives(m_ftry, false).gradient();
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
        return lbound;
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
    public IFunctionPoint getResult() {
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
        return ubound;
    }

    private boolean iterate(int nsteps) {
        // evaluate the function at the grid points
        double step = (b - a) / nsteps;
        double[] vals = new double[nsteps + 1];
        vals[0] = va;
        vals[nsteps] = vb;
        for (int i = 1; i < nsteps; ++i) {
            vals[i] = evaluate(a + i * step);
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
        curX = a + step * imin;
        if (imin == 0) {
            b = a + step;
            vb = vals[1];
        } else if (imin == nsteps) {
            a = b - step;
            va = vals[nsteps - 1];
        } else {
            a += step * (imin - 1);
            b = a + 2 * step;
            va = vals[imin - 1];
            vb = vals[imin + 1];
        }
        dfn = Math.abs(va - vb);
        return true;
    }

    // / <summary>
    // /
    // / </summary>
    // / <param name="function"></param>
    // / <param name="reader">Unused. Should be null</param>
    // / <returns></returns>
    @Override
    public boolean minimize(IFunctionPoint start) {
        clear();
        IFunction function = start.getFunction();
        if (function.getDomain().getDim() != 1 || lbound >= ubound
                || m_nsteps0 < 3 || m_nsteps1 < 3) {
            return false;
        }
        fn = function;
        a = lbound;
        b = ubound;
        va = evaluate(a);
        vb = evaluate(b);
        niter = 0;
        while (niter++ < maxIter && b - a > precision && (Double.isNaN(dfn) || dfn > epsilon)) {
            if (!iterate(niter == 1 ? m_nsteps0 : m_nsteps1)) {
                return false;
            }
        }
        m_ftry = fn.evaluate(DataBlock.ofInternal(new double[]{curX}));
        return true;
    }

    private double evaluate(double x) {
        try {
            IFunctionPoint fx = this.fn.evaluate(DataBlock.ofInternal(new double[]{x}));
            return fx.getValue();
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
        lbound = lb;
        ubound = ub;
    }

    @Override
    public void setFunctionPrecision(double value) {
        epsilon = value;
    }

    public void setPrecision(double value) {
        precision = value;
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

    @Override
    public double getParametersPrecision() {
        return precision;
    }

    @Override
    public void setParametersPrecsion(double value) {
        precision = value;
    }
}

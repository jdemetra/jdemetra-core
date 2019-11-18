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
package jdplus.math.functions;

import jdplus.data.DataBlock;
import demetra.design.Development;
import jdplus.math.matrices.Matrix;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GridSearch implements FunctionMinimizer {

    public static class GridSearchBuilder {

        private double fnPrecision = 1e-7;
        private double paramPrecision = 1e-6;
        private int maxIter = 100;
        private int gridCount = 4, initialGridCount = 30;
        private double lbound = -1, ubound = 1;

        private GridSearchBuilder() {
        }

        public GridSearchBuilder functionPrecision(double eps) {
            fnPrecision = eps;
            return this;
        }

        public GridSearchBuilder parametersPrecision(double eps) {
            paramPrecision = eps;
            return this;
        }

        public GridSearchBuilder maxIter(int niter) {
            maxIter = niter;
            return this;
        }

        public GridSearchBuilder initialGridCount(int n) {
            initialGridCount = n;
            return this;
        }

        public GridSearchBuilder gridCount(int n) {
            gridCount = n;
            return this;
        }

        public GridSearchBuilder bounds(double l, double u) {
            lbound = l;
            ubound = u;
            return this;
        }

        public GridSearch build() {
            return new GridSearch(this);
        }

    }

    public static GridSearchBuilder builder() {
        return new GridSearchBuilder();
    }

    private final double epsilon, precision;
    private final double lbound, ubound;
    private final int m_nsteps0, m_nsteps1;
    private final int maxIter;

    private double a, b, dfn, va, vb, curX;
    private IFunction fn;
    private IFunctionPoint m_ftry;
    private int niter;

    /**
     *
     */
    private GridSearch(GridSearchBuilder builder) {
        epsilon = builder.fnPrecision;
        precision = builder.paramPrecision;
        lbound = builder.lbound;
        ubound = builder.ubound;
        m_nsteps0 = builder.initialGridCount;
        m_nsteps1 = builder.gridCount;
        maxIter = builder.maxIter;
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
    public Matrix curvatureAtMinimum() {
        Matrix h = Matrix.square(1);
        new NumericalDerivatives(m_ftry, false).hessian(h);
        return h;
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
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
    public double getLBound() {
        return lbound;
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
        m_ftry = fn.evaluate(DataBlock.of(new double[]{curX}));
        return true;
    }

    private double evaluate(double x) {
        try {
            IFunctionPoint fx = this.fn.evaluate(DataBlock.of(new double[]{x}));
            return fx.getValue();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

}

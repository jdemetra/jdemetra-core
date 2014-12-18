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
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.FunctionException;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.NumericalDerivatives;
import ec.tstoolkit.maths.realfunctions.ParamValidation;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class Bfgs implements IFunctionMinimizer {

    private double[] m_x, m_y, m_s, m_g, m_xprev, m_gprev, m_d;
    private IFunction m_fn;
    private IFunctionInstance m_ftry;
    private Matrix m_B;
    private ILineSearch m_lsearch = new QuadraticLineSearch();
    private double m_eps = 1e-9, m_geps = 1e-7, m_alpha, m_f, m_fprev,
            m_falpha = .5;
    private int m_iter, m_maxiter = 100, m_bdirty;
    private boolean m_bUpdated, m_gUpdated, m_bStrong=true, m_bConverged;
    private static final int m_bminiter = 5;

    /** Creates a new instance of L_BFGS_B */
    public Bfgs() {
    }

    private void calcD() {
        // d=-H*g
        DataBlock d = new DataBlock(m_d), g = new DataBlock(m_gprev);
        d.product(m_B.rows(), g);
        d.chs();
    }

    private void calcGrad(double[] g) {
        double[] grad = m_fn.getDerivatives(m_ftry).getGradient();
        System.arraycopy(grad, 0, g, 0, g.length);
    }

    private void calcSGrad(double[] g) {
        NumericalDerivatives D = new NumericalDerivatives(m_fn, m_ftry, true);
        double[] grad = D.getGradient();
        System.arraycopy(grad, 0, g, 0, g.length);
    }

    private boolean canStop(int test) {
        if (test == 1 && m_bdirty < 2) {
            return false;
        }
        if (test == 1) {
            return m_f <= m_fprev
                    && (m_fprev - m_f) / Math.abs(m_fprev) < m_eps;
        } else if (test == 2) {
            double gnorm = new DataBlock(m_g).nrm2();
            return gnorm / Math.abs(m_fprev) < m_geps;
        }
        return false;
    }

    @Override
    public IFunctionMinimizer exemplar() {
        Bfgs bfgs = new Bfgs();
        bfgs.m_lsearch = m_lsearch.exemplar();
        bfgs.m_eps = m_eps;
        bfgs.m_geps = m_geps;
        bfgs.m_alpha = m_alpha;
        bfgs.m_falpha = m_falpha;
        bfgs.m_maxiter = m_maxiter;
        bfgs.m_bStrong=m_bStrong;
        return bfgs;
    }

    /**
     * 
     * @return
     */
    public Matrix getB() {
        if (m_ftry == null) {
            return null;
        }
        if (!m_bUpdated) {
            if (!m_gUpdated) {
                calcGrad(m_g);
                m_gUpdated = true;
            }
            if (m_bdirty < m_bminiter || !update()) {
                Matrix h = new NumericalDerivatives(m_fn, m_ftry, true).getHessian();
                m_B = SymmetricMatrix.inverse(h);
            }
            m_bUpdated = true;
        }
        return m_B;
    }

    @Override
    public double getConvergenceCriterion() {
        return m_eps;
    }

    @Override
    public Matrix getCurvature() {
        Matrix b = getB();
        if (b == null) {
            return null;
        } else {
            return SymmetricMatrix.inverse(b);
        }
    }

    /**
     * 
     * @return
     */
    public double[] getGradient() {
        if (m_ftry == null) {
            return null;
        }
        if (!m_gUpdated) {
            calcGrad(m_g);
            m_gUpdated = true;
        }
        return m_g;
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return m_iter;
    }

    /**
     * 
     * @return
     */
    public ILineSearch getLineSearch() {
        return m_lsearch;
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
        return m_maxiter;
    }

    @Override
    public IFunctionInstance getResult() {
        return m_ftry;
    }

    @Override
    public double getObjective() {
        return m_ftry == null ? Double.NaN : m_ftry.getValue();
    }

    private void initialize(IFunction fn, IFunctionInstance pstart) {
        m_fn = fn;
        m_iter = 0;
        int n = m_fn.getDomain().getDim();
        m_g = new double[n];
        m_gprev = new double[n];
        m_x = new double[n];
        m_xprev = new double[n];
        m_d = new double[n];
        m_s = new double[n];
        m_y = new double[n];
        m_B = new Matrix(n, n);
        start(pstart);
    }

    /**
     * 
     * @return
     */
    public boolean isUsingStrongStopConditions() {
        return m_bStrong;
    }

    private boolean iterate() {
        if (canStop(2)) {
            m_bConverged = true;
            return false;
        }

        // copy old results...
        int n = m_x.length;
        System.arraycopy(m_x, 0, m_xprev, 0, m_x.length);
        System.arraycopy(m_g, 0, m_gprev, 0, m_g.length);
        m_fprev = m_f;
        m_bUpdated = false;
        m_gUpdated = false;
        if (!lsearch()) {
            return false;
        }
        if (m_bdirty > 0) {
            if (!m_bStrong) {
                if (canStop(1)) {
                    m_bConverged = true;
                    return false;
                }
                calcGrad(m_g);
                m_gUpdated = true;
                if (canStop(2)) {
                    m_bConverged = true;
                    return false;
                }
            } else {
                calcGrad(m_g);
                m_gUpdated = true;
                if (canStop(1) && canStop(2)) {
                    return false;
                }
            }

            update();
        }
        return true;
    }

    private boolean lsearch() {
        try {
            calcD();
            DefaultLineFunction fn = new DefaultLineFunction(m_fn, m_ftry, m_d,
                    m_gprev);
            if (fn.getDerivative() >= 0) {
                if (m_bdirty == 0) {
                    return false;
                }
                start(null);
                return true;
            }

            if (!m_lsearch.optimize(fn, m_alpha)) {
                if (m_bdirty == 0) {
                    return false;
                }
                start(null);
                return true;
            }
            ++m_bdirty;

            IFunctionInstance ftry = fn.getResult();
            DataBlock newval = new DataBlock(ftry.getParameters());
            ParamValidation v = m_fn.getDomain().validate(newval);
            if (v == ParamValidation.Invalid) {
                throw new FunctionException(FunctionException.BOUND_ERR);
            } else if (v == ParamValidation.Changed) {
                start(m_fn.evaluate(newval));
                return true;
            } else {
                m_ftry = ftry;
                m_f = ftry.getValue();
                newval.copyTo(m_x, 0);
                return true;
            }
        } catch (RuntimeException err) {
            return false;
        }
    }

    @Override
    public boolean minimize(IFunction function, IFunctionInstance start) {
        initialize(function, start);
        while (iterate() && m_iter < m_maxiter) {
            ++m_iter;
        }

        return true;
    }
    
    public boolean hasConverged(){
        return this.m_bConverged;
    }

    /**
     * 
     * @param function
     * @param pstart
     * @return
     */
    public boolean minimize(IFunction function, IReadDataBlock pstart) {
        return minimize(function, function.evaluate(pstart));
    }

    @Override
    public void setConvergenceCriterion(double value) {
        m_eps = value;
    }

    /**
     * 
     * @param value
     */
    public void setLineSearch(ILineSearch value) {
        m_lsearch = value;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setMaxIter(int value) {
        m_maxiter = value;
    }

    private void start(IFunctionInstance start) {
        int n = m_x.length;
        if (start != null) {
            m_ftry = start;
            IReadDataBlock p = m_ftry.getParameters();
            p.copyTo(m_x, 0);
            m_alpha = 1;
            m_falpha = 1 / Math.sqrt(n);
            m_f = m_ftry.getValue();
            calcGrad(m_g);
        }
        m_B.subMatrix().set(0);
        m_B.subMatrix().diagonal().set(1);
        for (int i = 0; i < n; ++i) {
            m_d[i] = -m_g[i];// *g;
        }
        m_bConverged = false;
        m_bdirty = 0;
    }

    private boolean update() {
        int n = m_s.length;
        for (int i = 0; i < n; ++i) {
            m_s[i] = m_x[i] - m_xprev[i];
            m_y[i] = m_g[i] - m_gprev[i];
        }
        DataBlock p = new DataBlock(m_s);
        DataBlock q = new DataBlock(m_y);
        double r = p.dot(q);
        if (r <= 0 || r <= 1e-12 * p.nrm2() * q.nrm2()) {
            return false;
        } else {
            double[] bq = new double[n];
            DataBlockIterator brows = m_B.rows();
            DataBlock brow = brows.getData();
            do {
                bq[brows.getPosition()] = brow.dot(q);
            } while (brows.next());

            double b1 = 1 + SymmetricMatrix.quadraticForm(m_B, m_y) / r;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    m_B.add(i, j,
                            (b1 * m_s[i] * m_s[j] - m_s[i] * bq[j] - m_s[j]
                            * bq[i])
                            / r);
                }
            }
            return true;
        }
    }

    /**
     * 
     * @param value
     */
    public void useStrongStopConditions(boolean value) {
        m_bStrong = value;
    }
}

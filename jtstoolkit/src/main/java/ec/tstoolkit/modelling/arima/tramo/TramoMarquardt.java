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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.FunctionException;
import ec.tstoolkit.maths.realfunctions.ISsqFunction;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ParamValidation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
class TramoMarquardt implements ISsqFunctionMinimizer {

    public TramoMarquardt() {
        //
        // TODO: Add constructor logic here
        //
    }

    @Override
    public ISsqFunctionMinimizer exemplar() {
        TramoMarquardt qrm = new TramoMarquardt();
        qrm.m_criterion = m_criterion;
        qrm.m_dstep = m_dstep;
        qrm.m_istep = m_istep;
        qrm.m_lambda = m_lambda;
        qrm.m_maxiter = m_maxiter;
        return qrm;
    }

    /**
     *
     * @return
     */
    @Override
    public double getConvergenceCriterion() {
        return m_criterion;
    }

    @Override
    public void setConvergenceCriterion(double value) {
        m_criterion = value;
    }

    /**
     *
     * @return
     */
    public double getCurrentLambda() {
        return m_curlambda;
    }

    @Override
    public Matrix getCurvature() {
        calcVar();
        return m_alpha;
    }

    @Override
    public double[] getGradient() {
        int n = m_beta.length;
        double[] g = new double[n];
        for (int c = 0; c < n; ++c) {
            g[c] = -m_beta[c];
        }
        return g;
    }

    /**
     *
     * @return
     */
    public double getDecreaseStep() {
        return m_dstep;
    }

    /**
     *
     * @return
     */
    public double getIncreaseStep() {
        return m_istep;
    }

    /**
     *
     * @return
     */
    public double getInitialLambda() {
        return m_lambda;
    }

    /**
     *
     * @return
     */
    public Matrix getInvCurvature() {
        calcVar();
        return m_alpha;
    }

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
        return m_niter;
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
    public void setMaxIter(int n) {
        m_maxiter = n;
    }

    @Override
    public ISsqFunctionInstance getResult() {
        return m_ftry;
    }

    @Override
    public double getObjective() {
        return m_ftry == null ? Double.NaN : m_ftry.getSsqE();
    }

    /**
     *
     * @param fn
     * @param pstart
     * @return
     */
    public boolean minimize(ISsqFunction fn, IReadDataBlock pstart) {
        return minimize(fn, fn.ssqEvaluate(pstart));
    }

    @Override
    public boolean minimize(final ISsqFunction fn,
            final ISsqFunctionInstance start) {
        clear();
        m_fn = fn;
        initialize(start);
        if (m_a.isEmpty()) {
            return true;
        }
        boolean bnewval = true;

        do {
            bnewval = iterate(bnewval);
        } while (nextIteration());
        endIteration();
        return m_niter < m_maxiter;
    }

    protected void endIteration() {
    }

    protected boolean nextIteration() {
        if (m_niter >= m_maxiter) {
            return false;
        }
        double dobj = Math.abs(m_obj0 - m_obj1) / (1 + Math.abs(m_obj0));
        return m_niter < m_maxiter && dobj > m_criterion
                && m_dmax > Math.sqrt(m_criterion);
    }

    private void calcVar() {
        if (m_alpha == null) {
            int n = m_beta.length;
            m_alpha = new Matrix(n, n);
            for (int c = 0; c < n; ++c) {
                for (int r = 0; r <= c; ++r) {
                    double salpha = 0;
                    for (int i = 0; i < m_ndata; ++i) {
                        salpha += m_dfn.get(i, c) * m_dfn.get(i, r);// *sig2i;
                    }
                    m_alpha.set(c, r, salpha);
                }
            }
            SymmetricMatrix.fromLower(m_alpha);
        }
        try {
            m_var = SymmetricMatrix.inverse(m_alpha);
        } catch (MatrixException ex) {
            m_var = null;
        }
    }

    protected void clear() {
        m_ftry = null;
        m_e = null;
        m_alpha = null;
        m_niter = 0;
        m_curlambda = 0;
    }

    protected void initialize(final ISsqFunctionInstance start) {
        // initialization
        m_niter = 0;
        m_ftry = start;
        IReadDataBlock p = start.getParameters();
        m_a = new DataBlock(p.getLength());
        p.copyTo(m_a.getData(), 0);
        int n = m_a.getLength();
        m_beta = new double[n];
        m_curlambda = 0;
        m_e = m_ftry.getE();
        m_obj1 = m_ftry.getSsqE();
    }

    protected boolean iterate(final boolean bnewval) {
        ++m_niter;
        m_obj0 = m_obj1;

        int ne = m_e.length, n = m_beta.length;
        m_ndata = ne;
        int nc = ne + n;

        // if (bnewval)
        calcDerivatives();

        double[] errtmp = new double[nc];
        for (int r = 0; r < ne; ++r) {
            errtmp[r] = -m_e[r];
        }

        if (m_curlambda != 0) {
            double sg = Math.sqrt(m_curlambda);
            for (int c = 0; c < n; ++c) {
                m_dfn.set(ne + c, c, sg);
            }
        }

        double sum = 0;
        m_dmax = 0;
        DataBlock atry = m_a.deepClone();
        try {
            Householder qr = new Householder(true);
            qr.decompose(m_dfn);
            double[] da = qr.solve(errtmp);
            for (int i = 0; i < n; ++i) {
                atry.set(i, atry.get(i) + da[i]);
                sum += (m_beta[i] + m_curlambda * da[i]) * da[i];
                double dmax = Math.abs(da[i]) / (1 + Math.abs(m_a.get(i)));
                if (dmax > m_dmax) {
                    m_dmax = dmax;
                }
            }
        } catch (MatrixException e) {
            return false;
        }

        // Did the trial succeed?
        sum /= 2;

        ParamValidation val = m_fn.getDomain().validate(atry);
        if (val == ParamValidation.Invalid) {
            throw new FunctionException(FunctionException.BOUND_ERR);
        }
        ISsqFunctionInstance ftry = m_fn.ssqEvaluate(atry);
        if (ftry != null) {
            m_obj1 = ftry.getSsqE();
            m_e = ftry.getE();
        } else {
            m_obj1 = Double.MAX_VALUE;
        }

        // simulate tramo errors
        if (!nextIteration()) {
            if (m_niter <= m_maxiter) {
                m_ftry = ftry;
                m_a = atry;
            }
            return false;
        }

        double s = (m_obj0 - m_obj1) / sum;
        if (s > 0) {
            // Success, accept the new solution.
            m_ftry = ftry;
            m_a = atry;
        }
        if (s < 0.25) {
            if (m_curlambda == 0) {
                m_curlambda = m_lambda;
            } else {
                m_curlambda *= m_istep;
            }
        }
        if (s > 0.75) {
            m_curlambda *= m_dstep;
        }

        return s > 0;

    }

    private void calcDerivatives() {
        int ne = m_e.length, n = m_beta.length;
        int nc = ne + n;
        m_dfn = new Matrix(nc, n);
        double[] dfn = m_dfn.internalStorage();
        for (int i = 0; i < n; ++i) {
            DataBlock cp = new DataBlock(m_ftry.getParameters());
            double dx = m_fn.getDomain().epsilon(cp, i);
            cp.set(i, cp.get(i) + dx);
            ISsqFunctionInstance ftmp = m_fn.ssqEvaluate(cp);

            // dEdX(i) = (ftmp.E-m_e)/dx;
            // grad(i) = m_e*dEdX
            double[] e = ftmp.getE();
            double grad = 0;
            for (int j = 0; j < ne; ++j) {
                e[j] = (e[j] - m_e[j]) / dx;
                grad += m_e[j] * e[j];
            }
            System.arraycopy(e, 0, dfn, nc * i, ne);

            // compute grad
            m_beta[i] = -grad * 2;
        }
    }
    private int m_niter = 0, m_maxiter = 50, m_ndata;
    private double[] m_beta;
    private double m_lambda = 0.001, m_istep = 4, m_dstep = 0.5,
            m_criterion = 1e-4;
    private double m_curlambda = 0, m_dmax;
    private double m_obj0 = 0, m_obj1 = 0;
    private double[] m_e;
    private Matrix m_dfn;
    private Matrix m_alpha, m_var;
    private ISsqFunction m_fn;
    private ISsqFunctionInstance m_ftry;
    private DataBlock m_a;
}

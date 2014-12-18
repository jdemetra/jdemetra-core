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

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class QRMarquardt implements ISsqFunctionMinimizer {

    private int m_niter, m_maxiter = 100;
    private boolean m_checkdecrease = false;
    private boolean m_strict = false;
    private double[] m_beta;
    private double m_lambda = 0.001, m_istep = 4, m_dstep = 0.5,
            m_criterion = 1e-4;
    private double m_curlambda, m_dmax;
    private double m_obj0, m_obj1;
    private double[] m_e;
    private Matrix m_dfn;
    private Matrix m_alpha;
    private ISsqFunction m_fn;
    private ISsqFunctionInstance m_ftry;
    private DataBlock m_a;
    private double m_tol;

    /**
     *
     */
    public QRMarquardt() {
    }

    /**
     *
     * @param qrm
     */
    public QRMarquardt(QRMarquardt qrm) {
        m_criterion = qrm.m_criterion;
        m_dstep = qrm.m_dstep;
        m_istep = qrm.m_istep;
        m_lambda = qrm.m_lambda;
        m_maxiter = qrm.m_maxiter;
        m_tol = qrm.m_tol;
    }

    /**
     *
     */
    protected void calcCurvature() {
        if (m_alpha != null) {
            return;
        }
        int ne = m_e.length, n = m_beta.length;
        m_alpha = new Matrix(n, n);
        SubMatrix dfn = m_dfn.subMatrix(n, ne, 0, n);
        for (int c = 0; c < n; ++c) {
            for (int r = 0; r <= c; ++r) {
                m_alpha.set(c, r, dfn.column(c).dot(dfn.column(r)));
            }
        }
        SymmetricMatrix.fromLower(m_alpha);
        m_alpha.mul(2);

    }

    /**
     *
     */
    protected void clear() {
        m_ftry = null;
        m_fn = null;
        m_e = null;
        m_alpha = null;
        m_a = null;
        m_beta = null;
    }

    /**
     *
     */
    protected void endIteration() {
    }

    /**
     *
     * @return
     */
    @Override
    public ISsqFunctionMinimizer exemplar() {
        QRMarquardt qrm = new QRMarquardt();
        qrm.m_criterion = m_criterion;
        qrm.m_dstep = m_dstep;
        qrm.m_istep = m_istep;
        qrm.m_lambda = m_lambda;
        qrm.m_maxiter = m_maxiter;
        qrm.m_tol = m_tol;
        qrm.m_checkdecrease = m_checkdecrease;
        qrm.m_strict = m_strict;
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

    /**
     *
     * @return
     */
    public double getCurrentLambda() {
        return m_curlambda;
    }

    @Override
    public Matrix getCurvature() {
        calcCurvature();
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

    public boolean isCheckingDecrease() {
        return m_checkdecrease;
    }

    public void setCheckDecrease(boolean decrease) {
        m_checkdecrease = decrease;
    }

    public boolean isStrict() {
        return m_strict;
    }

    public void setStrict(boolean value) {
        m_strict = value;
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
    public ISsqFunctionInstance getResult() {
        return m_ftry;
    }

    @Override
    public double getObjective() {
        return m_ftry == null ? Double.NaN : m_ftry.getSsqE();
    }

    /**
     *
     * @param start
     */
    protected void initialize(ISsqFunctionInstance start) {
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
        m_obj0 = m_ftry.getSsqE();
    }

    /**
     *
     * @param bnewval
     * @return
     */
    protected boolean iterate(boolean bnewval) {

        ++m_niter;
        int ne = m_e.length, n = m_beta.length;
        int nc = ne + n;

        if (bnewval) {
            // to optimize
            try {
                ISsqFunctionDerivatives der = m_fn.getDerivatives(m_ftry);
                double[] grad = der.getGradient();
                m_dfn = new Matrix(nc, n);
                DataBlockIterator dfn = m_dfn.subMatrix(0, ne, 0, n).columns();
                DataBlock cdfn = dfn.getData();
                do {
                    int i = dfn.getPosition();
                    double[] de = der.dEdX(i);

                    cdfn.copy(new DataBlock(de));
                    m_beta[i] = -grad[i];

                } while (dfn.next());
            } catch (RuntimeException err) {
            }
        }

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
                atry.add(i, da[i]);
                sum += (.5 * m_beta[i] + m_curlambda * da[i]) * da[i];
                double dmax = Math.abs(da[i]) / (1 + Math.abs(m_a.get(i)));
                if (dmax > m_dmax) {
                    m_dmax = dmax;
                }
            }
        } catch (MatrixException ex) {
            return false;
        }

        // Did the trial succeed?
        if (sum < 0) {
            return false;
        }

        ParamValidation val = m_fn.getDomain().validate(atry);
        if (val == ParamValidation.Invalid) {
            throw new FunctionException(FunctionException.BOUND_ERR);
        } /*
         * else if (val == ParamValidation.Changed) { if (m_curlambda == 0)
         * m_curlambda = m_lambda; else m_curlambda *= m_istep; return false; }
         */ else {
            ISsqFunctionInstance ftry = m_fn.ssqEvaluate(atry);
            if (ftry != null) {
                m_obj1 = ftry.getSsqE();
            } else {
                m_obj1 = Double.MAX_VALUE;
            }

            double s = (m_obj0 - m_obj1) / sum;
            if (s > 0) {
                m_ftry = ftry;
                // Success, accept the new solution.
                m_a = atry;
                m_e = ftry.getE();
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

    /**
     *
     * @param fn
     * @param start
     * @return
     */
    @Override
    public boolean minimize(ISsqFunction fn, ISsqFunctionInstance start) {
        clear();
        m_fn = fn;
        initialize(start);
        if (m_a.getLength() == 0) {
            return true;
        }
        boolean bnewval = true;

        do {
            bnewval = iterate(bnewval);
        } while (nextIteration());
        endIteration();
        return m_niter < m_maxiter;
    }

    /**
     *
     * @return
     */
    protected boolean nextIteration() {
        if (m_niter >= m_maxiter) {
            return false;
        }
        if (m_checkdecrease && m_obj1 > m_obj0) {
            return true;
        }
        double dobj = Math.abs(m_obj0 - m_obj1) / (1 + Math.abs(m_obj0));
        if (m_obj1 < m_obj0) {
            m_obj0 = m_obj1;
        }
        if (m_niter >= m_maxiter) {
            return false;
        }
        if (m_strict) {
            return dobj > m_criterion || m_dmax > Math.sqrt(m_criterion);
        } else {
            return dobj > m_criterion && m_dmax > Math.sqrt(m_criterion);
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void setConvergenceCriterion(double value) {
        m_criterion = value;
    }

    /**
     *
     * @param value
     */
    public void setDecreaseStep(double value) {
        m_dstep = value;
    }

    /**
     *
     * @param value
     */
    public void setIncreaseStep(double value) {
        m_istep = value;
    }

    /**
     *
     * @param value
     */
    public void setInitialLambda(double value) {
        m_lambda = value;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setMaxIter(int value) {
        m_maxiter = value;
    }
}

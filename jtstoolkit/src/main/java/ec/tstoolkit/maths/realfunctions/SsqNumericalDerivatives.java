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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqNumericalDerivatives implements ISsqFunctionDerivatives {

    private static final int nThreads = Runtime.getRuntime().availableProcessors();

    private double[] m_epsp;
    private double[][] m_ep;
    private double[] m_epsm;
    private double[][] m_em;
    private double[][] m_de;
    private double[] m_grad;
    private Matrix m_h;
    private final ISsqFunction m_fn;
    private IReadDataBlock m_pt;
    private double[] m_ecur;
    private final boolean m_sym, m_mt;
    private static int g_nsteps = 2;

    /**
     *
     * @param fn
     * @param point
     */
    public SsqNumericalDerivatives(ISsqFunction fn, ISsqFunctionInstance point) {
        this(fn, point, false, false);
    }

    public SsqNumericalDerivatives(ISsqFunction fn, ISsqFunctionInstance point, boolean sym, boolean mt) {
        m_sym = sym;
        m_mt = mt;
        m_fn = fn;
        m_ecur = point.getE();
        m_pt = point.getParameters();
    }

    /**
     *
     * @param fn
     * @param point
     * @param sym
     */
    public SsqNumericalDerivatives(ISsqFunction fn, ISsqFunctionInstance point,
            boolean sym) {
        this(fn, point, sym, false);
    }

    private void calcgrad() {
        int n = m_pt.getLength();
        m_grad = new double[n];
        m_epsp = new double[n];
        m_ep = new double[n][];
        if (m_sym) {
            m_epsm = new double[n];
            m_em = new double[n][];
        }
        m_de = new double[n][];
        if (!m_mt || n < 2) {
            for (int i = 0; i < n; ++i) {
                m_epsp[i] = m_fn.getDomain().epsilon(m_pt, i);
                checkepsilon(i);
                if (m_sym) {
                    checkmepsilon(i);
                }
                m_ep[i] = err(i, m_epsp[i]);
                if (m_sym) {
                    m_em[i] = err(i, m_epsm[i]);
                }
            }
        } else {
            for (int i = 0; i < n; ++i) {
                m_epsp[i] = m_fn.getDomain().epsilon(m_pt, i);
                checkepsilon(i);
                if (m_sym) {
                    checkmepsilon(i);
                }
            }
            List<Callable<Void>> tasks = createTasks(n, m_sym);
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
            try {
                executorService.invokeAll(tasks);
                executorService.shutdown();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        for (int i = 0; i < n; ++i) {
            double[] ep = m_ep[i];
            double gr = 0;
            double[] de = new double[m_ecur.length];
            for (int j = 0; j < m_ecur.length; ++j) {
                if (m_sym) {
                    double[] em = m_em[i];
                    de[j] = (ep[j] - em[j]) / (m_epsp[i] - m_epsm[i]);
                } else {
                    de[j] = (ep[j] - m_ecur[j]) / m_epsp[i];
                }
                gr += m_ecur[j] * de[j];
            }
            m_grad[i] = 2 * gr;
            m_de[i] = de;
        }
    }

    private void calch() {
        if (m_grad == null) {
            calcgrad();
        }
        int n = m_grad.length;
        m_h = new Matrix(n, n);
        // compute first the diagonal
        for (int i = 0; i < n; ++i) {
            double h = 0;
            double[] de = m_de[i];
            for (int k = 0; k < de.length; ++k) {
                h += de[k] * de[k];
            }
            m_h.set(i, i, 2 * h);
        }
        // other elements
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                double h = 0;
                // double[] ei = m_ep[i];
                // double[] ej = m_ep[j];
                double[] dei = m_de[i];
                double[] dej = m_de[j];
                for (int k = 0; k < dei.length; ++k) {
                    h += dei[k] * dej[k];
                }
                m_h.set(i, j, 2 * h);
            }
        }
        SymmetricMatrix.fromLower(m_h);
    }

    private void checkepsilon(int i) {
        double eps = m_epsp[i];
        if (eps == 0) {
            return;
        }
        DataBlock pcur=new DataBlock(m_pt);
        double pi = pcur.get(i);
        pcur.add(i, eps);
        if (m_fn.getDomain().checkBoundaries(pcur)) {
            return;
        }
        int k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= g_nsteps && !m_fn.getDomain().checkBoundaries(pcur));
        if (k <= g_nsteps) {
            m_epsp[i] = eps;
            return;
        }
        eps = -m_epsp[i];
        pcur.set(i, pi + eps);
        if (m_fn.getDomain().checkBoundaries(pcur)) {
            m_epsp[i] = eps;
            return;
        }
        k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= g_nsteps && !m_fn.getDomain().checkBoundaries(pcur));
        if (k <= g_nsteps) {
            m_epsp[i] = eps;
            return;
        }
        m_epsp[i] = 0;
    }

    private void checkmepsilon(int i) {
        double eps = -m_epsp[i];
        DataBlock pcur=new DataBlock(m_pt);
        double pi = pcur.get(i);
        pcur.set(i, pi + eps);
        if (m_fn.getDomain().checkBoundaries(pcur)) {
            m_epsm[i] = eps;
        }
    }

    /**
     * Computes d e(t,p)/dp(i)
     *
     * @param idx
     * @return
     */
    @Override
    public double[] dEdX(int idx) {
        if (m_de == null) {
            calcgrad();
        }
        return m_de[idx];
    }

    private double[] err(int i, double dx) {
        try {
            DataBlock pcur=new DataBlock(m_pt);
            pcur.add(i, dx);
            ISsqFunctionInstance fn = m_fn.ssqEvaluate(pcur);
            return fn.getE();
        } catch (Exception err) {
            return m_ecur;
        }
    }

    /**
     * Gets the gradient of the function. = sum(e(t,p) * d e(t,p)/dp(i))
     *
     * @return
     */
    @Override
    public double[] getGradient() {
        if (m_grad == null) {
            calcgrad();
        }
        return m_grad;
    }

    /**
     *
     * @param m
     * @return
     */
    @Override
    public void getJacobian(SubMatrix m) {
        if (m_de == null) {
            calcgrad();
        }
        for (int i = 0; i < m_de.length; ++i) {
            m.column(i).copyFrom(m_de[i], 0);
        }
    }

    /**
     * Gets the hessian of the function. h(i,j) is approximated by
     * sum(de(t,p)/dp(i)*de(t,p)/dp(j)) We consider that d^2e/dp(i)dp(j) is
     * negligible, which is not necessary true.
     *
     * @return
     */
    @Override
    public Matrix getHessian() {
        if (m_h == null) {
            calch();
        }
        return m_h;
    }

    private List<Callable<Void>> createTasks(int n, boolean sym) {
        List<Callable<Void>> result = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            result.add(new Err(m_ep, i, m_epsp[i]));
        }
        if (sym) {
            for (int i = 0; i < n; ++i) {
                result.add(new Err(m_em, i, m_epsm[i]));
            }
        }
        return result;
    }

    private class Err implements Callable<Void> {

        double[][] rslt;
        int pos;
        double eps;

        private Err(double[][] rslt, int pos, double eps) {
            this.rslt = rslt;
            this.pos = pos;
            this.eps = eps;

        }

        @Override
        public Void call() throws Exception {
            try {
                DataBlock cur = new DataBlock(m_pt);
                cur.add(pos, eps);
                ISsqFunctionInstance fn = m_fn.ssqEvaluate(cur);
                rslt[pos] = fn.getE();
            } catch (Exception err) {
                rslt[pos] = m_ecur;
            }
            return null;
        }

    }

}

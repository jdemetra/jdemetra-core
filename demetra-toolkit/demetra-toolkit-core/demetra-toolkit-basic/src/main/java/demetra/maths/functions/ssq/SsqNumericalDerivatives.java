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
package demetra.maths.functions.ssq;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.functions.IFunction;
import demetra.maths.matrices.Matrix;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import demetra.data.Doubles;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqNumericalDerivatives implements ISsqFunctionDerivatives {
    
    private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
    
    private DoubleSeq[] m_ep, m_em, m_de;
    private double[] m_epsp;
    private double[] m_epsm;
    private double[] m_grad;
    private Matrix m_h;
    private final ISsqFunction fn;
    private DoubleSeq m_pt;
    private DoubleSeq m_ecur;
    private final boolean m_sym, m_mt;
    private static int g_nsteps = 2;

    /**
     *
     * @param point
     */
    public SsqNumericalDerivatives(ISsqFunctionPoint point) {
        this(point, false, false);
    }
    
    public SsqNumericalDerivatives(ISsqFunctionPoint point, boolean sym, boolean mt) {
        m_sym = sym;
        m_mt = mt;
        fn = point.getSsqFunction();
        m_ecur = point.getE();
        m_pt = point.getParameters();
    }

    /**
     *
     * @param point
     * @param sym
     */
    public SsqNumericalDerivatives(ISsqFunctionPoint point,
            boolean sym) {
        this(point, sym, false);
    }
    
    private void calcgrad() {
        int n = m_pt.length();
        m_grad = new double[n];
        m_epsp = new double[n];
        m_ep = new DoubleSeq[n];
        if (m_sym) {
            m_epsm = new double[n];
            m_em = new DoubleSeq[n];
        }
        m_de = new DoubleSeq[n];
        if (!m_mt || n < 2) {
            for (int i = 0; i < n; ++i) {
                m_epsp[i] = fn.getDomain().epsilon(m_pt, i);
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
                m_epsp[i] = fn.getDomain().epsilon(m_pt, i);
                checkepsilon(i);
                if (m_sym) {
                    checkmepsilon(i);
                }
            }
            List<Callable<Void>> tasks = createTasks(n, m_sym);
            ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
            try {
                executorService.invokeAll(tasks);
                executorService.shutdown();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        int ne = m_ecur.length();
        for (int i = 0; i < n; ++i) {
            DoubleSeq ep = m_ep[i];
            DoubleSeq em = m_ep[i];
            DataBlock de = DataBlock.make(ne);
            if (m_sym) {
                final double eps = m_epsp[i] - m_epsm[i];
                de.set(ep, em, (x, y) -> (x - y) / eps);
            } else {
                final double eps = m_epsp[i];
                de.set(ep, m_ecur, (x, y) -> (x - y) / eps);
            }
            m_grad[i] = 2 * Doubles.dot(m_ecur, de);
            m_de[i] = de;
        }
    }
    
    private void calch() {
        if (m_grad == null) {
            calcgrad();
        }
        int n = m_grad.length;
        m_h = Matrix.square(n);
        // compute first the diagonal
        for (int i = 0; i < n; ++i) {
            DoubleSeq de = m_de[i];
            m_h.set(i, i, 2 * Doubles.ssq(de));
        }
        // other elements
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                DoubleSeq dei = m_de[i];
                DoubleSeq dej = m_de[j];
                double z=2 * Doubles.dot(dei, dej);
                m_h.set(i, j, z);
                m_h.set(j, i, z);
            }
        }
    }
    
    private void checkepsilon(int i) {
        double eps = m_epsp[i];
        if (eps == 0) {
            return;
        }
        DataBlock pcur = DataBlock.of(m_pt);
        double pi = pcur.get(i);
        pcur.add(i, eps);
        if (fn.getDomain().checkBoundaries(pcur)) {
            return;
        }
        int k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= g_nsteps && !fn.getDomain().checkBoundaries(pcur));
        if (k <= g_nsteps) {
            m_epsp[i] = eps;
            return;
        }
        eps = -m_epsp[i];
        pcur.set(i, pi + eps);
        if (fn.getDomain().checkBoundaries(pcur)) {
            m_epsp[i] = eps;
            return;
        }
        k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= g_nsteps && !fn.getDomain().checkBoundaries(pcur));
        if (k <= g_nsteps) {
            m_epsp[i] = eps;
            return;
        }
        m_epsp[i] = 0;
    }
    
    private void checkmepsilon(int i) {
        double eps = -m_epsp[i];
        DataBlock pcur = DataBlock.of(m_pt);
        double pi = pcur.get(i);
        pcur.set(i, pi + eps);
        if (fn.getDomain().checkBoundaries(pcur)) {
            m_epsm[i] = eps;
        }// else m_epsm == 0 and the asymmetric num. derivative is computed
    }

    @Override
    public IFunction getFunction(){
        return fn.asFunction();
    }
    
    /**
     * Computes d e(t,p)/dp(i)
     *
     * @param idx
     * @return
     */
    @Override
    public DoubleSeq dEdX(int idx) {
        if (m_de == null) {
            calcgrad();
        }
        return m_de[idx];
    }
    
    private DoubleSeq err(int i, double dx) {
        try {
            if (dx == 0) {
                return m_ecur;
            }
            DataBlock pcur = DataBlock.of(m_pt);
            pcur.add(i, dx);
            ISsqFunctionPoint fn = this.fn.ssqEvaluate(pcur);
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
    public DoubleSeq gradient() {
        if (m_grad == null) {
            calcgrad();
        }
        return DataBlock.ofInternal(m_grad);
    }

    /**
     *
     * @param m
     * @return
     */
    @Override
    public void jacobian(Matrix m) {
        if (m_de == null) {
            calcgrad();
        }
        for (int i = 0; i < m_de.length; ++i) {
            m.column(i).copy(m_de[i]);
        }
    }

    /**
     * Gets the hessian of the function. h(i,j) is approximated by
     * sum(de(t,p)/dp(i)*de(t,p)/dp(j)) We consider that d^2e/dp(i)dp(j) is
     * negligible, which is not necessary true.
     *
     * @param h
     */
    @Override
    public void hessian(Matrix h) {
        if (m_h == null) {
            calch();
        }
        h.copy(m_h);
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
        
        DoubleSeq[] rslt;
        int pos;
        double eps;
        
        private Err(DoubleSeq[] rslt, int pos, double eps) {
            this.rslt = rslt;
            this.pos = pos;
            this.eps = eps;
            
        }
        
        @Override
        public Void call() throws Exception {
            try {
                DataBlock cur = DataBlock.of(m_pt);
                cur.add(pos, eps);
                ISsqFunctionPoint fn = SsqNumericalDerivatives.this.fn.ssqEvaluate(cur);
                rslt[pos] = fn.getE();
            } catch (Exception err) {
                rslt[pos] = m_ecur;
            }
            return null;
        }
        
    }
    
}

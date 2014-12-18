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

import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NumericalDerivatives implements IFunctionDerivatives {

    private static final int nThreads = Runtime.getRuntime().availableProcessors();

    private double[] m_eps, m_fp, m_fm, m_grad;

    private Matrix m_h;

    private IFunction m_fn;

    private IReadDataBlock m_pt;

    private double m_fcur;

    private static int g_nsteps = 2;

    /**
     *
     * @param fn
     * @param point
     * @param sym
     */
    public NumericalDerivatives(IFunction fn, IFunctionInstance point,
            boolean sym) {
        this(fn, point, sym, false);
    }

    public NumericalDerivatives(IFunction fn, IFunctionInstance point,
            boolean sym, boolean mt) {
        m_fn = fn;
        m_fcur = point.getValue();
        m_pt = point.getParameters();
        int n = m_pt.getLength();
        m_fp = new double[n];
        m_eps = new double[n];
        if (!mt || n < 2) {
            if (!sym) {
                for (int i = 0; i < n; ++i) {
                    m_eps[i] = m_fn.getDomain().epsilon(m_pt, i);
                    checkepsilon(i);
                    m_fp[i] = newval(i, m_eps[i]);
                }
            } else {
                m_fm = new double[n];
                for (int i = 0; i < n; ++i) {
                    m_eps[i] = m_fn.getDomain().epsilon(m_pt, i);
                    checkepsilon(i);
                    m_fp[i] = newval(i, m_eps[i]);
                    m_fm[i] = newval(i, -m_eps[i]);
                }
            }
        } else {
            if (!sym) {
                for (int i = 0; i < n; ++i) {
                    m_eps[i] = m_fn.getDomain().epsilon(m_pt, i);
                    checkepsilon(i);
                }
            } else {
                m_fm = new double[n];
                for (int i = 0; i < n; ++i) {
                    m_eps[i] = m_fn.getDomain().epsilon(m_pt, i);
                    checkepsilon(i);
                }
            }
            List<Callable<Void>> tasks = createTasks(n, sym);
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
            try {
                executorService.invokeAll(tasks);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            executorService.shutdown();
        }
    }

    private void calcgrad() {
        int n = m_fn.getDomain().getDim();
        m_grad = new double[n];
        for (int i = 0; i < n; ++i) {
            if (m_fp[i] == m_fcur) {
                m_grad[i] = 0;
            } else if (m_fm == null) {
                m_grad[i] = (m_fp[i] - m_fcur) / m_eps[i];
            } else {
                m_grad[i] = (m_fp[i] - m_fm[i]) / (2 * m_eps[i]);
            }
        }
    }

    private void calch() {
        int n = m_fn.getDomain().getDim();
        double[] e = new double[n];
        for (int i = 0; i < n; ++i) {
            e[i] = Math.sqrt(Math.abs(m_eps[i]));
         }
        m_h = new Matrix(n, n);
        // compute the diagonal
        // df/di(x) = (f(x+ei)-f(x))/ei
        // d2f/di2(x) = (df/di(x)-df/di(x-ei))/ei=(f(x+ei)-f(x)-f(x)+f(x-ei))/ei*ei
        DataBlock diag = m_h.diagonal();
        for (int i = 0; i < n; ++i) {
            double di = e[i];
            double num = newval(i, di) - 2 * m_fcur + newval(i, -di);
            if (num != 0 && m_eps[i] != 0) {
                diag.set(i, num / (di*di));
            }
        }
        // other elements. 
        // df/di(x) = (f(x+ei)-f(x-ei))/(2ei)
        // d2f/didj(x) = (f(x+ei+ej)-f(x+ei-ej)-f(x-ei+ej)+f(x-ei-ej))/(4*ei*ej)
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                double di = e[i], dj = e[j];
                double num = newval(i, j, di/2, dj/2) + newval(i, j, -di/2, -dj/2)
                        - newval(i, j, di/2, -dj/2) - newval(i, j, -di/2, dj/2);
                if (num != 0 && di != 0 && dj != 0) {
                    m_h.set(i, j, num / (di * dj));
                }
            }
        }
        SymmetricMatrix.fromLower(m_h);
    }

    private void checkepsilon(int i) {
        double eps = m_eps[i];
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
            m_eps[i] = eps;
            return;
        }
        eps = -m_eps[i];
        pcur.set(i, pi + eps);
        if (m_fn.getDomain().checkBoundaries(pcur)) {
            m_eps[i] = eps;
            return;
        }
        k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= g_nsteps && !m_fn.getDomain().checkBoundaries(pcur));
        if (k <= g_nsteps) {
            m_eps[i] = eps;
            return;
        }
        m_eps[i] = 0;
    }

//    /**
//     * 
//     * @param iparam
//     * @return
//     */
//    public double D1(int iparam) {
//	calcpm();
//	double num = m_fp[iparam] - m_fm[iparam];
//	if (num == 0) {
//	    return 0;
//	}
//
//	return (num) / (2 * m_eps[iparam]);
//    }
//
//    /**
//     * 
//     * @param iparam
//     * @return
//     */
//    public double D2(int iparam) {
//	calcpm();
//	double num = m_fp[iparam] - 2 * m_fcur + m_fm[iparam];
//	if (num == 0) {
//	    return 0;
//	} else {
//	    return num / (m_eps[iparam] * m_eps[iparam]);
//	}
//    }
//
//    /**
//     * 
//     * @param iparam
//     * @param jparam
//     * @return
//     */
//    public double D2(int iparam, int jparam) {
//	if (iparam == jparam) {
//	    return D2(jparam);
//	}
//
//	double di = m_eps[iparam], dj = m_eps[jparam];
//	double num = newval(iparam, jparam, di, dj)
//		+ newval(iparam, jparam, -di, -dj)
//		- newval(iparam, jparam, di, -dj)
//		- newval(iparam, jparam, -di, dj);
//	if (num == 0) {
//	    return 0;
//	} else {
//	    return num / (4 * di * dj);
//	}
//    }
    @Override
    public double[] getGradient() {
        if (m_grad == null) {
            calcgrad();
        }
        return m_grad;
    }

    /**
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

    private double newval(int i, double dx) {
        try {
            DataBlock cur = new DataBlock(m_pt);
            cur.add(i, dx);
            IFunctionInstance fn = m_fn.evaluate(cur);
            return fn.getValue();
        } catch (Exception err) {
            return m_fcur;
        }
    }

    private double newval(int i, int j, double dxi, double dxj) {
        try {
            DataBlock cur = new DataBlock(m_pt);
            cur.add(i, dxi);
            cur.add(j, dxj);
            IFunctionInstance fn = m_fn.evaluate(cur);
            return fn.getValue();
        } catch (Exception err) {
            return m_fcur;
        }
    }

    private List<Callable<Void>> createTasks(int n, boolean sym) {
        List<Callable<Void>> result = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            result.add(new NewVal(m_fp, i, m_eps[i]));
        }
        if (sym) {
            for (int i = 0; i < n; ++i) {
                result.add(new NewVal(m_fm, i, -m_eps[i]));
            }
        }
        return result;
    }

    private class NewVal implements Callable<Void> {

        double[] rslt;
        int pos;
        double eps;

        private NewVal(double[] rslt, int pos, double eps) {
            this.rslt = rslt;
            this.pos = pos;
            this.eps = eps;

        }

        @Override
        public Void call() throws Exception {
            try {
                DataBlock cur = new DataBlock(m_pt);
                cur.add(pos, eps);
                IFunctionInstance fn = m_fn.evaluate(cur);
                rslt[pos] = fn.getValue();
            } catch (Exception err) {
                rslt[pos] = m_fcur;
            }
            return null;
        }

    }
}

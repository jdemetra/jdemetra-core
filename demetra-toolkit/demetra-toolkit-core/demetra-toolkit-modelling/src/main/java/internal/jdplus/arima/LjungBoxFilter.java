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
package internal.jdplus.arima;

import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.LogSign;
import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Legacy;
import demetra.design.Development;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.RationalFunction;
import nbbrd.service.ServiceProvider;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.FastMatrix;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm = ArmaFilter.class, feature = Legacy)
@ServiceProvider(ArmaFilter.class)
public class LjungBoxFilter implements ArmaFilter {

    private int m_n, m_p, m_q;
    private Polynomial m_ar, m_ma;
    private double[] m_u;
    private Matrix m_G, m_X, m_V1, m_L;
    private double m_s, m_t;

    private void ar(double[] a) {
        // AR(w)
        if (m_p > 0) {
            for (int i = a.length - 1; i >= m_p; --i) {
                double s = 0;
                for (int j = 1; j <= m_p; ++j) {
                    s += m_ar.get(j) * a[i - j];
                }
                a[i] += s;
            }
            // last steps
            for (int i = m_p - 1; i > 0; --i) {
                double s = 0;
                for (int j = 1; j <= i; ++j) {
                    s += m_ar.get(j) * a[i - j];
                }
                a[i] += s;
            }

        }

    }

    // / <summary>
    // / MA(a0) = AR(w) or a0 = M w
    // / </summary>
    // / <param name="w"></param>
    // / <returns></returns>
    private double[] calca0(DoubleSeq w) {
        double[] a0 = new double[w.length()];
        w.copyTo(a0, 0);
        ar(a0);
        rma(a0);

        return a0;
    }

    // / <summary>
    // / MA'(g)= a0 or g = L2^-1 * a0
    // / </summary>
    // / <param name="a0"></param>
    // / <returns></returns>
    private double[] calcg(double[] a0) {
        double[] g = a0.clone();
        if (m_q > 0) {
            for (int i = m_n - 2; i >= 0; --i) {
                double s = 0;

                for (int j = 1, k = i + 1; j <= m_q && k < m_n; ++j, ++k) {
                    s += m_ma.get(j) * g[k];
                }
                g[i] -= s;
            }
        }
        return g;
    }

    // / <summary>
    // / G = PI' * PI
    // / </summary>
    // / <param name="m"></param>
    private void calcg(int m) {
        RationalFunction rf = RationalFunction.of(Polynomial.ONE, m_ma);
        double[] pi = rf.coefficients(m_n);
        Matrix gg = Matrix.square(m);

        // compute first column
        for (int i = 0; i < m; ++i) {
            double s = 0;
            for (int j = i; j < m_n; ++j) {
                s += pi[j] * pi[j - i];
            }
            gg.set(i, 0, s);
        }

        for (int c = 1; c < m; ++c) {
            DataBlock col = gg.column(c), prevcol = gg.column(c - 1);
            for (int r = c; r < m; ++r) {
                col.set(r, prevcol.get(r - 1) - pi[m_n - r] * pi[m_n - c]);
            }
        }
        SymmetricMatrix.fromLower(gg);
        m_G = gg;
    }

    // / <summary>
    // / V1' * g
    // / </summary>
    // / <param name="g"></param>
    // / <returns></returns>
    private double[] calch(double[] g) {
        double[] h = new double[m_p + m_q];
        for (int i = 0; i < m_p; ++i) {
            for (int j = 0; j <= i; ++j) {
                h[i] -= m_ar.get(m_p - i + j) * g[j];
            }
        }
        for (int i = 0; i < m_q; ++i) {
            for (int j = 0; j <= i; ++j) {
                h[i + m_p] += m_ma.get(m_q - i + j) * g[j];
            }
        }
        return h;
    }

    // / <summary>
    // / v = V1 * m_u
    // / </summary>
    // / <param name="v"></param>
    private void calcv(double[] v) {
        for (int i = 0; i < m_p; ++i) {
            for (int j = i; j < m_p; ++j) {
                v[i] -= m_ar.get(m_p + i - j) * m_u[j];
            }
        }
        for (int i = 0; i < m_q; ++i) {
            for (int j = i; j < m_q; ++j) {
                v[i] += m_ma.get(m_q + i - j) * m_u[m_p + j];
            }
        }
        rma(v);
    }

    @Override
    public void apply(DoubleSeq w, DataBlock wl) {
        if (m_G == null) {
            int n = wl.length();
            for (int i = 0; i < n; ++i) {
                wl.set(i, w.get(i));
            }
        } else {
            // compute a0=Mw
            double[] a0 = calca0(w);
            double[] g = calcg(a0);
            m_u = calch(g);
            DataBlock U = DataBlock.of(m_u);
            LowerTriangularMatrix.rsolve(m_X, U);
            LowerTriangularMatrix.lsolve(m_X, U);
            double[] v = new double[w.length()];
            calcv(v);
            for (int i = 0; i < a0.length; ++i) {
                a0[i] -= v[i];
            }
            wl.drop(m_u.length, 0).copyFrom(a0, 0);
            DataBlock wl0 = wl.range(0, m_u.length);
            wl0.copyFrom(m_u, 0);
            LowerTriangularMatrix.rsolve(m_L, wl0);
        }
    }

    @Override
    public double getLogDeterminant() {
        return m_s + m_t;
    }

    @Override
    public int prepare(IArimaModel arima, int n) {
        clear();
        m_ar = arima.getAr().asPolynomial();
        m_ma = arima.getMa().asPolynomial();
        m_n = n;
        m_p = m_ar.degree();
        m_q = m_ma.degree();

        int m = Math.max(m_p, m_q);
        if (m > 0) {
            // compute V1' * G * V1 = X' X and V (covar model)

            m_L = Matrix.square(m_p + m_q);
            m_L.diagonal().set(1);
            m_V1 = Matrix.make(m, m_p + m_q);
            if (m_p > 0) {
                double[] cov = arima.getAutoCovarianceFunction().values(m_p);
                FastMatrix W = m_L.extract(0, m_p, 0, m_p);
                W.diagonal().set(cov[0]);

                for (int i = 1; i < m_p; ++i) {
                    W.subDiagonal(i).set(cov[i]);
                }
                FastMatrix P = m_V1.extract(0, m_p, 0, m_p);
                P.diagonal().set(-m_ar.get(m_p));
                for (int i = 1; i < m_p; ++i) {
                    P.subDiagonal(i).set(-m_ar.get(m_p - i));
                }
            }

            if (m_q > 0) {
                FastMatrix Q = m_V1.extract(0, m_q, m_p, m_q);
                Q.diagonal().set(m_ma.get(m_q));

                for (int i = 1; i < m_q; ++i) {
                    Q.subDiagonal(i).set(m_ma.get(m_q - i));
                }
            }

            if (m_q > 0 && m_p > 0) {
                double[] psi = RationalFunction.of(m_ma, m_ar).coefficients(m_q);
                FastMatrix W = m_L.extract(0, m_p, m_p, m_q);
                int imin = m_q - m_p;
                for (int i = 0; i < m_q; ++i) {
                    W.subDiagonal(imin - i).set(psi[i]);
                }
            }

            SymmetricMatrix.fromUpper(m_L);

            // compute G
            calcg(m);
            m_X = SymmetricMatrix.XtSX(m_G, m_V1);

            // compute the inverse of the covariance matrix
            SymmetricMatrix.lcholesky(m_L);
            m_s = 2 * LogSign.of(m_L.diagonal()).getValue();
            Matrix I = Matrix.identity(m_p + m_q);
            LowerTriangularMatrix.rsolve(m_L, I);
            LowerTriangularMatrix.lsolve(m_L, I.transpose());
            m_X.add(I);
            SymmetricMatrix.lcholesky(m_X);
            m_t = 2 * LogSign.of(m_X.diagonal()).getValue();
        }

        return n + m_p + m_q;
    }

    void rma(double[] a) {
        // MA(a) by induction
        if (m_q > 0) {
            // first q steps
            for (int i = 1; i < m_q; ++i) {
                double s = 0;
                for (int j = 1; j <= i; ++j) {
                    s += m_ma.get(j) * a[i - j];
                }
                a[i] -= s;
            }
            // next steps
            for (int i = m_q; i < a.length; ++i) {
                double s = 0;
                for (int j = 1; j <= m_q; ++j) {
                    s += m_ma.get(j) * a[i - j];
                }
                a[i] -= s;
            }
        }
    }

    private void clear() {
        m_G = null;
        m_X = null;
        m_V1 = null;
        m_L = null;
        m_s = 0;
        m_t = 0;
    }
}

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
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.design.Development;
import demetra.maths.Complex;
import demetra.data.FFT;
import demetra.design.Immutable;
import demetra.utilities.Arrays2;

/**
 * A toeplitz matrix T is defined by T[i, j] = r[abs(i-j)]
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class ToeplitzMatrix {

    /**
     * Durbin algorithm to solve the Yule-Walker equations: <br>
     * Ty=-r, where T[i, j] = r[abs(i-j)](Toeplitz matrix). <br>
     * Reference G.H.Golub and C.F.Van Loan. Matrix Computations, third edition,
     * pages 194-195. ISBN 0-8018-5414-8.
     *
     * @param r
     * @return
     */
    public static double[] solveDurbinSystem(final double[] r) {
        int n = r.length - 1;
        double[] y = new double[n];
        double alpha = -r[1] / r[0], beta = r[0];
        y[0] = alpha;
        for (int k = 0; k < n - 1; ++k) {
            beta *= (1 - alpha * alpha);
            double tmp = 0;
            for (int i = k; i >= 0; --i) {
                tmp += r[k - i + 1] * y[i];
            }
            alpha = -(r[k + 2] + tmp) / beta;
            int k2 = (1 + k) / 2;
            for (int i = 0; i < k2; ++i) {
                double yi = y[i];
                y[i] += alpha * y[k - i];
                y[k - i] += alpha * yi;
            }
            if (k % 2 == 0) {
                y[k2] *= (1 + alpha);
            }
            y[k + 1] = alpha;
        }
        return y;
    }

    /**
     * Solve the system T x = b where T is the toeplitz matrix defined by T[i,
     * j] = r[abs(i-j)]
     *
     * @param r The coefficients of the toeplitz matix
     * @param b The array of the system
     * @return The solution of the system
     */
    public static double[] solveLevinsonSystem(final double[] r,
            final double[] b) {
        int n = r.length - 1;
        double[] y = new double[n - 1];
        double[] x = new double[n];
        double alpha = -r[1] / r[0], beta = r[0];
        y[0] = alpha;
        x[0] = b[0] / r[0];
        for (int k = 0; k < n - 1; ++k) {
            beta *= (1 - alpha * alpha);
            double tmp = 0;
            for (int i = k; i >= 0; --i) {
                tmp += r[k - i + 1] * x[i];
            }
            double mu = (b[k + 1] - tmp) / beta;
            for (int i = 0; i <= k; ++i) {
                x[i] += mu * y[k - i];
            }
            x[k + 1] = mu;
            
            if (k < n - 2) {
                tmp = 0;
                for (int i = k; i >= 0; --i) {
                    tmp += r[k - i + 1] * y[i];
                }
                alpha = -(r[k + 2] + tmp) / beta;
                int k2 = (1 + k) / 2;
                for (int i = 0; i < k2; ++i) {
                    double yi = y[i];
                    y[i] += alpha * y[k - i];
                    y[k - i] += alpha * yi;
                }
                if (k % 2 == 0) {
                    y[k2] *= (1 + alpha);
                }
                y[k + 1] = alpha;
            }
        }
        return x;
    }
    
    private final double[] r;

    /**
     *
     * @param r The coefficients of the toeplitz matrix
     */
    public ToeplitzMatrix(final double[] r) {
        this.r = r.clone();
    }
    
    public Matrix asMatrix() {
        Matrix T = Matrix.square(r.length);
        DataBlock diag = T.diagonal();
        diag.set(r[0]);
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < r.length; ++i) {
            ldiag.slideAndShrink(T.rowInc).set(r[i]);
            udiag.slideAndShrink(T.colInc).set(r[i]);
        }
        return T;
    }

    /**
     * Trench algorithm for the inversion of a symmetric Toeplitz matrix.
     *
     * @return
     */
    public Matrix inverse() {
        int n = r.length, nc = n - 1;
        Matrix m = Matrix.square(n);
        // double[] rc = new double[nc];
        // Array.Copy(m_r, rc, nc);
        double[] y = solveDurbinSystem(r);
        double tmp = r[0];
        for (int i = 0; i < nc; ++i) {
            tmp += r[i + 1] * y[i];
        }
        double gamma = 1 / tmp;
        m.set(0, 0, gamma);
        m.set(nc, nc, gamma);
        for (int i = 0; i < nc; ++i) {
            y[i] *= gamma;
            m.set(0, i + 1, y[i]);
            m.set(nc - i - 1, nc, y[i]);
        }
        Arrays2.reverse(y);
        int imax = (n + 1) / 2;
        for (int i = 1; i < imax; ++i) {
            for (int j = i; j < n - i; ++j) {
                double x = m.get(i - 1, j - 1)
                        + (y[nc - i] * y[nc - j] - y[i - 1] * y[j - 1]) / gamma;
                m.set(i, j, x);
                m.set(nc - j, nc - i, x);
            }
        }
        SymmetricMatrix.fromUpper(m);
        return m;
    }

    /**
     *
     * @param x
     */
    public void mul(final DataBlock x) {
        int n = r.length;
        if (n == 1) {
            x.mul(0, r[0]);
        } else {
            // By FFT.
            // circulant matrix
            int nc = 2 * n - 2;
            // DFT length:
            int q = 2;
            while (q < nc) {
                q <<= 1;
            }
            Complex[] fx = new Complex[q];
            Complex[] fr = new Complex[q];
            for (int i = 0; i < n; ++i) {
                fx[i] = Complex.cart(x.get(i));
                fr[i] = Complex.cart(r[i]);
            }
            for (int i = n - 2, j = n; i > 0; --i, ++j) {
                fr[j] = Complex.cart(r[i]);
            }
            FFT fft = new FFT();
            fft.transform(fx);
            fft.transform(fr);
            for (int i = 0; i < q; ++i) {
                fx[i] = fx[i].times(fr[i]);
            }
            fft.backTransform(fx);
            for (int i = 0; i < n; ++i) {
                x.set(i, fx[i].getRe());
            }
        }
        
    }
}

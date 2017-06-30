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
package demetra.arima;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import java.lang.reflect.Array;


/**
 * The auto-covariance function provides the auto-covariance of any stationary
 * Linear model for any lags. The auto-covariances are computed recursively. The
 * object stores previously computed values for faster processing. Example: in
 * the case of the Arima model (1-.5B)y=(1+.8B)e, var(e)=4, The auto-covariance
 * function is created as follows: AutoCovarianceFunction acf=new
 * AutoCovarianceFunction( new double[]{1, -.5}, new double[]{1, +.8}, 4); the
 * variance is retrieved by acf.get(0); the auto-covariance of rank 7 is
 * retrieved by acf.get(7);
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Alpha)
public class AutoCovarianceFunction_1 {
    
//    public static interface Computer{
//        double[] ac(IArimaModel arima, int rank);        
//    }
//
//    /**
//     * Method used for estimating the auto-covariance function
//     */
//    public static enum Method {
//
//        /**
//         * Default method. See for instance Brockwell and Davis, 3.3, method 1.
//         * The underlying linear system is solved by means of a QR decomposition
//         * with pivoting
//         */
//        Default,
//        /**
//         * Default method. See for instance Brockwell and Davis, 3.3, method 1.
//         * The underlying linear system is solved by means of a LU decomposition
//         * with pivoting
//         */
//        Default2,
//        /**
//         * Use the decomposition [Q(B)Q(F)]/[P(B)P(F)] = N(B)/D(B) + N(F)/D(F).
//         * The underlying linear system is solved by means of a QR decomposition
//         * with pivoting. This method is the default one.
//         */
//        SymmetricFilterDecomposition,
//        /**
//         * Use the decomposition [Q(B)Q(F)]/[P(B)P(F)] = N(B)/D(B) + N(F)/D(F).
//         * The underlying linear system is solved by means of a LU decomposition
//         * with pivoting
//         */
//        SymmetricFilterDecomposition2
//    }
//    private static final int BLOCK = 36;
//    private Polynomial ar, ma;
//    private SymmetricFilter sma_;
//    private double[] c_;
//    private double var_;
//    private Method method_ = Method.Default2;
//
//    /**
//     * Creates the auto-covariance function for a model identified by its moving
//     * average polynomial, its auto-regressive polynomial and its innovation
//     * variance.
//     *
//     * @param ma The values of the moving average polynomial
//     * @param ar The values of the auto-regressive polynomial
//     * @param var The innovation variance.
//     */
//    public AutoCovarianceFunction(final Polynomial ma, final Polynomial ar, final double var) {
//        this.ma = ma;
//        this.ar = ar;
//        var_ = var;
//    }
//
//    /**
//     * Creates the auto-covariance function for a model identified by its moving
//     * average symmetric filter (= var*Q(B)*Q(F) = auto-covariance filter of the
//     * ma part of the model) and its auto-regressive polynomial.
//     *
//     * @param sma The symmetric moving average filter
//     * @param ar The stationary auto-regressive polynomial
//     */
//    public AutoCovarianceFunction(final SymmetricFilter sma, final BackFilter ar) {
//        sma_ = sma;
//        this.ar = ar.getPolynomial();
//        var_ = 1;
//        method_ = Method.SymmetricFilterDecomposition;
//    }
//
//    /**
//     * Gets the method in use.
//     *
//     * @return The current method (Default2 is the default).
//     */
//    public Method getMethod() {
//        return method_;
//    }
//
//    /**
//     * Sets the computation method
//     *
//     * @param method The new computation method.
//     */
//    public void setMethod(Method method) {
//        if (ma == null && (method == Method.Default || method == Method.Default2)) {
//            throw new ArimaException("Invalid acf method");
//        }
//        method_ = method;
//        c_ = null;
//    }
//
//    /**
//     * Gets all the auto-covariances up to a given rank.
//     *
//     * @param n The number of requested auto-covariances
//     * @return An array of n values, going from the variance up to the
//     * auto-covariance of rank(n-1).
//     */
//    public double[] values(final int n) {
//        prepare(n);
//        double[] a = new double[n];
//        int nmax = Math.min(n, c_.length);
//        System.arraycopy(c_, 0, a, 0, nmax);
//        return a;
//    }
//
//    /**
//     * Gets a specific auto-covariance.
//     *
//     * @param k The rank of the auto-covariance (0 for variance).
//     * @return The auto-covariance of rank k.
//     */
//    public double get(final int k) {
//        prepare(k + 1);
//        if (k >= c_.length) {
//            return 0;
//        } else {
//            return c_[k];
//        }
//    }
//
//    /**
//     * Gets the last rank with an auto-covariance different from 0
//     *
//     * @return The rank of the last non-null auto-covariance. -1 if the
//     * auto-covariance function is unbounded.
//     */
//    public int getBound() {
//        if (!hasBound()) {
//            return -1;
//        }
//        return ma.getDegree() + 1;
//    }
//
//    /**
//     * Checks that the auto-covariance is bounded. 
//     * @return True if the auto-covariance function is bounded, which means that
//     * the auto-regressive polynomial is 1; false otherwise.
//     */
//    public boolean hasBound() {
//        return ar.getDegree() + 1 == 1;
//    }
//
//    /**
//     * Computes the auto-covariances up to the given rank (included).
//     * @param rank The rank to be computed.
//     */
//    public void prepare(int rank) {
//        if (rank == 0) {
//            rank = BLOCK;
//        } else {
//            int r = rank % BLOCK;
//            if (r != 0) {
//                rank += BLOCK - r;
//            }
//        }
//        if (c_ != null && c_.length > rank) {
//            return;
//        }
//
//        switch (method_) {
//            case Default:
//                computeDefault(rank);
//                break;
//            case Default2:
//                computeDefault2(rank);
//                break;
//            case SymmetricFilterDecomposition:
//                computeSymmetric(rank, true);
//                break;
//            case SymmetricFilterDecomposition2:
//                computeSymmetric(rank, false);
//                break;
//        }
//    }
//
//    private void computeDefault(int rank) {
//        int p = ar.getDegree() + 1;
//        int q = ma.getDegree() + 1;
//        int r0 = Math.max(p, q);
//        if (rank < r0) {
//            rank = r0;
//        }
//        int k0 = r0;
//        if (c_ == null) {
//            // initialization process
//            c_ = new double[rank + 1];
//            RationalFunction rfe = RationalFunction.of(ma, ar);
//            double[] cr = rfe.coefficients(q);
//
//            Matrix c = Matrix.make(r0, r0 + 1);
//            for (int i = 0; i < q; ++i) {
//                double s = 0;
//                for (int j = i; j < q; ++j) {
//                    s += ma.get(j) * cr[j - i];
//                }
//                c.set(i, r0, s);
//            }
//
//            for (int i = 0; i < r0; ++i) {
//                for (int j = 0; j < p; ++j) {
//                    double w = ar.get(j);
//                    if (w != 0) {
//                        c.add(i, i < j ? j - i : i - j, w);
//                    }
//                }
//            }
//
//            if (!SparseSystemSolver.solve(c)) {
//                throw new ArimaException(ArimaException.NonStationary);
//            }
//
//            for (int i = 0; i < r0; ++i) {
//                c_[i] = c.get(i, r0) * var_;
//            }
//        } else {
//            double[] tmp = new double[rank + 1];
//            k0 = c_.length;
//            for (int u = 0; u < k0; ++u) {
//                tmp[u] = c_[u];
//            }
//            c_ = tmp;
//        }
//        // after the initialization process
//        for (int r = k0; r <= rank; ++r) {
//            double s = 0;
//            for (int x = 1; x < p; ++x) {
//                s += ar.get(x) * c_[r - x];
//            }
//            c_[r] = -s;
//        }
//    }
//
//    private void computeDefault2(int rank) {
//        int p = ar.getDegree() + 1;
//        int q = ma.getDegree() + 1;
//        int r0 = Math.max(p, q);
//        if (rank < r0) {
//            rank = r0;
//        }
//        int k0 = r0;
//        if (c_ == null) {
//            try {
//                // initialization process
//                c_ = new double[rank + 1];
//                RationalFunction rfe = RationalFunction.of(ma, ar);
//                double[] cr = rfe.coefficients(q);
//                double[] m = new double[r0];
//                for (int i = 0; i < q; ++i) {
//                    double s = 0;
//                    for (int j = i; j < q; ++j) {
//                        s += ma.get(j) * cr[j - i];
//                    }
//                    m[i] = s;//*m_var;
//                }
//
//                Matrix c = Matrix.make(r0);
//                for (int i = 0; i < r0; ++i) {
//                    for (int j = 0; j < p; ++j) {
//                        double w = ar.get(j);
//                        if (w != 0) {
//                            c.add(i, i < j ? j - i : i - j, w);
//                        }
//                    }
//                }
//
//                Householder qr = new Householder(false);
//                qr.decompose(c);
//                double[] tmp = qr.solve(m);
//
//                for (int i = 0; i < r0; ++i) {
//                    c_[i] = tmp[i] * var_;
//                }
//            } catch (MatrixException err) {
//                throw new ArimaException(ArimaException.NonStationary);
//            }
//        } else {
//            double[] tmp = new double[rank + 1];
//            k0 = c_.length;
//            for (int u = 0; u < k0; ++u) {
//                tmp[u] = c_[u];
//            }
//            c_ = tmp;
//        }
//        // after the initialization process
//        for (int r = k0; r <= rank; ++r) {
//            double s = 0;
//            for (int x = 1; x < p; ++x) {
//                s += ar.get(x) * c_[r - x];
//            }
//            c_[r] = -s;
//        }
//    }
//
//    private void computeSymmetric(int rank, boolean dsym) {
//        int p = ar.getDegree() + 1;
//        int q = sma_ != null ? sma_.getDegree() + 1 : ma.getDegree() + 1;
//        int r0 = Math.max(p, q);
//        if (rank < r0) {
//            rank = r0;
//        }
//        if (p == 1) {
//            // pure moving average...
//            if (sma_ == null) {
//                sma_ = SymmetricFilter.convolution(new BackFilter(ma));
//            }
//            c_ = sma_.getCoefficients();
//            new DataBlock(c_).mul(var_);
//        } else {
//            if (c_ == null) {
//                c_ = new double[rank + 1];
//                if (sma_ == null) {
//                    sma_ = SymmetricFilter.createFromFilter(new BackFilter(ma));
//                }
//                BackFilter g = dsym ? sma_.decompose(new BackFilter(ar)) : sma_.decompose2(new BackFilter(ar));
//                double[] tmp = new RationalFunction(g.getPolynomial(), ar).coefficients(rank + 1);
//
//                if (var_ != 1) {
//                    c_[0] = 2 * tmp[0] * var_;
//                    for (int i = 1; i < tmp.length; ++i) {
//                        c_[i] = tmp[i] * var_;
//                    }
//                } else {
//                    System.arraycopy(tmp, 0, c_, 0, tmp.length);
//                    c_[0] *= 2;
//                }
//
//            }
//            if (rank < c_.length) {
//                return;
//            }
//
//            int k0 = c_.length;
//            double[] tmp = new double[rank];
//            for (int u = 0; u < k0; ++u) {
//                tmp[u] = c_[u];
//            }
//            c_ = tmp;
//
//            // after the initialization process
//            for (int r = k0; r < rank; ++r) {
//                double s = 0;
//                for (int x = 1; x < p; ++x) {
//                    s += ar.get(x) * c_[r - x];
//                }
//                c_[r] = -s;
//            }
//        }
//    }
}

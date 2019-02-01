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
package demetra.arima.internal;

import demetra.arima.ArimaException;
import demetra.arima.IArimaModel;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.DeterminantalTerm;
import demetra.likelihood.Likelihood;
import demetra.maths.matrices.Matrix;
import demetra.util.SubArrayOfInt;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.leastsquares.QRSolvers;
import demetra.leastsquares.QRSolver;

/**
 * The FastKalmanFilter class provides fast computation of Regression models
 * with stationary Arma noises, by means of a Kalman filter It is a simplified
 * implementation of the routine used in Tramo. It should be noted that other
 * implementations of the Kalman filter provide exactly the same results.
 * However, this one, which is intensively used in several high-level routines,
 * has been optimised as much as possible.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastKalmanFilter {

    /**
     * Contains the number of function calls to the main routine of the class,
     * i.e. "process".
     */
    private IArimaModel arma;
    private double[] phi;
    private int dim;
    private double h0;
    private double[] c0;
    private double eps = 1e-12;
    private boolean fast;

    /**
     * Creates a new Kalman filter for a given stationary Arima model
     *
     * @param arma The Arima model. Should be stationary.
     */
    public FastKalmanFilter(final IArimaModel arma) {
        initmodel(arma, 0);
    }

    /**
     * Fast processing. The exact filter is used on the Max(p, q) first data.
     * Fast iteration is used on the following data. Roughly speaking, the fast
     * processing is twice faster than the exact processing.
     *
     * @param y
     * @return
     */
    public DataBlock fastFilter(final DoubleSequence y) {
        double[] C = c0.clone();
        double h = h0;

        double var = arma.getInnovationVariance();
        if (var != 1) {
            h /= var;
            for (int i = 0; i < C.length; ++i) {
                C[i] /= var;
            }
        }

        double[] L = C.clone();

        double[] a = new double[dim];
        int n = y.length();
        double[] yl = new double[n];
        // iteration
        int ilast = dim - 1;

        double[] theta = arma.getMA().asPolynomial().toArray();
        int np = phi.length - 1, nq = theta.length - 1;
        int im = np > nq ? np : nq;

        boolean steady = false;

        for (int pos = 0; pos < im; ++pos) {
            // filter y
            double s = Math.sqrt(h);
            double e = (y.get(pos) - a[0]) / s;
            yl[pos] = e;
            double la = tlast(a);
            double v = e / s;
            for (int i = 0; i < ilast; ++i) {
                a[i] = a[i + 1] + C[i] * v;
            }
            a[ilast] = la + C[ilast] * v;
            // filter x if any

            double zl = L[0];
            double zlv = zl / h;
            h -= zl * zlv;
            if (h < 1) {
                h = 1;
            }
            if (Double.isNaN(h)) {
                throw new ArimaException();
            }
            if (!steady) {
                double llast = tlast(L), clast = C[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[i] * zlv;
                        C[i] -= zlv * li;
                    } else {
                        L[i] = li;
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[ilast] -= zlv * llast;

                int k = 0;
                for (; k < L.length; ++k) {
                    if (Math.abs(L[k]) > eps) {
                        break;
                    }
                }
                if (k == L.length) {
                    steady = true;
                }
            }
        }

        for (int i = im; i < n; ++i) {
            double x = y.get(i);
            for (int p = 1; p <= np; ++p) {
                x += y.get(i - p) * phi[p];
            }
            for (int q = 1; q <= nq; ++q) {
                x -= yl[i - q] * theta[q];
            }
            yl[i] = x;
        }

        return DataBlock.ofInternal(yl);
    }

    /**
     * Fast processing. The exact filter is used on the Max(p, q) first
     * observations. Fast iteration is used on the following observations
     *
     * @param y
     * @param nparams
     * @return BIC statistics
     */
    public double fastProcessing(final DoubleSequence y, int nparams) {
        DataBlock yl = fastFilter(y);
        int n = yl.length();
        double ssqerr = yl.ssq();
        // BIC from residuals ...
        return Math.log(ssqerr / n) + nparams * Math.log(n) / n;
    }

    private void initmodel(final IArimaModel arma, int statedim) {
        if (!arma.isStationary()) {
            throw new ArimaException(ArimaException.NONSTATIONARY);
        }
        this.arma = arma;
        phi = this.arma.getAR().asPolynomial().toArray();
        if (statedim == 0) {
            statedim = Math.max(arma.getAROrder(), arma.getMAOrder() + 1);
        }
        dim = statedim;
        c0 = this.arma.getAutoCovarianceFunction().values(dim);
        h0 = c0[0];
        tx(c0);

    }

    /**
     *
     * @param y
     * @param res
     * @param stde
     * @return
     */
    public boolean process(final DoubleSequence y, final DataBlock res,
            final DataBlock stde) {
        fast = false;
        DeterminantalTerm det = new DeterminantalTerm();
        double[] C = c0.clone();
        double[] L = C.clone();
        double h = h0;
        double var = arma.getInnovationVariance();

        double[] a = new double[dim];
        int n = y.length();
        // iteration
        int pos = 0, ilast = dim - 1;
        do {
            if (Double.isNaN(h) || h < 0) {
                return false;
            }
            det.add(h);
            // filter y
            double s = Math.sqrt(h);
            double e = (y.get(pos) - a[0]) / s;
            res.set(pos, e);
            stde.set(pos, s);
            double la = tlast(a);
            double v = e / s;
            for (int i = 0; i < ilast; ++i) {
                a[i] = a[i + 1] + C[i] * v;
            }
            a[ilast] = la + C[ilast] * v;
            // filter x if any

            double zl = L[0];
            double zlv = zl / h;

            if (!fast) {

                double llast = tlast(L), clast = C[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[i] * zlv;
                        C[i] -= zlv * li;
                    } else {
                        L[i] = li;
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[ilast] -= zlv * llast;
                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                if (h - var <= eps) {
                    fast = true;
                }
            }

        } while (++pos < n);

        return true;
    }

    /**
     * Exact processing. The likelihood is computed. The filtered data
     * (residuals) are returned along with the likelihood
     *
     * @param y The data that have to be filtered
     * @param ll The likelihood that will contain the results. Must be
     * uninitialised on entry. The likelihood is completed if the processing is
     * successful.
     * @return True if the processing is successful, false otherwise.
     */
    public Likelihood process(final DoubleSequence y) {
        fast = false;
        DeterminantalTerm det = new DeterminantalTerm();
        double[] C = c0.clone();
        double[] L = C.clone();
        double h = h0;
        double var = arma.getInnovationVariance();

        double[] a = new double[dim];
        int n = y.length();
        double[] yl = new double[n];
        // iteration
        int pos = 0, ilast = dim - 1;
        do {
            if (Double.isNaN(h) || h < 0) {
                return null;
            }
            det.add(h);
            // filter y
            double s = Math.sqrt(h);
            double e = (y.get(pos) - a[0]) / s;
            yl[pos] = e;
            double la = tlast(a);
            double v = e / s;
            for (int i = 0; i < ilast; ++i) {
                a[i] = a[i + 1] + C[i] * v;
            }
            a[ilast] = la + C[ilast] * v;
            // filter x if any

            double zl = L[0];
            double zlv = zl / h;

            if (!fast) {
                double llast = tlast(L), clast = C[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[i] * zlv;
                        C[i] -= zlv * li;
                    } else {
                        L[i] = li;
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[ilast] -= zlv * llast;
                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                if (h - var <= eps) {
                    fast = true;
                }
            }

        } while (++pos < n);
        DoubleSequence dy = DoubleSequence.ofInternal(yl);
        return Likelihood.builder(n)
                .ssqErr(Doubles.ssq(dy))
                .residuals(dy)
                .logDeterminant(det.getLogDeterminant())
                .build();
    }

    /**
     * @param y
     * @param ao Positions of AO outliers corresponding to missing values int
     * the regressors. Can be null
     * @param x
     * @param ll
     * @return
     */
    public ConcentratedLikelihoodWithMissing process(final DoubleSequence y, final SubArrayOfInt ao,
            final Matrix x) {
        fast = false;
        DeterminantalTerm det = new DeterminantalTerm();
        double[] c = c0.clone();
        double[] l = c.clone();
        double h = h0;
        double var = arma.getInnovationVariance();

        double[] a = new double[dim];
        int nx = x.getColumnsCount();
        int n = y.length();
        double[] yl = new double[n];
        // double[] xa = new double[nx * dim];
        Matrix xl = Matrix.make(n, nx);
        double[][] A = new double[nx][];
        double[] px = xl.getStorage();

        // iteration
        int pos = 0;
        int ilast = dim - 1;

        DataBlockIterator xrows = x.rowsIterator();
        while (xrows.hasNext()) {
            det.add(h);
            // filter y
            double s = Math.sqrt(h);
            double e = (y.get(pos) - a[0]) / s;
            yl[pos] = e;
            double la = tlast(a);
            double v = e / s;
            for (int i = 0; i < ilast; ++i) {
                a[i] = a[i + 1] + c[i] * v;
            }
            a[ilast] = la + c[ilast] * v;
            // filter x if any
            DataBlock xrow = xrows.next();
            for (int ix = 0, ipx = pos; ix < nx; ++ix, ipx += n) {
                double[] acur = A[ix];
                double xcur = xrow.get(ix);

                if (acur == null && xcur != 0) {
                    acur = new double[dim];
                    A[ix] = acur;
                }

                if (acur != null) {
                    e = (xcur - acur[0]) / s;
                    // xlrow[ix] = e;
                    px[ipx] = e;
                    v = e / s;
                    la = tlast(acur);
                    for (int i = 0; i < ilast; ++i) {
                        acur[i] = acur[i + 1] + c[i] * v;
                    }
                    acur[ilast] = la + c[ilast] * v;
                }
            }

            double zl = l[0];
            double zlv = zl / h;
            if (!fast) {

                double llast = tlast(l), clast = c[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = l[i + 1];
                    if (zlv != 0) {
                        l[i] = li - c[i] * zlv;
                        c[i] -= zlv * li;
                    } else {
                        l[i] = li;
                    }
                }

                l[ilast] = llast - zlv * clast;
                c[ilast] -= zlv * llast;
                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                if (h - var <= eps) {
                    fast = true;
                }
            }
            xrows.next();
        }

        QRSolver solver = QRSolvers.fastSolver();
        solver.solve(DataBlock.ofInternal(yl), xl);
        Matrix R = solver.R();
        double ssqerr = solver.ssqerr();

        double ldet = det.getLogDeterminant();
        if (ao != null && !ao.isEmpty()) {
            DataBlock rdiag = R.diagonal();
            n -= ao.getLength();
            for (int i = 0; i < ao.getLength(); ++i) {
                ldet += 2 * Math.log(Math.abs(rdiag.get(ao.get(i))));
            }
        }
        return ConcentratedLikelihoodWithMissing.builder()
                .ndata(n)
                .coefficients(solver.coefficients())
                .ssqErr(ssqerr)
                .residuals(solver.residuals())
                .rfactor(R)
                .build();
    }

    public void setEpsilon(double eps) {
        this.eps = eps;
    }

    public double getEpsilon() {
        return eps;
    }

    private double tlast(final double[] x) {
        double last = 0;
        for (int i = 1; i < phi.length; ++i) {
            last -= phi[i] * x[dim - i];
        }
        return last;
    }

    private void tx(final double[] x) {
        double last = 0;
        for (int i = 1; i < phi.length; ++i) {
            last -= phi[i] * x[dim - i];
        }
        for (int i = 1; i < dim; ++i) {
            x[i - 1] = x[i];
        }
        x[dim - 1] = last;

    }
}

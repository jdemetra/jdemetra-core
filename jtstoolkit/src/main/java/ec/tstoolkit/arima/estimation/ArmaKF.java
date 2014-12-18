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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.*;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Determinant;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * The ArmaKF class provides fast computation of Regression models
 * with stationary Arma noises, by means of a Kalman filter
 * It is a simplified implementation of the routine used in Tramo.
 * It should be noted that other implementations of the Kalman filter provide 
 * exactly the same results. However, this one, which is intensively used in 
 * several high-level routines, has been optimised as much as possible.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ArmaKF {

    /**
     * Contains the number of function calls to the main routine of the class,
     * i.e. "process".
     */
    public static int fnCalls;
    private IArimaModel arma_;
    private Polynomial phi_;
    private int dim_;
    private double h0_;
    private double[] c0_;
    private double eps_ = 0; //1e-12;
    private boolean fast_;

    /**
     * Creates a new Kalman filter for a given stationary Arima model 
     * @param arma The Arima model. Should be stationary.
     * @throws An ArimaException is thrown when the model is not stationary. 
     */
    public ArmaKF(final IArimaModel arma) {
        initmodel(arma, 0);
    }

    /**
     * Fast processing. The exact filter is used on the Max(p, q) first
     * data. Fast iteration is used on the following data.
     * Roughly speaking, the fast processing is twice faster than the exact processing.
     * @param y
     * @return
     */
    public DataBlock fastFilter(final IReadDataBlock y) {
        double[] C = c0_.clone();
        double h = h0_;

        double var = arma_.getInnovationVariance();
        if (var != 1) {
            h /= var;
            for (int i = 0; i < C.length; ++i) {
                C[i] /= var;
            }
        }

        double[] L = C.clone();

        double[] a = new double[dim_];
        int n = y.getLength();
        double[] yl = new double[n];
        // iteration
        int ilast = dim_ - 1;

        Polynomial theta = arma_.getMA().getPolynomial();
        int np = phi_.getDegree(), nq = theta.getDegree();
        int im = np > nq ? np : nq;

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
            if (h - 1 > eps_) {
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
            }
        }

        for (int i = im; i < n; ++i) {
            double x = y.get(i);
            for (int p = 1; p <= np; ++p) {
                x += y.get(i - p) * phi_.get(p);
            }
            for (int q = 1; q <= nq; ++q) {
                x -= yl[i - q] * theta.get(q);
            }
            yl[i] = x;
        }

        return new DataBlock(yl);
    }

    /**
     * Fast processing. The exact filter is used on the Max(p, q) first
     * observations. Fast iteration is used on the following observations
     * 
     * @param y
     * @param nparams
     * @return BIC statistics
     */
    public double fastProcessing(final IReadDataBlock y, int nparams) {
        DataBlock yl = fastFilter(y);
        int n = yl.getLength();
        double ssqerr = yl.ssq();
        // BIC from residuals ...
        return Math.log(ssqerr / n) + nparams * Math.log(n) / n;
    }

    private void initmodel(final IArimaModel arma, int statedim) {
        if (!arma.isStationary()) {
            throw new ArimaException(ArimaException.NonStationary);
        }
        this.arma_ = arma;
        phi_ = this.arma_.getAR().getPolynomial();
        if (statedim == 0) {
            statedim = Math.max(phi_.getDegree(), this.arma_.getMA().getLength());
        }
        dim_ = statedim;
        c0_ = this.arma_.getAutoCovarianceFunction().values(dim_);
        h0_ = c0_[0];
        tx(c0_);

    }

    /**
     * 
     * @param y
     * @param res
     * @param stde
     * @return
     */
    public boolean process(final IReadDataBlock y, final IDataBlock res,
            final IDataBlock stde) {
        ++fnCalls;
        fast_ = false;
        Determinant det = new Determinant();
        double[] C = c0_.clone();
        double[] L = C.clone();
        double h = h0_;
        double var = arma_.getInnovationVariance();

        double[] a = new double[dim_];
        int n = y.getLength();
        // iteration
        int pos = 0, ilast = dim_ - 1;
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

            if (!fast_) {

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
                if (h - var <= eps_) {
                    fast_ = true;
                }
            }

        } while (++pos < n);

        return true;
    }

    /**
     * Exact processing. The likelihood is computed. The filtered data (residuals)
     * are returned along with the likelihood
     * @param y The data that have to be filtered
     * @param ll The likelihood that will contain the results. Must be uninitialised
     * on entry. The likelihood is completed if the processing is successful.
     * @return True if the processing is successful, false otherwise.
     */
    public boolean process(final IReadDataBlock y, final Likelihood ll) {
        ++fnCalls;
        fast_ = false;
        Determinant det = new Determinant();
        double[] C = c0_.clone();
        double[] L = C.clone();
        double h = h0_;
        double var = arma_.getInnovationVariance();

        double[] a = new double[dim_];
        int n = y.getLength();
        double[] yl = new double[n];
        // iteration
        int pos = 0, ilast = dim_ - 1;
        do {
            if (Double.isNaN(h) || h < 0) {
                return false;
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

            if (!fast_) {
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
                if (h - var <= eps_) {
                    fast_ = true;
                }
            }

        } while (++pos < n);

        double ssqerr = 0;
        for (int i = 0; i < n; ++i) {
            ssqerr += yl[i] * yl[i];
        }
        ll.set(ssqerr, det.getLogDeterminant(), n);
        ll.setRes(yl);
        return true;
    }

    /**
     * @param y
     * @param ao
     *            Positions of AO outliers corresponding to missing values int
     *            the regressors. Can be null
     * @param x
     * @param ll
     * @return
     */
    public boolean process(final IReadDataBlock y, final SubArrayOfInt ao,
            final SubMatrix x, final ConcentratedLikelihood ll) {
        ++fnCalls;
        fast_ = false;
        Determinant det = new Determinant();
        double[] c = c0_.clone();
        double[] l = c.clone();
        double h = h0_;
        double var = arma_.getInnovationVariance();

        double[] a = new double[dim_];
        int nx = x.getColumnsCount();
        int n = y.getLength();
        double[] yl = new double[n];
        // double[] xa = new double[nx * dim_];
        Matrix xl = new Matrix(n, nx);
        double[][] A = new double[nx][];
        double[] px = xl.internalStorage();

        // iteration
        int pos = 0;
        int ilast = dim_ - 1;

        DataBlockIterator xrows = x.rows();
        DataBlock xrow = xrows.getData();
        do {
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

            for (int ix = 0, ipx = pos; ix < nx; ++ix, ipx += n) {
                double[] acur = A[ix];
                double xcur = xrow.get(ix);

                if (acur == null && xcur != 0) {
                    acur = new double[dim_];
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
            if (!fast_) {

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
                if (h - var <= eps_) {
                    fast_ = true;
                }
            }
            xrows.next();
        } while (++pos < n);

        double[] res = new double[n - nx];
        double[] b = new double[nx];
        Householder qr = new Householder(false);
        qr.decompose(xl);
        qr.leastSquares(new DataBlock(yl), new DataBlock(b),
                new DataBlock(res));
        Matrix R = qr.getR();
        double ssqerr = 0;
        for (int i = 0; i < res.length; ++i) {
            ssqerr += res[i] * res[i];
        }
        double ldet = det.getLogDeterminant();
        if (ao != null && !ao.isEmpty()) {
            DataBlock rdiag = qr.getRDiagonal();
            n -= ao.getLength();
            for (int i = 0; i < ao.getLength(); ++i) {
                ldet += 2 * Math.log(Math.abs(rdiag.get(ao.get(i))));
            }
        }
        ll.set(ssqerr, ldet, n);
        ll.setRes(res);
        Matrix bvar = SymmetricMatrix.XXt(UpperTriangularMatrix.inverse(R));
        bvar.mul(ssqerr / n);
        ll.setB(b, bvar, nx);
        return true;
    }
    
    public void setEpsilon(double eps){
        eps_=eps;
    }
      
    public double getEpsilon(){
        return eps_;
    }

    private double tlast(final double[] x) {
        double last = 0;
        for (int i = 1; i <= phi_.getDegree(); ++i) {
            last -= phi_.get(i) * x[dim_ - i];
        }
        return last;
    }

    private void tx(final double[] x) {
        double last = 0;
        for (int i = 1; i <= phi_.getDegree(); ++i) {
            last -= phi_.get(i) * x[dim_ - i];
        }
        for (int i = 1; i < dim_; ++i) {
            x[i - 1] = x[i];
        }
        x[dim_ - 1] = last;

    }
}

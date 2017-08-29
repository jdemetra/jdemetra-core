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
package demetra.likelihood;

import demetra.data.DoubleMatrix;
import demetra.design.IBuilder;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.data.LogSign;
import demetra.eco.EcoException;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihood implements IConcentratedLikelihood {

    public static Builder likelihood(int n) {
        return new Builder(n);
    }

    public static class Builder implements IBuilder<ConcentratedLikelihood> {

        final int n;
        double ssqerr, ldet;
        double[] res;
        double[] b;
        Matrix bvar, r;

        Builder(int n) {
            this.n = n;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder ssqErr(double ssq) {
            this.ssqerr = ssq;
            return this;
        }

        public Builder residuals(DoubleSequence residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                ssqerr = Doubles.ssq(residuals);
            }
            this.res = residuals.toArray();
            return this;
        }

        public Builder coefficients(DoubleSequence coeff) {
            if (coeff != null) {
                b = coeff.toArray();
            }
            return this;
        }

        public Builder unscaledCovariance(Matrix var) {
            bvar = var;
            return this;
        }

        /**
         * R factor of a QR decomposition of the regression matrix (X = Q R)
         *
         * @param r An upper triangular matrix
         * @return
         */
        public Builder rfactor(Matrix r) {
            this.r = r;
            return this;
        }

        @Override
        public ConcentratedLikelihood build() {
            return new ConcentratedLikelihood(this);
        }
    }

    private final double ll, ssqerr, ldet;
    private final int n;
    private final double[] res;
    private final double[] b;
    private volatile Matrix bvar;
    private final Matrix r;

    private ConcentratedLikelihood(final int n, final double ssqerr, final double ldet, final double[] b, final Matrix bvar, final Matrix r, final double[] res) {
        this.n = n;
        this.ldet = ldet;
        this.ssqerr = ssqerr;
        this.b = b;
        this.bvar = bvar;
        this.r = r;
        this.res = res;
        this.ll = -.5
                * (n * Math.log(2 * Math.PI) + n
                * (1 + Math.log(ssqerr / n)) + ldet);
    }

    private ConcentratedLikelihood(Builder builder) {
        this.n = builder.n;
        this.ldet = builder.ldet;
        this.ssqerr = builder.ssqerr;
        this.b = builder.b;
        this.bvar = builder.bvar;
        this.r = builder.r;
        this.res = builder.res;
        this.ll = -.5
                * (n * Math.log(2 * Math.PI) + n
                * (1 + Math.log(ssqerr / n)) + ldet);
    }

    @Override
    public double logDeterminant() {
        return ldet;
    }

    /**
     *
     * @return The factor of the likelihood.
     */
    @Override
    public double factor() {
        return Math.exp(ldet / n);
    }

    /**
     *
     * @return
     */
    @Override
    public double logLikelihood() {
        return ll;
    }

    /**
     *
     * @return
     */
    @Override
    public int dim() {
        return n;
    }

    @Override
    public DoubleSequence e() {
        return DoubleSequence.of(res);
    }

    @Override
    public DoubleSequence coefficients() {
        return DoubleSequence.of(b);
    }

    @Override
    public DoubleMatrix unscaledCovariance() {
        Matrix tmp = bvar;
        if (tmp == null && r != null) {
            synchronized (this) {
                tmp = bvar;
                if (tmp == null) {
                    Matrix u = UpperTriangularMatrix.inverse(r);
                    tmp = SymmetricMatrix.UUt(u);
                    bvar = tmp;
                }
            }
        }
        return bvar.unmodifiable();
    }

    /**
     * Gets the sum of the squares of the (transformed) observations.
     *
     * @return A positive number.
     */
    @Override
    public double ssq() {
        return ssqerr;
    }

    /**
     * Adjust the likelihood if the toArray have been pre-multiplied by a given
     * scaling factor
     *
     * @param yfactor The scaling factor
     * @param xfactor
     * @return
     */
    public ConcentratedLikelihood rescale(final double yfactor, double[] xfactor) {
        double nssqerr = ssqerr / (yfactor * yfactor);
        double[] nres = null;
        if (res != null) {
            nres = new double[res.length];
            for (int i = 0; i < res.length; ++i) {
                nres[i] = res[i] / yfactor;
            }
        }
        double[] nb = null;
        Matrix nbvar = null, nr = null;
        if (b != null && xfactor != null) {
            nb = new double[b.length];
            nr = r.deepClone();
            for (int i = 0; i < b.length; ++i) {
                nb[i] = b[i] * xfactor[i];
            }
            if (nbvar != null) {
                nbvar = bvar.deepClone();
                for (int i = 0; i < b.length; ++i) {
                    double ifactor = xfactor[i];
                    for (int j = 0; j < i; ++j) {
                        double ijfactor = ifactor * xfactor[j];
                        nbvar.apply(i, j, x -> x * ijfactor);
                        nbvar.apply(j, i, x -> x * ijfactor);
                    }
                    nbvar.apply(i, i, x -> x * ifactor * ifactor);
                }
            }
            if (r != null) {
                nr = r.deepClone();
                for (int i = 0; i < b.length; ++i) {
                    nr.column(i).div(xfactor[i]);
                }
            }
        } else {
            nb = b;
            nbvar = bvar;
            nr = r;
        }
        return new ConcentratedLikelihood(n, nssqerr, ldet, nb, nbvar, nr, nres);
    }

    public ConcentratedLikelihood correctForMissing(int nm) {
        if (r == null) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        double corr = LogSign.of(r.diagonal().extract(0, nm)).value;
        double nldet = ldet + 2 * corr;
        int nb = b.length - nm;
        if (nb == 0) {
            return new ConcentratedLikelihood(n - nm, ssqerr, nldet, new double[0], Matrix.EMPTY, Matrix.EMPTY, res);
        } else {
            return new ConcentratedLikelihood(n - nm, ssqerr, nldet, exclude(b, nm), bvar.extract(nm, nb, nm, nb), r.extract(nm, nb, nm, nb), res);
        }
    }

    private double[] exclude(double[] x, int nm) {
        double[] nx = new double[x.length - nm];
        System.arraycopy(x, nm, nx, 0, nx.length);
        return nx;
    }
}

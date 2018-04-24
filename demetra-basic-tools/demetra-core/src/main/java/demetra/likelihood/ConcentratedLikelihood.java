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

import demetra.design.IBuilder;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.data.LogSign;
import demetra.eco.EcoException;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.MatrixType;
import javax.annotation.Nonnull;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihood implements IConcentratedLikelihood {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements IBuilder<ConcentratedLikelihood> {

        private static double[] B_EMPTY = new double[0];

        int n;
        int nmissing = 0;
        double ssqerr, ldet;
        double[] res;
        double[] b = B_EMPTY;
        Matrix bvar, r;

        private Builder() {
        }

        /**
         * Number of data, including the missing values;
         *
         * @param n
         * @return
         */
        public Builder ndata(int n) {
            this.n = n;
            return this;
        }

        /**
         * Number of missing values, estimated by means of additive outliers.
         * The regression variables corresponding to the missing values should be put
         * at the beginning. All information included in the builder contains
         * the effects of the missing values.
         *
         * @param nmissing
         * @return
         */
        public Builder nmissing(int nmissing) {
            this.nmissing = nmissing;
            return this;
        }

        /**
         * Log determinant, NOT corrected for missing values
         *
         * @param ldet
         * @return
         */
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
            if (nmissing > 0) {
                if (r == null) {
                    throw new EcoException(EcoException.UNEXPECTEDOPERATION);
                }
                double corr = LogSign.of(r.diagonal().extract(0, nmissing)).getValue();
                double nldet = ldet + 2 * corr;
                return new ConcentratedLikelihood(n - nmissing, nmissing, ssqerr, nldet, res, b, bvar, r);
            } else {
                return new ConcentratedLikelihood(n, 0, ssqerr, ldet, res, b, bvar, r);
            }
        }

    }

    /**
     * n = number of actual observations, nmissing = number of missing values
     */
    private final int n, nmissing;
    private final double ll, ssqerr, ldet;
    private final double[] res;
    private final double[] b;
    private volatile Matrix bvar;
    private final Matrix r;

    private ConcentratedLikelihood(final int n, final int nmissing, final double ssqerr, final double ldet, final double[] res,
            final double[] b, final Matrix bvar, final Matrix r) {
        this.n = n;
        this.nmissing = nmissing;
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

    public int nmissing() {
        return nmissing;
    }

    public DoubleSequence missingEstimates() {
        return nmissing == 0 ? DoubleSequence.EMPTY : DoubleSequence.ofInternal(b, 0, nmissing);
    }

    public DoubleSequence missingUnscaledVariances() {
        if (nmissing == 0) {
            return DoubleSequence.EMPTY;
        }
        bvariance();
        return DoubleSequence.ofInternal(bvar.data(), 0, nmissing, bvar.getRowsCount() + 1);
    }

    /**
     * Returns all the coefficients, including the missing values
     * @return 
     */
    public DoubleSequence allCoefficients() {
        return DoubleSequence.ofInternal(b);
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

    /**
     * Number of coefficients (excluding missing value estimates)
     * @return
     */
    @Override
    public int nx() {
        return b.length-nmissing;
    }

    @Override
    public DoubleSequence e() {
        return DoubleSequence.ofInternal(res);
    }

    @Override
    @Nonnull
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(b, nmissing, b.length - nmissing);
    }

    @Override
    @Nonnull
    public MatrixType unscaledCovariance() {
        bvariance();
        if (bvar == null) {
            return MatrixType.EMPTY;
        }
        if (nmissing == 0) {
            return bvar.unmodifiable();
        } else {
            int nb = b.length - nmissing;
            return bvar.extract(nmissing, nb, nmissing, nb).unmodifiable();
        }
    }

    private void bvariance() {
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
     * Adjust the likelihood if the data have been pre-multiplied by a given
     * scaling factor
     *
     * @param yfactor The scaling factor of y
     * @param xfactor The scaling factor of x
     * @return
     */
    public ConcentratedLikelihood rescale(final double yfactor, double[] xfactor) {
        // rescale the residuals
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
        if (b != null) {
            nb = new double[b.length];
            if (xfactor == null) {
                // rescale the coefficients (but not the unscaled variances)
                for (int i = 0; i < b.length; ++i) {
                    nb[i] = b[i] / yfactor;
                }
                nbvar = bvar;
                nr = r;
            } else {
                // rescale everything
                for (int i = 0; i < b.length; ++i) {
                    nb[i] = b[i] * xfactor[i] / yfactor;
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
            }
        }
        return new ConcentratedLikelihood(n, nmissing, nssqerr, ldet, nres, nb, nbvar, nr);
    }
    
    public int degreesOfFreedom(){
        return n-nx();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("log-likelihood: ").append(this.ll)
                .append(", sigma2: ").append(this.ssqerr / this.n);

        return builder.toString();

    }
}

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
package jdplus.likelihood;

import nbbrd.design.Immutable;
import nbbrd.design.BuilderPattern;
import demetra.math.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import demetra.data.DoublesMath;
import jdplus.math.matrices.Matrix;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihoodWithMissing implements ConcentratedLikelihood {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ConcentratedLikelihoodWithMissing.class)
    public static class Builder {

        private static final double[] B_EMPTY = new double[0];

        private int n;
        private int nmissing = 0;
        private double ssqerr, ldet;
        private double[] res;
        private double[] b = B_EMPTY;
        private Matrix bvar;
        private boolean scalingFactor = true;

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

        public Builder scalingFactor(boolean scalingFactor) {
            this.scalingFactor = scalingFactor;
            return this;
        }

        /**
         * Number of missing values, estimated by means of additive outliers.
         * The regression variables corresponding to the missing values should
         * be put at the beginning. All information included in the builder
         * contains the effects of the missing values.
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

        public Builder residuals(DoubleSeq residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                ssqerr = DoublesMath.ssq(residuals);
            }
            this.res = residuals.toArray();
            return this;
        }

        public Builder coefficients(DoubleSeq coeff) {
            if (coeff != null) {
                b = coeff.toArray();
            }
            return this;
        }

        public Builder unscaledCovariance(Matrix var) {
            bvar = var;
            return this;
        }

        public ConcentratedLikelihoodWithMissing build() {
            return new ConcentratedLikelihoodWithMissing(n - nmissing, nmissing, ssqerr, ldet, res, b, bvar, scalingFactor);
        }

    }

    /**
     * n = number of actual observations, nmissing = number of missing values
     */
    private final int n, nmissing;
    private final double ll, ssqerr, ldet;
    private final double[] res;
    private final double[] b;
    private final Matrix bvar;
    private final boolean scalingFactor;

    private ConcentratedLikelihoodWithMissing(final int n, final int nmissing, final double ssqerr, final double ldet, final double[] res,
            final double[] b, final Matrix bvar, final boolean scalingFactor) {
        this.n = n;
        this.nmissing = nmissing;
        this.ldet = ldet;
        this.ssqerr = ssqerr;
        this.b = b;
        this.bvar = bvar;
        this.res = res;
        this.scalingFactor = scalingFactor;
        if (scalingFactor) {
            this.ll = -.5
                    * (n * Constants.LOGTWOPI + n
                    * (1 + Math.log(ssqerr / n)) + ldet);
        } else {
            this.ll = -.5 * (n * Constants.LOGTWOPI + ssqerr + ldet);
        }
    }

    public int nmissing() {
        return nmissing;
    }

    /**
     * Returns the values of the additive outliers corresponding to missing.
     * The actual estimate of a missing value if y[t]-missingCorrections[t]
     * @return 
     */
    public DoubleSeq missingCorrections() {
        return nmissing == 0 ? Doubles.EMPTY : DoubleSeq.of(b, 0, nmissing);
    }

    public DoubleSeq missingUnscaledVariances() {
        if (nmissing == 0) {
            return Doubles.EMPTY;
        }
        return bvar.diagonal().extract(0, nmissing);
    }

    /**
     * Returns all the coefficients, including the missing values
     * 
     * @return
     */
    public DoubleSeq allCoefficients() {
        return DoubleSeq.of(b);
    }

    @Override
    public boolean isScalingFactor() {
        return scalingFactor;
    }

    @Override
    public double logDeterminant() {
        return ldet;
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
     *
     * @return
     */
    @Override
    public int nx() {
        return b.length - nmissing;
    }

    @Override
    public DoubleSeq e() {
        return DoubleSeq.of(res);
    }

    @Override
    @NonNull
    public DoubleSeq coefficients() {
        return DoubleSeq.of(b, nmissing, b.length - nmissing);
    }

    @Override
    public double coefficient(int pos) {
        return b[pos + nmissing];
    }

    @Override
    @NonNull
    public Matrix unscaledCovariance() {
        if (bvar == null) {
            return Matrix.EMPTY;
        }
        if (nmissing == 0) {
            return bvar;
        } else {
            int nb = b.length - nmissing;
            return bvar.extract(nmissing, nb, nmissing, nb);
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
    public ConcentratedLikelihoodWithMissing rescale(final double yfactor, double[] xfactor) {
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
        Matrix nbvar = null;
        if (b != null) {
            nb = new double[b.length];
            if (xfactor == null) {
                // rescale the coefficients (but not the unscaled variances)
                for (int i = 0; i < b.length; ++i) {
                    nb[i] = b[i] / yfactor;
                }
                nbvar = bvar;
            } else {
                // rescale everything
                for (int i = 0; i < b.length; ++i) {
                    nb[i] = b[i] * xfactor[i] / yfactor;
                }
                Matrix tmp = bvar.deepClone();
                for (int i = 0; i < b.length; ++i) {
                    double ifactor = xfactor[i];
                    for (int j = 0; j < i; ++j) {
                        double ijfactor = ifactor * xfactor[j];
                        tmp.apply(i, j, x -> x * ijfactor);
                        tmp.apply(j, i, x -> x * ijfactor);
                    }
                    tmp.apply(i, i, x -> x * ifactor * ifactor);
                }
                nbvar=tmp;
            }
        }
        double nldet = ldet;
        if (!scalingFactor) {
            nldet += n * Math.log(yfactor);
        }

        return new ConcentratedLikelihoodWithMissing(n, nmissing, nssqerr, nldet, nres, nb, nbvar, scalingFactor);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("log-likelihood: ").append(this.ll)
                .append(", sigma2: ").append(this.ssqerr / this.n);

        return builder.toString();

    }
}

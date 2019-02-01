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

import demetra.design.Immutable;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.maths.Constants;
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

    @BuilderPattern(ConcentratedLikelihood.class)
    public static class Builder {

        private static final double[] B_EMPTY = new double[0];

        private int n;
        private double ssqerr, ldet;
        private double[] res;
        private double[] b = B_EMPTY;
        private MatrixType bvar;
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
                ssqerr = residuals.ssq();
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

        public Builder unscaledCovariance(MatrixType var) {
            bvar = var;
            return this;
        }

        public ConcentratedLikelihood build() {
            return new ConcentratedLikelihood(n, ssqerr, ldet, res, b, bvar, scalingFactor);
        }

    }

    /**
     * n = number of actual observations, nmissing = number of missing values
     */
    private final int n;
    private final double ll, ssqerr, ldet;
    private final double[] res;
    private final double[] b;
    private final MatrixType bvar;
    private final boolean scalingFactor;

    private ConcentratedLikelihood(final int n, final double ssqerr, final double ldet, final double[] res,
            final double[] b, final MatrixType bvar, final boolean scalingFactor) {
        this.n = n;
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
        return b.length;
    }

    @Override
    public DoubleSequence e() {
        return DoubleSequence.ofInternal(res);
    }

    @Override
    @Nonnull
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(b);
    }

    @Override
    public double coefficient(int pos) {
        return b[pos];
    }

    @Override
    @Nonnull
    public MatrixType unscaledCovariance() {
        if (bvar == null) {
            return MatrixType.EMPTY;
        } else {
            return bvar;
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

    public int degreesOfFreedom() {
        return n - nx();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("log-likelihood: ").append(this.ll)
                .append(", sigma2: ").append(this.ssqerr / this.n);

        return builder.toString();

    }
}

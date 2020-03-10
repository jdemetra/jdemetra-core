/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.likelihood;

import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import demetra.eco.EcoException;
import demetra.data.DoubleSeq;
import demetra.design.BuilderPattern;
import jdplus.math.matrices.Matrix;

/**
 * This interface represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ConcentratedLikelihood extends Likelihood {

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
                ssqerr = residuals.ssq();
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

        public ConcentratedLikelihood build() {
            return new InternalConcentratedLikelihood(n, ssqerr, ldet, res, b, bvar, scalingFactor);
        }

    }

    /**
     * The coefficients of the regression variables
     *
     * @return
     */
    DoubleSeq coefficients();

    /**
     * The coefficient of the i-th regression variables (0-based). The first one
     * is the mean correction, if any.
     *
     * @param idx Position of the variable
     * @return
     */
    double coefficient(int idx);

    /**
     *
     * @return
     */
    Matrix unscaledCovariance();

    default Matrix covariance(int nhp, boolean unbiased) {

        if (nx() == 0) {
            return Matrix.EMPTY;
        }

        Matrix v = unscaledCovariance().deepClone();
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        double sig2 = ssq() / ndf;
        v.mul(sig2);
        return v;
    }

    /**
     * Number of regression variables
     *
     * @return
     */
    default int nx() {
        return coefficients().length();
    }

    default int degreesOfFreedom() {
        return dim() - nx();
    }

    /**
     * Gets the standard deviation for the given regression variable (including
     * mean, excluding missing identified by additive outliers)
     *
     * @param ix Position of the variable
     * @param nhp Number of hyper-parameters (for correction of the degrees of
     * freedom)
     * @param unbiased True for use of unbiased variance estimate, false for ML
     * variance estimate
     * @return
     */
    default double ser(int ix, int nhp, boolean unbiased) {

        double e = unscaledCovariance().get(ix, ix);
        if (e == 0) {
            return Double.NaN;
        }
        double b = coefficients().get(ix);
        if (b == 0) {
            return 0;
        }
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        return Math.sqrt(e * ssq() / ndf);
    }

    default double[] ser(int nhp, boolean unbiased) {

        if (nx() == 0) {
            return DoubleSeq.EMPTYARRAY;
        }
        double[] e = unscaledCovariance().diagonal().toArray();
        int ndf = unbiased ? dim() - nx() - nhp : dim();
        double ssq = ssq();
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i] * ssq / ndf);
        }
        return e;
    }

    /**
     * Gets the T-Stat of the given variable. This method is only defined
     * when the likelihood contains a scaling factor. In the other case,
     * the user should use the corresponding ser() method.
     * When it is defined T(i) = coefficient(i)/ser(i)
     *
     * @param ix The 0-based position of the variable. 0 for mean correction, if
     * any
     * @param nhp The number of hyper-parameters; used to correct the degrees
     * of freedom (unused if we use the ML (biased) estimator.
     * @param unbiased True if the estimator of the scaling factor is unbiased.
     * False
     * if we use the (biased) ML estimator.
     * @return
     */
    default double tstat(int ix, int nhp, boolean unbiased) {
        if (!isScalingFactor()) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        return coefficient(ix) / ser(ix, nhp, unbiased);
    }

    default double[] tstats(int nhp, boolean unbiased) {
        if (!isScalingFactor()) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        double[] t = ser(nhp, unbiased);
        DoubleSeqCursor reader = coefficients().cursor();
        for (int i = 0; i < t.length; ++i) {
            t[i] = reader.getAndNext() / t[i];
        }
        return t;
    }

}

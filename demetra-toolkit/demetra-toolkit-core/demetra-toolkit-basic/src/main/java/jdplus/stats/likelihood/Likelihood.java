/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.likelihood;

import nbbrd.design.Development;
import demetra.eco.EcoException;
import demetra.math.Constants;
import demetra.data.DoubleSeq;
import nbbrd.design.BuilderPattern;

/**
 * The Likelihood interface formalizes the likelihood of a usual gaussian
 * model. If isConcentratedScalingFactor is true, we suppose that the scaling
 * factor (sig2) is part of the parameters and that it is concentrated out of
 * the likelihood In the other case, we can suppose that sig2 = 1.
 * <br>
 * For a N(0, [sig2*]V) distribution (dim = n), the log-likelihood is then given
 * by
 * <br>
 * -.5*[n*log(2*pi)+log(det(V)[*sig2^n])+[(1/sig2)*]y'(V^-1)y].
 * <br>
 * If we factorize V as LL' (L is the Cholesky factor of V) and if we write
 * e=L^-1*y, we get
 * <br>
 * ll=-.5*[n*log(2*pi)+log(det(V))[+n*log(sig2)]+[(1/sig2)*]e'e]
 * <br>
 * To be noted that det(V) is then the square of the product of the main
 * diagonal of L.
 * <br>
 * The ML estimator of sig2 is given by sig2=e'e/n
 * <br>
 * If we concentrate it out of the likelihood, we get:
 * <br>
 * ll=-.5[n*log(2*pi)+n*(log(ssq/n)+1)+ldet]
 * <br>
 * So, the likelihood is defined by means of:
 * <br> - n = dim()
 * <br> - ldet = logDeterminant()
 * <br> - ssq = ssq()
 * <br> -sig2 =ssq/n is given by sigma2()
 <br>
 * Maximizing the concentrated likelihood is equivalent to minimizing the
 * function:
 * <br>
 * ssq * det^1/n (= ssq*factor)
 * <br>
 * if e are the e and v = e*det^1/(2n), we try to minimize the sum of squares
 * defined by vv'. This last formulation will be used in optimization procedures
 * based like Levenberg-Marquardt or similar algorithms.
 */
@Development(status = Development.Status.Release)
public interface Likelihood {

    public static Builder builder(int n) {
        return new Builder(n);
    }

    @BuilderPattern(Likelihood.class)
    public static class Builder {

        private final int n;
        private double ssqerr, ldet;
        private double[] res;
        private boolean scalingFactor = true;

        private Builder(int n) {
            this.n = n;
        }

        public Builder scalingFactor(boolean scalingFactor) {
            this.scalingFactor = scalingFactor;
            return this;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder ssqErr(double ssq) {
            this.ssqerr = ssq;
            return this;
        }

        public Builder residuals(DoubleSeq residuals) {
            if (residuals == null)
                return this;
            this.res = residuals.toArray();
            return this;
        }

        public Likelihood build() {
            return new InternalLikelihood(n, ssqerr, ldet, res, scalingFactor);
        }
    }
    /**
     * Aikake Information Criterion for a given number of (hyper-)parameters
     * AIC=2*nparams-2*ll
     *
     * @param nparams The number of parameters
     * @return The AIC. Models with lower AIC shoud be preferred.
     */
    default double AIC(final int nparams) {
        return -2 * logLikelihood() + 2 * nparams;
    }

    /**
     *
     * @param nparams
     * @return
     */
    default double BIC(final int nparams) {
        return -2 * logLikelihood() + nparams * Math.log(dim());
    }

    /**
     * @return Log of the likelihood
     */
    default double logLikelihood() {
        int n = dim();
        if (isScalingFactor()) {
            return -.5 * (n * Constants.LOGTWOPI + n * (1 + Math.log(ssq() / n)) + logDeterminant());
        } else {
            return -.5 * (n * Constants.LOGTWOPI + ssq() + logDeterminant());
        }
    }

    /**
     * @return Square root of Sigma.
     */
    int dim();

    /**
     * Return the log-determinant
     *
     * @return
     */
    double logDeterminant();

    /**
     * True if there is a scaling factor in the distribution. The scaling factor
     * is then concentrated out of the likelihood and replaced by its ML
     * estimate. So the likelihood is then in fact the concentrated likelihood.
     *
     * @return
     */
    default boolean isScalingFactor() {
        return true;
    }

    /**
     * Gets the sqrt of sigma2.
     *
     * @return A positive number. 1 if the likelihood is not concentrated.
     */
    default double ser() {
        return isScalingFactor() ? Math.sqrt(ssq() / dim()) : 1;
    }

    /**
     * Gets the ML estimate of the scaling factor. sigma=ssq/n
     *
     * @return A positive number. 1 if the likelihood is not concentrated.
     */
    default double sigma2() {
        return isScalingFactor() ? ssq() / dim() : 1;
    }

    /**
     * @return Sum of the squared standardized innovations
     */
    double ssq();

    /**
     * @return The Standardized innovations. =L^-1 * y where L is the Cholesky
     * factor of the (unscaled) covariance matrix. May be null if the e are not
     * stored
     */
    DoubleSeq e();

    /**
     * @return The determinantal factor (n-th root). Not used if the likelihood
     * is not concentrated
     */
    default double factor() {
        if (!isScalingFactor()) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
        return Math.exp(logDeterminant() / dim());
    }

    /**
     * @return The deviances = e*sqrt(factor). Not used if the likelihood is not
     * concentrated.
     */
    default DoubleSeq deviances() {
        double f = factor();
        DoubleSeq e = e();
        if (f == 1) {
            return e;
        } else {
            final double sf = Math.sqrt(f);
            return e.map(x -> x * sf);
        }
    }
    
}

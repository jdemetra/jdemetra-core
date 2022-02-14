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
package jdplus.stats.likelihood;

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import demetra.math.Constants;
import demetra.data.DoubleSeq;

/**
 * Log-Likelihood of a multi-variate gaussian distribution. For a N(0, sig2*V)
 * distribution (dim = n), the log-likelihood is given by
 * -.5*[n*log(2*pi)+log(det(V)*sig2^n)+(1/sig2)*y'(V^-1)y] = If we factorize V
 * as LL' (L is the Cholesky factor of V) and if we write e=L^-1*y, we get
 * ll=-.5*[n*log(2*pi)+log(det(V))+n*log(sig2)+(1/sig2)*e'e] det(V) is then the
 * square of the product of the main diagonal of L.
 *
 * We consider that sig2 is concentrated out of the likelihood and that it is
 * given by its max-likelihood estimator: sig2=e'e/n where e'e= y*(V^-1)y.
 *
 * So, we get: ll=-.5[n*log(2*pi)+n*(log(ssq/n)+1)+ldet]
 *
 * The likelihood is initialized by means of - its dimension: n - the log of the
 * determinantal term: ldet - the sum of the squares: ssq
 */
@Development(status = Development.Status.Release)
@Immutable
final class InternalLikelihood implements Likelihood {


    private final double ll, ssqerr, ldet;
    private final int n;
    private final double[] res;
    private final boolean scalingFactor;

    /**
     * Initializes the likelihood/ See the description of the class for further
     * information.
     *
     * @param ssqerr The sum of the squares of the (transformed) observations.
     * @param ldet The log of the determinantal term
     * @param ndim The number of observations
     * @param res
     */
    InternalLikelihood(final int ndim, final double ssqerr, final double ldet, final double[] res, final boolean scalingFactor) {
        this.scalingFactor = scalingFactor;
        if (scalingFactor) {
            this.ll = -.5
                    * (ndim * Constants.LOGTWOPI + ndim
                    * (1 + Math.log(ssqerr / ndim)) + ldet);
        } else {
            this.ll = -.5 * (ndim * Constants.LOGTWOPI + ssqerr + ldet);
        }
        this.ssqerr = ssqerr;
        this.ldet = ldet;
        this.n = ndim;
        this.res = res;
    }

    @Override
    public double logDeterminant() {
        return ldet;
    }

    /**
     * Computes the factor of the likelihood. The log-likelihood is:
     * ll=-.5[n*log(2*pi)+n*(log(ssq/n)+1)+ldet]
     * =-.5[n*log(2*pi)+n+n*(log(ssq/n)+ldet/n)] So, for a given n, maximizing
     * the likelihood is equivalent to minimizing sigma*factor where:
     * sigma=ssq/n factor=exp(ldet/n)=exp(log(det(V)^1/n)=(det(L)^1/n)^2
     *
     * So, the factor is the square of the geometric mean of the main diagonal
     * of the Cholesky factor.
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
    public DoubleSeq e() {
        return res == null ? null : DoubleSeq.of(res);
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
     * @param factor The scaling factor
     * @return
     */
    public InternalLikelihood rescale(final double factor) {
        if (factor == 1) {
            return this;
        }
        double nssqerr = ssqerr / factor * factor;
        double[] nres = null;
        if (res != null) {
            nres = new double[res.length];
            for (int i = 0; i < res.length; ++i) {
                nres[i] = res[i] / factor;
            }
        }
        double nldet = ldet;
        if (!scalingFactor) {
            nldet += n * Math.log(factor);
        }
        return new InternalLikelihood(n, nssqerr, nldet, nres, scalingFactor);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ll=").append(this.logLikelihood()).append("\r\n");
        builder.append("n=").append(this.dim()).append("\r\n");
        builder.append("ssq=").append(this.ssq()).append("\r\n");
        builder.append("ldet=").append(this.logDeterminant()).append("\r\n");
        return builder.toString();
    }

}

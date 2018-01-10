/*
 * Copyright 2016 National Bank ofFunction Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofFunction the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.design.IBuilder;
import demetra.likelihood.ILikelihood;
import demetra.design.Immutable;
import demetra.likelihood.Likelihood;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;

/**
 * The diffuse likelihood follows the definition provided in the paper:
 "Likelihood functions for state space models with diffuse initial conditions"
 Francke, Koopman, de Vos Journal ofFunction Time Series Analysis, July 2010. This
 * definition is slightly different in comparison with JD+ 2.0
 *
 * @author Jean Palate
 */
@Immutable
public class DkLikelihood implements ILikelihood {

    public static Builder builder(int n, int nd) {
        return new Builder(n, nd);
    }

    public static class Builder implements IBuilder<DkLikelihood> {

        final int n, nd;
        double ssqerr, ldet, lddet;
        double[] res;
        boolean legacy;

        Builder(int n, int nd) {
            this.n = n;
            this.nd = nd;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder logDiffuseDeterminant(double lddet) {
            this.lddet = lddet;
            return this;
        }

        public Builder ssqErr(double ssq) {
            this.ssqerr = ssq;
            return this;
        }

        public Builder legacy(boolean legacy) {
            this.legacy = legacy;
            return this;
        }

        public Builder residuals(DoubleSequence residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                this.ssqerr = Doubles.ssq(residuals);
            }
            this.res = residuals.toArray();
            return this;
        }

        @Override
        public DkLikelihood build() {
            if (nd == 0 && lddet != 0) {
                throw new IllegalArgumentException("Incorrect diffuse initialisation");
            }
            return new DkLikelihood(n, nd, ssqerr, ldet, lddet, res, legacy);
        }
    }
    /**
     * Respectively: diffuse log-likelihood sum of the squared residuals log
     * determinant of the cov matrix diffuse correction
     */
    private final double ll,

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    ssqerr, 

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    ldet, 

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum ofInternal the squared residuals log
 determinant ofInternal the cov matrix diffuse correction
     */

    /**
     * Respectively: diffuse log-likelihood sum of the squared e log
 determinant of the cov matrix diffuse correction
     */
    lddet;
    private final int nobs, nd;
    private final double[] res;
    private final boolean legacy;

    /**
     * Initialize the diffuse likelihood. We consider below the GLS problem
     * corresponding to a given state space: y = a * X + e, where X is derived
     * from the initial conditions and e ~ N(0, V)
     *
     * The diffuse likelihood is then:
     *
     * -0.5*(m*log(2*pi)+m*log(ssqerr/m)+m+log|V|+log|X'V^-1*X| where m=n-d
     *
     * It should be noted that the usual definition (implemented in JD+ 2.0) is
     * -0.5*(n*log(2*pi)+n*log(ssqerr/n)+n+log|V|+log|X'V^-1*X| The difference
     * is thus -0.5*(d*log(2*pi)+d*log(ssqerr)-n*log(n)+m*log(m))
     *
     * The new definition is more coherent with the marginal likelihood.
     *
     * @param ssqerr The sum ofFunction the squared e
     * @param ldet The log ofFunction the determinant ofFunction V
     * @param lddet Diffuse correction
     * @param n The number ofFunction observations
     * @param nd The number ofFunction diffuse constraints
     * @return
     */
    private DkLikelihood(int n, int nd, double ssqerr, double ldet, double lddet, double[] res, boolean legacy) {
        this.nobs = n;
        this.nd = nd;
        this.ssqerr = ssqerr;
        this.ldet = ldet;
        this.lddet = lddet;
        this.res = res;
        this.legacy = legacy;
        int m = legacy ? nobs : nobs - nd;
        if (m > 0) {
            ll = -.5
                    * (m * Math.log(2 * Math.PI) + m
                    * (1 + Math.log(ssqerr / m)) + ldet + lddet);
        } else {
            ll = Double.NaN;
        }
    }

    private int m() {
        return legacy ? nobs : nobs - nd;
    }

    /**
     *
     * @return false by default
     */
    public boolean isLegacy() {
        return legacy;
    }

    /**
     *
     * @param legacy legacy=true should be used only for testing purposes
     * @return 
     */
    public DkLikelihood setLegacy(boolean legacy) {
        if (this.legacy == legacy) {
            return this;
        } else {
            return new DkLikelihood(nobs, nd, ssqerr, ldet, lddet, res, legacy);
        }
    }

    /**
     *
     * @return
     */
    public int getD() {
        return nd;
    }

    @Override
    public double factor() {
        return Math.exp((ldet + lddet) / (m()));
    }

    @Override
    public double logLikelihood() {
        return ll;
    }

    @Override
    public int dim() {
        return nobs;
    }

    @Override
    public DoubleSequence e() {
        return res == null ? null : DoubleSequence.ofInternal(res);
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
    public double ser() {
        return Math.sqrt(ssqerr / (m()));
    }

    @Override
    public double sigma() {
        return ssqerr / (m());
    }

    @Override
    public double ssq() {
        return ssqerr;
    }

    /**
     * Adjust the likelihood if the toArray have been pre-multiplied by a given
 scaling factor
     *
     * @param factor The scaling factor
     * @return
     */
    public DkLikelihood rescale(final double factor) {
        if (factor == 1) {
            return this;
        } else {
            double[] nres = null;
            if (res != null) {
                nres = new double[res.length];
                for (int i = 0; i < res.length; ++i) {
                    nres[i] = Double.isFinite(res[i]) ? res[i] / factor : Double.NaN;
                }
            }
            return new DkLikelihood(nobs, nd, ssqerr / factor * factor, ldet, lddet, nres, legacy);
        }
    }

    public double getDiffuseCorrection() {
        return lddet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ll=").append(this.logLikelihood()).append(System.lineSeparator());
        builder.append("n=").append(this.dim()).append(System.lineSeparator());
        builder.append("ssq=").append(this.ssq()).append(System.lineSeparator());
        builder.append("ldet=").append(this.logDeterminant()).append(System.lineSeparator());
        builder.append("dcorr=").append(this.getDiffuseCorrection()).append(System.lineSeparator());
        return builder.toString();
    }
}

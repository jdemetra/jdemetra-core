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
package demetra.ssf.likelihood;

import demetra.likelihood.ILikelihood;
import demetra.design.Immutable;
import demetra.data.Doubles;
import demetra.design.BuilderPattern;
import demetra.maths.Constants;
import demetra.data.DoubleSeq;

/**
 * The diffuse likelihood follows the definition provided in the paper:
 * "Likelihood functions for state space models with diffuse initial conditions"
 * Francke, Koopman, de Vos Journal ofFunction Time Series Analysis, July 2010.
 * This definition is slightly different in comparison with JD+ 2.0
 *
 * @author Jean Palate
 */
@Immutable
public final class DiffuseLikelihood implements ILikelihood {

    public static Builder builder(int n, int nd) {
        return new Builder(n, nd);
    }

    @BuilderPattern(DiffuseLikelihood.class)
    public static class Builder {

        private final int n, nd;
        private double ssqerr, ldet, dcorr;
        private double[] res;
        private boolean legacy;
        private boolean concentratedScalingFactor = true;

        Builder(int n, int nd) {
            this.n = n;
            this.nd = nd;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder diffuseCorrection(double dcorr) {
            this.dcorr = dcorr;
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

        public Builder concentratedScalingFactor(boolean concentrated) {
            this.concentratedScalingFactor = concentrated;
            return this;
        }

        public Builder residuals(DoubleSeq residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                this.ssqerr = Doubles.ssq(residuals);
            }
            this.res = residuals.toArray();
            return this;
        }

        public DiffuseLikelihood build() {
            if (nd == 0 && dcorr != 0) {
                throw new IllegalArgumentException("Incorrect diffuse initialisation");
            }
            return new DiffuseLikelihood(concentratedScalingFactor, n, nd, ssqerr, ldet, dcorr, res, legacy);
        }
    }
    /**
     * Respectively: diffuse log-likelihood sum of the squared residuals log
     * determinant of the cov matrix diffuse correction
     */
    private final double ll, ssqerr, ldet, dcorr;
    private final int nobs, nd;
    private final double[] res;
    private final boolean legacy, scalingFactor;

    /**
     * Initialize the diffuse likelihood. We consider below the GLS problem
     * corresponding to a given state space: y = a * X + e, where X is derived
     * from the initial conditions and e ~ N(0, V) or e ~ N(0, s Q)
     *
     * The diffuse likelihood is then:
     * + non-concentrated scaling factor:
     * -0.5*(m*log(2*pi)+log|V|+log|X'V^-1*X|+ssqerr where m=n-d
     * 
     * + concentrated scaling factor (s = ssqerr/n)
     * -0.5*(m*log(2*pi)+m*log(ssqerr/m)+m+log|Q|+log|X'Q^-1*X| where m=n-d
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
    private DiffuseLikelihood(boolean concentrated, int n, int nd, double ssqerr, double ldet, double lddet, double[] res, boolean legacy) {
        this.scalingFactor = concentrated;
        this.nobs = n;
        this.nd = nd;
        this.ssqerr = ssqerr;
        this.ldet = ldet;
        this.dcorr = lddet;
        this.res = res;
        this.legacy = legacy;
        int m = legacy ? nobs : nobs - nd;
        if (m > 0) {
            if (scalingFactor) {
                ll = -.5
                        * (m * Constants.LOGTWOPI + m
                        * (1 + Math.log(ssqerr / m)) + ldet + lddet);
            } else {
                ll = -.5
                        * (m * Constants.LOGTWOPI + ssqerr + ldet + lddet);
            }
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
    public DiffuseLikelihood setLegacy(boolean legacy) {
        if (this.legacy == legacy) {
            return this;
        } else {
            return new DiffuseLikelihood(scalingFactor, nobs, nd, ssqerr, ldet, dcorr, res, legacy);
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
        return Math.exp((ldet + dcorr) / (m()));
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
    public DoubleSeq e() {
        return res == null ? null : DoubleSeq.of(res);
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
     * scaling factor
     *
     * @param factor The scaling factor
     * @return
     */
    public DiffuseLikelihood rescale(final double factor) {
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
            return new DiffuseLikelihood(scalingFactor, nobs, nd, ssqerr / factor * factor, ldet, dcorr, nres, legacy);
        }
    }

    public double getDiffuseCorrection() {
        return dcorr;
    }
    
            public DiffuseLikelihood add(ILikelihood ll) {
        return DiffuseLikelihood.builder(nobs+ll.dim(), nd)
                .ssqErr(ssqerr+ll.ssq())
                .logDeterminant(ldet+ll.logDeterminant())
                .diffuseCorrection(dcorr)
                .legacy(legacy)
                .concentratedScalingFactor(scalingFactor)
                .build();
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

/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.dk;

import demetra.design.IBuilder;
import demetra.likelihood.IConcentratedLikelihood;
import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class DkConcentratedLikelihood implements IConcentratedLikelihood {

    public static Builder builder(int n, int nd) {
        return new Builder(n, nd);
    }

    public static class Builder implements IBuilder<DkConcentratedLikelihood> {

        final int n, nd;
        double ssqerr, ldet, lddet;
        double[] res;
        boolean legacy;
        double[] b;
        Matrix bvar;

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

        public Builder residuals(Doubles residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                this.ssqerr = residuals.ssq();
            }
            this.res = residuals.toArray();
            return this;
        }

        public Builder coefficients(Doubles coeff) {
            if (coeff != null) {
                b = new double[coeff.length()];
                coeff.copyTo(res, n);
            }
            return this;
        }

        public Builder covariance(Matrix var) {
            bvar = var;
            return this;
        }

        @Override
        public DkConcentratedLikelihood build() {
            return new DkConcentratedLikelihood(n, nd, ssqerr, ldet, lddet, b, bvar, res, legacy);
        }
    }

    private final double ll, ssqerr, ldet, lddet;
    private final int nobs, nd;
    private final double[] res;
    private final double[] b;
    private final Matrix bvar;
    private boolean legacy;

    private DkConcentratedLikelihood(final int n, final int nd, final double ssqerr, final double ldet, final double lddet,
            final double[] b, final Matrix bvar, final double[] res, boolean legacy) {
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
        this.b = b;
        this.bvar = bvar;
    }

    /**
     * Returns the number of degrees of freedom used in the computation of the
     * different variance/standard deviations
     *
     * @param unbiased True if ML estimates are used, false otherwise.
     * @param hpcount Number of hyper-paraneters that should be taken into
     * account. hpcount is not considered if unbiased is set to false.
     * @return
     */
    @Override
    public int getDegreesOfFreedom(boolean unbiased, int hpcount) {
        int n = getN();
        if (unbiased) {
            n -= hpcount;
            if (b != null) {
                n -= b.length;
            }
        }
        return n;
    }

    /**
     * Number of regression variables
     *
     * @return
     */
    @Override
    public int getNx() {
        return b == null ? 0 : b.length;
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
    public DkConcentratedLikelihood setLegacy(boolean legacy) {
        if (this.legacy == legacy) {
            return this;
        } else {
            return new DkConcentratedLikelihood(nobs, nd, ssqerr, ldet, lddet, b, bvar, res, legacy);
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
    public double getFactor() {
        return Math.exp((ldet + lddet) / (m()));
    }

    @Override
    public double getLogLikelihood() {
        return ll;
    }

    @Override
    public int getN() {
        return nobs;
    }

    @Override
    public Doubles getResiduals() {
        return res == null ? null : Doubles.of(res);
    }

    @Override
    public double getLogDeterminant() {
        return ldet;
    }

    /**
     *
     * @return
     */
    @Override
    public double getSer() {
        return Math.sqrt(ssqerr / (m()));
    }

    @Override
    public double getSigma() {
        return ssqerr / (m());
    }

    @Override
    public double getSsqErr() {
        return ssqerr;
    }

    @Override
    public Doubles getCoefficients() {
        return Doubles.of(b);
    }

    @Override
    public Matrix getCoefficientsCovariance() {
        return bvar;
    }
    /**
     * Adjust the likelihood if the toArray have been pre-multiplied by a given
 scaling factor
     *
     * @param yfactor
     * @param xfactor
     * @return
     */
    public DkConcentratedLikelihood rescale(final double yfactor, double[] xfactor) {
        double[] nres = res;
        if (yfactor != 1) {
            if (res != null) {
                nres = new double[res.length];
                for (int i = 0; i < res.length; ++i) {
                    nres[i] = Double.isFinite(res[i]) ? res[i] / yfactor : Double.NaN;
                }
            }
        }
        double[] nb = null;
        Matrix nbvar = null;
        if (b != null && xfactor != null) {
            nb = new double[b.length];
            nbvar = bvar.deepClone();
            for (int i = 0; i < b.length; ++i) {
                double ifactor = xfactor[i] / yfactor;
                nb[i] = b[i] * ifactor;
                for (int j = 0; j < i; ++j) {
                    double ijfactor = ifactor * xfactor[j] / yfactor;
                    bvar.apply(i, j, x -> x * ijfactor);
                    bvar.apply(j, i, x -> x * ijfactor);
                }
                bvar.apply(i, i, x -> x * ifactor * ifactor);
            }
        }
        return new DkConcentratedLikelihood(nobs, nd, ssqerr / yfactor * yfactor, ldet, lddet, nb, nbvar, nres, legacy);
    }

    public double getDiffuseCorrection() {
        return lddet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ll=").append(this.getLogLikelihood()).append(System.lineSeparator());
        builder.append("n=").append(this.getN()).append(System.lineSeparator());
        builder.append("ssq=").append(this.getSsqErr()).append(System.lineSeparator());
        builder.append("ldet=").append(this.getLogDeterminant()).append(System.lineSeparator());
        builder.append("dcorr=").append(this.getDiffuseCorrection()).append(System.lineSeparator());
        return builder.toString();
    }
}

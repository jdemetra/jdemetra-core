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
package demetra.likelihood;

import java.util.function.Supplier;
import demetra.design.BuilderPattern;
import demetra.eco.EcoException;
import demetra.maths.Constants;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class DiffuseConcentratedLikelihood implements IConcentratedLikelihood {

    public static Builder builder(int n, int nd) {
        return new Builder(n, nd);
    }

    @BuilderPattern(DiffuseConcentratedLikelihood.class)
    public static class Builder {

        private final int n, nd;
        private double ssqerr, ldet, lddet;
        private double[] res;
        private boolean legacy;
        private double[] b;
        private Matrix bvar;
        private boolean scalingFactor = true;

        Builder(int n, int nd) {
            this.n = n;
            this.nd = nd;
        }

        public Builder scalingFactor(boolean scalingFactor) {
            this.scalingFactor = scalingFactor;
            return this;
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

        public Builder residuals(DoubleSeq residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                this.ssqerr = residuals.ssq();
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

        public DiffuseConcentratedLikelihood build() {
            return new DiffuseConcentratedLikelihood(this);
        }
    }

    private final double ll, ssqerr, ldet, lddet;
    private final int nobs, nd;
    private final double[] res;
    private final double[] b;
    private final Matrix bvar;
    private final boolean legacy;
    private final boolean scalingFactor;

    private DiffuseConcentratedLikelihood(final int n, final int nd, final double ssqerr, final double ldet, final double lddet,
            final double[] b, final Matrix bvar, final double[] res, final boolean legacy, final boolean scalingFactor) {
        this.nobs = n;
        this.nd = nd;
        this.ssqerr = ssqerr;
        this.ldet = ldet;
        this.lddet = lddet;
        this.res = res;
        this.legacy = legacy;
        int m = nobs - nd, nc = legacy ? nobs : m;
        if (m > 0) {
            if (scalingFactor) {
                ll = -.5
                        * (nc * Constants.LOGTWOPI + m
                        * (1 + Math.log(ssqerr / m)) + ldet + lddet);
            } else {
                ll = -.5 * (nc * Constants.LOGTWOPI + ssqerr + ldet + lddet);
            }
        } else {
            ll = Double.NaN;
        }
        this.b = b;
        this.bvar = bvar;
        this.scalingFactor = scalingFactor;
    }

    private DiffuseConcentratedLikelihood(final Builder builder) {
        this(builder.n, builder.nd, builder.ssqerr, builder.ldet, builder.lddet,
                builder.b, builder.bvar, builder.res, builder.legacy, builder.scalingFactor);
    }

    public int ndiffuse() {
        return nd;
    }

    /**
     * Number ofFunction regression variables
     *
     * @return
     */
    @Override
    public int nx() {
        return b == null ? 0 : b.length;
    }

    private int m() {
        return legacy ? nobs : nobs - nd;
    }

    /**
     *
     * @param legacy legacy=true should be used only for testing purposes
     * @return
     */
    public DiffuseConcentratedLikelihood legacy(boolean legacy) {
        if (this.legacy == legacy) {
            return this;
        } else {
            return new DiffuseConcentratedLikelihood(nobs, nd, ssqerr, ldet, lddet, b, bvar, res, legacy, scalingFactor);
        }
    }

    @Override
    public double factor() {
        if (!scalingFactor) {
            throw new EcoException(EcoException.UNEXPECTEDOPERATION);
        }
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

    @Override
    public DoubleSeq coefficients() {
        return DoubleSeq.of(b);
    }

    @Override
    public double coefficient(int idx) {
        return b[idx];
    }

    @Override
    public Matrix unscaledCovariance() {
        return bvar;
    }

    @Override
    public boolean isScalingFactor() {
        return scalingFactor;
    }

    /**
     * Adjust the likelihood if the toArray have been pre-multiplied by a given
     * scaling factor
     *
     * @param yfactor
     * @param xfactor
     * @return
     */
    public DiffuseConcentratedLikelihood rescale(final double yfactor, double[] xfactor) {
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
        if (b != null) {
            int nx = b.length;
            if (xfactor != null) {
                nb = new double[nx];
                double[] nbv = bvar.toArray();
                for (int i = 0; i < nx; ++i) {
                    double ifactor = xfactor[i];
                    nb[i] = b[i] * ifactor / yfactor;
                    for (int j = 0; j < i; ++j) {
                        double ijfactor = ifactor * xfactor[j];
                        nbv[i + j * nx] *= ijfactor;
                        nbv[j + i * nx] *= ijfactor;
                    }
                    nbv[i * (nx + 1)] *= ifactor * ifactor;
                }
                nbvar = Matrix.ofInternal(nbv, nx, nx);
            } else if (yfactor != 1) {
                nb = new double[nx];
                for (int i = 0; i < nx; ++i) {
                    nb[i] = b[i] / yfactor;
                }
            } else {
                nb = b;
            }
        }
        double nldet = ldet;
        if (!scalingFactor) {
            nldet += (nobs - nd) * Math.log(yfactor);
        }
        double nlddet = lddet;
        if (!scalingFactor) {
            nlddet += nd * Math.log(yfactor);
        }
        return new DiffuseConcentratedLikelihood(nobs, nd, ssqerr / (yfactor * yfactor), nldet, nlddet, nb, nbvar, nres, legacy, scalingFactor);
    }

    public double diffuseCorrection() {
        return lddet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ll=").append(this.logLikelihood()).append(System.lineSeparator());
        builder.append("n=").append(this.dim()).append(System.lineSeparator());
        builder.append("ssq=").append(this.ssq()).append(System.lineSeparator());
        builder.append("ldet=").append(this.logDeterminant()).append(System.lineSeparator());
        builder.append("dcorr=").append(this.diffuseCorrection()).append(System.lineSeparator());
        return builder.toString();
    }
}

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
package demetra.likelihood;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.eco.EcoException;
import demetra.maths.Constants;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
final class InternalDiffuseConcentratedLikelihood implements DiffuseConcentratedLikelihood{
    
    private final double ll, ssqerr, ldet, lddet;
    private final int nobs, nd;
    private final double[] res;
    private final double[] b;
    private final Matrix bvar;
    private final boolean legacy;
    private final boolean scalingFactor;

    InternalDiffuseConcentratedLikelihood(final int n, final int nd, final double ssqerr, final double ldet, final double lddet,
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

    @Override
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
    @Override
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
        return new InternalDiffuseConcentratedLikelihood(nobs, nd, ssqerr / (yfactor * yfactor), nldet, nlddet, nb, nbvar, nres, legacy, scalingFactor);
    }

    @Override
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

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

import demetra.design.IBuilder;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;
import demetra.data.Doubles;
import java.util.function.Supplier;

/**
 * This class represents the concentrated likelihood of a linear regression
 * model.
 *
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihood implements IConcentratedLikelihood {

    public static Builder likelihood(int n) {
        return new Builder(n);
    }

    public static class Builder implements IBuilder<ConcentratedLikelihood> {

        final int n;
        double ssqerr, ldet;
        double[] res;
        double[] b;
        Matrix bvar;
        Supplier<Matrix> bvarFn;

        Builder(int n) {
            this.n = n;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder ssqErr(double ssq) {
            this.ssqerr = ssq;
            return this;
        }

        public Builder residuals(Doubles residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                ssqerr = residuals.ssq();
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

        public Builder unscaledCovariance(Matrix var) {
            bvar = var;
            return this;
        }

        public Builder unscaledCovarianceSupplier(Supplier<Matrix> s) {
            bvarFn = s;
            return this;
        }

        @Override
        public ConcentratedLikelihood build() {
            return new ConcentratedLikelihood(this);
        }
    }

    private final double ll, ssqerr, ldet;
    private final int n;
    private final double[] res;
    private final double[] b;
    private volatile Matrix bvar;
    private final Supplier<Matrix> bvarFn;

    private ConcentratedLikelihood(final int n, final double ssqerr, final double ldet, final double[] b, final Matrix bvar, final Supplier<Matrix> s, final double[] res) {
        this.n = n;
        this.ldet = ldet;
        this.ssqerr = ssqerr;
        this.b = b;
        this.bvar = bvar;
        this.bvarFn = s;
        this.res = res;
        this.ll = -.5
                * (n * Math.log(2 * Math.PI) + n
                * (1 + Math.log(ssqerr / n)) + ldet);
    }

    private ConcentratedLikelihood(Builder builder) {
        this.n = builder.n;
        this.ldet = builder.ldet;
        this.ssqerr = builder.ssqerr;
        this.b = builder.b;
        this.bvar = builder.bvar;
        this.bvarFn = builder.bvarFn;
        this.res = builder.res;
        this.ll = -.5
                * (n * Math.log(2 * Math.PI) + n
                * (1 + Math.log(ssqerr / n)) + ldet);
    }

    @Override
    public double logDeterminant() {
        return ldet;
    }

    /**
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
    public Doubles e() {
        return Doubles.of(res);
    }

    @Override
    public Doubles coefficients() {
        return Doubles.of(b);
    }

    @Override
    public Matrix unscaledCovariance() {
        Matrix tmp = bvar;
        if (tmp == null && bvarFn != null) {
            synchronized (this) {
                tmp = bvar;
                if (tmp == null) {
                    tmp = bvarFn.get();
                    bvar = tmp;
                }
            }
        }
        return bvar;
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
     * @param yfactor The scaling factor
     * @param xfactor
     * @return
     */
    public ConcentratedLikelihood rescale(final double yfactor, double[] xfactor) {
        if (yfactor == 1) {
            return this;
        }
        double nssqerr = ssqerr / yfactor * yfactor;
        double[] nres = null;
        if (res != null) {
            nres = new double[res.length];
            for (int i = 0; i < res.length; ++i) {
                nres[i] = res[i] / yfactor;
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
        return new ConcentratedLikelihood(n, nssqerr, ldet, nb, nbvar, null, nres);

    }
}

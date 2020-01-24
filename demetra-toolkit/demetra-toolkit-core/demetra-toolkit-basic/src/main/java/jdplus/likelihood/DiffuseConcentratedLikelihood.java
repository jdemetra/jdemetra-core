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

import demetra.design.BuilderPattern;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public interface DiffuseConcentratedLikelihood extends ConcentratedLikelihood {

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
        private MatrixType bvar;
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

        public Builder unscaledCovariance(MatrixType var) {
            bvar = var;
            return this;
        }

        public DiffuseConcentratedLikelihood build() {
            return new InternalDiffuseConcentratedLikelihood(n, nd, ssqerr, 
                    ldet, lddet, b, bvar, res, legacy, scalingFactor);
        }
    }

    int ndiffuse();

    double diffuseCorrection();

    /**
     * Adjust the likelihood if the toArray have been pre-multiplied by a given
     * scaling factor
     *
     * @param yfactor
     * @param xfactor
     * @return
     */
    DiffuseConcentratedLikelihood rescale(final double yfactor, double[] xfactor);

    @Override
    default int degreesOfFreedom(){
        return dim()-nx()-ndiffuse();
    }

}

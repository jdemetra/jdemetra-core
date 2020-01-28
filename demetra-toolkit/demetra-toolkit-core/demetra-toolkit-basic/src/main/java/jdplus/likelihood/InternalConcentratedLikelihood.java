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

import demetra.design.Immutable;
import demetra.math.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
final class InternalConcentratedLikelihood implements ConcentratedLikelihood {

    /**
     * n = number of actual observations, nmissing = number of missing values
     */
    private final int n;
    private final double ll, ssqerr, ldet;
    private final double[] res;
    private final double[] b;
    private final Matrix bvar;
    private final boolean scalingFactor;

    InternalConcentratedLikelihood(final int n, final double ssqerr, final double ldet, final double[] res,
            final double[] b, final Matrix bvar, final boolean scalingFactor) {
        this.n = n;
        this.ldet = ldet;
        this.ssqerr = ssqerr;
        this.b = b;
        this.bvar = bvar;
        this.res = res;
        this.scalingFactor = scalingFactor;
        if (scalingFactor) {
            this.ll = -.5
                    * (n * Constants.LOGTWOPI + n
                    * (1 + Math.log(ssqerr / n)) + ldet);
        } else {
            this.ll = -.5 * (n * Constants.LOGTWOPI + ssqerr + ldet);
        }
    }

    @Override
    public boolean isScalingFactor() {
        return scalingFactor;
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

    /**
     * Number of coefficients (excluding missing value estimates)
     *
     * @return
     */
    @Override
    public int nx() {
        return b.length;
    }

    @Override
    public DoubleSeq e() {
        return DoubleSeq.of(res);
    }

    @Override
    @NonNull
    public DoubleSeq coefficients() {
        return DoubleSeq.of(b);
    }

    @Override
    public double coefficient(int pos) {
        return b[pos];
    }

    @Override
    @NonNull
    public Matrix unscaledCovariance() {
        if (bvar == null) {
            return Matrix.EMPTY;
        } else {
            return bvar;
        }
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

    @Override
    public int degreesOfFreedom() {
        return n - nx();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("log-likelihood: ").append(this.ll)
                .append(", sigma2: ").append(this.ssqerr / this.n);

        return builder.toString();

    }
}

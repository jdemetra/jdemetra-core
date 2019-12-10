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
package jdplus.linearmodel;

import jdplus.data.DataBlock;
import demetra.design.Immutable;
import jdplus.dstats.F;
import jdplus.dstats.T;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;
import demetra.design.BuilderPattern;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class LeastSquaresResults {

    @BuilderPattern(LeastSquaresResults.class)
    public static class Builder {

        private Builder(DoubleSeq y, final Matrix X) {
            this.y = y;
            this.X = X;
        }

        public Builder mean(boolean mu) {
            this.mean = mu;
            return this;
        }

        public Builder estimation(DoubleSeq coefficients, Matrix ucov) {
            this.coefficients = coefficients;
            this.ucov = ucov;
            return this;
        }

        public Builder ssq(double ssq) {
            this.ssq = ssq;
            return this;
        }

        public Builder residuals(DoubleSeq res) {
            this.res = res;
            if (ssq == 0) {
                ssq = res.ssq();
            }
            return this;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        private final DoubleSeq y;
        private final Matrix X;
        private boolean mean;
        private DoubleSeq coefficients, res;
        private double ssq, ldet;
        private Matrix ucov;

        public LeastSquaresResults build() {
            return new LeastSquaresResults(y, X, mean, coefficients, ucov, ssq, ldet);
        }
    }

    public static Builder builder(@NonNull DoubleSeq Y, Matrix X) {
        return new Builder(Y, X);
    }

    private LeastSquaresResults(DoubleSeq Y, Matrix X, boolean mean, DoubleSeq coefficients, Matrix unscaledCov, double ssq, double ldet) {
        this.y = Y;
        this.X = X;
        this.mean = mean;
        this.coefficients = coefficients;
        this.ssq = ssq;
        this.ldet = ldet;
        this.n = y.length();
        this.ucov = unscaledCov;
        this.nx = ucov == null ? 0 : ucov.diagonal().count(x -> x != 0);
        // compute auxiliaries
        y2 = y.ssq();
        ym = y.average();
        bxy = y2 - ssq;
    }

    private final DoubleSeq y;
    private final Matrix X;
    private final boolean mean;
    private final int n, nx;
    private final DoubleSeq coefficients;
    private final double ssq, ldet;
    private final Matrix ucov;
    // auxiliary results
    private final double y2, ym, bxy;

    /**
     * @return the coefficients
     */
    public DoubleSeq getCoefficients() {
        return coefficients;
    }

    /**
     * SSe
     *
     * @return
     */
    public double getResidualSumOfSquares() {
        return ssq;
    }

    /**
     * MSe = SSe/(n-p)
     *
     * @return
     */
    public double getResidualMeanSquare() {
        return ssq / (n - nx);
    }

    /**
     * s
     *
     * @return the ser
     */
    public double getResidualStandardDeviation() {
        return Math.sqrt(ssq / (n - nx));
    }

    /**
     *
     * @return the r2
     */
    public double getR2() {
        if (mean) {
            return 1 - getResidualSumOfSquares() / (y2 - n * ym * ym);
        } else {
            return 1 - getResidualSumOfSquares() / y2;
        }
    }

    /**
     * @return the adjustedR2
     */
    public double getAdjustedR2() {
        return 1 - (n - 1) * (1 - getR2()) / (n - nx);
    }

    public double getRegressionSumOfSquares() {
        if (mean) {
            return bxy - n * ym * ym;
        } else {
            return bxy;
        }
    }

    public double getRegressionMeanSquare() {
        if (mean) {
            return (bxy - n * ym * ym) / (nx - 1);
        } else {
            return bxy / nx;
        }
    }

    private int degreesOfFreedom() {
        return mean ? nx - 1 : nx;
    }

    public StatisticalTest Ftest() {
        F f = new F(degreesOfFreedom(), n - nx);
        return new StatisticalTest(f, getRegressionMeanSquare() / getResidualMeanSquare(), TestType.Upper, false);
    }

    /**
     * Computes a Joint F-Test on the n variables starting at the given position
     *
     * @param v0 Position of the first variable
     * @param nvars Number of variables
     * @return
     */
    public StatisticalTest Ftest(int v0, int nvars) {
        Matrix bvar = ucov.extract(v0, nvars, v0, nvars).deepClone();
        SymmetricMatrix.lcholesky(bvar);
        DataBlock b = DataBlock.of(coefficients.extract(v0, nvars));
        LowerTriangularMatrix.solveLx(bvar, b);
        double fval = b.ssq() / nvars / (ssq / (n - nx));
        F f = new F(nvars, n - nx);
        return new StatisticalTest(f, fval, TestType.Upper, false);

    }

    /**
     * @return the covariance matrix of the coefficients
     */
    public Matrix covariance() {
        return ucov.times(ssq / (n - nx));
    }

    public double standardDeviation(int idx) {
        double v = ucov.get(idx, idx);
        return Math.sqrt(ssq / (n - nx) * v);
    }

    public double T(int idx) {
        double e = ucov.get(idx, idx);
        if (e == 0) {
            return Double.NaN;
        }
        double b = coefficients.get(idx);
        if (b == 0) {
            return 0;
        }
        return b / Math.sqrt(e * ssq / (n - nx));
    }

    public StatisticalTest Ttest(int idx) {
        double t = T(idx);
        if (!Double.isFinite(t)) {
            return null;
        } else {
            return new StatisticalTest(new T(n - nx), t, TestType.TwoSided, false);
        }
    }

    /**
     * @return the likelihood
     */
    public ConcentratedLikelihoodWithMissing getLikelihood() {
        return ConcentratedLikelihoodWithMissing.builder()
                .ndata(n)
                .coefficients(coefficients)
                .unscaledCovariance(ucov)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("R2=").append(getR2());
        builder.append(System.lineSeparator());
        builder.append("Adjusted R2=").append(getAdjustedR2());
        builder.append(System.lineSeparator());
        builder.append("Residual standard deviation=").append(getResidualStandardDeviation());
        builder.append(System.lineSeparator());
        builder.append("F=").append(Ftest().getValue());
        builder.append(System.lineSeparator());
        int idx = 0;
        if (mean) {
            builder.append(System.lineSeparator());
            builder.append("mean").append('\t').append(coefficients.get(0))
                    .append('\t').append(standardDeviation(idx));
            idx++;
        }
        for (int j = 1; idx < coefficients.length(); ++idx, ++j) {
            double c = coefficients.get(idx);
            if (c != 0) {
                builder.append(System.lineSeparator());
                builder.append("x").append(j).append('\t').append(c)
                        .append('\t').append(standardDeviation(idx));
            }
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }

}

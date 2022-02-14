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
package jdplus.stats.linearmodel;

import jdplus.data.DataBlock;
import nbbrd.design.Immutable;
import jdplus.dstats.F;
import jdplus.dstats.T;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.stats.TestType;
import nbbrd.design.BuilderPattern;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.stats.StatisticalTest;
import jdplus.data.DataBlockIterator;
import jdplus.dstats.Chi2;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.stats.tests.TestsUtility;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class LeastSquaresResults {

    @BuilderPattern(LeastSquaresResults.class)
    public static class Builder {

        private Builder(DoubleSeq y, final FastMatrix X) {
            this.y = y;
            this.X = X;
        }

        public Builder mean(boolean mu) {
            this.mean = mu;
            return this;
        }

        public Builder estimation(DoubleSeq coefficients, FastMatrix ucov) {
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
        private final FastMatrix X;
        private boolean mean;
        private DoubleSeq coefficients, res;
        private double ssq, ldet;
        private FastMatrix ucov;

        public LeastSquaresResults build() {
            return new LeastSquaresResults(y, X, mean, coefficients, ucov, ssq, ldet);
        }
    }

    public static Builder builder(@NonNull DoubleSeq Y, FastMatrix X) {
        return new Builder(Y, X);
    }

    private LeastSquaresResults(DoubleSeq Y, FastMatrix X, boolean mean, DoubleSeq coefficients, FastMatrix unscaledCov, double ssq, double ldet) {
        this.y = Y;
        this.X = X;
        this.mean = mean;
        this.coefficients = coefficients;
        this.ldet = ldet;
        this.n = y.length();
        this.ucov = unscaledCov;
        // nx contains the intercept !!
        this.nx = ucov == null ? 0 : ucov.diagonal().count(x -> x != 0);
        // compute auxiliaries
        if (mean) {
            double ybar = y.average();
            sst = y.ssqc(ybar);
        } else {
            sst = y.ssq();
        }
        this.sse=Math.min(ssq, sst); // be carefull with dummy models
        ssm = sst - sse; 
    }

    private final DoubleSeq y;
    private final FastMatrix X;
    private final boolean mean;
    private final int n, nx;
    private final DoubleSeq coefficients;
    private final double sse, ldet;
    /**
     * (X'X)^-1
     */
    private final FastMatrix ucov;
    // auxiliary results
    private final double sst, ssm;

    public DoubleSeq getY() {
        return y;
    }

    /**
     * Regression variables (including the constant when it is used)
     *
     * @return
     */
    public Matrix X() {
        return X.unmodifiable();
    }

    /**
     * Gets X(X'X)^-1X'
     *
     * @return
     */
    public FastMatrix projectionMatrix() {
        return SymmetricMatrix.XSXt(ucov, X);
    }

    public boolean isMean() {
        return mean;
    }

    /**
     * Returns X*b
     *
     * @return
     */
    public DoubleSeq regressionEffect() {
        DataBlock e = DataBlock.make(y.length());
        DoubleSeqCursor c = coefficients.cursor();
        DataBlockIterator cols = X.columnsIterator();
        while (cols.hasNext()) {
            e.addAY(c.getAndNext(), cols.next());
        }
        return e.unmodifiable();
    }

    /**
     * Returns e=y-Xb
     *
     * @return
     */
    public DoubleSeq residuals() {
        DataBlock e = DataBlock.of(y);
        DoubleSeqCursor c = coefficients.cursor();
        DataBlockIterator cols = X.columnsIterator();
        while (cols.hasNext()) {
            e.addAY(-c.getAndNext(), cols.next());
        }
        return e.unmodifiable();
    }

    public DoubleSeq studentizedResiduals() {
        DoubleSeq e = residuals();
        double[] v = new double[e.length()];
        double sig = getErrorStandardDeviation();
        DataBlockIterator rows = X.rowsIterator();
        DoubleSeqCursor cursor = e.cursor();
        for (int i = 0; i < v.length; ++i) {
            v[i] = cursor.getAndNext() / (sig * Math.sqrt(1 - QuadraticForm.apply(ucov, rows.next())));
        }
        return DoubleSeq.of(v);
    }

    /**
     * @return the coefficients
     */
    public DoubleSeq getCoefficients() {
        return coefficients;
    }

    /**
     * MSe = SSe/(n-p)
     *
     * @return
     */
    public double getErrorMeanSquares() {
        return sse / degreesOfFreedomForError();
    }

    public double getErrorStandardDeviation() {
        return Math.sqrt(getErrorMeanSquares());
    }
    /**
     * ssm/degrees of freedom for model
     * @return 
     */
    public double getModelMeanSquares() {
        return ssm / degreesOfFreedomForModel();
    }

   /**
     * sst/degrees of freedom for total
     * @return 
     */
    public double getTotalMeanSquares() {
        return sst / degreesOfFreedomForTotal();
    }
    
    /**
     * sum(y-ybar)^2 (or SST = SSR + SSE)
     *
     * @return
     */
    public double getTotalSumOfSquares() {
        return sst;
    }

    /**
     * SSE
     *
     * @return
     */
    public double getErrorSumOfSquares() {
        return sse;
    }

   /**
     * SSM
     *
     * @return
     */
    public double getModelSumOfSquares() {
        return ssm;
    }
    /**
     *
     * @return the r2
     */
    public double getR2() {
        return 1 - sse / sst;
    }

    /**
     * @return the adjustedR2
     */
    public double getAdjustedR2() {
        return 1 - degreesOfFreedomForTotal() * (1 - getR2()) / degreesOfFreedomForError();
    }

 
    private int degreesOfFreedomForModel() {
        return mean ? nx - 1 : nx;
    }

    private int degreesOfFreedomForError() {
        return n - nx;
    }

    private int degreesOfFreedomForTotal() {
        return mean ? n - 1 : n;
    }

    public StatisticalTest Ftest() {
        F f = new F(degreesOfFreedomForModel(), degreesOfFreedomForError());
        return TestsUtility.testOf(getModelMeanSquares()/ getErrorMeanSquares(), f, TestType.Upper);
    }

    public StatisticalTest Khi2Test() {
        Chi2 chi = new Chi2(degreesOfFreedomForModel());
        return TestsUtility.testOf(n * getR2(), chi, TestType.Upper);
    }

    /**
     * Computes a Joint F-Test on the n variables starting at the given position
     *
     * @param v0 Position of the first variable
     * @param nvars Number of variables
     * @return
     */
    public StatisticalTest Ftest(int v0, int nvars) {
        FastMatrix bvar = ucov.extract(v0, nvars, v0, nvars).deepClone();
        SymmetricMatrix.lcholesky(bvar);
        DataBlock b = DataBlock.of(coefficients.extract(v0, nvars));
        LowerTriangularMatrix.solveLx(bvar, b);
        double fval = b.ssq() / nvars / getErrorMeanSquares();
        F f = new F(nvars, degreesOfFreedomForError());
        return TestsUtility.testOf(fval, f, TestType.Upper);

    }

    /**
     * @return the covariance matrix of the coefficients
     */
    public FastMatrix covariance() {
        return ucov.times(sse / (n - nx));
    }

    public double standardDeviation(int idx) {
        double v = ucov.get(idx, idx);
        return Math.sqrt(getErrorMeanSquares() * v);
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
        return b / Math.sqrt(e * getErrorMeanSquares());
    }

    public StatisticalTest Ttest(int idx) {
        double t = T(idx);
        if (!Double.isFinite(t)) {
            return null;
        } else {
            return TestsUtility.testOf(t, new T(degreesOfFreedomForError()), TestType.TwoSided);
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
                .ssqErr(sse)
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
        builder.append("Residual standard deviation=").append(getErrorStandardDeviation());
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

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
import nbbrd.design.BuilderPattern;
import jdplus.dstats.F;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixFactory;

/**
 * TODO: create tests with robust covariance matrix
 * @author Jean Palate
 */
@BuilderPattern(StatisticalTest.class)
public class JointTest {

    private final DoubleSeq b;
    private final Matrix bvar;
    private final double rss;
    private final int n;
    private int hyperParameters;
    private Matrix R;
    private DoubleSeq alpha;
    private int[] coef;
    private boolean blue = true, deterministicRegressors = true;

    public JointTest(final DoubleSeq coefficients, final Matrix unscaledVariance, final double rss, final int n) {
        this.b = coefficients;
        this.bvar = unscaledVariance;
        this.rss = rss;
        this.n = n;
    }

    public JointTest(final ConcentratedLikelihoodWithMissing ll) {
        this.b = ll.coefficients();
        this.bvar = Matrix.of(ll.unscaledCovariance());
        this.rss = ll.ssq();
        this.n = ll.dim();
    }

    public JointTest variableSelection(int[] variableSelection) {
        this.coef = variableSelection;
        R = null;
        alpha = null;
        return this;
    }

    public JointTest variableSelection(int start, int n) {
        this.coef = new int[n];
        for (int i=0; i<n; ++i){
            coef[i]=start+i;
        }
        R = null;
        alpha = null;
        return this;
    }

    public JointTest constraints(@NonNull Matrix R, @NonNull DoubleSeq alpha) {
        if (R.getRowsCount() != alpha.length()) {
            throw new IllegalArgumentException();
        }
        this.R = R;
        this.alpha = alpha;
        coef = null;
        return this;
    }

    public JointTest ml() {
        this.blue = false;
        return this;
    }

    public JointTest blue() {
        this.blue = true;
        return this;
    }

    public JointTest hyperParametersCount(int nhp) {
        this.hyperParameters = nhp;
        return this;
    }

    public JointTest deterministicRegressors(boolean det) {
        this.deterministicRegressors=det;
        return this;
    }

    public StatisticalTest build() {
        final double f;
        DataBlock rb = rb();
        Matrix rwr = rwr();
        int nx = rb.length(), df = df();
        SymmetricMatrix.lcholesky(rwr);
        LowerTriangularMatrix.solveLx(rwr, rb);
        f = (rb.ssq() / nx) / (rss / df);
        F fdist = new F(nx, df);
        return new StatisticalTest(fdist, f, TestType.Upper, !deterministicRegressors);
    }

    private Matrix rwr() {
        if (coef != null) {
            return MatrixFactory.select(bvar, coef, coef);
        } else if (R != null){
            return SymmetricMatrix.XSXt(bvar, R);
        } else{
            return bvar.deepClone();
        }
    }

    private DataBlock rb() {
        double[] rb;
        if (coef != null) {
            rb = new double[coef.length];
            for (int i = 0; i < rb.length; ++i) {
                rb[i] = b.get(coef[i]);
            }
        } else if (R != null){
            rb = new double[R.getRowsCount()];
            for (int i = 0; i < rb.length; ++i) {
                rb[i] = R.row(i).dot(b) - alpha.get(i);
            }
        }else{
            rb=b.toArray();
        }
        return DataBlock.of(rb);
    }

    private int df() {
        if (blue) {
            return n - bvar.getRowsCount() - hyperParameters;
        } else {
            return n;
        }
    }

}

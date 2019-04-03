/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.linearmodel;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.design.BuilderPattern;
import demetra.dstats.F;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;

/**
 *
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

    public JointTest constraints(@Nonnull Matrix R, @Nonnull DoubleSeq alpha) {
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
        LowerTriangularMatrix.rsolve(rwr, rb);
        f = (rb.ssq() / nx) / (rss / df);
        F fdist = new F(nx, df);
        return new StatisticalTest(fdist, f, TestType.Upper, !deterministicRegressors);
    }

    private Matrix rwr() {
        if (coef != null) {
            return bvar.select(coef, coef);
        } else {
            return SymmetricMatrix.XSXt(bvar, R);
        }
    }

    private DataBlock rb() {
        double[] rb;
        if (coef != null) {
            rb = new double[coef.length];
            for (int i = 0; i < rb.length; ++i) {
                rb[i] = b.get(coef[i]);
            }
        } else {
            rb = new double[R.getRowsCount()];
            for (int i = 0; i < rb.length; ++i) {
                rb[i] = Doubles.dot(R.row(i), b) - alpha.get(i);
            }
        }
        return DataBlock.ofInternal(rb);
    }

    private int df() {
        if (blue) {
            return n - bvar.getRowsCount() - hyperParameters;
        } else {
            return n;
        }
    }

}

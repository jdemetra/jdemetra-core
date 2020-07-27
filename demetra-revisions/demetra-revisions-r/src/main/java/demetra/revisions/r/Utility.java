/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.revisions.r;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoublesMath;
import demetra.math.matrices.MatrixType;
import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.Coefficient;
import demetra.revisions.parametric.OlsTest;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.parametric.RevisionAnalysis;
import demetra.stats.TestResult;
import java.time.LocalDate;
import jdplus.math.matrices.Matrix;
import jdplus.revisions.parametric.BiasComputer;
import jdplus.revisions.parametric.OlsTestComputer;
import jdplus.stats.StatUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {

    /**
     * Theil coefficients computed on the columns of the vintages matrix
     *
     * @param vintages Vintages
     * @param gap Delay between the compared vintages (should be &ge 1)
     * @return
     */
    public double[] theil(MatrixType vintages, int gap) {
        if (gap < 1) {
            throw new IllegalArgumentException("gap should be >= 1");
        }
        int n = vintages.getColumnsCount() - gap;
        if (n <= 0) {
            return null;
        }
        double[] u = new double[n];
        for (int i = 0; i < n; ++i) {
            u[i] = StatUtility.theilInequalityCoefficient(vintages.column(i + gap), vintages.column(i));
        }
        return u;
    }

    private final int OLS = 16, C = 3;

    /**
     * v(t)=a+b*v(t-gap)
     *
     * @param vintages Vintages
     * @param gap Delay between the compared vintages (should be &ge 1)
     * @return
     */
    public MatrixType slopeAndDrift(MatrixType vintages, int gap) {
        if (gap < 1) {
            throw new IllegalArgumentException("gap should be >= 1");
        }
        int n = vintages.getColumnsCount() - gap;
        if (n <= 0) {
            return null;
        }
        Matrix rslt = Matrix.make(n, OLS + 2 * C);

        for (int i = 0; i < n; ++i) {
            DoubleSeqCursor.OnMutable cursor = rslt.row(i).cursor();
            OlsTest test = OlsTestComputer.of(vintages.column(i + gap), vintages.column(i));
            olsInformation(test, cursor);
        }
        return rslt;
    }

    /**
     * rev(t)=a+b*v(t-gap)
     *
     * @param vintages Vintages
     * @param gap Delay between the compared vintages (should be &ge 1)
     * @return
     */
    public MatrixType efficiencyModel1(MatrixType vintages, int gap) {
        if (gap < 1) {
            throw new IllegalArgumentException("gap should be >= 1");
        }
        int n = vintages.getColumnsCount() - gap;
        if (n <= 0) {
            return null;
        }
        Matrix rslt = Matrix.make(n, OLS + 2 * C);

        for (int i = 0; i < n; ++i) {
            DoubleSeq prev = vintages.column(i), cur = vintages.column(i + gap);
            DoubleSeqCursor.OnMutable cursor = rslt.row(i).cursor();
            OlsTest test = OlsTestComputer.of(DoublesMath.subtract(cur, prev), prev);
            olsInformation(test, cursor);
        }
        return rslt;
    }

    /**
     * rev(t)=a+b*rev(t-1)
     *
     * @param vintages Vintages
     * @param gap Delay between the vintages used to compute the revisions
     * (should be &ge 1)
     * @return
     */
    public MatrixType efficiencyModel2(MatrixType vintages, int gap) {
        int n = vintages.getColumnsCount() - gap - 1;
        Matrix rslt = Matrix.make(n, OLS + 2 * C);
        for (int i = 0; i < n; ++i) {
            DoubleSeqCursor.OnMutable cursor = rslt.row(i).cursor();
            try {
                OlsTest test = OlsTestComputer.of(DoublesMath.subtract(vintages.column(i + gap + 1), vintages.column(i + 1)),
                        DoublesMath.subtract(vintages.column(i + gap), vintages.column(i)));
                olsInformation(test, cursor);
            } catch (Exception err) {
            }
        }
        return rslt;
    }

    /**
     * rev(t)=a+b(1)*rev(t-1)+b(2)*rev(t-2)+...+b(nrevs)*rev(t-nrevs)
     *
     * @param revs
     * @param nrevs
     * @return
     */
    public MatrixType orthogonallyModel1(MatrixType revs, int nrevs) {
        int n = revs.getColumnsCount();
        if (nrevs >= n) {
            return null;
        }
        Matrix rslt = Matrix.make(n - nrevs, OLS + C * (1 + nrevs));
        DoubleSeq[] x = new DoubleSeq[nrevs];
        for (int i = nrevs; i < n; ++i) {
            for (int j = 0; j < nrevs; ++j) {
                x[j] = revs.column(i - j - 1);
            }
            DoubleSeqCursor.OnMutable cursor = rslt.row(i - nrevs).cursor();
            try {
                OlsTest test = OlsTestComputer.of(revs.column(i), x);
                olsInformation(test, cursor);
            } catch (Exception err) {
            }
        }
        return rslt;
    }

    /**
     *
     * @param revs
     * @param ref
     * @return
     */
    public MatrixType orthogonallyModel2(MatrixType revs, int ref) {
        int n = revs.getColumnsCount();
        if (ref >= n || ref < 1) {
            return null;
        }
        Matrix rslt = Matrix.make(n - 1, OLS + C * 2);
        DoubleSeq cref = revs.column(ref - 1);
        for (int i = 0, j = 0; i < n; ++i) {
            if (i != ref-1) {
                DoubleSeqCursor.OnMutable cursor = rslt.row(j++).cursor();
                try{
                OlsTest test = OlsTestComputer.of(revs.column(i), cref);
                olsInformation(test, cursor);
            } catch (Exception err) {
            }
            }
        }
        return rslt;
    }

    public double theil(RegressionBasedAnalysis<LocalDate> analysis, int k) {
        if (k > analysis.getRevisions().size()) {
            return Double.NaN;
        }
        return analysis.getRevisions().get(k - 1).getTheilCoefficient();
    }

    private static final int BIAS = 9;

    /**
     * Bias computed on a matrix of revisions (each column corresponds to a
     * revision)
     *
     * @param revs The revisions
     * @return
     */
    public MatrixType bias(MatrixType revs) {
        int n = revs.getColumnsCount();
        Matrix rslt = Matrix.make(n, BIAS);

        for (int i = 0; i < n; ++i) {
            DoubleSeq cur = revs.column(i);
            DoubleSeqCursor.OnMutable cursor = rslt.row(i).cursor();
            Bias bias = BiasComputer.of(cur);
            biasInformation(bias, cursor);
        }
        return rslt;
    }

    public void olsInformation(OlsTest reg, DoubleSeqCursor.OnMutable cursor) {
        if (reg == null) {
            return;
        }
        Coefficient[] c = reg.getCoefficients();
        TestResult jb = reg.getDiagnostics().getJarqueBera();
        TestResult bp = reg.getDiagnostics().getBreuschPagan();
        TestResult w = reg.getDiagnostics().getWhite();
        TestResult arch = reg.getDiagnostics().getArch();
        cursor.setAndNext(reg.getN());
        cursor.setAndNext(reg.getR2());
        cursor.setAndNext(reg.getF());
        for (int i = 0; i < c.length; ++i) {
            cursor.setAndNext(c[i].getEstimate());
            cursor.setAndNext(c[i].getStdev());
            cursor.setAndNext(c[i].getPvalue());
        }
        cursor.setAndNext(reg.getDiagnostics().getSkewness());
        cursor.setAndNext(reg.getDiagnostics().getKurtosis());
        cursor.setAndNext(jb.getValue());
        cursor.setAndNext(jb.getPvalue());
        cursor.setAndNext(reg.getDiagnostics().getBpr2());
        cursor.setAndNext(bp.getValue());
        cursor.setAndNext(bp.getPvalue());
        cursor.setAndNext(reg.getDiagnostics().getWr2());
        cursor.setAndNext(w.getValue());
        cursor.setAndNext(w.getPvalue());
        cursor.setAndNext(reg.getDiagnostics().getArchr2());
        cursor.setAndNext(arch.getValue());
        cursor.setAndNext(arch.getPvalue());
    }

    public double[] biasInformation(RegressionBasedAnalysis<LocalDate> analysis, int k) {
        if (k > analysis.getRevisions().size()) {
            return null;
        }
        RevisionAnalysis<LocalDate> cur = analysis.getRevisions().get(k - 1);
        if (cur == null) {
            return null;
        }
        Bias bias = cur.getBias();
        if (bias == null) {
            return null;
        }
        return new double[]{
            bias.getN(),
            bias.getMu(),
            bias.getSigma(),
            bias.getT(),
            bias.getTPvalue(),
            bias.getAr(),
            bias.getAdjustedSigma(),
            bias.getAdjustedT(),
            bias.getAdjustedTPvalue()};
    }

    public void biasInformation(Bias bias, DoubleSeqCursor.OnMutable cursor) {
        if (bias == null) {
            return;
        }
        cursor.setAndNext(bias.getN());
        cursor.setAndNext(bias.getMu());
        cursor.setAndNext(bias.getSigma());
        cursor.setAndNext(bias.getT());
        cursor.setAndNext(bias.getTPvalue());
        cursor.setAndNext(bias.getAr());
        cursor.setAndNext(bias.getAdjustedSigma());
        cursor.setAndNext(bias.getAdjustedT());
        cursor.setAndNext(bias.getAdjustedTPvalue());
    }

}

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
import jdplus.revisions.parametric.OlsTestComputer;
import jdplus.stats.StatUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {

    public double[] theil(MatrixType m) {
        double[] u = new double[m.getColumnsCount() - 1];
        DoubleSeq prev = m.column(0);
        for (int i = 0; i < u.length; ++i) {
            DoubleSeq cur = m.column(i + 1);
            u[i] = StatUtility.theilInequalityCoefficient(cur, prev);
            prev = cur;
        }
        return u;
    }

    /**
     * v(t)=a+b*v(t-1)
     *
     * @param m
     * @return
     */
    public MatrixType slopeAndDrift(MatrixType m) {
        int n = m.getColumnsCount();
        DoubleSeq prev = m.column(0);
        Matrix rslt = Matrix.make(n - 1, 22);

        for (int i = 1; i < n; ++i) {
            DoubleSeq cur = m.column(i);
            DoubleSeqCursor.OnMutable cursor = rslt.row(i - 1).cursor();
            OlsTest test = OlsTestComputer.of(cur, prev);
            olsInformation(test, cursor);
            prev = cur;
        }
        return rslt;
    }

    /**
     * rev(t)=a+b*v(t-1)
     *
     * @param m
     * @return
     */
    public MatrixType efficiencyModel1(MatrixType m) {
        int n = m.getColumnsCount();
        DoubleSeq prev = m.column(0);
        Matrix rslt = Matrix.make(n - 1, 22);

        for (int i = 1; i < n; ++i) {
            DoubleSeq cur = m.column(i);
            DoubleSeqCursor.OnMutable cursor = rslt.row(i - 1).cursor();
            OlsTest test = OlsTestComputer.of(DoublesMath.subtract(cur, prev), prev);
            olsInformation(test, cursor);
            prev = cur;
        }
        return rslt;
    }

    /**
     * rev(t)=a+b*rev(t-1)
     *
     * @param m
     * @return
     */
    public MatrixType efficiencyModel2(MatrixType m) {
        int n = m.getColumnsCount();
        DoubleSeq prev0 = m.column(0), prev1 = m.column(1);
        Matrix rslt = Matrix.make(n - 2, 22);
        for (int i = 2; i < n; ++i) {
            DoubleSeq cur = m.column(i);
            DoubleSeqCursor.OnMutable cursor = rslt.row(i - 1).cursor();
            OlsTest test = OlsTestComputer.of(DoublesMath.subtract(cur, prev1), DoublesMath.subtract(prev1, prev0));
            olsInformation(test, cursor);
            prev0 = prev1;
            prev1 = cur;
        }
        return rslt;
    }

    public double theil(RegressionBasedAnalysis<LocalDate> analysis, int k) {
        if (k > analysis.getRevisions().size()) {
            return Double.NaN;
        }
        return analysis.getRevisions().get(k - 1).getTheilCoefficient();
    }

    public double[] olsInformation(RegressionBasedAnalysis<LocalDate> analysis, int k) {
        if (k > analysis.getRevisions().size()) {
            return null;
        }
        RevisionAnalysis<LocalDate> cur = analysis.getRevisions().get(k - 1);
        if (cur == null) {
            return null;
        }
        OlsTest reg = cur.getRegression();
        if (reg == null) {
            return null;
        }
        Coefficient b0 = reg.getIntercept();
        Coefficient b1 = reg.getSlope();
        TestResult jb = reg.getDiagnostics().getJarqueBera();
        TestResult bp = reg.getDiagnostics().getBreuschPagan();
        TestResult w = reg.getDiagnostics().getWhite();
        return new double[]{
            reg.getN(), reg.getR2(),
            b0.getEstimate(), b0.getStdev(), b0.getTstat(), b0.getPvalue(),
            b1.getEstimate(), b1.getStdev(), b1.getTstat(), b1.getPvalue(),
            jb.getValue(), jb.getPvalue(),
            bp.getValue(), bp.getPvalue(),
            w.getValue(), w.getPvalue()
        };
    }

    public void olsInformation(OlsTest reg, DoubleSeqCursor.OnMutable cursor) {
        if (reg == null) {
            return;
        }

        Coefficient b0 = reg.getIntercept();
        Coefficient b1 = reg.getSlope();
        TestResult jb = reg.getDiagnostics().getJarqueBera();
        TestResult bp = reg.getDiagnostics().getBreuschPagan();
        TestResult w = reg.getDiagnostics().getWhite();
        TestResult arch = reg.getDiagnostics().getArch();
        cursor.setAndNext(reg.getN());
        cursor.setAndNext(reg.getR2());
        cursor.setAndNext(reg.getF());
        cursor.setAndNext(b0.getEstimate());
        cursor.setAndNext(b0.getStdev());
        cursor.setAndNext(b0.getPvalue());
        cursor.setAndNext(b1.getEstimate());
        cursor.setAndNext(b1.getStdev());
        cursor.setAndNext(b1.getPvalue());
        cursor.setAndNext(jb.getValue());
        cursor.setAndNext(jb.getPvalue());
        cursor.setAndNext(reg.getDiagnostics().getSkewness());
        cursor.setAndNext(reg.getDiagnostics().getKurtosis());
        cursor.setAndNext(reg.getDiagnostics().getBpr2());
        cursor.setAndNext(bp.getValue());
        cursor.setAndNext(bp.getPvalue());
        cursor.setAndNext(reg.getDiagnostics().getWr2());
        cursor.setAndNext(w.getValue());
        cursor.setAndNext(w.getPvalue());
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
}

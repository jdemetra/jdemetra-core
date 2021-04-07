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
import demetra.revisions.parametric.AutoCorrelationTests;
import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.Coefficient;
import demetra.revisions.parametric.OlsTests;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.parametric.RevisionAnalysis;
import demetra.revisions.parametric.SignalNoise;
import demetra.revisions.parametric.UnitRoot;
import demetra.stats.StatisticalTest;
import java.time.LocalDate;
import jdplus.math.matrices.Matrix;
import jdplus.revisions.parametric.AutoCorrelationTestsComputer;
import jdplus.revisions.parametric.BiasComputer;
import jdplus.revisions.parametric.OlsTestsComputer;
import jdplus.revisions.parametric.SignalNoiseComputer;
import jdplus.revisions.parametric.UnitRootTestsComputer;
import jdplus.stats.tests.JohansenCointegration;
import jdplus.stats.StatUtility;
import jdplus.stats.tests.DickeyFuller;

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
            OlsTests test = OlsTestsComputer.of(vintages.column(i + gap), vintages.column(i));
            olsInformation(test, cursor);
        }
        return rslt;
    }

    private final int AC = 5;

    /**
     * v(t)=a+b*v(t-gap)
     *
     * @param vintages Vintages
     * @param nbg Number of lags in Breusch-Godfrey test
     * @param nlb Number of lag in Ljung-Box
     * @return
     */
    public MatrixType autoCorrelation(MatrixType vintages, int nbg, int nlb) {
        int n = vintages.getColumnsCount();
        Matrix rslt = Matrix.make(n * (n - 1) / 2, AC);

        for (int i = 0, k = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                try {
                    DoubleSeqCursor.OnMutable cursor = rslt.row(k++).cursor();
                    AutoCorrelationTests test = AutoCorrelationTestsComputer.of(vintages.column(i), vintages.column(j), nbg, nlb);
                    acInformation(test, cursor);
                } catch (Exception err) {
                }
            }
        }
        return rslt;
    }

    private final int EG = 4;

    /**
     * v(t)=a+b*v(t-gap)
     *
     * @param vintages Vintages
     * @param adfk Number of lags in augmented dickey-fuller test
     * @return
     */
    public MatrixType cointegration(MatrixType vintages, int adfk) {
        int n = vintages.getColumnsCount();
        Matrix rslt = Matrix.make(n * (n - 1) / 2, EG);

        for (int i = 0, k = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                try {
                    DoubleSeqCursor.OnMutable cursor = rslt.row(k++).cursor();
                    DickeyFuller df = DickeyFuller.engleGranger(vintages.column(j), vintages.column(i))
                            .numberOfLags(adfk).build();
                    if (df != null) {
                        cursor.setAndNext(df.getRho());
                        cursor.setAndNext(df.getSer());
                        cursor.setAndNext(df.getTest());
                        cursor.setAndNext(df.getPvalue());
                    }
                } catch (Exception err) {
                }
            }
        }
        return rslt;
    }

    private static final int JOHANSEN = 2;

    /**
     * v(t)=a+b*v(t-gap)
     *
     * @param vintages Vintages
     * @param lag Number of lags in augmented dickey-fuller test
     * @param model
     * @return
     */
    public MatrixType vecm(MatrixType vintages, int lag, String model) {
        int n = vintages.getColumnsCount();
        Matrix rslt = Matrix.make(n * (n - 1) / 2, JOHANSEN * lag);
        JohansenCointegration.ECDet ecdet = JohansenCointegration.ECDet.valueOf(model);
        JohansenCointegration computer = JohansenCointegration.builder()
                .errorCorrectionModel(ecdet)
                .lag(lag)
                .build();
        Matrix M = Matrix.make(vintages.getRowsCount(), 2);
        for (int i = 0, k = 0; i < n; ++i) {
            M.column(0).copy(vintages.column(i));
            for (int j = i + 1; j < n; ++j) {
                M.column(1).copy(vintages.column(j));
                try {
                    DoubleSeqCursor.OnMutable cursor = rslt.row(k++).cursor();
                    computer.process(M, null);
                    for (int l = lag - 1; l >= 0; --l) {
                        cursor.setAndNext(computer.traceTest(l));
                    }
                    for (int l = lag - 1; l >= 0; --l) {
                        cursor.setAndNext(computer.maxTest(l));
                    }
                } catch (Exception err) {
                }
            }
        }
        return rslt;
    }

    private final int UR = 4 * 4;

    /**
     * Computes unit roots tests.
     * The tests are givenin the following order:
     * Dickey-Fuller
     * Augmented Dickey-Fuller
     * Dickey-Fuller with c and trend
     * Philips-Perron
     *
     * @param vintages
     * @param adfk Number of lags in augmented dickey-fuller test
     * @return
     */
    public MatrixType unitroot(MatrixType vintages, int adfk) {
        int n = vintages.getColumnsCount();
        Matrix rslt = Matrix.make(n, UR);

        for (int i = 0, k = 0; i < n; ++i) {
            try {
                DoubleSeqCursor.OnMutable cursor = rslt.row(k++).cursor();
                UnitRoot ur = UnitRootTestsComputer.of(vintages.column(i), adfk);
                urInformation(ur, cursor);
            } catch (Exception err) {
            }

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
            OlsTests test = OlsTestsComputer.of(DoublesMath.subtract(cur, prev), prev);
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
                OlsTests test = OlsTestsComputer.of(DoublesMath.subtract(vintages.column(i + gap + 1), vintages.column(i + 1)),
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
                OlsTests test = OlsTestsComputer.of(revs.column(i), x);
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
            if (i != ref - 1) {
                DoubleSeqCursor.OnMutable cursor = rslt.row(j++).cursor();
                try {
                    OlsTests test = OlsTestsComputer.of(revs.column(i), cref);
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

    private final int SN = 6;

    public MatrixType signalNoise(MatrixType vintages, int gap) {
        if (gap < 1) {
            throw new IllegalArgumentException("gap should be >= 1");
        }
        int n = vintages.getColumnsCount() - gap;
        if (n <= 0) {
            return null;
        }
        Matrix rslt = Matrix.make(n, SN);

        for (int i = 0; i < n; ++i) {
            DoubleSeqCursor.OnMutable cursor = rslt.row(i).cursor();
            SignalNoise test = SignalNoiseComputer.of(vintages.column(i), vintages.column(i + gap));
            signalNoiseInformation(test, cursor);
        }
        return rslt;
    }

    public void olsInformation(OlsTests reg, DoubleSeqCursor.OnMutable cursor) {
        if (reg == null) {
            return;
        }
        Coefficient[] c = reg.getCoefficients();
        StatisticalTest jb = reg.getDiagnostics().getJarqueBera();
        StatisticalTest bp = reg.getDiagnostics().getBreuschPagan();
        StatisticalTest w = reg.getDiagnostics().getWhite();
        StatisticalTest arch = reg.getDiagnostics().getArch();
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

    public void acInformation(AutoCorrelationTests ac, DoubleSeqCursor.OnMutable cursor) {
        if (ac == null) {
            return;
        }
        StatisticalTest bg = ac.getBreuschGodfrey();
        StatisticalTest lb = ac.getLjungBox();
        cursor.setAndNext(ac.getBgr2());
        cursor.setAndNext(bg.getValue());
        cursor.setAndNext(bg.getPvalue());
        cursor.setAndNext(lb.getValue());
        cursor.setAndNext(lb.getPvalue());
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

    private static void urInformation(UnitRoot ur, DoubleSeqCursor.OnMutable cursor) {
        if (ur == null) {
            return;
        }
        UnitRoot.Test t = ur.getDickeyFuller();
        cursor.setAndNext(t.getValue());
        cursor.setAndNext(t.getStdev());
        cursor.setAndNext(t.getStatistic());
        cursor.setAndNext(t.getPvalue());
        t = ur.getAugmentedDickeyFuller();
        cursor.setAndNext(t.getValue());
        cursor.setAndNext(t.getStdev());
        cursor.setAndNext(t.getStatistic());
        cursor.setAndNext(t.getPvalue());
        t = ur.getDickeyFullerWithTrendAndIntercept();
        cursor.setAndNext(t.getValue());
        cursor.setAndNext(t.getStdev());
        cursor.setAndNext(t.getStatistic());
        cursor.setAndNext(t.getPvalue());
        t = ur.getPhilipsPerron();
        cursor.setAndNext(t.getValue());
        cursor.setAndNext(t.getStdev());
        cursor.setAndNext(t.getStatistic());
        cursor.setAndNext(t.getPvalue());
    }

    private static void signalNoiseInformation(SignalNoise sn, DoubleSeqCursor.OnMutable cursor) {
        if (sn == null) {
            return;
        }
        cursor.setAndNext(sn.getNewsR2());
        cursor.setAndNext(sn.getNewsF());
        cursor.setAndNext(sn.getNewsPvalue());
        cursor.setAndNext(sn.getNoiseR2());
        cursor.setAndNext(sn.getNoiseF());
        cursor.setAndNext(sn.getNoisePvalue());
    }

}

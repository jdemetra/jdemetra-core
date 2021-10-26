/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regular.tests;

import jdplus.data.analysis.WindowFunction;
import nbbrd.design.BuilderPattern;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.stats.RobustCovarianceComputer;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansenForTradingDays {

    public static Builder test(TsData s) {
        return new Builder(s);
    }

    @BuilderPattern(CanovaHansenForTradingDays.class)
    public static class Builder {

        private final TsData s;
        private int[] differencingLags;
        private WindowFunction winFunction = WindowFunction.Bartlett;
        private int truncationLag = 12;

        private Builder(TsData s) {
            this.s = s;
        }

        public Builder differencingLags(int... lags) {
            this.differencingLags = lags;
            return this;
        }

        public Builder truncationLag(int truncationLag) {
            this.truncationLag = truncationLag;
            return this;
        }

        public Builder windowFunction(WindowFunction winFunction) {
            this.winFunction = winFunction;
            return this;
        }

        public CanovaHansenForTradingDays build() {
            FastMatrix x = sx();
            LinearModel lm = buildModel(x);
            return new CanovaHansenForTradingDays(x, lm, winFunction, truncationLag);
        }

        private FastMatrix sx() {

            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            FastMatrix m = Regression.matrix(s.getDomain(), td);
            FastMatrix dm = m;
            if (differencingLags != null) {
                for (int j = 0; j < differencingLags.length; ++j) {
                    int lag = differencingLags[j];
                    if (lag > 0) {
                        FastMatrix mj = dm;
                        int nr = mj.getRowsCount(), nc = mj.getColumnsCount();
                        dm = mj.extract(lag, nr - lag, 0, nc).deepClone();
                        dm.sub(mj.extract(0, nr - lag, 0, nc));
                    }
                }
            }
            return dm;
        }

        private DoubleSeq y() {

            DoubleSeq dy = s.getValues();
            if (differencingLags != null) {
                for (int j = 0; j < differencingLags.length; ++j) {
                    int lag = differencingLags[j];
                    if (lag > 0) {
                        dy = dy.delta(lag);
                    }
                }
            }
            return dy;
        }

        private LinearModel buildModel(FastMatrix sx) {

            return LinearModel.builder()
                    .y(y())
                    .addX(sx)
                    .meanCorrection(true)
                    .build();
        }
    }

    /**
     * @return the e
     */
    public DoubleSeq getE() {
        return u;
    }

    private final FastMatrix x, xe, cxe, omega;
    private final DoubleSeq c, u;

    private CanovaHansenForTradingDays(final FastMatrix x, final LinearModel lm, final WindowFunction winFunction, int truncationLag) {
        this.x = x;
        LeastSquaresResults olsResults = Ols.compute(lm);
        c = olsResults.getCoefficients();
        u = lm.calcResiduals(c);
        xe = x.deepClone();
        // multiply the columns of x by e
        xe.applyByColumns(col -> col.apply(u, (a, b) -> a * b));
        omega = RobustCovarianceComputer.covariance(xe, winFunction, truncationLag);
        cxe = xe.deepClone();
        cxe.applyByColumns(col -> col.cumul());
    }

    public double test(int var) {
        return computeStat(omega.extract(var, 1, var, 1), cxe.extract(0, cxe.getRowsCount(), var, 1));
    }

    public double test(int var, int nvars) {
        return computeStat(omega.extract(var, nvars, var, nvars), cxe.extract(0, cxe.getRowsCount(), var, nvars));
    }

    public double testAll() {
        return computeStat(omega, cxe);
    }

    private double computeStat(FastMatrix O, FastMatrix cx) {
        int n = cx.getRowsCount(), nx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
        FastMatrix FF = FastMatrix.square(nx);
        for (int i = 0; i < n; ++i) {
            FF.addXaXt(1, cx.row(i));
        }
        // LL'^-1 * xe2 = L'^-1* L^-1 xe2 = L'^-1*a <-> a=L^-1 xe2 <->La=xe2
        FastMatrix sig = O.deepClone();
        SymmetricMatrix.lcholesky(sig);
        LowerTriangularMatrix.solveLX(sig, FF);
        // b=L'^-1*a <-> L'b=a 
        LowerTriangularMatrix.solveLtX(sig, FF);
        double tr = FF.diagonal().sum();
        return tr / (n * n);
    }

}

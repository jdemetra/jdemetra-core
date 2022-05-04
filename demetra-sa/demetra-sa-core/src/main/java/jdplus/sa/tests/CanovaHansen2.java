/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.tests;

import jdplus.data.analysis.TrigonometricSeries;
import jdplus.data.analysis.WindowFunction;
import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.RobustCovarianceComputer;
import demetra.data.DoubleSeq;
import jdplus.stats.linearmodel.Ols;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen2 {

    public static CanovaHansen2 of(DoubleSeq s) {
        return new CanovaHansen2(s);
    }

    private final DoubleSeq s;
    private boolean trend = false;
    private double period;
    private WindowFunction winFunction = WindowFunction.Bartlett;
    private int truncationLag = 12;
    private boolean lag1 = true;

    private CanovaHansen2(DoubleSeq s) {
        this.s = s;
    }

    public CanovaHansen2 periodicity(double period) {
        this.period = period;
        return this;
    }

    public CanovaHansen2 lag1(boolean lag1) {
        this.lag1 = lag1;
        return this;
    }

    public CanovaHansen2 trend(boolean trend) {
        this.trend = trend;
        return this;
    }

    public CanovaHansen2 truncationLag(int truncationLag) {
        this.truncationLag = truncationLag;
        return this;
    }

    public CanovaHansen2 windowFunction(WindowFunction winFunction) {
        this.winFunction = winFunction;
        return this;
    }

    public double compute() {
        FastMatrix x = sx();
        LinearModel lm = buildModel(x);
        LeastSquaresResults olsResults = Ols.compute(lm);
        DoubleSeq e = lm.calcResiduals(olsResults.getCoefficients());
        double rvar = RobustCovarianceComputer.covariance(e, winFunction, truncationLag);
         int n = lm.getObservationsCount();
      FastMatrix xe = lag1 ? x.extract(1, n, 0, x.getColumnsCount()) :  x;
 
        // multiply the columns of x by e
        xe.applyByColumns(c -> c.apply(e, (a, b) -> a * b));
        xe.applyByColumns(c -> c.cumul());
        if (xe.getColumnsCount() == 1) {
            return xe.column(0).ssq() / (n * n * rvar);
        } else {
            return 2 * (xe.column(0).ssq() + xe.column(1).ssq()) / (n * n * rvar);
        }
    }

    private FastMatrix sx() {
        int len = s.length();
        TrigonometricSeries vars = TrigonometricSeries.specific(period);
        return vars.matrix(len, 0);
    }

    private LinearModel buildModel(FastMatrix sx) {
        if (!lag1) {
            LinearModel.Builder builder = LinearModel.builder();
            builder.y(s);
            if (trend) {
                builder.addX(DoubleSeq.onMapping(s.length(), i -> i));
            }
            builder.addX(sx)
                    .meanCorrection(true);
            return builder.build();
        } else {
            LinearModel.Builder builder = LinearModel.builder();
            builder.y(s.drop(1, 0))
                    .addX(s.drop(0, 1))
                    .meanCorrection(true);
            if (trend) {
                builder.addX(DoubleSeq.onMapping(s.length(), i -> i));
            }
            return builder.build();

        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.tests.seasonal;

import jdplus.data.analysis.TrigonometricSeries;
import jdplus.data.analysis.WindowFunction;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.stats.RobustCovarianceComputer;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen2 {
    
    public static CanovaHansen2 of(DoubleSeq s){
        return new CanovaHansen2(s);
    }

    private final DoubleSeq s;
    private boolean trend = false;
    private double period;
    private WindowFunction winFunction = WindowFunction.Bartlett;
    private int truncationLag = 12;

    private CanovaHansen2(DoubleSeq s) {
        this.s = s;
    }

    public CanovaHansen2 periodicity(double period) {
        this.period = period;
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
        Ols ols = new Ols();
        LeastSquaresResults olsResults = ols.compute(lm);
        DoubleSeq e = lm.calcResiduals(olsResults.getCoefficients());
        double rvar = RobustCovarianceComputer.covariance(e, winFunction, truncationLag);
        FastMatrix xe = x.deepClone();
        int n=lm.getObservationsCount();
        
        // multiply the columns of x by e
        xe.applyByColumns(c -> c.apply(e, (a, b) -> (a * b)/n));
        FastMatrix cxe = xe.deepClone();
        cxe.applyByColumns(c -> c.cumul());
        if (cxe.getColumnsCount() == 1)
            return cxe.column(0).ssq()/rvar;
        else{
            return 2*(cxe.column(0).ssq()+cxe.column(1).ssq())/rvar;
        }
    }

    private FastMatrix sx() {
        int len = s.length();
        TrigonometricSeries vars = TrigonometricSeries.specific(period);
        return vars.matrix(len, 0);
    }

    private LinearModel buildModel(FastMatrix sx) {

        LinearModel.Builder builder = LinearModel.builder();
        builder.y(s);
        if (trend) {
            builder.addX(DoubleSeq.onMapping(s.length(), i -> i));
        }
        builder.addX(sx)
                .meanCorrection(true);
        return builder.build();
    }

}

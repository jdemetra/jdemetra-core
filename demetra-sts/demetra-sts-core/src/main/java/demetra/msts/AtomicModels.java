/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.msts.internal.ArItem;
import demetra.msts.internal.ArItem2;
import demetra.msts.internal.ArimaItem;
import demetra.msts.internal.ArmaItem;
import demetra.msts.internal.CycleItem;
import demetra.msts.internal.LocalLevelItem;
import demetra.msts.internal.LocalLinearTrendItem;
import demetra.msts.internal.MsaeItem;
import demetra.msts.internal.MsaeItem2;
import demetra.msts.internal.MsaeItem3;
import demetra.msts.internal.NoiseItem;
import demetra.msts.internal.RegressionItem;
import demetra.msts.internal.SaeItem;
import demetra.msts.internal.SarimaItem;
import demetra.msts.internal.SeasonalComponentItem;
import demetra.msts.internal.TdRegressionItem;
import demetra.timeseries.TsDomain;
import demetra.maths.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AtomicModels {

    public ModelItem arma(final String name, double[] ar, double[] ma, double var, boolean fixed) {
        return new ArmaItem(name, ar, ma, var, fixed);
    }

    public ModelItem sarima(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        return new SarimaItem(name, period, orders, seasonal, parameters, fixed, var, fixedvar);
    }

    public ModelItem localLevel(String name, final double lvar, final boolean fixed, final double initial) {
        return new LocalLevelItem(name, lvar, fixed, initial);
    }

    public ModelItem localLinearTrend(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
    return new LocalLinearTrendItem(name, lvar, svar, lfixed, sfixed);
    }

    public ModelItem seasonalComponent(String name, String smodel, int period, double seasvar, boolean fixed) {
    return new SeasonalComponentItem(name, smodel, period, seasvar, fixed);
    }

    public ModelItem noise(String name, double var, boolean fixed) {
        return new NoiseItem(name, var, fixed);
    }

    public ModelItem regression(String name, MatrixType x) {
        return new RegressionItem(name, x, null, true);
    }

    public ModelItem timeVaryingRegression(String name, MatrixType x, double var, boolean fixed) {
        return new RegressionItem(name, x, new double[]{var}, fixed);
    }

    public ModelItem timeVaryingRegression(String name, MatrixType x, final double[] vars, final boolean fixed) {
         return new RegressionItem(name, x, vars, fixed);
    }

    public ModelItem tdRegression(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
    return new TdRegressionItem(name, domain, groups, contrast, var, fixed);
    }

    public ModelItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        return new ArItem(name, ar, fixedar, var, fixedvar, nlags, zeroinit);
    }

    public ModelItem sae(String name, double[] ar, boolean fixedar, int lag, boolean zeroinit) {
        return new SaeItem(name, ar, fixedar, lag, zeroinit);
    }

//    // ABS-like
//    public ModelItem waveSpecificSurveyError(String name, int nwaves, double ar1, double[] ar2, boolean fixedar) {
//        return mapping -> {
//            final boolean bar1 = Double.isFinite(ar1), bar2 = ar2 != null;
//            if (bar1) {
//                mapping.add(new ArParameters(name + "_sae1", new double[]{ar1}, fixedar));
//            }
//            if (bar2) {
//                mapping.add(new ArParameters(name + "_sae2", ar2, fixedar));
//            }
//            mapping.add((p, builder) -> {
//                int np = 0;
//                double par1 = Double.NaN;
//                if (bar1) {
//                    par1 = p.get(0);
//                    ++np;
//                }
//                double[] par2 = null;
//                if (bar2) {
//                    par2 = p.extract(np, 2).toArray();
//                    np += 2;
//                }
//                StateComponent cmp = WaveSpecificSurveyErrors.of(par1, par2[0], par2[1], nwaves);
//                builder.add(name, cmp, null);
//                return np;
//            });
//        };
//    }

    public ModelItem waveSpecificSurveyError(String name, int nwaves, MatrixType ar, boolean fixedar, int lag) {
        return new MsaeItem(name, nwaves, ar, fixedar, lag);
    }

    public ModelItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, MatrixType ar, boolean fixedar, int lag) {
        return new MsaeItem2(name, var, fixedVar, ar, fixedar, lag);
    }

    public ModelItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, double[] ar, boolean fixedar, MatrixType k, int lag) {
        return new MsaeItem3(name, var, fixedVar, ar, fixedar, k, lag);
    }

    public ModelItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        return new ArItem2(name, ar, fixedar, var, fixedvar, nlags, nfcasts);
    }

    public ModelItem arima(String name, double[] ar, boolean fixedar, double[] diff, double[] ma, boolean fixedma, double var, boolean fixedvar) {
        return new ArimaItem(name, ar, fixedar, diff, ma, fixedma, var, fixedvar);
    }

    public ModelItem cycle(String name, double dumpingFactor, double cyclicalPeriod, boolean fixedcycle, double cvar, boolean fixedvar) {
        return new CycleItem(name, dumpingFactor, cyclicalPeriod, fixedcycle, cvar, fixedvar);
    }

}

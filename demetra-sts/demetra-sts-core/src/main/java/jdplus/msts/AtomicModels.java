/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.msts.internal.ArItem;
import jdplus.msts.internal.ArItem2;
import jdplus.msts.internal.ArimaItem;
import jdplus.msts.internal.ArmaItem;
import jdplus.msts.internal.CycleItem;
import jdplus.msts.internal.LocalLevelItem;
import jdplus.msts.internal.LocalLinearTrendItem;
import jdplus.msts.internal.MsaeItem;
import jdplus.msts.internal.MsaeItem2;
import jdplus.msts.internal.MsaeItem3;
import jdplus.msts.internal.NoiseItem;
import jdplus.msts.internal.RegressionItem;
import jdplus.msts.internal.SaeItem;
import jdplus.msts.internal.SarimaItem;
import jdplus.msts.internal.SeasonalComponentItem;
import jdplus.msts.internal.TdRegressionItem;
import demetra.timeseries.TsDomain;
import jdplus.msts.internal.CumulatorItem;
import jdplus.msts.internal.PeriodicItem;
import jdplus.msts.internal.VarLocalLevelItem;
import jdplus.msts.internal.VarLocalLinearTrendItem;
import jdplus.msts.internal.VarSeasonalComponentItem;
import jdplus.msts.internal.VarNoiseItem;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AtomicModels {

    public StateItem arma(final String name, double[] ar, double[] ma, double var, boolean fixed) {
        return new ArmaItem(name, ar, ma, var, fixed);
    }

    public StateItem sarima(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        return new SarimaItem(name, period, orders, seasonal, parameters, fixed, var, fixedvar);
    }

    public StateItem localLevel(String name, final double lvar, final boolean fixed, final double initial) {
        return new LocalLevelItem(name, lvar, fixed, initial);
    }

    public StateItem localLevel(String name, final double[] lstd, final double lscale, final boolean fixed, final double initial) {
        return new VarLocalLevelItem(name, lstd, lscale, fixed, initial);
    }

    public StateItem localLinearTrend(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        return new LocalLinearTrendItem(name, lvar, svar, lfixed, sfixed);
    }

    public StateItem localLinearTrend(final String name, double[] lstd, double[] sstd, double lscale, double sscale, boolean lfixed, boolean sfixed) {
        return new VarLocalLinearTrendItem(name, lstd, sstd, lscale, sscale, lfixed, sfixed);
    }

    public StateItem seasonalComponent(String name, String smodel, int period, double seasvar, boolean fixed) {
        return new SeasonalComponentItem(name, smodel, period, seasvar, fixed);
    }

    public StateItem seasonalComponent(String name, String smodel, int period, double[] std, double scale, boolean fixed) {
        return new VarSeasonalComponentItem(name, smodel, period, std, scale, fixed);
    }

    public StateItem noise(String name, double var, boolean fixed) {
        return new NoiseItem(name, var, fixed);
    }

    public StateItem noise(String name, double[] std, double scale, boolean fixed) {
        return new VarNoiseItem(name, std, scale, fixed);
    }

    public StateItem regression(String name, Matrix x) {
        return new RegressionItem(name, x, null, true);
    }

    public StateItem timeVaryingRegression(String name, Matrix x, double var, boolean fixed) {
        return new RegressionItem(name, x, new double[]{var}, fixed);
    }

    public StateItem timeVaryingRegression(String name, Matrix x, final double[] vars, final boolean fixed) {
        return new RegressionItem(name, x, vars, fixed);
    }

    public StateItem tdRegression(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        return new TdRegressionItem(name, domain, groups, contrast, var, fixed);
    }

    public StateItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        return new ArItem(name, ar, fixedar, var, fixedvar, nlags, zeroinit);
    }

    public StateItem sae(String name, double[] ar, boolean fixedar, int lag, boolean zeroinit) {
        return new SaeItem(name, ar, fixedar, lag, zeroinit);
    }

    public StateItem waveSpecificSurveyError(String name, int nwaves, Matrix ar, boolean fixedar, int lag) {
        return new MsaeItem(name, nwaves, ar, fixedar, lag);
    }

    public StateItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, Matrix ar, boolean fixedar, int lag) {
        return new MsaeItem2(name, var, fixedVar, ar, fixedar, lag);
    }

    public StateItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, double[] ar, boolean fixedar, Matrix k, int lag) {
        return new MsaeItem3(name, var, fixedVar, ar, fixedar, k, lag);
    }

    public StateItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        return new ArItem2(name, ar, fixedar, var, fixedvar, nlags, nfcasts);
    }

    public StateItem arima(String name, double[] ar, boolean fixedar, double[] diff, double[] ma, boolean fixedma, double var, boolean fixedvar) {
        return new ArimaItem(name, ar, fixedar, diff, ma, fixedma, var, fixedvar);
    }

    public StateItem cycle(String name, double dumpingFactor, double cyclicalPeriod, boolean fixedcycle, double cvar, boolean fixedvar) {
        return new CycleItem(name, dumpingFactor, cyclicalPeriod, fixedcycle, cvar, fixedvar);
    }

    public StateItem periodicComponent(String name, double period, int[] k, double cvar, boolean fixedvar) {
        return new PeriodicItem(name, period, k, cvar, fixedvar);
    }

}

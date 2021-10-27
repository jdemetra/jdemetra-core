/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima.regular;

import demetra.data.DoubleSeq;
import demetra.likelihood.LikelihoodStatistics;
import demetra.modelling.implementations.RegSarimaProcessor;
import demetra.modelling.implementations.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import java.util.Arrays;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.regarima.RegArimaForecasts;

/**
 *
 * @author PALATEJ
 */
public class CheckLast {

    private final RegSarimaProcessor kernel;
    private RegSarimaModel model;
    private final int nback;
    private TsData y, fy, oforecasts;
    private double[] f, ef;

    /**
     * Creates a new module for detecting outliers in the last observations
     *
     * @param kernel
     * @param nback
     */
    public CheckLast(RegSarimaProcessor kernel, int nback) {
        this.kernel = kernel;
        this.nback = nback;
    }

    /**
     * Check outliers at the end of a given series
     *
     * @param data The checked series
     * @return true if the series has been successfully processed, false
     * otherwise. The returned value doesn't indicate the presence or not of
     * outliers.
     */
    public boolean check(TsData data) {
        try {
            clear();
            if (!testSeries(data)) {
                return false;
            }
            GeneralLinearModel<SarimaSpec> gmodel = kernel.process(data.drop(0, nback), null);
            if (gmodel == null || !(gmodel instanceof RegSarimaModel)) {
                return false;
            }
            model = (RegSarimaModel) gmodel;

            RegArimaForecasts.Result fcasts;
            DoubleSeq b = model.getEstimation().getCoefficients();
            LikelihoodStatistics ll = model.getEstimation().getStatistics();
            double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + 1);
            TsDomain edom = model.getDetails().getEstimationDomain();
            if (b.isEmpty()) {
                fcasts = RegArimaForecasts.calcForecast(model.arima(),
                        model.getEstimation().originalY(), nback, sig2);
            } else {
                Variable[] variables = model.getDescription().getVariables();
                TsDomain xdom = edom.extend(0, nback);
                FastMatrix matrix = Regression.matrix(xdom, Arrays.stream(variables).map(v -> v.getCore()).toArray(n -> new ITsVariable[n]));
                fcasts = RegArimaForecasts.calcForecast(model.arima(),
                        model.getEstimation().originalY(), matrix,
                        b, model.getEstimation().getCoefficientsCovariance(), sig2);
            }
            TsDomain fdom = TsDomain.of(edom.getEndPeriod(), nback);
            f = fcasts.getForecasts();
            ef = fcasts.getForecastsStdev();

            y = TsData.fitToDomain(data, fdom);
            fy = model.transform(y, true);
            TsData tf = TsData.ofInternal(fdom.getStartPeriod(), f);
            oforecasts = model.backTransform(tf, true);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    /**
     * Gets the values of the (transformed) series. More especially, if the
     * chosen model implies a log-transformation, the values are obtained after
     * a log-transformation. Other transformations, such leap year corrections
     * or length-of periods corrections may also be used.
     *
     * @return An array with the (transformed) data at the end of the series.
     * The number of data depends on the "backCount" property
     */
    public DoubleSeq getRawValues() {
        return fy.getValues();
    }

    /**
     * Gets the values of the untransformed series.
     *
     * @return An array with the actual data at the end of the series. The
     * number of data depends on the "backCount" property
     */
    public DoubleSeq getActualValues() {
        return y.getValues();
    }

    /**
     * Gets the absolute errors (=observed-forecasts).
     *
     * @return An array with the absolute errors. The
     * number of data depends on the "backCount" property
     */
    public DoubleSeq getAbsoluteErrors() {
        return TsData.subtract(y, oforecasts).getValues();
    }

    /**
     * Gets the untransformed (= comparable to the initial data) forecasts.
     *
     * @return An array with the forecasts at the end of the series. The number
     * of data depends on the "backCount" property
     */
    public DoubleSeq getForecastsValues() {
        return oforecasts.getValues();
    }

    /**
     * Gets the "scores" (ratios between the forecast errors and the standard
     * deviation of the forecasts) of the last observations (positive values
     * mean under-estimation).
     *
     * @return An array with the forecasts at the end of the series. The number
     * of data depends on the "backCount" property
     */
    public double[] getScores() {
        if (f == null || fy == null) {
            return null;
        }
        double[] s = new double[nback];
        for (int i = 0; i < s.length; ++i) {
            s[i] = (fy.getValue(i) - f[i]) / ef[i];
        }
        return s;
    }

    /**
     * Alias for getScores
     *
     * @return
     */
    public double[] getRelativeErrors() {
        return getScores();
    }

    /**
     * Gets the score of the last observations, defined as the ratio between the
     * forecast error (= observed-predicted) and the standard error of the
     * forecast.
     *
     * @param i The considered last observation. It must belong to [0,
     * backCount[.
     * @return The score of the considered last observation
     */
    public double getScore(int i) {
        if (f == null || fy == null) {
            return Double.NaN;
        }
        return (fy.getValue(i) - f[i]) / ef[i];
    }

    /**
     * Alias for getScore
     *
     * @param i
     * @return
     */
    public double getRelativeError(int i) {
        return getScore(i);
    }

    public double[] getRawForecasts() {
        return f;
    }

    public double[] getRawForecastsStdev() {
        return ef;
    }

    /**
     * Gets the number of last observations that will be considered
     *
     * @return A strictly positive integer. 1 by default
     */
    public int getBackCount() {
        return nback;
    }

    public boolean testSeries(final TsData y) {
        if (y == null) {
            return false;
        }
        int nz = y.length();
        int ifreq = y.getAnnualFrequency();
        if (nz < Math.max(8, 3 * ifreq)) {
            return false;
        }
        int nrepeat = y.getValues().getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            return false;
        }
        int nm = y.getValues().count(z -> !Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            return false;
        }
        return true;
    }

    private void clear() {
        model=null;
        f = null;
        ef = null;
        fy = null;
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima.regular;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;

/**
 *
 * @author PALATEJ
 */
public class CheckLast {
    private final RegSarimaProcessor kernel;
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
        this.nback=nback;
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

        clear();
        if (!testSeries(data)) {
            return false;
        }
        ModelEstimation model = kernel.process(data.drop(0, nback), null);
        if (model == null) {
            return false;
        }

//         TsVariableList vars = model_.description.buildRegressionVariables();
//        TsDomain fdomain = new TsDomain(model_.description.getSeriesDomain().getEnd(), nback);
//        List<DataBlock> x = vars.all().data(fdomain);
//
//        forecasts_ = new Forecasts();
//
//        RegArimaEstimation<SarimaModel> estimation
//                = new RegArimaEstimation<>(model_.estimation.getRegArima(), model_.estimation.getLikelihood());
//
//        try {
//            forecasts_.calcForecast(estimation, x, nback, model_.description.getArimaComponent().getFreeParametersCount());
//        } catch (RuntimeException err) {
//            return false;
//        }
//
//        y = data.fittoDomain(fdomain);
//        fy = y.clone();
//        oforecasts = new TsData(fdomain);
//        for (int i = 0; i < oforecasts.getLength(); ++i) {
//            oforecasts.set(i, forecasts_.forecast(i));
//        }
//        model_.backTransform(oforecasts, true, true);
//
//        List<ITsDataTransformation> transformations = model_.description.transformations();
//        for (ITsDataTransformation tr : transformations) {
//            tr.transform(fy, null);
//        }
//
        return true;
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
    public DoubleSeq getValues() {
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
     * @return An array with the absolute errors. The
     * number of data depends on the "backCount" property
     */
    public DoubleSeq getAbsoluteErrors() {
        return TsData.subtract(oforecasts, y).getValues();
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
     * @param i
     * @return 
     */
    public double getRelativeError(int i){
        return getScore(i);
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
        int nm = y.getValues().count(z->! Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            return false;
        }
        return true;
    }

    private void clear() {
        f = null;
        ef = null;
        fy = null;
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;
}

/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.estimation.Forecasts;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.ITsDataTransformation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 * The CheckLast class corresponds to the program "Terror" developed in
 * Tramo-Seats. However, it is able to use a larger class of pre-processing
 * algorithms.
 *
 * The pre-processing algorithm will automatically estimate a RegArima model for
 * given series. Such models may include - log transformation - calendar
 * corrections - outliers detection - arima modelling
 *
 * The model is estimated on a restricted part of the series (see the
 * "setBackCount" method). and the out-of-sample forecasts are compared to the
 * actual (transformed) figures. The "score" of each last observation is defined
 * by the ratio between the forecast error and the standard deviation of the
 * forecast. The detection of anomalies should focus on that score. Values lower
 * than -4 or higher than 4 should be considered as abnormal. Following the
 * problem, such thresholds should be modulated (higher thresholds mean less
 * sensitive detection).
 *
 * For multi-threaded processing, the user should create separate "CheckLast"
 * and separate "IPreprocessor" modules.
 *
 * For example:
 *
 * CheckLast terror0 = new CheckLast(TramoSpecification.TR4.build()); ...
 * CheckLast terrorn = new CheckLast(TramoSpecification.TR4.build());
 *
 * TsData si=...
 *
 * terrori.process(si) may be safely called in parallel.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class CheckLast {

    private final IPreprocessor preprocessor_;
    private PreprocessingModel model_;
    private InformationSet info_;
    private Forecasts forecasts_;
    private TsData y_, fy_, ofcasts_;
    private int nback_ = 1;

    /**
     * Creates a new module for detecting outliers in the last observations
     *
     * @param preprocessor The pre-processor that will identify the RegArima
     * model
     */
    public CheckLast(IPreprocessor preprocessor) {
        preprocessor_ = preprocessor;
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
        if (! testSeries(data))
            return false;
        ModellingContext context = new ModellingContext();
        model_ = preprocessor_.process(data.drop(0, nback_), context);
        if (model_ == null) {
            return false;
        }

        info_ = context.information;
        TsVariableList vars = model_.description.buildRegressionVariables();
        TsDomain fdomain = new TsDomain(model_.description.getSeriesDomain().getEnd(), nback_);
        List<DataBlock> x = vars.all().data(fdomain);

        forecasts_ = new Forecasts();

        RegArimaEstimation<SarimaModel> estimation
                = new RegArimaEstimation<>(model_.estimation.getRegArima(), model_.estimation.getLikelihood());

        try {
            forecasts_.calcForecast(estimation, x, nback_, model_.description.getArimaComponent().getFreeParametersCount());
        } catch (RuntimeException err) {
            return false;
        }

        y_ = data.fittoDomain(fdomain);
        fy_ = y_.clone();
        ofcasts_ = new TsData(fdomain);
        for (int i = 0; i < ofcasts_.getLength(); ++i) {
            ofcasts_.set(i, forecasts_.forecast(i));
        }
        model_.backTransform(ofcasts_, true, true);

        List<ITsDataTransformation> transformations = model_.description.transformations();
        for (ITsDataTransformation tr : transformations) {
            tr.transform(fy_, null);
        }

        return true;
    }

    /**
     * Gets the forecast(s) of the shortened (transformed) series
     *
     * @return The forecasts
     */
    public Forecasts getForecast() {
        return forecasts_;
    }

    /**
     * Gets the values of the (transformed) series. More especially, if the
     * chosen model implies a log-transformation, the values are obtained after
     * a log-transformation. Other transformations, such leap year corrections or
     * length-of periods corrections may also be used.
     *
     * @return An array with the (transformed) data at the end of the series.
     * The number of data depends on the "backCount" property
     */
    public double[] getValues() {
        return fy_.internalStorage();
    }

    /**
     * Gets the values of the untransformed series.
     *
     * @return An array with the actual data at the end of the series. The
     * number of data depends on the "backCount" property
     */
    public double[] getActualValues() {
        return y_.internalStorage();
    }

    /**
     * Gets the untransformed (= comparable to the initial data) forecasts.
     *
     * @return An array with the forecasts at the end of the series. The number
     * of data depends on the "backCount" property
     */
    public double[] getForecastsValues() {
        return ofcasts_.internalStorage();
    }

    /**
     * Gets the "scores" (ratios between the forecast errors and the standard
     * deviation of the forecasts) of the last observations.
     *
     * @return An array with the forecasts at the end of the series. The number
     * of data depends on the "backCount" property
     */
    public double[] getScores() {
        if (forecasts_ == null || fy_ == null) {
            return null;
        }
        double[] s = new double[nback_];
        for (int i = 0; i < s.length; ++i) {
            s[i] = (fy_.get(i) - forecasts_.forecast(i)) / forecasts_.forecastStdev(i);
        }
        return s;
    }

    /**
     * Gets the score of the last observations, defined as the ratio between the
     * forecast error and the standard error of the forecast.
     *
     * @param i The considered last observation. It must belong to [0,
     * backCount[.
     * @return The score of the considered last observation
     */
    public double getScore(int i) {
        if (forecasts_ == null || fy_ == null) {
            return Double.NaN;
        }
        return (fy_.get(i) - forecasts_.forecast(i)) / forecasts_.forecastStdev(i);
    }

    /**
     * Gets the information set used during the pre-processing
     *
     * @return
     */
    public InformationSet getInformation() {
        return info_;
    }

    /**
     * Gets the number of last observations that will be considered
     *
     * @return A strictly positive integer. 1 by default
     */
    public int getBackCount() {
        return nback_;
    }

    /**
     * Sets the number of observations that will be considered at the end of the
     * series. The series is modelled without the n last observations and the
     * out-of-sample forecasts are used for detecting outliers.
     *
     * @param n A strictly positive integer
     */
    public void setBackCount(int n) {
        if (n != nback_) {
            nback_ = n;
            clear();
        }
    }

    /**
     * Gets the estimated reg-arima model
     *
     * @return The estimated model
     */
    public PreprocessingModel getEstimatedModel() {
        return model_;
    }

    public boolean testSeries(final TsData y) {
        if (y == null) {
            return false;
        }
        int nz = y.getObsCount();
        int ifreq = y.getFrequency().intValue();
        if (nz < Math.max(8, 3 * ifreq)) {
            return false;
        }
        int nrepeat = y.getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            return false;
        }
        int nm = y.getMissingValuesCount();
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            return false;
        }
        return true;
    }

    private void clear() {
        model_ = null;
        info_ = null;
        forecasts_ = null;
        fy_ = null;
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;
}

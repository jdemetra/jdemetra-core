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
package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsException;
import java.util.Date;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Internal;
import ec.tstoolkit.timeseries.simplets.ObsList.LongObsList;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/**
 * A TSDataCollecor collects time observations (identified by pairs of
 * date-double) to create simple time series. Time series can be created
 * following different aggregation mode or in an automatic way. See the "make"
 * method for further information
 *
 * @author Jean Palate. National Bank of Belgium
 */
@Development(status = Development.Status.Alpha)
public class TsDataCollector {

    private final LongObsList m_obs;
    private double missing;

    /**
     * Creates a new TSData Collector.
     */
    public TsDataCollector() {
        this.missing = -99999;
        this.m_obs = ObsList.newLongObsList(false, TsPeriod.CalendarUtil.getInstance()::calcTsPeriodId);
    }

    /**
     * Adds a missing value. Adding a missing value is usually unnecessary. This
     * method should be used only if we want to give the series a precise Time
     * domain.
     *
     * @param date Date that corresponds to the observation
     *
     */
    public void addMissingValue(Date date) {
        m_obs.add(date.getTime(), Double.NaN);
    }

    /**
     * Adds an observation
     *
     * @param date Date that corresponds to the observation. The date has just
     * to belong to the considered period (it is not retained in the final time
     * series.
     * @param value Value of the observation
     */
    public void addObservation(Date date, double value) {
        if (Double.isNaN(value) || value == missing) {
            m_obs.add(date.getTime(), Double.NaN);
        } else {
            m_obs.add(date.getTime(), value);
        }
    }

    /**
     * Removes all observations.
     */
    public void clear() {
        m_obs.clear();
    }

    /**
     * Gets all the data stored in the object
     *
     * @return An array of doubles, which corresponds to the sorted (by date)
     * observations. The object doesn't correspond to the current internal
     * state. It can be freely reused.
     */
    @NewObject
    public double[] data() {
        m_obs.sortByPeriod();
        return m_obs.getValues();
    }

    /**
     * Returns the number of observations in this object.
     *
     * @return The number of observations
     */
    public int getCount() {
        return m_obs.size();
    }

    /**
     * Gets the double that represents a missing value
     *
     * @return Double that correspond to a missing value. -999999 by default
     */
    public double getMissingValue() {
        return missing;
    }

    /**
     * Sets the double that represents a missing value. Any value that
     * corresponds to this value (or to Double.NaN) will be considered as
     * missing values in the final object.
     *
     * @param value New missing value. Could be Double.NaN.
     */
    public void setMissingValue(double value) {
        missing = value;
    }

    /**
     * Creates a time series with the observations in this Object. The Object is
     * not cleared after the creation. The creation process is defined by the
     * following steps: If frequency is undefined, the TSDataCollector searches
     * for the smaller frequency, if any, such that every period of the domain
     * contains only one observation. The conversion mode must be set to none in
     * that case. If the frequency is specified, the values of the created
     * series are calculated according to the conversion mode. If this one is
     * none, each period must contain at most one observation. Otherwise, an
     * exception is thrown.
     *
     * @param frequency The frequency of the time series. Can be undefined.
     * @param convMode The conversion mode. Must be none when the frequency is
     * unspecified.
     * @return The created time series. If the collector can't create a time
     * series corresponding to the observation and to the parameters (for
     * example if there are several observation for a given month and if the
     * aggregation mode is none), a null is returned (no exception).
     */
    public TsData make(TsFrequency frequency, TsAggregationType convMode) {
        if (frequency == TsFrequency.Undefined) {
            if (convMode != TsAggregationType.None) {
                throw new TsException(TsException.INVALID_AGGREGATIONMODE);
            }
            return makeFromUnknownFrequency(m_obs);
        }
        if (convMode != TsAggregationType.None) {
            return makeWithAggregation(m_obs, frequency, convMode);
        }
        return makeWithoutAggregation(m_obs, frequency);
    }

    /**
     * INTERNAL USE ONLY
     *
     * @param freq
     * @param convMode
     * @param obs
     * @return
     * @since 2.2.0
     */
    @Internal
    public static TsData makeWithAggregation(ObsList obs, TsFrequency freq, TsAggregationType convMode) {
        int n = obs.size();
        if (n == 0) {
            return null; // NO_DATA
        }
        obs.sortByPeriod();

        double[] vals = new double[n];
        int[] ids = new int[n];
        int ncur = -1;

        int avn = 0;
        for (int i = 0; i < n; ++i) {
            int curid = obs.getPeriodId(freq, i);
            double value = obs.getValue(i);
            switch (convMode) {
                case Average: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        if (ncur >= 0) {
                            vals[ncur] /= avn;
                        }
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                        avn = 1;
                    } else {
                        vals[ncur] += value;
                        ++avn;
                    }
                    break;
                }
                case Sum: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        vals[ncur] += value;
                    }
                    break;
                }
                case First: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    }
                    break;
                }
                case Last: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        ids[++ncur] = curid;
                    }
                    vals[ncur] = value;
                    break;
                }
                case Max: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = value;
                        if (dcur > vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
                case Min: {
                    if (!Double.isFinite(value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = value;
                        if (dcur < vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
            }
        }

        // correction pour le dernier cas
        if (convMode == TsAggregationType.Average && ncur >= 0) {
            vals[ncur] /= avn;
        }

        int firstId = ids[0];
        int lastId = ids[ncur];

        TsPeriod start = new TsPeriod(freq, firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == ncur + 1) {
            return new TsData(start, ncur + 1 == n ? vals : Arrays.copyOf(vals, ncur + 1), false);
        } else {
            return new TsData(start, expand(ncur + 1, l, ids, o -> vals[o]), false);
        }
    }

    private static boolean isNewPeriod(int ncur, int curid, int[] ids) {
        return ncur < 0 || curid != ids[ncur];
    }

    /**
     * INTERNAL USE ONLY
     *
     * @param obs
     * @return
     * @since 2.2.0
     */
    @Internal
    public static TsData makeFromUnknownFrequency(ObsList obs) {
        int n = obs.size();
        if (n < 2) {
            return null; // NO_DATA or GUESS_SINGLE
        }
        obs.sortByPeriod();

        int s = 0;

        int[] ids = new int[n];
        TsFrequency[] freqs = TsFrequency.allFreqs;
        for (; s < freqs.length; ++s) {
            if (makeIdsFromFrequency(obs, freqs[s], ids)) {
                break;
            }
        }
        if (s == freqs.length) {
            return null; // GUESS_DUPLICATION
        }
        int firstId = ids[0];
        int lastId = ids[n - 1];

        TsPeriod start = new TsPeriod(freqs[s], firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == n) {
            return new TsData(start, obs.getValues(), false);
        } else {
            return new TsData(start, expand(n, l, ids, obs::getValue), false);
        }
    }

    private static boolean makeIdsFromFrequency(ObsList obs, TsFrequency frequency, int[] ids) {
        ids[0] = obs.getPeriodId(frequency, 0);
        for (int i = 1; i < ids.length; ++i) {
            ids[i] = obs.getPeriodId(frequency, i);
            if (ids[i] == ids[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * INTERNAL USE ONLY
     *
     * @param freq
     * @param obs
     * @return
     * @since 2.2.0
     */
    @Internal
    public static TsData makeWithoutAggregation(ObsList obs, TsFrequency freq) {
        int n = obs.size();
        if (n == 0) {
            return null; // NO_DATA
        }

        obs.sortByPeriod();

        int[] ids = new int[n];
        ids[0] = obs.getPeriodId(freq, 0);
        for (int i = 1; i < n; i++) {
            ids[i] = obs.getPeriodId(freq, i);
            if (ids[i] == ids[i - 1]) {
                return null; // DUPLICATION_WITHOUT_AGGREGATION
            }
        }

        int firstId = ids[0];
        int lastId = ids[n - 1];

        TsPeriod start = new TsPeriod(freq, firstId);

        // check if the series is continuous and complete.
        int l = lastId - firstId + 1;
        if (l == n) {
            return new TsData(start, obs.getValues(), false);
        } else {
            return new TsData(start, expand(n, l, ids, obs::getValue), false);
        }
    }

    private static double[] expand(int currentSize, int expectedSize, int[] ids, IntToDoubleFunction valueFunc) {
        double[] result = new double[expectedSize];
        Arrays.fill(result, Double.NaN);
        result[0] = valueFunc.applyAsDouble(0);
        for (int j = 1; j < currentSize; ++j) {
            result[ids[j] - ids[0]] = valueFunc.applyAsDouble(j);
        }
        return result;
    }
}

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
import java.util.ArrayList;
import java.util.Date;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.VisibleForTesting;
import java.util.Arrays;
import java.util.function.ToIntFunction;

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

    private final ArrayList<DateObs> m_obs;
    private double missing;
    private boolean m_bIsSorted;

    /**
     * Creates a new TSData Collector.
     */
    public TsDataCollector() {
        this.missing = -99999;
        this.m_obs = new ArrayList<>();
        this.m_bIsSorted = true;
    }

    /**
     * Check if the list is still sorted after an add to avoid sort overhead.
     *
     * @return
     */
    @VisibleForTesting
    boolean isStillSortedAfterAdd() {
        int size = m_obs.size();
        return size <= 1 || (m_bIsSorted && m_obs.get(size - 2).date <= m_obs.get(size - 1).date);
    }
    
    /**
     * Adds a missing value. Adding a missing value is usually unnecessary.
     * This method should be used only if we want to give the series a precise
     * Time domain.
     * 
     * @param date Date that corresponds to the observation
     *
     */
    public void addMissingValue(Date date) {
	m_obs.add(new DateObs(date.getTime()));
	m_bIsSorted = isStillSortedAfterAdd();
    }

    /**
     * Adds an observation
     * 
     * @param date Date that corresponds to the observation. The date has just
     * to belong to the considered period (it is not retained in the final time series.
     * @param value Value of the observation
     */
    public void addObservation(Date date, double value) {
	if (Double.isNaN(value) || value == missing)
	    m_obs.add(new DateObs(date.getTime()));
	else
	    m_obs.add(new DateObs(date.getTime(), value));
	m_bIsSorted = isStillSortedAfterAdd();
    }

    /**
     * Removes all observations.
     */
    public void clear() {
	m_obs.clear();
        m_bIsSorted = true;
    }

    /**
     * Gets all the data stored in the object
     * 
     * @return An array of doubles, which corresponds to the sorted (by date)
     *         observations. The object doesn't correspond to the current
     *         internal state. It can be freely reused.
     */
    @NewObject
    public double[] data() {
	sort();
	int n = m_obs.size();
	double[] vals = new double[n];
	for (int i = 0; i < n; ++i)
	    vals[i] = m_obs.get(i).value;
	return vals;
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
     * Creates a time series with the observations in this Object. The Object is
     * not cleared after the creation. The creation process is defined by the
     * following steps: If frequency is undefined, the TSDataCollector searches
     * for the smaller frequency, if any, such that every period of the domain
     * contains only one observation. The conversion mode must be set to none in
     * that case. If the frequency is specified, the values of the created
     * series are calculated according to the conversion mode. If this one is
     * none, each period must contain at most one observation. Otherwise, an
     * exception is thrown.
     *      *
     * @param frequency The frequency of the time series. Can be undefined.
     * @param convMode The conversion mode. Must be none when the frequency is
     *            unspecified.
     * @return The created time series. If the collector can't create a time 
     * series corresponding to the observation and to the parameters 
     * (for example if there are several observation for a given month and 
     * if the aggregation mode is none), a null is returned (no exception).
     */
    public TsData make(TsFrequency frequency, TsAggregationType convMode) {
	if (frequency == TsFrequency.Undefined) {
	    if (convMode != TsAggregationType.None)
		throw new TsException(TsException.INVALID_AGGREGATIONMODE);
	    return makeFromUnknownFrequency();
	}

	int n = m_obs.size();
	if (n == 0)
	    return null;

	sort();

        ToIntFunction<DateObs> tsPeriodIdFunc = getTsPeriodIdFunc(frequency);
        
	double[] vals = new double[n];
	int[] ids = new int[n];
	int ncur = -1;

	int avn = 0;
        for (int i = 0; i < n; ++i) {
            DateObs o = m_obs.get(i);
            int curid = tsPeriodIdFunc.applyAsInt(o);
            switch (convMode) {
                case Average: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        if (ncur >= 0) {
                            vals[ncur] /= avn;
                        }
                        vals[++ncur] = o.value;
                        ids[ncur] = curid;
                        avn = 1;
                    } else {
                        vals[ncur] += o.value;
                        ++avn;
                    }
                    break;
                }
                case Sum: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = o.value;
                        ids[ncur] = curid;
                    } else {
                        vals[ncur] += o.value;
                    }
                    break;
                }
                case First: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = o.value;
                        ids[ncur] = curid;
                    }
                    break;
                }
                case Last: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        ids[++ncur] = curid;
                    }
                    vals[ncur] = o.value;
                    break;
                }
                case Max: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = o.value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = o.value;
                        if (dcur > vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
                case Min: {
                    if (!Double.isFinite(o.value)) {
                        continue;
                    }
                    if (isNewPeriod(ncur, curid, ids)) {
                        vals[++ncur] = o.value;
                        ids[ncur] = curid;
                    } else {
                        double dcur = o.value;
                        if (dcur < vals[ncur]) {
                            vals[ncur] = dcur;
                        }
                    }
                    break;
                }
                default: // none
                {
                    if (!isNewPeriod(ncur, curid, ids)) {
                        return null;
                    }
                    vals[++ncur] = o.value;
                    ids[ncur] = curid;
                    break;
                }
            }
        }
	// }
	// correction pour le dernier cas
	if (convMode == TsAggregationType.Average && ncur >= 0)
	    vals[ncur] /= avn;

	int firstId = tsPeriodIdFunc.applyAsInt(m_obs.get(0));
	int lastId = tsPeriodIdFunc.applyAsInt(m_obs.get(n - 1));
        
	TsPeriod start = new TsPeriod(frequency, firstId);
        
	// check if the series is continuous and complete.
	int l = lastId - firstId + 1;
	if (l == n && ncur + 1 == n)
	    return new TsData(start, vals, false);
	else {
	    double[] valsc = new double[l];
            Arrays.fill(valsc, Double.NaN);
	    for (int j = 0; j <= ncur; ++j)
		valsc[ids[j] - firstId] = vals[j];
	    return new TsData(start, valsc, false);
	}
    }

    private static ToIntFunction<DateObs> getTsPeriodIdFunc(TsFrequency frequency) {
        TsPeriod.CalendarUtil util = TsPeriod.CalendarUtil.getInstance();
        return o -> util.calcTsPeriodId(frequency, o.date);
    }
    
    private static boolean isNewPeriod(int ncur, int curid, int[] ids) {
        return ncur < 0 || curid != ids[ncur];
    }
    
    private TsData makeFromUnknownFrequency() {
	int n = m_obs.size();
	if (n < 2)
	    return null;
	sort();

	int s = 0;

	int[] ids = new int[n];
	TsFrequency[] freqs = TsFrequency.allFreqs;
	for (; s < freqs.length; ++s)
	    if (makeIdsFromFrequency(freqs[s], ids))
		break;
	if (s == freqs.length)
	    return null;

        int firstId = ids[0];
        int lastId = ids[n - 1];
        
	TsPeriod start = new TsPeriod(freqs[s], firstId);

	double[] vtmp = new double[lastId - firstId + 1];
        Arrays.fill(vtmp, Double.NaN);
	for (int i = 0; i < n; ++i) {
	    DateObs o = m_obs.get(i);
	    vtmp[ids[i] - firstId] = o.value;
	}
	return new TsData(start, vtmp, false);
    }

    private boolean makeIdsFromFrequency(TsFrequency frequency, int[] ids) {
        ToIntFunction<DateObs> tsPeriodIdFunc = getTsPeriodIdFunc(frequency);
	ids[0] = tsPeriodIdFunc.applyAsInt(m_obs.get(0));
	for (int i = 1; i < ids.length; ++i) {
	    ids[i] = tsPeriodIdFunc.applyAsInt(m_obs.get(i));
	    if (ids[i] == ids[i - 1])
		return false;
	}
	return true;
    }

    /**
     * Sets the double that represents a missing value. Any value that corresponds
     * to this value (or to Double.NaN) will be considered as missing values in
     * the final object.
     * 
     * @param value New missing value. Could be Double.NaN.
     */
    public void setMissingValue(double value) {
	missing = value;
    }

    private void sort() {
	if (!m_bIsSorted) {
	    java.util.Collections.sort(m_obs, (l, r) -> Long.compare(l.date, r.date));
	    m_bIsSorted = true;
	}
    }
    
    private static final class DateObs {

        public final long date;

        public final double value;

        public DateObs(long d) {
            date = d;
            value = Double.NaN;
        }

        public DateObs(long d, double v) {
            date = d;
            value = v;
        }
    }
}

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
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.Development;

class DateObs {

    Date date;

    double value;

    DateObs(Date d) {
	date = d;
	value = Double.NaN;
    }

    DateObs(Date d, double v) {
	date = d;
	value = v;
    }
}

enum DateObsComparer implements java.util.Comparator<DateObs> {

    INSTANCE;
    @Override
    public int compare(DateObs o1, DateObs o2) {
	return o1.date.compareTo(o2.date);
    }
}

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

    private double missing = -99999;
    private ArrayList<DateObs> m_obs = new ArrayList<>();

    private boolean m_bIsSorted;

    /**
     * Creates a new TSData Collector.
     */
    public TsDataCollector() {
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
	m_obs.add(new DateObs(date));
	m_bIsSorted = false;
    }

    /**
     * Adds an observation
     * 
     * @param date Date that corresponds to the observation. The date has just
     * to belong to the considered period (it is not retained in the final time series.
     * @param Value Value of the observation
     */
    public void addObservation(Date date, double Value) {
	if (Double.isNaN(Value) || Value == missing)
	    m_obs.add(new DateObs(date));
	else
	    m_obs.add(new DateObs(date, Value));
	m_bIsSorted = false;
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
     * @exception A TsException is thrown if the frequency and the aggregation mode
     * are incompatible (frequency is none and aggregation mode is not none).
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

	double[] vals = new double[n];
	int[] ids = new int[n];

	TsPeriod p0 = new TsPeriod(frequency, m_obs.get(0).date);
	int ids0 = p0.id();
	TsPeriod p1 = new TsPeriod(frequency, m_obs.get(n - 1).date);
	int ids1 = p1.id();

	int avn = 0;
	int ncur = -1;
	for (int i = 0; i < n; ++i) {
	    DateObs o = m_obs.get(i);
	    TsPeriod p = new TsPeriod(frequency, o.date);
	    int curid = p.id();
	    switch (convMode) {
	    case Average: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur]) {
		    if (ncur >= 0)
			vals[ncur] /= avn;
		    vals[++ncur] = o.value;
		    ids[ncur] = curid;
		    avn = 1;
		} else {
		    vals[ncur] += o.value;
		    ++avn;
		}
	    }
		break;

	    case Sum: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur]) {
		    vals[++ncur] = o.value;
		    ids[ncur] = curid;
		} else
		    vals[ncur] += o.value;
	    }
		break;

	    case First: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur]) {
		    vals[++ncur] = o.value;
		    ids[ncur] = curid;
		}
	    }
		break;

	    case Last: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur])
		    ids[++ncur] = curid;
		vals[ncur] = o.value;
	    }
		break;

	    case Max: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur]) {
		    vals[++ncur] = o.value;
		    ids[ncur] = curid;
		} else {
		    double dcur = o.value;
		    if (dcur > vals[ncur])
			vals[ncur] = dcur;
		}
	    }
		break;

	    case Min: {
		if (!DescriptiveStatistics.isFinite(o.value))
		    continue;
		if (ncur < 0 || curid != ids[ncur]) {
		    vals[++ncur] = o.value;
		    ids[ncur] = curid;
		} else {
		    double dcur = o.value;
		    if (dcur < vals[ncur])
			vals[ncur] = dcur;
		}
	    }
		break;

	    default: // none
	    {
		if (ncur >= 0 && curid == ids[ncur])
		    return null;
		vals[++ncur] = o.value;
		ids[ncur] = curid;
	    }
		break;
	    }
	}
	// }
	// correction pour le dernier cas
	if (convMode == TsAggregationType.Average && ncur >= 0)
	    vals[ncur] /= avn;

	// check if the series is continuous and complete.
	int l = ids1 - ids0 + 1;
	if (l == n && ncur + 1 == n)
	    return new TsData(p0, vals, false);
	else {
	    double[] valsc = new double[l];
	    for (int j = 0; j < l; ++j)
		valsc[j] = Double.NaN;
	    for (int j = 0; j <= ncur; ++j)
		valsc[ids[j] - ids0] = vals[j];
	    return new TsData(p0, valsc, false);
	}
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

	TsPeriod start = new TsPeriod(freqs[s], ids[0]);

	double[] vtmp = new double[ids[n - 1] - ids[0] + 1];
	for (int i = 0; i < vtmp.length; ++i)
	    vtmp[i] = Double.NaN;
	for (int i = 0; i < n; ++i) {
	    DateObs o = m_obs.get(i);
	    vtmp[ids[i] - ids[0]] = o.value;
	}
	return new TsData(start, vtmp, false);
    }

    private boolean makeIdsFromFrequency(TsFrequency frequency, int[] ids) {
	TsPeriod p = new TsPeriod(frequency, (m_obs.get(0)).date);
	ids[0] = p.id();
	for (int i = 1; i < ids.length; ++i) {
	    p = new TsPeriod(frequency, m_obs.get(i).date);
	    ids[i] = p.id();
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
	    java.util.Collections.sort(m_obs, DateObsComparer.INSTANCE);
	    m_bIsSorted = true;
	}
    }
}

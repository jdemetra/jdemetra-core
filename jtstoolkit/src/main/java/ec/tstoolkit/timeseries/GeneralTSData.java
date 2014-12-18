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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.data.Values;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class GeneralTSData {
    private IDomain m_domain;

    private Values m_vals;

    /**
     *
     */
    public GeneralTSData() {
    }

    /**
     * 
     * @param ts
     */
    public GeneralTSData(final GeneralTSData ts) {
	if (ts.m_domain != null)
	    m_domain = (IDomain) ts.m_domain;
	if (ts.m_vals != null)
	    m_vals = new Values(ts.m_vals);
    }

    /**
     * 
     * @param dom
     */
    public GeneralTSData(final IDomain dom) {
	m_domain = dom;
	m_vals = new Values(dom.getLength());
    }

    /**
     * 
     * @param dom
     * @param vals
     */
    public GeneralTSData(final IDomain dom, final double[] vals) {
	m_domain = dom;
	m_vals = new Values(vals);
    }

    private Values average(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    int m = 0;
	    double s = 0;
	    for (int j = j0; j < j1; ++j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    ++m;
		    s += v;
		}
	    }
	    if (m != 0)
		vals.set(i, s / m);
	}
	return vals;
    }

    @Override
    protected Object clone() {
	return new GeneralTSData(this);
    }

    /**
     * 
     * @param newdom
     * @param conv
     * @return
     */
    public GeneralTSData convert(final IDomain newdom,
	    final TsAggregationType conv) {
	DomainConverter converter = new DomainConverter();
	if (!converter.convert(newdom, m_domain))
	    return null;
	Values vals = null;
	switch (conv) {
	case Average:
	    vals = average(converter);
	    break;

	case Sum:
	    vals = sum(converter);
	    break;
	case First:
	    vals = first(converter);
	    break;
	case Last:
	    vals = last(converter);
	    break;
	case Max:
	    vals = max(converter);
	    break;
	case Min:
	    vals = min(converter);
	    break;
	}
	if (vals != null) {
	    GeneralTSData ts = new GeneralTSData();
	    ts.m_domain = newdom;
	    ts.m_vals = vals;
	    return ts;
	} else
	    return null;
    }

    private Values first(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    for (int j = j0; j < j1; ++j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    vals.set(i, v);
		    break;
		}
	    }
	}
	return vals;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double get(final int idx) {
	return m_vals.get(idx);
    }

    /**
     * 
     * @return
     */
    public IDomain getDomain() {
	return m_domain;
    }

    /**
     * 
     * @param day
     * @return
     */
    public double getValue(final Day day) {
	int idx = m_domain.search(day);
	if (idx >= 0)
	    return m_vals.get(idx);
	else
	    return Double.NaN;
    }

    /**
     * 
     * @param p
     * @return
     */
    public double getValue(final IPeriod p) {
	// if (p == null)
	// throw new ArgumentNullException("p");
	Day fd = p.firstday();
	int idx = m_domain.search(fd);
	if (idx < 0)
	    return Double.NaN;
	IPeriod pdom = m_domain.get(idx);
	if (pdom.firstday() != fd)
	    return Double.NaN;
	Day ld = p.lastday();
	if (pdom.lastday() != ld)
	    return Double.NaN;
	else
	    return m_vals.get(idx);
    }

    /**
     * 
     * @return
     */
    public Values getValues() {
	return m_vals;
    }

    private Values last(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    for (int j = j1 - 1; j >= j0; --j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    vals.set(i, v);
		    break;
		}
	    }
	}
	return vals;
    }

    private Values max(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    double m = Double.NaN;
	    for (int j = j1 - 1; j >= j0; --j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    if (Double.isNaN(m) || (v > m))
			m = v;
		}
	    }
	    if (!Double.isNaN(m))
		vals.set(i, m);
	}
	return vals;
    }

    private Values min(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    double m = Double.NaN;
	    for (int j = j1 - 1; j >= j0; --j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    if (Double.isNaN(m) || (v < m))
			m = v;
		}
	    }
	    if (!Double.isNaN(m))
		vals.set(i, m);
	}
	return vals;
    }

    /**
     * 
     * @param day
     * @param value
     */
    public void set(final Day day, final double value) {
	m_vals.set(m_domain.search(day), value);
    }

    /**
     * 
     * @param idx
     * @param value
     */
    public void set(final int idx, final double value) {
	m_vals.set(idx, value);
    }

    private Values sum(final DomainConverter converter) {
	int n = converter.getNewDomain().getLength();
	Values vals = new Values(n);
	for (int i = 0; i < n; ++i) {
	    int j0 = converter.startPos(i), j1 = converter.endPos(i);
	    double s = 0;
	    boolean ok = false;
	    for (int j = j0; j < j1; ++j) {
		double v = m_vals.get(j);
		if (!Double.isNaN(v)) {
		    ok = true;
		    s += v;
		}
	    }
	    if (ok)
		vals.set(i, s);
	}
	return vals;
    }

}

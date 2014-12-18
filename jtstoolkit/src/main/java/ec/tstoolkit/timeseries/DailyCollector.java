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

import ec.tstoolkit.design.Development;
import java.util.Date;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DailyCollector {
    private int[] m_ids;

    private double[] m_vals;

    private int m_n;

    private final static int g_blocksize = 512;

    /**
	 *
	 */
    public DailyCollector() {
    }

    /**
     * 
     * @param date
     * @param val
     * @return
     */
    public boolean add(final Date date, final double val) {
	return add(new Day(date), val);
    }

    /**
     * 
     * @param day
     * @param val
     * @return
     */
    public boolean add(final Day day, final double val) {
	int id = day.getId();
	if ((m_n > 0) && (m_ids[m_n - 1] >= id))
	    return false;
	if ((m_ids == null) || (m_ids.length == m_n))
	    do_extends();
	m_ids[m_n] = id;
	m_vals[m_n] = val;
	++m_n;
	return true;
    }

    /**
     * 
     */
    public void clear()
    {
	m_n = 0;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public Day day(final int idx) {
	// if (idx >= m_n)
	// throw new ArgumentException("idx too high");
	return new Day(m_ids[idx]);
    }

    private void do_extends() {
	if (m_ids == null) {
	    m_ids = new int[g_blocksize];
	    m_vals = new double[g_blocksize];
	} else {
	    int[] ids = new int[m_ids.length + g_blocksize];
	    double[] vals = new double[m_vals.length + g_blocksize];
	    System.arraycopy(m_ids, 0, ids, 0, m_ids.length);
	    System.arraycopy(m_vals, 0, vals, 0, m_vals.length);
	    m_ids = ids;
	    m_vals = vals;
	}
    }

    /**
     * 
     * @return
     */
    public int getSize()
    {
	return m_n;
    }

    /**
     * 
     * @return
     */
    public GeneralTSData makeSeries()
    {
	if (m_n == 0)
	    return null;
	Days days = new Days(m_ids, 0, m_n);
	GeneralTSData ts = new GeneralTSData(days);
	for (int i = 0; i < m_n; ++i)
	    ts.set(i, m_vals[i]);
	return ts;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double value(final int idx) {
	return (idx >= m_n) ? Double.NaN : m_vals[idx];
    }

}

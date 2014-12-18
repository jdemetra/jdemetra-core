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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsPeriodSelector;

/**
 * Iterator that walk through a time series year by year. Years can be
 * incomplete
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class YearIterator implements java.util.Enumeration<TsDataBlock> {
    /**
     * 
     * @param series
     * @return
     */
    public static YearIterator fullYears(TsData series)
    {
	TsDomain domain = series.getDomain();
	int ifreq = domain.getFrequency().intValue();
	int nbeg = series.getStart().getPosition();
	int nend = series.getEnd().getPosition();
	domain = domain.drop(nbeg == 0 ? 0 : ifreq - nbeg, nend);
	return new YearIterator(series, domain);

    }

    private TsDataBlock m_cur;
    private final TsDataBlock m_data;

    /**
     * 
     * @param series
     */
    public YearIterator(TsData series) {
	if (!series.isEmpty())
	    m_data = TsDataBlock.all(series);
	else
	    m_data = null;
    }

    /**
     * 
     * @param series
     * @param domain
     */
    public YearIterator(TsData series, TsDomain domain)
    {
	m_data = TsDataBlock.select(series, domain);
    }

    /**
     * 
     * @param series
     * @param selector
     */
    public YearIterator(TsData series, TsPeriodSelector selector)
    {
	m_data = TsDataBlock.select(series, selector);
    }

    @Override
    public boolean hasMoreElements() {
	if (m_data == null)
	    return false;
	if (m_cur == null)
	    return true;
	int ifreq = m_data.start.getFrequency().intValue();
	int ibeg = m_cur.start.minus(m_data.start);
	int iend = m_data.data.getLength() - ifreq - ibeg;
	return iend > 0;
    }

    private void initialize() {
	int ifreq = m_data.start.getFrequency().intValue();
	int beg = m_data.start.getPosition();
	m_cur = new TsDataBlock(m_data.start.minus(beg), m_data.data.extract(
		-beg, ifreq, 1));
    }

    @Override
    public TsDataBlock nextElement() {
	int ifreq = m_data.start.getFrequency().intValue();
	if (m_cur == null)
	    initialize();
	else
	    m_cur.move(ifreq);
	int ibeg = m_cur.start.minus(m_data.start);
	int iend = m_data.data.getLength() - ifreq - ibeg;
	if (ibeg >= 0 && iend >= 0)
	    return m_cur;
	else
	    return m_cur.drop(ibeg < 0 ? -ibeg : 0, iend < 0 ? -iend : 0);
    }

    /**
     *
     */
    public void reset() {
	m_cur = null;
    }
}

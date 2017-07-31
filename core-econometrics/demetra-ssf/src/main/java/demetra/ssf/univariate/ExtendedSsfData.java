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
package demetra.ssf.univariate;

import demetra.design.Development;
import demetra.data.DoubleSequence;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ExtendedSsfData implements ISsfData {

    private int m_nbcasts;

    private int m_nfcasts;

    private final ISsfData m_data;

    /**
     * 
     * @param data
     */
    public ExtendedSsfData(final ISsfData data)
    {
	m_data = data;
    }

    /**
     * 
     * @param data
     */
    public ExtendedSsfData(final ISsfData data, int fcasts)
    {
	m_data = data;
        m_nfcasts=fcasts;
    }

    /**
     * 
     * @param data
     */
    public ExtendedSsfData(final DoubleSequence data, int fcasts)
    {
	m_data = new SsfData(data);
        m_nfcasts=fcasts;
    }
    /**
     * 
     * @param n
     * @return
     */
    @Override
    public double get(final int n)
    {
	if (n < m_nbcasts)
	    return Double.NaN;
	else
	    return m_data.get(n - m_nbcasts);
    }

    /**
     * 
     * @return
     */
    public int getBackcastsCount()
    {
	return m_nbcasts;
    }

    /**
     * 
     * @return
     */
    @Override
    public int length()
    {
	return m_nbcasts + m_nfcasts + m_data.length();
    }

    /**
     * 
     * @return
     */
    public int getForecastsCount()
    {
	return m_nfcasts;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasData()
    {
	return m_data.hasData();
    }

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(final int pos)
    {
	if (pos < m_nbcasts)
	    return true;
	return m_data.isMissing(pos - m_nbcasts);
    }

    /**
     * 
     * @param value
     */
    public void setBackcastsCount(final int value)
    {
	m_nbcasts = value;
    }

    /**
     * 
     * @param value
     */
    public void setForecastsCount(final int value)
    {
	m_nfcasts = value;
    }

}

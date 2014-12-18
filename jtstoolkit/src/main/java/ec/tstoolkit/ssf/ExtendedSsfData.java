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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;

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
     * @param n
     * @return
     */
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
    public int getCount()
    {
	return m_nbcasts + m_nfcasts + m_data.getCount();
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
    public double[] getInitialState()
    {
	return m_data.getInitialState();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getObsCount()
    {
	return m_data.getObsCount();
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
     * @return
     */
    @Override
    public boolean hasMissingValues()
    {
	return m_nbcasts > 0 || m_nfcasts > 0 || m_data.hasMissingValues();
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

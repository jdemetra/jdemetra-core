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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Deprecated
public class RangeTest {

    private double m_lb = .2, m_ub = 99999;

    /**
     * 
     */
    public RangeTest()
    {
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean findBigValues(double[] data)
    {
	for (int i = 0; i < data.length; ++i)
	    if (data[i] >= m_ub)
		return true;
	return false;
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean findSmallValues(double[] data)
    {
	for (int i = 0; i < data.length; ++i)
	    if (data[i] <= m_lb)
		return true;
	return false;
    }

    /**
     * 
     * @return
     */
    public double getLBound()
    {
	return m_lb;
    }

    /**
     * 
     * @return
     */
    public double getUBound()
    {
	return m_ub;
    }

    /**
     * 
     * @param value
     */
    public void setLBound(double value)
    {
	m_lb = value;
    }

    /**
     * 
     * @param value
     */
    public void setUBound(double value)
    {
	m_ub = value;
    }
}

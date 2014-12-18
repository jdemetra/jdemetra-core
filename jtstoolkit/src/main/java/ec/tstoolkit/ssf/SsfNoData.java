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
public class SsfNoData implements ISsfData, Cloneable {

    private int m_n;

    /** Creates a new instance of SsfImplBase
     * @param n
     */
    public SsfNoData(final int n) {
	m_n = n;
    }

    @Override
    public Object clone() {
	return new SsfNoData(m_n);
    }

    /**
     * 
     * @param n
     * @return
     */
    public double get(final int n)
    {
	return 0;
    }

    /**
     * 
     * @return
     */
    public int getCount()
    {
	return m_n;
    }

    /**
     * 
     * @return
     */
    public double[] getInitialState()
    {
	return null;
    }

    /**
     * 
     * @return
     */
    public int getObsCount()
    {
	return 0;
    }

    /**
     * 
     * @return
     */
    public boolean hasData()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    public boolean hasMissingValues()
    {
	return false;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public boolean isMissing(final int pos)
    {
	return false;
    }

    /**
     * 
     * @param value
     */
    public void setCount(final int value)
    {
	m_n = value;
    }

}

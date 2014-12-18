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

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfData implements ISsfData, Cloneable {

    private final double[] m_data;

    private final double[] m_a0;

    /** Creates a new instance of SsfData
     * @param data
     * @param a0
     */
    public SsfData(final double[] data, final double[] a0) {
	m_data = data.clone();
	if (a0 != null)
	    m_a0 = a0.clone();
	else
	    m_a0 = null;
    }

    /**
     * 
     * @param data
     * @param a0
     */
    public SsfData(final IReadDataBlock data, final IReadDataBlock a0)
    {
	m_data = new double[data.getLength()];
	data.copyTo(m_data, 0);
	if (a0 != null) {
	    m_a0 = new double[a0.getLength()];
	    a0.copyTo(m_a0, 0);
	} else
	    m_a0 = null;
    }

    @Override
    public SsfData clone() {
	try {
	    SsfData data = (SsfData) super.clone();
            return data;
	} catch (CloneNotSupportedException err) {
            throw new AssertionError();
	}
    }

    /**
     *
     * @param n
     * @return
     */
    @Override
    public double get(final int n) {
	return n >= m_data.length ? Double.NaN : m_data[n];
    }

    /**
     *
     * @return
     */
    @Override
    public int getCount() {
	return m_data.length;
    }

    /**
     *
     * @return
     */
    @Override
    public double[] getInitialState() {
	return m_a0;
    }

    /**
     *
     * @return
     */
    @Override
    public int getObsCount() {

	int n = 0;
	for (int i = 0; i < m_data.length; ++i)
	    if (!Double.isNaN(m_data[i]))
		++n;
	return n;

    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasData() {
	return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasMissingValues() {

	for (int i = 0; i < m_data.length; ++i)
	    if (Double.isNaN(m_data[i]))
		return true;
	return false;

    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(final int pos) {
	return pos >= m_data.length || Double.isNaN(m_data[pos]);
    }
}

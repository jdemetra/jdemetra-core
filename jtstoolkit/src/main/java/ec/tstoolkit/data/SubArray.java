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
package ec.tstoolkit.data;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate, Philippe Charles
 * @param <T>
 */
@Development(status = Development.Status.Alpha)
public class SubArray<T> {

    private final T[] m_data;

    private final int m_beg, m_end, m_inc;

    /**
	 * 
	 */
    SubArray() {
	m_data = null;
	m_beg = 0;
	m_end = 0;
	m_inc = 1;
    }

    /**
     * 
     * @param data
     */
    public SubArray(final T[] data) {
	m_data = data;
	m_beg = 0;
	m_end = data.length;
	m_inc = 1;
    }

    /**
     * Defines an array of data corresponding to data[beg],
     * data[beg+inc]...data[end]. end should be equal to beg + n * inc, where n
     * is the number of data in the data block. The relationship between beg,
     * end and inc is not checked.
     * 
     * @param data
     *            Array that contains the data
     * @param beg
     *            First index of the data
     * @param end
     *            Last index of the data
     * @param inc
     *            Increment between two data in the underlying array
     **/
    public SubArray(final T[] data, final int beg, final int end, final int inc) {
	m_data = data;
	m_beg = beg;
	m_end = end;
	m_inc = inc;
    }

    /**
     * 
     * @param a
     */
    public void copy(final SubArray<T> a) {
	int tcur = m_beg, scur = a.m_beg;
	if (m_inc == 1 && a.m_inc == 1)
	    while (tcur != m_end)
		m_data[tcur++] = a.m_data[scur++];
	else
	    while (tcur != m_end) {
		m_data[tcur] = a.m_data[scur];
		tcur += m_inc;
		scur += a.m_inc;
	    }
    }

    /**
     * 
     * @param idx
     * @return
     */
    public T get(final int idx) {
	return m_data[m_beg + idx * m_inc];
    }

    /**
     * 
     * @return
     */
    public int getLength() {
	return m_inc == 0 ? 0 : (m_end - m_beg) / m_inc;
    }

    /**
     * 
     * @return
     */
    public boolean isEmpty() {
        for (int i=m_beg; i != m_end; i+=m_inc){
            if (m_data[i] != null)
                return false;
        }
	return true;
    }

    /**
     * Returns a sub-array from i0 (included) to i1 (excluded)
     * 
     * @param i0
     * @param i1
     * @return
     */
    public SubArray<T> range(final int i0, final int i1) {
	return new SubArray<>(m_data, m_beg + i0 * m_inc, m_beg + i1 * m_inc,
		m_inc);
    }

    /**
     * 
     * @param idx
     * @param value
     */
    public void set(final int idx, final T value) {
	m_data[m_beg + idx * m_inc] = value;
    }

    /**
     * 
     * @param tval
     */
    public void set(final T tval) {
	int cur = m_beg;
	if (m_inc == 1)
	    while (cur != m_end)
		m_data[cur++] = tval;
	else
	    while (cur != m_end) {
		m_data[cur] = tval;
		cur += m_inc;
	    }
    }

}

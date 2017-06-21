/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.utilities;

import demetra.design.Development;
import demetra.design.PrimitiveReplacementOf;

/**
 * 
 * @author Jean Palate, Philippe Charles
 */
@Development(status = Development.Status.Alpha)
@PrimitiveReplacementOf(generic = SubArray.class, primitive = boolean.class)
public final class SubArrayOfBoolean {

    /**
     *
     */
    public static final SubArrayOfBoolean EMPTY = create(Arrays2.EMPTY_BOOLEAN_ARRAY);

    /**
     * SubArrayOfInt factory<br>
     * 
     * @param data
     * @return
     */
    public static SubArrayOfBoolean create(final boolean[] data) {
	return new SubArrayOfBoolean(data, 0, data.length, 1);
    }

    /**
     * SubArrayOfInt factory<br>
     * 
     * @param data
     *            Reference array
     * @param beg
     *            start of the sub-array (included)
     * @param end
     *            end of the sub-array (excluded)
     * @param inc
     * @return
     */
    public static SubArrayOfBoolean create(final boolean[] data,
	    final int beg, final int end, final int inc) {
	return new SubArrayOfBoolean(data, beg, end, inc);
    }

    private final boolean[] m_data;

    private final int m_beg, m_end, m_inc;

    private SubArrayOfBoolean(final boolean[] data, final int beg,
	    final int end, final int inc) {
	m_data = data;
	m_beg = beg;
	m_end = end;
	m_inc = inc;
    }

    /**
     * 
     * @param a
     */
    public void copy(final SubArrayOfBoolean a) {
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

    public void copyFrom(final boolean[] a, int start) {
	int tcur = m_beg, scur = start;
	if (m_inc == 1 )
	    while (tcur != m_end)
		m_data[tcur++] = a[scur++];
	else
	    while (tcur != m_end) {
		m_data[tcur] = a[scur++];
		tcur += m_inc;
	    }
    }
    /**
     * 
     * @param idx
     * @return
     */
    public boolean get(final int idx) {
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
	return m_beg == m_end;
    }

    /**
     * Returns a sub-array from i0 (included) to i1 (excluded)
     * 
     * @param i0
     * @param i1
     * @return
     */
    public SubArrayOfBoolean range(final int i0, final int i1) {
	return new SubArrayOfBoolean(m_data, m_beg + i0 * m_inc, m_beg + i1
		* m_inc, m_inc);
    }

    /**
     * 
     * @param tval
     */
    public void set(final boolean tval) {
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

    /**
     * 
     * @param idx
     * @param value
     */
    public void set(final int idx, final boolean value) {
	m_data[m_beg + idx * m_inc] = value;
    }

}

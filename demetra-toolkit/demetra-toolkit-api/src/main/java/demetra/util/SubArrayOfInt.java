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
package demetra.util;

import nbbrd.design.Development;
import nbbrd.design.PrimitiveReplacementOf;
import demetra.util.Arrays2;

/**
 * 
 * @author Philippe Charles
 */
@Development(status = Development.Status.Alpha)
@PrimitiveReplacementOf(generic = SubArray.class, primitive = int.class)
public final class SubArrayOfInt {

    /**
     *
     */
    public static final SubArrayOfInt EMPTY = create(Arrays2.EMPTY_INT_ARRAY);

    /**
     * SubArrayOfInt factory<br>
     * 
     * @param data
     * @return
     */
    public static SubArrayOfInt create(final int[] data) {
	return new SubArrayOfInt(data, 0, data.length, 1);
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
    public static SubArrayOfInt create(final int[] data, final int beg,
	    final int end, final int inc) {
	return new SubArrayOfInt(data, beg, end, inc);
    }

    private final int[] m_data;

    private final int m_beg, m_end, m_inc;

    private SubArrayOfInt(final int[] data, final int beg, final int end,
	    final int inc) {
	m_data = data;
	m_beg = beg;
	m_end = end;
	m_inc = inc;
    }

    /**
     * 
     * @param idx
     * @param value
     */
    public void add(final int idx, final int value) {
	m_data[m_beg + idx * m_inc] += value;
    }

    /**
     * 
     * @param a
     */
    public void copy(final SubArrayOfInt a) {
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

    public void copyFrom(final int[] a, int start) {
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
    public int get(final int idx) {
	return m_data[m_beg + idx * m_inc];
    }

    /**
     * 
     * @return
     */
    public int getLength()
    {
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
    public SubArrayOfInt range(final int i0, final int i1) {
	return new SubArrayOfInt(m_data, m_beg + i0 * m_inc,
		m_beg + i1 * m_inc, m_inc);
    }

    /**
     * 
     * @param tval
     */
    public void set(final int tval) {
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
    public void set(final int idx, final int value) {
	m_data[m_beg + idx * m_inc] = value;
    }

    public void add(int del) {
        if (del == 0)
            return;
	int cur = m_beg;
	if (m_inc == 1)
	    while (cur != m_end)
		m_data[cur++] += del;
	else
	    while (cur != m_end) {
		m_data[cur] += del;
		cur += m_inc;
	    }

    }

}

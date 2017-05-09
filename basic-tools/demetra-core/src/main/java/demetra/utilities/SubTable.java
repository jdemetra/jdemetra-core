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
package demetra.utilities;

import demetra.design.Development;

/**
 * 
 * @author Jean Palate, Philippe Charles
 * @param <T> Type of each element of the sub-table
 */
@Development(status = Development.Status.Alpha)
public class SubTable<T> {

    T[] m_data;

    int m_start, m_nrows, m_ncols, m_row_inc, m_col_inc;

    /**
     * 
     * @param m
     */
    public SubTable(final SubTable<T> m) {
	m_data = m.m_data;
	m_start = m.m_start;
	m_nrows = m.m_nrows;
	m_ncols = m.m_ncols;
	m_row_inc = m.m_row_inc;
	m_col_inc = m.m_col_inc;
    }

    /**
     * Creates a new instance of SubTable
     * 
     * @param data
     * @param nrows
     * @param ncols
     */
    public SubTable(final T[] data, final int nrows, final int ncols) {
	m_data = data;
	m_nrows = nrows;
	m_ncols = ncols;
	m_row_inc = 1;
	m_col_inc = nrows;
    }

    /**
     * 
     * @param data
     * @param start
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     */
    public SubTable(final T[] data, final int start, final int nrows,
	    final int ncols, final int rowinc, final int colinc) {
	m_data = data;
	m_start = start;
	m_nrows = nrows;
	m_ncols = ncols;
	m_row_inc = rowinc;
	m_col_inc = colinc;
    }

    /**
     * 
     * @param c
     * @return
     */
    public SubArray<T> column(final int c) {
	int beg = m_start + c * m_col_inc, end = beg + m_row_inc * m_nrows;
	return new SubArray<>(m_data, beg, end, m_row_inc);
    }

    /**
     * 
     * @return
     */
    public SubArray<T> diagonal() {
	int n = Math.min(m_nrows, m_ncols), inc = m_row_inc + m_col_inc;
	return new SubArray<>(m_data, m_start, m_start + inc * n, inc);
    }

    /**
     * 
     * @param r0
     * @param r1
     * @param c0
     * @param c1
     * @return
     */
    public SubTable<T> extract(final int r0, final int r1, final int c0,
	    final int c1) {
	return new SubTable<>(m_data, m_start + r0 * m_row_inc + c0
		* m_col_inc, r1 - r0, c1 - c0, m_row_inc, m_col_inc);
    }

    /**
     * 
     * @param r0
     * @param c0
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     * @return
     */
    public SubTable<T> extract(final int r0, final int c0, final int nrows,
	    final int ncols, final int rowinc, final int colinc) {
	return new SubTable<>(m_data, m_start + r0 * m_row_inc + c0
		* m_col_inc, nrows, ncols, m_row_inc * rowinc, m_col_inc
		* colinc);
    }

    /**
     * 
     * @param row
     * @param col
     * @return
     */
    public T get(final int row, final int col) {
	return m_data[m_start + row * m_row_inc + col * m_col_inc];
    }

    /**
     * 
     * @return
     */
    public int getColumnsCount() {

	return m_ncols;
    }

    /**
     * 
     * @return
     */
    public int getRowsCount() {

	return m_nrows;
    }

    /**
     * 
     * @param dr
     * @param dc
     */
    public void move(final int dr, final int dc) {
	m_start += dr * m_row_inc + dc * m_col_inc;
    }

    /**
     * 
     * @param r
     * @return
     */
    public SubArray<T> row(final int r) {
	int beg = m_start + r * m_row_inc, end = beg + m_col_inc * m_ncols;
	return new SubArray<>(m_data, beg, end, m_col_inc);
    }

    /**
     * 
     * @param row
     * @param col
     * @param value
     */
    public void set(final int row, final int col, final T value) {
	m_data[m_start + row * m_row_inc + col * m_col_inc] = value;
    }

    /**
     * 
     * @param val
     */
    public void set(final T val) {
	if (m_row_inc == 1) {
	    for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc)
		for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir)
		    m_data[ir] = val;
	} else if (m_col_inc == 1) {
	    for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc)
		for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic)
		    m_data[ic] = val;
	} else {
	    for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc)
		for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc)
		    m_data[ir] = val;
	}
    }

    /**
     * 
     * @param n
     */
    public void shift(final int n) {
	if (n < 0) {
	    int del = (m_row_inc + m_col_inc) * n;

	    for (int c = 0, i = m_start; c < m_ncols + n; ++c, i += m_col_inc)
		for (int r = 0, j = i; r < m_nrows + n; ++r, j += m_row_inc)
		    m_data[j] = m_data[j - del];
	} else if (n > 0) {
	    int del = (m_row_inc + m_col_inc) * n;

	    for (int c = n, i = m_start + (m_nrows - 1) * m_row_inc
		    + (m_ncols - 1) * m_col_inc; c < m_ncols; ++c, i -= m_col_inc)
		for (int r = n, j = i; r < m_nrows; ++r, j -= m_row_inc)
		    m_data[j] = m_data[j - del];

	}
    }

    /**
     * 
     * @param pos
     * @return
     */
    public SubArray<T> subDiagonal(int pos) {
	if (pos >= m_ncols)
	    return new SubArray<>();
	if (-pos >= m_nrows)
	    return new SubArray<>();
	int beg = m_start, inc = m_row_inc + m_col_inc;
	int n = 0;
	if (pos > 0) {
	    beg += pos * m_col_inc;
	    n = Math.min(m_nrows, m_ncols - pos);
	} else if (pos < 0) {
	    beg -= pos * m_row_inc;
	    n = Math.min(m_nrows + pos, m_ncols);
	}
	return new SubArray<>(m_data, beg, beg + inc * n, inc);
    }

    /**
     * 
     * @return
     */
    public SubTable<T> transpose() {
	return new SubTable<>(m_data, m_start, m_ncols, m_nrows, m_col_inc,
		m_row_inc);
    }

}

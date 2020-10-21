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

/**
 * 
 * @author Philippe Charles
 */
@Development(status = Development.Status.Alpha)
@PrimitiveReplacementOf(generic = SubTable.class, primitive = boolean.class)
public class SubTableOfBoolean {

    boolean[] m_data;

    int m_start, m_nrows, m_ncols, m_row_inc, m_col_inc;

    /**
     * Creates a new instance of SubTable
     * 
     * @param data
     * @param nrows
     * @param ncols
     */
    public SubTableOfBoolean(final boolean[] data, final int nrows,
	    final int ncols) {
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
    public SubTableOfBoolean(final boolean[] data, final int start,
	    final int nrows, final int ncols, final int rowinc, final int colinc) {
	m_data = data;
	m_start = start;
	m_nrows = nrows;
	m_ncols = ncols;
	m_row_inc = rowinc;
	m_col_inc = colinc;
    }

    /**
     * 
     * @param m
     */
    public SubTableOfBoolean(final SubTableOfBoolean m) {
	m_data = m.m_data;
	m_start = m.m_start;
	m_nrows = m.m_nrows;
	m_ncols = m.m_ncols;
	m_row_inc = m.m_row_inc;
	m_col_inc = m.m_col_inc;
    }

    /**
     * 
     * @param c
     * @return
     */
    public SubArrayOfBoolean column(final int c) {
	int beg = m_start + c * m_col_inc, end = beg + m_row_inc * m_nrows;
	return SubArrayOfBoolean.create(m_data, beg, end, m_row_inc);
    }

    /**
     * 
     * @param r0
     * @param r1
     * @param c0
     * @param c1
     * @return
     */
    public SubTableOfBoolean extract(final int r0, final int r1, final int c0,
	    final int c1) {
	return new SubTableOfBoolean(m_data, m_start + r0 * m_row_inc + c0
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
    public SubTableOfBoolean extract(final int r0, final int c0,
	    final int nrows, final int ncols, final int rowinc, final int colinc) {
	return new SubTableOfBoolean(m_data, m_start + r0 * m_row_inc + c0
		* m_col_inc, nrows, ncols, m_row_inc * rowinc, m_col_inc
		* colinc);
    }

    /**
     * 
     * @param row
     * @param col
     * @return
     */
    public boolean get(final int row, final int col) {
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
    public SubArrayOfBoolean row(final int r) {
	int beg = m_start + r * m_row_inc, end = beg + m_col_inc * m_ncols;
	return SubArrayOfBoolean.create(m_data, beg, end, m_col_inc);
    }

    /**
     * 
     * @param row
     * @param col
     * @param value
     */
    public void set(final int row, final int col, final boolean value)
    {
	m_data[m_start + row * m_row_inc + col * m_col_inc] = value;
    }

    /**
     * 
     * @return
     */
    public SubTableOfBoolean transpose()
    {
	return new SubTableOfBoolean(m_data, m_start, m_ncols, m_nrows,
		m_col_inc, m_row_inc);
    }

}

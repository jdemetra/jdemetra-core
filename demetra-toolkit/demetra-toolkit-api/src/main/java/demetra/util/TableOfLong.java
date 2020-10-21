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
@PrimitiveReplacementOf(generic = Table.class, primitive = long.class)
public class TableOfLong {

    private final long[] m_data;

    private final int m_nrows, m_ncols;

    /**
     * 
     * @param nrows
     * @param ncols
     */
    public TableOfLong(final int nrows, final int ncols) {
	m_data = new long[nrows * ncols];
	m_nrows = nrows;
	m_ncols = ncols;
    }

    /**
     * 
     * @param table
     */
    public TableOfLong(final TableOfLong table) {
	m_data = table.m_data.clone();
	m_nrows = table.m_nrows;
	m_ncols = table.m_ncols;
    }

    /**
     * 
     * @param col
     * @return
     */
    public SubArrayOfLong column(final int col) {
	return SubArrayOfLong.create(m_data, col * m_nrows, (col + 1) * m_nrows,
		1);
    }

    /**
     * 
     * @return
     */
    public SubTableOfLong extract() {
	return new SubTableOfLong(m_data, 0, m_nrows, m_ncols, 1, m_nrows);
    }

    /**
     * 
     * @param r0
     * @param r1
     * @param c0
     * @param c1
     * @return
     */
    public SubTableOfLong extract(final int r0, final int r1, final int c0,
	    final int c1) {
	return new SubTableOfLong(m_data, r0 + c0 * m_nrows, r1 - r0, c1 - c0,
		1, m_nrows);
    }

    /**
     *
     * @param r0
     * @param c0
     * @param nr
     * @param nc
     * @param rinc
     * @param cinc
     * @return
     */
    public SubTableOfLong extract(final int r0, final int c0, final int nr,
	    final int nc, final int rinc, final int cinc) {
	return new SubTableOfLong(m_data, r0 + c0 * m_nrows, nr, nc, rinc, cinc
		* m_nrows);
    }

    /**
     * 
     * @param r
     * @param c
     * @return
     */
    public long get(final int r, final int c) {
	return m_data[r + c * m_nrows];
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
     * @return
     */
    public boolean isEmpty() {
	return m_data == null;
    }

    /**
     * 
     * @param row
     * @return
     */
    public SubArrayOfLong row(final int row) {
	return SubArrayOfLong.create(m_data, row, row + m_ncols * m_nrows,
		m_nrows);
    }
    
    public long[] internalStorage(){
        return m_data;
    }

    /**
     * 
     * @param r
     * @param c
     * @param value
     */
    public void set(final int r, final int c, final long value) {
	m_data[r + c * m_nrows] = value;
    }
}

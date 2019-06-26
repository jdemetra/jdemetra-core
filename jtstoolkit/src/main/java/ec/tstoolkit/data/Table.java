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
import java.util.Arrays;

/**
 * 
 * @author Jean Palate, Philippe Charles
 * @param <T>
 */
@Development(status = Development.Status.Alpha)
public class Table<T> {
    private final T[] m_data;

    private final int m_nrows, m_ncols;

    /**
     * 
     * @param nrows
     * @param ncols
     */
    @SuppressWarnings("unchecked")
    public Table(final int nrows, final int ncols) {
	m_data = (T[]) new Object[nrows * ncols];
	m_nrows = nrows;
	m_ncols = ncols;
    }

    /**
     * 
     * @param table
     */
    public Table(final Table<T> table) {
	m_data = table.m_data.clone();
	m_nrows = table.m_nrows;
	m_ncols = table.m_ncols;
    }

    /**
     * 
     * @param col
     * @return
     */
    public SubArray<T> column(final int col) {
	return new SubArray<>(m_data, col * m_nrows, (col + 1) * m_nrows, 1);
    }

    /**
     * 
     * @return
     */
    public SubTable<T> extract() {
	return new SubTable<>(m_data, 0, m_nrows, m_ncols, 1, m_nrows);
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
	return new SubTable<>(m_data, r0 + c0 * m_nrows, r1 - r0, c1 - c0, 1,
		m_nrows);
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
    public SubTable<T> extract(final int r0, final int c0, final int nr,
	    final int nc, final int rinc, final int cinc) {
	return new SubTable<>(m_data, r0 + c0 * m_nrows, nr, nc, rinc, cinc
		* m_nrows);
    }

    /**
     * 
     * @param r
     * @param c
     * @return
     */
    public T get(final int r, final int c) {
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
    public int getRowsCount()
    {
	return m_nrows;
    }

    /**
     * 
     * @return
     */
    public boolean isEmpty() {
	if (m_data == null)
            return true;
        for (int i=0; i<m_data.length; ++i){
            if (m_data[i] != null)
                return false;
        }
        return true;
    }

    /**
     * 
     * @param row
     * @return
     */
    public SubArray<T> row(final int row) {
	return new SubArray<>(m_data, row, row + m_ncols * m_nrows, m_nrows);
    }

    /**
     * 
     * @param r
     * @param c
     * @param value
     */
    public void set(final int r, final int c, final T value) {
	m_data[r + c * m_nrows] = value;
    }
    
    public boolean copyTo(T[] buffer){
        if (buffer.length != m_data.length)
            return false;
        for (int i=0; i<m_data.length; ++i){
            buffer[i]=m_data[i];
        }
        return true;
    }
    
    public boolean copyFrom(T[] buffer){
        if (buffer.length != m_data.length)
            return false;
        for (int i=0; i<m_data.length; ++i){
            m_data[i]=buffer[i];
        }
        return true;
    }

    public int size(){
        return m_data.length;
    }
    
    public boolean deepEquals(Table<T> table){
        return Arrays.deepEquals(m_data, table.m_data);
    }
}
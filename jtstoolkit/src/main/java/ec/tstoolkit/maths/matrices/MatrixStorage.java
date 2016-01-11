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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class MatrixStorage {

    private double[] m_data;

    private int m_n, m_nused;

    private final int m_size, m_nr;

    private int m_nc;

    /**
     * Matrix storage for square matrices
     *
     * @param dim dimension of the matrices
     * @param capacity Initial capacity of the storage
     */
    public MatrixStorage(final int dim, final int capacity) {
        m_n = DataBlockStorage.calcSize(capacity);
        m_nr = m_nc = dim;
        m_size = dim * dim;
        m_data = new double[m_size * m_n];
    }

    /**
     *
     * @param nr
     * @param nc
     * @param capacity
     */
    public MatrixStorage(final int nr, final int nc, final int capacity) {
        m_n = DataBlockStorage.calcSize(capacity);
        m_nr = nr;
        m_nc = nc;
        m_size = nr * nc;
        m_data = new double[m_size * m_n];
    }

    public int getMatrixRowsCount() {
        return m_nr;
    }

    public int getMatrixColumnsCount() {
        return m_nc;
    }

    /**
     *
     * @return
     */
    public int getCapacity() {
        return m_n;
    }

    /**
     *
     * @return
     */
    public int getCurrentSize() {
        return m_nused;
    }

    /**
     *
     * @param pos
     * @return
     */
    public SubMatrix matrix(final int pos) {
        return new SubMatrix(m_data, m_size * pos, m_nr, m_nc, 1, m_nr);
    }
    
    public DataBlock item(final int row, final int col){
        int start=row+m_nr*col;
        int end=start+m_nused*m_size;
        return new DataBlock(m_data, start, end, m_size);
    }

    /**
     *
     * @param ncapacity
     */
    public void resize(final int ncapacity) {
        int n = DataBlockStorage.calcSize(ncapacity);
        if (n <= m_n) {
            return;
        }
        double[] data = new double[m_size * n];
        System.arraycopy(m_data, 0, data, 0, m_data.length);
        m_data = data;
        m_n = n;
    }

    /**
     *
     * @param pos
     * @param m
     */
    public void save(final int pos, final Matrix m) {
        m.copyTo(m_data, pos * m_size);
        if (pos >= m_nused) {
            m_nused = pos + 1;
        }
    }

    /**
     *
     * @param pos
     * @param m
     */
    public void save(final int pos, final SubMatrix m) {
        matrix(pos).copy(m);
        if (pos >= m_nused) {
            m_nused = pos + 1;
        }
    }

    /**
     * Multiplies the current matrices by a given factor
     * @param factor 
     */
    public void rescale(double factor) {
        if (factor == 1)
            return;
        int n=m_size*m_nused;
        for (int i=0; i<n; ++i){
            m_data[i]*=factor;
        }
    }
}

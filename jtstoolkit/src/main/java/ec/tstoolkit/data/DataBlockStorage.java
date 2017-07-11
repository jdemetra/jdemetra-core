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

//import ec.tstoolkit.maths.matrices.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DataBlockStorage {

    /**
     *
     * @param capacity
     * @return
     */
    public static int calcSize(final int capacity) {
        return (1 + (capacity - 1) / g_atom) * g_atom;
    }

    private double[] m_data;

    private int m_n, m_nused;

    private final int m_dim;

    private final static int g_atom = 16;

    /**
     *
     * @param dim
     * @param capacity
     */
    public DataBlockStorage(final int dim, final int capacity) {
        m_n = calcSize(capacity);
        m_dim = dim;
        m_data = new double[m_dim * m_n];
    }

    /**
     * Returns the block at the given position
     * @param pos The position of the block
     * @return
     */
    public DataBlock block(final int pos) {
        int start = m_dim * pos;
        return new DataBlock(m_data, start, start + m_dim, 1);
    }

    /**
     * Returns the capacity of the storage (including possible unused data blocks)
     * @return
     */
    public int getCapacity() {
        return m_n;
    }

    /**
     * Returns the current size of the storage (which is the number of used data blocks)
     * @return
     */
    public int getCurrentSize() {
        return m_nused;
    }

    /**
     *
     * @return
     */
    public int getDim() {
        return m_dim;
    }

    /**
     *
     * @param index
     * @return
     */
    public DataBlock item(final int index) {
        return new DataBlock(m_data, index, index + m_dim * m_nused, m_dim);
    }

    /**
     *
     * @param ncapacity
     */
    public void resize(final int ncapacity) {
        int n = calcSize(ncapacity);
        if (n <= m_n) {
            return;
        }
        double[] data = new double[m_dim * n];
        System.arraycopy(m_data, 0, data, 0, m_data.length);
        m_data = data;
        m_n = n;
    }

    /**
     *
     * @param ncapacity
     * @param value
     */
    public void resize(final int ncapacity, final double value) {
        int n = calcSize(ncapacity);
        if (n <= m_n) {
            return;
        }
        double[] data = new double[m_dim * n];
        Arrays.fill(data, m_data.length, data.length, value);
        System.arraycopy(m_data, 0, data, 0, m_data.length);
        m_data = data;
        m_n = n;
    }
    /**
     *
     * @param pos
     * @param rc
     */
    public void save(final int pos, final DataBlock rc) {
        rc.copyTo(m_data, pos * m_dim);
        if (pos >= m_nused) {
            m_nused = pos + 1;
        }
    }

    /**
     *
     * @param pos
     * @param v
     */
    public void save(final int pos, final double[] v) {
        System.arraycopy(v, 0, m_data, pos * m_dim, m_dim);
        if (pos >= m_nused) {
            m_nused = pos + 1;
        }
    }

    /**
     *
     * @param start
     * @param end
     * @return
     */
    public DataBlock storage(int start, int end) {
        int p0 = m_dim * start, p1 = m_dim * end;
        return new DataBlock(m_data, p0, p1, 1);

    }

    public double[] storage() {
        return m_data;
    }

    /**
     * Gets a matrix representation of the saved data blocks, from the position start (included)
     * to the position end (excluded). 
     * The successive data blocks are stored in the columns of the sub-matrix
     * @param start First position (included)
     * @param end Last position (excluded)
     * @return
     */
    public SubMatrix subMatrix(final int start, final int end) {
        return new SubMatrix(m_data, m_dim * start, m_dim, end - start, 1,
                m_dim);
    }
    
    /**
     * Gets a matrix representation of all the saved data blocks. 
     * The successive data blocks are stored in the columns of the sub-matrix
     * @return
     */
    public SubMatrix subMatrix() {
        return new SubMatrix(m_data, 0, m_dim, m_nused, 1,
                m_dim);
    }

    /**
     * Multiplies the current data blocks by a given factor
     * @param factor 
     */
    public void rescale(double factor) {
        if (factor == 1)
            return;
        int n=m_dim*m_nused;
        for (int i=0; i<n; ++i){
            m_data[i]*=factor;
        }
    }
}

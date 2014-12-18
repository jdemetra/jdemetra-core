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

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SmoothingResults {

    private int m_r, m_n;
    private int m_start;

    private DataBlockStorage m_a;

    private MatrixStorage m_P;

    private double[] m_c, m_cvar;

    private boolean m_bP = false, m_bA = true;

    private double m_ser = 1, m_ser2 = 1;

    /**
     *
     */
    public SmoothingResults() {
    }

    /**
     *
     * @param hasData
     * @param hasVar
     */
    public SmoothingResults(boolean hasData, boolean hasVar) {
        m_bA = hasData;
        m_bP = hasVar;
    }

    /**
     *
     * @param idx
     * @return
     */
    public DataBlock A(int idx) {
        return (m_a == null || idx < m_start) ? null : m_a.block(idx-m_start);
    }

    public double getStandardError() {
        return m_ser;
    }

    public void setStandardError(double value) {
        m_ser = value;
        m_ser2 = value * value;
    }

    private int check(DataBlock z) {
        int idx = -1;
        for (int i = 0; i < z.getLength(); ++i) {
            if (z.get(i) != 0) {
                if (idx != -1) {
                    return -1;
                } else {
                    idx = i;
                }
            }
        }
        return idx;
    }

    /**
     *
     */
    public void clear() {
        m_a = null;
        m_P = null;
    }

    /**
     *
     * @param idx
     * @return
     */
    public double[] component(int idx) {
        if (m_a == null) {
            return null;
        }
        double[] c = new double[m_n-m_start];
        m_a.item(idx).copyTo(c, 0);
        return c;
    }

    /**
     *
     * @param i
     * @param j
     * @return
     */
    public double[] componentCovar(int i, int j) {
        if (m_P == null) {
            return null;
        }
        int n=m_n-m_start;
        double[] c = new double[n];
        for (int z = 0; z < n; ++z) {
            c[z] = m_P.matrix(z).get(i, j) * m_ser2;
        }
        return c;
    }

    /**
     *
     * @param idx
     * @return
     */
    public double[] componentStdev(int idx) {
        double[] c = componentVar(idx);
        if (c != null) {
            for (int i = 0; i < m_n; ++i) {
                c[i] = Math.sqrt(c[i]);
            }
        }
        return c;
    }

    /**
     *
     * @param idx
     * @return
     */
    public double[] componentVar(int idx) {
        return componentCovar(idx, idx);
    }

    /**
     *
     * @return
     */
    public int getComponentsCount() {
        return m_r;
    }

    /**
     *
     * @return
     */
    public double[] getSmoothations() {
        return m_c;
    }

    /**
     *
     * @return
     */
    public double[] getSmoothationsVariance() {
        return m_cvar;
    }

    /**
     *
     * @return
     */
    public DataBlockStorage getSmoothedStates() {
        return m_a;
    }

    /**
     *
     * @return
     */
    public MatrixStorage getSmoothedStatesVariance() {
        return m_P;
    }

    /**
     *
     * @return
     */
    public boolean isSavingA() {
        return m_bA;
    }

    /**
     *
     * @return
     */
    public boolean isSavingP() {
        return m_bP;
    }

    /**
     *
     * @param idx
     * @return
     */
    public SubMatrix P(int idx) {
        return (m_P == null || idx < m_start) ? null : m_P.matrix(idx - m_start);
    }

    /**
     *
     * @param n
     * @param r
     */
    public void prepare(int n, int r) {
        int nz = n - m_start;
        m_n = n;
        m_r = r;
        clear();
        if (m_bA) {
            m_a = new DataBlockStorage(m_r, nz);
        }
        if (m_bP) {
            m_P = new MatrixStorage(m_r, nz);
        }
        m_c = new double[nz];
        m_cvar = new double[nz];
    }

    /**
     *
     * @param pos
     * @param a
     * @param p
     * @param c
     * @param cvar
     */
    public void save(int pos, DataBlock a, Matrix p, double c, double cvar) {
        int np = pos - m_start;
        if (np < 0) {
            return;
        }
        if (m_bA) {
            m_a.save(np, a);
        }
        if (m_bP && p != null) {
            m_P.save(np, p);
        }
        m_c[np] = c;
        m_cvar[np] = cvar;
    }

    /**
     *
     * @param value
     */
    public void setSaveA(boolean value) {
        m_bA = value;
        clear();
    }

    /**
     *
     * @param value
     */
    public void setSaveP(boolean value) {
        m_bP = value;
        clear();
    }

    /**
     *
     * @return
     */
    public int getSavingStart() {
        return m_start;
    }

    /**
     *
     * @param start
     */
    public void setSavingStart(int start) {
        m_start = start;
        clear();
    }

    /**
     *
     * @param z
     * @return
     */
    public double[] zcomponent(DataBlock z) {
        int iz = check(z);
        if (iz >= 0) {
            return component(iz);
        }
        if (m_a == null) {
            return null;
        }
        if (m_r != z.getLength()) {
            return null;
        }

        int n = m_n - m_start;
        double[] c = new double[n];
        for (int i = 0; i < n; ++i) {
            c[i] = m_a.block(i).dot(z);
        }
        return c;
    }

    /**
     *
     * @param idx
     * @param z
     * @return
     */
    public double zcomponent(int idx, DataBlock z) {
        return m_a == null || idx < m_start ? Double.NaN : m_a.block(idx - m_start).dot(z);
    }

    /**
     *
     * @param z
     * @return
     */
    public double[] zvariance(DataBlock z) {
        if (m_P == null) {
            return null;
        }
        if (m_r != z.getLength()) {
            return null;
        }
        int iz = check(z);
        double[] var = new double[m_n - m_start];
        if (iz >= 0) {
            return componentVar(iz);
        } else {
            for (int i = 0; i < m_n - m_start; ++i) {
                var[i] = SymmetricMatrix.quadraticForm(m_P.matrix(i), z) * m_ser2;
            }
        }
        return var;
    }

    /**
     *
     * @param idx
     * @param z
     * @return
     */
    public double zvariance(int idx, DataBlock z) {
        return m_P == null || idx < m_start ? Double.NaN : SymmetricMatrix.quadraticForm(m_P.matrix(idx - m_start), z) * m_ser2;
    }
}

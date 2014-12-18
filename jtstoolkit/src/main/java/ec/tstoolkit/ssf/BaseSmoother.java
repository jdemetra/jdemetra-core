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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class BaseSmoother {

    ISsf m_ssf;
    ISsfData m_data;
    int m_pos, m_r;
    DataBlock m_Rf, m_Kf;
    double m_v, m_ff;
    Matrix m_Nf;
    Matrix m_L, m_T;
    boolean m_bCalcVar = false, m_bMissing;
    double m_c, m_cvar;

    /**
     *
     */
    protected BaseSmoother() {
    }

    /**
     *
     */
    protected void clear() {
        m_data = null;
        m_Rf = null;
        m_Kf = null;
        m_Nf = null;
        m_L = null;
        m_T = null;
    }

    /**
     *
     * @return
     */
    protected abstract FilteredData getFilteredData();

    /**
     *
     * @return
     */
    public ISsf getSsf() {
        return m_ssf;
    }

    /**
     *
     * @return
     */
    protected abstract VarianceFilter getVarianceFilter();

    /**
     *
     */
    protected void initSmoother() {
        m_pos = m_data.getCount() - 1;
        m_r = m_ssf.getStateDim();
        if (m_data.hasData()) {
            m_Rf = new DataBlock(m_r);
        }
        m_Kf = new DataBlock(m_r);
        if (m_bCalcVar) {
            m_Nf = new Matrix(m_r, m_r);
            m_L = new Matrix(m_r, m_r);
            m_T = new Matrix(m_r, m_r);
        }
    }

    /**
     *
     * @return
     */
    public boolean isCalcVar() {
        return m_bCalcVar;
    }

    /**
     *
     */
    protected void iterateN() {
        if (!m_bMissing && m_ff!= 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
	    /*
             * m_ssf.L(m_pos, m_Kf, m_L.SubMatrix()); m_Nf =
             * SymmetricMatrix.QuadraticForm(m_Nf, m_L); m_ssf.VpZdZ(m_pos,
             * m_Nf.SubMatrix(), 1 / m_ff);
             */

            // Optimized version...
            double vkv = SymmetricMatrix.quadraticForm(m_Nf, m_Kf);
            m_cvar = vkv + 1 / m_ff;

            DataBlockIterator nrows = m_Nf.rows();
            DataBlockIterator ncols = m_Nf.columns();
            DataBlock nrow = nrows.getData();
            DataBlock ncol = ncols.getData();
            // compute N*T (in place)
            do {
                m_ssf.XT(m_pos, nrow);
            } while (nrows.next());
            nrows.begin();

            // compute K'VT
            // compute (XT)'*T. (XT)' rows are the columns of XT
            DataBlock kvt = new DataBlock(m_r);
            do {
                kvt.set(ncols.getPosition(), m_Kf.dot(ncol));
                m_ssf.XT(m_pos, ncol);
            } while (ncols.next());
            ncols.begin();

            do {
                double k = kvt.get(nrows.getPosition());
                if (k != 0) {
                    m_ssf.XpZd(m_pos, nrow, -k);
                    m_ssf.XpZd(m_pos, ncol, -k);
                }
            } while (nrows.next() && ncols.next());

            m_ssf.VpZdZ(m_pos, m_Nf.subMatrix(), m_cvar);
            SymmetricMatrix.reinforceSymmetry(m_Nf);
        } else {
            // m_Nf = SymmetricMatrix.QuadraticForm(m_Nf, m_T);
            DataBlockIterator nrows = m_Nf.rows();
            DataBlockIterator ncols = m_Nf.columns();
            DataBlock nrow = nrows.getData();
            DataBlock ncol = ncols.getData();
            do {
                m_ssf.XT(m_pos, nrow);
            } while (nrows.next());
            do {
                m_ssf.XT(m_pos, ncol);
            } while (ncols.next());
            SymmetricMatrix.reinforceSymmetry(m_Nf);
        }

    }

    /**
     *
     */
    protected void iterateR() {
        // R(t-1)=(v(t)/f(t)-R(t)*K(t))*Z(t)+R(t)*T(t)
        if (!m_bMissing && m_ff!=0) {
            m_c = m_v / m_ff - m_Rf.dot(m_Kf);
            m_ssf.XT(m_pos, m_Rf);
            m_ssf.XpZd(m_pos, m_Rf, m_c);
        } else {
            m_c = 0;
            m_ssf.XT(m_pos, m_Rf);
        }
    }

    /**
     *
     */
    protected void loadInfo() {
        m_bMissing = getVarianceFilter().isMissing(m_pos);
        m_ff = getVarianceFilter().F(m_pos);
        if (!m_bMissing && m_ff != 0) {
            m_v = getFilteredData().E(m_pos);
            m_Kf.copy(getVarianceFilter().C(m_pos));
            m_Kf.mul(1 / m_ff);
        } else {
            m_v = 0;
        }
    }

    /**
     *
     */
    protected void loadModelInfo() {
        if (m_T != null) {
            m_T.set(0);
            m_ssf.T(m_pos, m_T.subMatrix());
        }
    }

    /**
     *
     * @param value
     */
    public void setCalcVar(boolean value) {
        m_bCalcVar = value;
    }

    /**
     *
     * @param value
     */
    public void setSsf(ISsf value) {
        m_ssf = value;
        clear();
    }
}

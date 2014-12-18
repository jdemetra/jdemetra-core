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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BaseDiffuseSmoother extends BaseSmoother {

    DiffuseFilteringResults m_frslts;
    int m_ndiffuse, m_enddiffuse;
    DataBlock m_Ri, m_Ki, m_Z, m_tmp0, m_tmp1;
    double m_fi;
    Matrix m_N2, m_V;
    Matrix m_N1;

    /**
     *
     */
    protected BaseDiffuseSmoother() {
    }

    /**
     *
     */
    @Override
    protected void clear() {
        super.clear();
        m_frslts = null;
        m_Ri = null;
        m_Ki = null;
        m_Z = null;
        m_tmp0 = null;
        m_tmp1 = null;
        m_N2 = null;
        m_V = null;
        m_N1 = null;
    }

    /**
     *
     * @return
     */
    public int getDiffuseCount() {
        return m_ndiffuse;
    }

    /**
     *
     * @return
     */
    public int getEndDiffusePosition() {
        return m_enddiffuse;
    }

    /**
     *
     * @return
     */
    @Override
    protected FilteredData getFilteredData() {
        return m_frslts.getFilteredData();
    }

    /**
     *
     * @return
     */
    public DiffuseFilteringResults getFilteringResults() {
        return m_frslts;
    }

    /**
     *
     * @return
     */
    @Override
    protected VarianceFilter getVarianceFilter() {
        return m_frslts.getVarianceFilter();
    }

    /**
     *
     */
    @Override
    protected void initSmoother() {
        super.initSmoother();
        m_ndiffuse = m_frslts.getDiffuseCount();
        m_enddiffuse = m_frslts.getEndDiffusePosition();
        m_tmp0 = new DataBlock(m_r);
        if (m_bCalcVar) {
            m_V = new Matrix(m_r, m_r);
        }
        if (m_enddiffuse > 0) {
            m_Ki = new DataBlock(m_r);
            if (m_data.hasData()) {
                m_Ri = new DataBlock(m_r);
            }
            if (m_bCalcVar) {
                m_N1 = new Matrix(m_r, m_r);
                m_N2 = new Matrix(m_r, m_r);
                m_Z = new DataBlock(m_r);
                m_tmp1 = new DataBlock(m_r);
            }
        }
    }

    /**
     *
     */
    protected void iterateInitialMissing() {
        SubMatrix t = m_T.subMatrix(), tt = t.transpose();
        m_N2.subMatrix().xmy(tt, t);
        m_N1.subMatrix().xmy(tt, t);
        m_Nf.subMatrix().xmy(tt, t);
    }

    /**
     *
     */
    protected void iterateInitialN() {
        if (m_bMissing || (m_fi == 0 && m_ff == 0)) {
            iterateInitialMissing();
        } else if (m_fi != 0) {
            iterateInitialN0();
        } else {
            iterateInitialN1();
        }
    }

    /**
     *
     */
    protected void iterateInitialN0() {
        m_L.set(0);
        m_ssf.L(m_pos, m_Ki, m_L.subMatrix());
        // Nf = Li'*Nf*Li
        // N1 = Z'Z/Fi + Li'*N1*Li - < Z'Kf'*Nf'*Li >
        // N2 = Z'Z * c + Li'*N2*Li - < Z'Kf'*N1'*Li >, c= Kf'*Nf*Kf-Ff/(Fi*Fi)
        // compute first N2 then N1 and finally Nf
        double c = SymmetricMatrix.quadraticForm(m_Nf, m_Kf) - m_ff
                / (m_fi * m_fi);
        // N2=Li'*N2*Li
        m_N2 = SymmetricMatrix.quadraticForm(m_N2, m_L);
        // compute K'* N1' in tmp0
        m_tmp0.product(m_N1.rows(), m_Kf);
        // compute K'* N1' *Li
        m_tmp1.product(m_tmp0, m_L.columns());
        for (int i = 0; i < m_r; ++i) {
            for (int j = 0; j <= i; ++j) {
                double x = 0, zi = m_Z.get(i), zj = m_Z.get(j), li = m_tmp1
                        .get(i), lj = m_tmp1.get(j);
                if (zi != 0 && zj != 0) {
                    x = c * zi * zj - zi * lj - zj * li;
                } else if (zi != 0) {
                    x = -zi * lj;
                } else if (zj != 0) {
                    x = -zj * li;
                }
                if (x != 0) {
                    m_N2.add(i, j, x);
                    if (i != j) {
                        m_N2.add(j, i, x);
                    }
                }
            }
        }

        m_N1.subMatrix().xmy(m_L.subMatrix().transpose(), m_L.subMatrix());
        // compute K'* Nf
        m_tmp0.product(m_Kf, m_Nf.columns());
        // compute K'* Nf *Li
        m_tmp1.product(m_tmp0, m_L.columns());
        for (int i = 0; i < m_r; ++i) {
            for (int j = 0; j <= i; ++j) {
                double x = 0, zi = m_Z.get(i), zj = m_Z.get(j), li = m_tmp1
                        .get(i), lj = m_tmp1.get(j);
                if (zi != 0 && zj != 0) {
                    x = zi * zj / m_fi - zi * lj - zj * li;
                } else if (zi != 0) {
                    x = -zi * lj;
                } else if (zj != 0) {
                    x = -zj * li;
                }
                if (x != 0) {
                    m_N1.add(i, j, x);
                    if (i != j) {
                        m_N1.add(j, i, x);
                    }
                }
            }
        }
        m_Nf = SymmetricMatrix.quadraticForm(m_Nf, m_L);
        SymmetricMatrix.reinforceSymmetry(m_Nf);
        // should be modified
        m_cvar = 0;
    }

    /**
     *
     */
    protected void iterateInitialN1() {
        // Nf(t-1) = Z'(t)*Z(t)/f(t) + Lf'(t)*Nf(t)*Lf(t)
        m_L.set(0);
        SubMatrix l = m_L.subMatrix(), t = m_T.subMatrix(), tt = t.transpose(), lt = l.transpose();
        m_ssf.L(m_pos, m_Kf, l);
        m_Nf.subMatrix().xmy(lt, l);
        m_ssf.VpZdZ(m_pos, m_Nf.subMatrix(), 1 / m_ff);
        SymmetricMatrix.reinforceSymmetry(m_Nf);
        m_N2.subMatrix().xmy(tt, t);
        // compute N1=T'*N1*L
        m_N1.subMatrix().xmy(tt, l);
    }

    /**
     *
     */
    protected void iterateInitialR() {
        // case m_fi != 0

        // Ri(t-1)=c*Z(t)+Ri(t)*T(t)
        // c = v/fi - Ri(t)*Ki - Rf(t)*Kf
        if (m_fi != 0) {
            if (!m_bMissing) {
                double c = (m_v - m_fi * (m_Ri.dot(m_Ki) + m_Rf.dot(m_Kf)))
                        / m_fi;
                m_ssf.XT(m_pos, m_Ri);
                m_ssf.XpZd(m_pos, m_Ri, c);
            } else {
                m_ssf.XT(m_pos, m_Ri);
            }

            // Rf(t-1)=c*Z(t)+Rf(t)*T(t)
            // c = -Rf(t)*Ki
            if (!m_bMissing) {
                m_c = -m_Rf.dot(m_Ki);
                m_ssf.XT(m_pos, m_Rf);
                m_ssf.XpZd(m_pos, m_Rf, m_c);
            } else {
                m_c = 0;
                m_ssf.XT(m_pos, m_Rf);
            }
        } else {
            m_ssf.XT(m_pos, m_Ri);
            iterateR();
        }
    }

    /**
     *
     */
    @Override
    protected void loadInfo() {
        m_bMissing = getVarianceFilter().isMissing(m_pos);
        m_ff = getVarianceFilter().F(m_pos);
        if (!m_bMissing) {
            m_v = getFilteredData().E(m_pos);
            m_Kf.copy(getVarianceFilter().C(m_pos));

            if (m_pos >= m_enddiffuse) {
                if (m_ff != 0) {
                    m_Kf.mul(1 / m_ff);
                }
            } else {
                m_fi = m_frslts.getVarianceFilter().Fi(m_pos);
                // update Kf, Ki
                if (m_fi == 0) {
                    if (m_ff != 0) {
                        m_Kf.mul(1 / m_ff);
                    }
                } else {
                    // Kf=Cf/fi-Ci*ff/(fi*fi), Ki=Ci/fi
                    m_Ki.copy(m_frslts.getVarianceFilter().Ci(m_pos));
                    m_Ki.mul(1 / m_fi);
                    m_Kf.addAY(-m_ff, m_Ki);
                    m_Kf.mul(1 / m_fi);
                }
            }
        } else {
            m_v = 0;
        }
    }

    /**
     *
     */
    @Override
    protected void loadModelInfo() {
        super.loadModelInfo();
        if (m_pos < m_enddiffuse) {
            if (m_Z != null) {
                m_Z.set(0);
                m_ssf.Z(m_pos, m_Z);
            }
        }
    }
}

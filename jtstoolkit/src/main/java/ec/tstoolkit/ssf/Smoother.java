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
import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Smoother extends BaseDiffuseSmoother {

    private DataBlock m_a;

    private SubMatrix m_Pf, m_Pi;

    private Matrix m_Vtmp0, m_Vtmp1;

    private SmoothingResults m_srslts;

    private int m_stop = 0;

    /**
     *
     */
    public Smoother() {
    }

    /**
     *
     * @param stop
     */
    public void setStopPosition(int stop) {
        m_stop = stop;
    }

    /**
     *
     * @return
     */
    public int getStopPosition() {
        return m_stop;
    }

    /**
     *
     */
    @Override
    protected void clear() {
        super.clear();
        m_a = null;
        m_Pf = null;
        m_Pi = null;
        m_Vtmp0 = null;
        m_Vtmp1 = null;
        m_srslts = null;
    }

    /**
     *
     */
    @Override
    protected void initSmoother() {
        super.initSmoother();
        m_srslts.prepare(m_data.getCount(), m_r);
        if (m_data.hasData()) {
            m_a = new DataBlock(m_r);
        }else
            m_a=DataBlock.EMPTY;
        if (m_bCalcVar) {
            m_Vtmp0 = new Matrix(m_r, m_r);
            m_Vtmp1 = new Matrix(m_r, m_r);
        }
    }

    /**
     *
     */
    protected void iterateSmoother() {
        if (m_pos >= m_enddiffuse) {
            if (m_a.getLength() > 0) {
                iterateR();
                m_tmp0.product(m_Rf, m_Pf.columns());
                m_a.add(m_tmp0);
            }
            if (m_bCalcVar) {
                iterateN();
                SymmetricMatrix.quadraticForm(m_Nf.subMatrix(), m_Pf, m_V
                        .subMatrix());
                m_V.chs();
                m_V.subMatrix().add(m_Pf);
            }
        } else {
            if (m_a.getLength() > 0) {
                iterateInitialR();
                m_tmp0.product(m_Rf, m_Pf.columns());
                m_a.add(m_tmp0);
                m_tmp0.product(m_Ri, m_Pi.columns());
                m_a.add(m_tmp0);
            }
            if (m_bCalcVar) {
                // V = Pf - Pf * Nf * Pf - < Pi * N1 * Pf > - Pi * N2 * Pi
                iterateInitialN();
                SymmetricMatrix.quadraticForm(m_Nf.subMatrix(), m_Pf, m_V
                        .subMatrix());
                SymmetricMatrix.quadraticForm(m_N2.subMatrix(), m_Pi, m_Vtmp0
                        .subMatrix());
                m_V.add(m_Vtmp0);
                m_Vtmp0.subMatrix().product(m_N1.subMatrix(), m_Pf);
                m_Vtmp1.subMatrix().product(m_Pi, m_Vtmp0.subMatrix());
                m_V.add(SymmetricMatrix.XpXt(m_Vtmp1));
                m_V.chs();
                m_V.subMatrix().add(m_Pf);

            }

        }
        // a = a + r*P
    }

    /**
     *
     */
    @Override
    protected void loadInfo() {
        super.loadInfo();
        m_Pf = m_frslts.getVarianceFilter().P(m_pos);
        if (m_pos < m_enddiffuse) {
            m_Pi = m_frslts.getVarianceFilter().Pi(m_pos);
        }
        if (m_a.getLength() != 0) {
            m_a.copy(m_frslts.getFilteredData().A(m_pos));
        }

    }

    /**
     *
     * @param data
     * @param frslts
     * @param rslts
     * @return
     */
    public boolean process(final ISsfData data,
            final DiffuseFilteringResults frslts, final SmoothingResults rslts) {
        clear();
        if (m_ssf == null) {
            return false;
        }
        m_data = data;
        m_frslts = frslts;
        m_srslts = rslts;
        m_srslts.setSaveP(m_bCalcVar);
        initSmoother();
        if (m_ssf.isTimeInvariant()) {
            loadModelInfo();
        }
        while (m_pos >= m_stop) {
            if (!m_ssf.isTimeInvariant() || m_pos == m_enddiffuse - 1) {
                loadModelInfo();
            }
            loadInfo();
            iterateSmoother();
            m_srslts.save(m_pos, m_a, m_V, m_c, m_cvar);
            --m_pos;
        }
        if (m_bCalcVar) {
            DiffuseLikelihood ll = new DiffuseLikelihood();
            LikelihoodEvaluation.evaluate(frslts, ll);
            double ser = ll.getSer();
            m_srslts.setStandardError(ser);
        }

        return true;
    }

    /**
     *
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsfData data, final SmoothingResults rslts) {
        if (m_ssf == null) {
            return false;
        }
        DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
        frslts.getVarianceFilter().setSavingP(true);
        frslts.getFilteredData().setSavingA(data.hasData());
        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(m_ssf);
        if (!filter.process(data, frslts)) {
            return false;
        }
        return process(data, frslts, rslts);
    }
}

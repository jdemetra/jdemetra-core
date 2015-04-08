/*
 * Copyright 2014 National Bank of Belgium
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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * The fixed point smoother computes the expectations and the covariance matrices 
 * of [M*a(fixpos) | fixpos + k].
 * The ordinary filter is used till position fixpos, where E(a(fixpos)|fixpos), 
 * Cov((a(fixpos)|fixpos)) is available. 
 * The moments of the augmented state vector a(fixpos), Ma(fixpos) canthen be easily
 * derived. The augmented vector is then used to compute the next expectations/cov.
 * See for instance Anderson and Moore (optimal filtering [1979]).
 * @author Jean Palate
 */
public class FixedPointSmoother {

    private final ISsf m_ssf;
    private final int m_fixpos;
    private final Matrix m_m;
    private Matrix m_p0;
    private double[] m_a0;
    private DataBlockStorage m_states;
    private MatrixStorage m_vstates;
    private final int m_r;

    public boolean process(ISsfData data) {
        // step 1: filtering till fixpos, smoothing the last pos
        SsfDataWindow d0 = new SsfDataWindow(data, data.getInitialState(), 0, m_fixpos + 1);
        Smoother smoother = new Smoother();
        smoother.setSsf(m_ssf);
        smoother.setCalcVar(true);
        smoother.setStopPosition(m_fixpos);
        SmoothingResults sm = new SmoothingResults(data.hasData(), true);
        sm.setSavingStart(m_fixpos);
        if (!smoother.process(d0, sm)) {
            return false;
        }
        DataBlock A = sm.A(m_fixpos);
        if (A != null) {
            if (m_m == null) {
                m_a0 = new double[2 * m_r];
                A.copyTo(m_a0, 0);
                A.copyTo(m_a0, m_r);
            } else {
                m_a0 = new double[m_r + m_m.getRowsCount()];
                A.copyTo(m_a0, 0);
                DataBlock ma = new DataBlock(m_a0, m_r, m_a0.length, 1);
                ma.product(m_m.rows(), A);
            }
        } else {
            m_a0 = null;
        }
        SubMatrix P = sm.P(m_fixpos);
        if (m_m == null) {
            m_p0 = new Matrix(2 * m_r, 2 * m_r);
            m_p0.subMatrix(0, m_r, 0, m_r).copy(P);
            m_p0.subMatrix(0, m_r, m_r, 2 * m_r).copy(P);
            m_p0.subMatrix(m_r, 2 * m_r, 0, m_r).copy(P);
            m_p0.subMatrix(m_r, 2 * m_r, m_r, 2 * m_r).copy(P);
        } else {
            int nm = m_m.getRowsCount();
            m_p0 = new Matrix(m_r + nm, m_r + nm);
            SubMatrix P00=m_p0.subMatrix(0, m_r, 0, m_r), P10=m_p0.subMatrix(m_r, m_r + nm, 0, m_r),
                    P01=m_p0.subMatrix(0, m_r, m_r, m_r + nm), P11= m_p0.subMatrix(m_r, m_r + nm, m_r, m_r + nm);
            P00.copy(P);
            P10.product(m_m.subMatrix(), P);
            P01.copy(P10.transpose());
            SymmetricMatrix.quadraticFormT(P, m_m.subMatrix(), P11);
        }
        FilteringResults frslts = new FilteringResults();
        frslts.getVarianceFilter().setSavingP(true);
        frslts.getFilteredData().setSavingA(true);
        int nf = data.getCount() - m_fixpos + 1;
        SsfDataWindow d1 = new SsfDataWindow(data, m_a0, m_fixpos, nf);
        Filter<ISsf> filter = new Filter<>();
        filter.setSsf(new XSsf());
        if (!filter.process(d1, frslts)) {
            return false;
        }

            int nm=m_m == null ? m_r : m_m.getRowsCount();
        if (data.hasData()) {
            m_states = new DataBlockStorage(nm, nf);
        }
        m_vstates = new MatrixStorage(nm, nf);
        for (int i = 0; i < d1.getCount(); ++i) {
            if (data.hasData()) {
                m_states.save(i, frslts.getFilteredData().A(i + 1).drop(m_r, 0));
            }
            m_vstates.save(i, frslts.getVarianceFilter().P(i + 1).extract(m_r, m_r+nm, m_r, m_r+nm));
        }
        return true;
    }

    /**
     * Returns the expectations of the augmented part of the filter at position
     * pos, computed after the fixed point position.
     * The size of the augmented part is either the size of the underlying state
     * space model or the number of rows of the M matrix that defines the linear
     * transformation of the state vector considered in the smoothing algorithm.
     * @param pos The position of the requested information (corresponds to fixdpos+pos)
     * @return The expectations vector. Should not be modified
     */
    public DataBlock A(int pos) {
        return m_states == null ? null : m_states.block(pos);
    }
    
    /**
     * Returns the covariance of the expectations of the augmented part of the filter 
     * at position pos, computed after the fixed point position.
     * The size of the augmented part is either the size of the underlying state
     * space model or the number of rows of the M matrix that defines the linear
     * transformation of the state vector considered in the smoothing algorithm.
     * @param pos The position of the requested information (corresponds to fixdpos+pos)
     * @return The covariance matrix. Should not be modified
     */
    public SubMatrix P(int pos) {
        return m_vstates.matrix(pos);
    }

    /**
     * Defines the smoother. The complete state array will be considered in the smoothing
     * @param ssf The original state space form
     * @param fixpos The position of the fixed point
     */
    public FixedPointSmoother(final ISsf ssf, final int fixpos) {
        m_ssf = ssf;
        m_fixpos = fixpos;
        m_r = m_ssf.getStateDim();
        m_m = null;
    }

    /**
     * Defines the smoother. The state array transformed by M will be considered in the smoothing
     * @param ssf The original state space form
     * @param fixpos The position of the fixed point
     * @param M The transformation matrix. May be null; in that case, M is considered to be I.
     */
    public FixedPointSmoother(final ISsf ssf, final int fixpos, final Matrix M) {
        
        if (M != null && ssf.getStateDim() != M.getColumnsCount())
            throw new SsfException("Invalid fixed point argument");
        m_ssf = ssf;
        m_fixpos = fixpos;
        m_r = m_ssf.getStateDim();
        m_m = M;
    }
    
    public Matrix getTransformationMatrix(){
        return m_m;
    }
    
    public int getFixedPointPosition(){
        return m_fixpos;
    }

    private class XSsf extends AbstractSsf {

        private XSsf() {
        }

        /**
         *
         * @param b
         */
        @Override
        public void diffuseConstraints(final SubMatrix b) {
        }

        /**
         *
         * @param pos
         * @param qm
         */
        @Override
        public void fullQ(final int pos, final SubMatrix qm) {
            m_ssf.fullQ(pos, qm.extract(0, m_r, 0, m_r));
        }

        /**
         *
         * @return
         */
        @Override
        public int getNonStationaryDim() {
            return 0;
        }

        /**
         *
         * @return
         */
        @Override
        public int getStateDim() {
            return m_m == null ? 2 * m_r : m_r + m_m.getRowsCount();
        }

        /**
         *
         * @return
         */
        @Override
        public int getTransitionResCount() {
            return getStateDim();
        }

        /**
         *
         * @return
         */
        @Override
        public int getTransitionResDim() {
            return getStateDim();
        }

        /**
         *
         * @return
         */
        @Override
        public boolean hasR() {
            return false;
        }

        /**
         *
         * @param pos
         * @return
         */
        @Override
        public boolean hasTransitionRes(final int pos) {
            return m_ssf.hasTransitionRes(pos);
        }

        /**
         *
         * @return
         */
        @Override
        public boolean hasW() {
            return false;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isDiffuse() {
            return false;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isMeasurementEquationTimeInvariant() {
            return m_ssf.isMeasurementEquationTimeInvariant();
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isTimeInvariant() {
            return m_ssf.isTimeInvariant();
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isTransitionEquationTimeInvariant() {
            return m_ssf.isTransitionEquationTimeInvariant();
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isTransitionResidualTimeInvariant() {
            return m_ssf.isTransitionResidualTimeInvariant();
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isValid() {
            return m_ssf.isValid();
        }

        /**
         *
         * @param pf0
         */
        @Override
        public void Pf0(final SubMatrix pf0) {
            pf0.copy(m_p0.subMatrix());
        }

        /**
         *
         * @param pi0
         */
        @Override
        public void Pi0(final SubMatrix pi0) {
        }

        /**
         *
         * @param pos
         * @param qm
         */
        @Override
        public void Q(final int pos, final SubMatrix qm) {
            fullQ(pos, qm);
        }

        /**
         *
         * @param pos
         * @param rv
         */
        @Override
        public void R(final int pos, final SubArrayOfInt rv) {
        }

        /**
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(final int pos, final SubMatrix tr) {
            m_ssf.T(pos, tr.extract(0, m_r, 0, m_r));
            //tr.extract(m_r, 2 * m_r, m_r, 2 * m_r).diagonal().set(1);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            m_ssf.TX(pos, x.range(0, m_r));
        }

        /**
         *
         * @param pos
         * @param vm
         * @param d
         */
        @Override
        public void VpZdZ(final int pos, final SubMatrix vm, final double d) {
            m_ssf.VpZdZ(pos, vm.extract(0, m_r, 0, m_r), d);
        }

        /**
         *
         * @param pos
         * @param wv
         */
        @Override
        public void W(final int pos, final SubMatrix wv) {
        }

        /**
         *
         * @param pos
         * @param x
         * @param d
         */
        @Override
        public void XpZd(final int pos, final DataBlock x, final double d) {
            m_ssf.XpZd(pos, x.range(0, m_r), d);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            m_ssf.XT(pos, x.range(0, m_r));
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void Z(final int pos, final DataBlock x) {
            m_ssf.Z(pos, x.range(0, m_r));
        }

        /**
         *
         * @param pos
         * @param vm
         * @return
         */
        @Override
        public double ZVZ(final int pos, final SubMatrix vm) {
            return m_ssf.ZVZ(pos, vm.extract(0, m_r, 0, m_r));
        }

        /**
         *
         * @param pos
         * @param x
         * @return
         */
        @Override
        public double ZX(final int pos, final DataBlock x) {
            return m_ssf.ZX(pos, x.range(0, m_r));
        }
    }
}

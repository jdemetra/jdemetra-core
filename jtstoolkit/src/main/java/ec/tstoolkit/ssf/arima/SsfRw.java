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
package ec.tstoolkit.ssf.arima;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * State space form for a random walk y(t) = y(t-1) + e(t).
 * 
 * The class is designed to handle models initialized by zero.
 * State: a(t) = [y(t)]'
 * Measurement: Z(t) = 1
 * Transition: T(t) = | 1 |
 * Innovations: V(t) = | 1 |
 * Initialization: default:
 * Pi0 = | 1 |
 * Pf0 = | 1 |
 * 0-initialization
 * Pi0 = | 0 |
 * Pf0 = | 1 |
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfRw implements ISsf {

    private boolean m_zeroinit;

    public SsfRw() {
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        Pi0(b);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(0, 0, 1);
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return m_zeroinit ? 0 : 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        return 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        return 1;
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
    public boolean hasTransitionRes(int pos) {
        return true;
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
        return !m_zeroinit;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isUsingZeroInitialization() {
        return m_zeroinit;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        lm.set(0, 0, 1 - k.get(0));
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        //pf0.set(0, 0, 1);
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        if (!m_zeroinit) {
            pi0.set(0, 0, 1);
        }
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(int pos, SubMatrix qm) {
        qm.set(0, 0, 1);
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(0, 0, 1);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(int pos, SubMatrix vm) {
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
    }

    /**
     *
     * @param value
     */
    public void useZeroInitialization(boolean value) {
        m_zeroinit = value;
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        vm.add(d);
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(int pos, SubMatrix wv) {
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        x.add(d);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void Z(int pos, DataBlock x) {
        x.set(0, 1);
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.copy(m.row(0));
    }

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        return vm.get(0, 0);
    }

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(int pos, DataBlock x) {
        return x.get(0);
    }
}

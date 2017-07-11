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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WeightedSsf<S extends ISsf> implements ISsf {

    private final S ssf_;
    private final double[] w_;

    /**
     *
     * @param w
     * @param ssf
     */
    public WeightedSsf(double[] w, S ssf) {
        ssf_ = ssf;
        w_ = w;
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        ssf_.diffuseConstraints(b);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        ssf_.fullQ(pos, qm.extract(1, qm.getRowsCount(), 1, qm.getColumnsCount()));
    }

    /**
     *
     * @return
     */
    public S getInternalSsf() {
        return ssf_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return ssf_.getNonStationaryDim();
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return ssf_.getStateDim();
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        return ssf_.getTransitionResCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        return ssf_.getTransitionResDim();
    }

    /**
     *
     * @return
     */
    public double[] getWeights() {
        return w_;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        return ssf_.hasR();
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(int pos) {
        return ssf_.hasTransitionRes(pos);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasW() {
        return ssf_.hasW();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
        return ssf_.isDiffuse();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return ssf_.isTransitionResidualTimeInvariant();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return ssf_.isValid();
    }

    /**
     * T-KZ = T-KZold/w
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        DataBlock kc = k.deepClone();
        double w = weight(pos);
        kc.mul(w);
        ssf_.L(pos, kc, lm);

    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        ssf_.Pf0(pf0);
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        ssf_.Pi0(pi0);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(int pos, SubMatrix qm) {
        ssf_.Q(pos, qm);
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(int pos, SubArrayOfInt rv) {
        if (ssf_.hasR()) {
            ssf_.R(pos, rv);
        }
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        ssf_.T(pos, tr);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(int pos, SubMatrix vm) {
        ssf_.TVT(pos, vm);
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        double w = weight(pos);
        ssf_.VpZdZ(pos, vm, w * w * d);
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(int pos, SubMatrix wv) {
        ssf_.W(pos, wv);
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        double w = weight(pos);
        ssf_.XpZd(pos, x, w * d);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
        ssf_.XT(pos, x);
    }

    /**
     *
     * @param pos
     * @param z
     */
    @Override
    public void Z(int pos, DataBlock z) {
        ssf_.Z(pos, z);
        z.mul(weight(pos));
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        double w = weight(pos);
        DataBlockIterator c = m.columns();
        DataBlock col = c.getData();
        do {
            x.set(c.getPosition(), w * ssf_.ZX(pos, col));
        } while (c.next());
    }

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        double w = weight(pos);
        return w * w * ssf_.ZVZ(pos, vm);
    }

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(int pos, DataBlock x) {
        return mweight(pos, ssf_.ZX(pos, x));
    }

    private double mweight(int pos, double m) {
        return w_ == null ? m : w_[pos] * m;
    }

    private double weight(int pos) {
        return w_ == null ? 1 : w_[pos];
    }

    @Override
    public void TX(int pos, DataBlock x) {
        ssf_.TX(pos, x);
    }

}

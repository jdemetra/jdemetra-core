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

package ec.benchmarking.ssf;

import ec.benchmarking.BaseDisaggregation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * 
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class WeightedSsfDisaggregation<S extends ISsf> extends BaseDisaggregation
        implements ISsf {

    private S ssf_;
    private double[] w_;

    /**
     * 
     * @param conv
     * @param w
     * @param ssf
     */
    public WeightedSsfDisaggregation(int conv, double[] w, S ssf) {
        super(conv);
        ssf_ = ssf;
        w_ = w;
    }

    /**
     * 
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        ssf_.diffuseConstraints(b.extract(1, b.getRowsCount(), 0, b.getColumnsCount()));
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
        return 1 + ssf_.getStateDim();
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
        return true;
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
        return true;
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
     * 
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        double w = weight(pos);
        SubMatrix l = lm.extract(1, lm.getRowsCount(), 1, lm.getColumnsCount());
        ssf_.L(pos, k.drop(1, 0), l);

        DataBlock c0 = lm.column(0);
        c0.set(0);
        double s = -k.get(0);
        if (pos % conversion != 0) {
            if ((pos + 1) % conversion != 0) {
                c0.set(0, 1);
            }
            c0.sub(k);
        }
        if ((pos + 1) % conversion != 0) {
            s += 1;
        }
        DataBlock r0 = lm.row(0).drop(1, 0);
        r0.set(0);
        ssf_.XpZd(pos, r0, s * w);
    }

    /**
     * 
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        ssf_.Pf0(pf0.extract(1, pf0.getRowsCount(), 1, pf0.getColumnsCount()));
    }

    /**
     * 
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        ssf_.Pi0(pi0.extract(1, pi0.getRowsCount(), 1, pi0.getColumnsCount()));
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
            for (int i = 0; i < rv.getLength(); ++i) {
                rv.add(i, 1);
            }
        } else {
            int n = ssf_.getStateDim();
            for (int i = 0; i < n; ++i) {
                rv.add(i, i + 1);
            }
        }
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        ssf_.T(pos, tr.extract(1, tr.getRowsCount(), 1, tr.getColumnsCount()));
        if ((pos + 1) % conversion != 0) {
            DataBlock t0 = tr.row(0).drop(1, 0);
            ssf_.Z(pos, t0);
            t0.mul(weight(pos));
            if (pos % conversion != 0) {
                tr.set(0, 0, 1);
            }
        }
    }

    /**
     * 
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(int pos, SubMatrix vm) {
        /*
         * double w = m_w[pos]; SubMatrix v = vm.Extract(1, vm.RowsCount, 1,
         * vm.ColumnsCount); if (pos % conversion == 0) { DataBlock v0 =
         * vm.Row(0).Drop(1, 0); m_ssf.ZM(pos, v, v0); vm[0, 0] = m_ssf.ZX(pos,
         * v0); m_ssf.TX(pos, v0); vm.Column(0).Drop(1, 0).Copy(v0); } else if
         * ((pos + 1) % conversion != 0) { DataBlock r0 = vm.Row(0).Drop(1, 0);
         * double zv0 = m_ssf.ZX(pos, r0); m_ssf.ZM(pos, v, r0); vm.Add(0, 0, 2
         * * zv0 + m_ssf.ZX(pos, r0)); m_ssf.TX(pos, r0); DataBlock c0 =
         * vm.Column(0).Drop(1, 0); m_ssf.TX(pos, c0); c0.Add(r0); r0.Copy(c0);
         * } else { vm.Row(0).Set(0); vm.Column(0).Set(0); } m_ssf.TVT(pos, v);
         */
        // default implementation
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            TX(pos, col);
        } while (cols.next());
        DataBlockIterator rows = vm.rows();
        DataBlock row = rows.getData();
        do {
            TX(pos, row);
        } while (rows.next());

    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        DataBlock xc = x.drop(1, 0);

        if ((pos + 1) % conversion != 0) {
            double s = ssf_.ZX(pos, xc);
            double w = weight(pos);
            if (pos % conversion == 0) {
                x.set(0, s * w);
            } else {
                x.add(0, s * w);
            }
        } else {
            x.set(0, 0);
        }
        ssf_.TX(pos, xc);
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
        SubMatrix v = vm.extract(1, vm.getRowsCount(), 1, vm.getColumnsCount());
        ssf_.VpZdZ(pos, v, w * w * d);
        if (pos % conversion != 0) {
            vm.add(0, 0, d);
            ssf_.XpZd(pos, vm.column(0).drop(1, 0), w * d);
            ssf_.XpZd(pos, vm.row(0).drop(1, 0), w * d);
        }
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
        ssf_.XpZd(pos, x.drop(1, 0), w * d);
        if (pos % conversion != 0) {
            x.add(0, d);
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
        DataBlock xc = x.drop(1, 0);
        double w = weight(pos);
        ssf_.XT(pos, xc);
        if ((pos + 1) % conversion != 0) {
            ssf_.XpZd(pos, xc, x.get(0) * w);
            if (pos % conversion == 0) {
                x.set(0, 0);
            }
        } else {
            x.set(0, 0);
        }
    }

    /**
     * 
     * @param pos
     * @param z
     */
    @Override
    public void Z(int pos, DataBlock z) {
        if (pos % conversion != 0) {
            z.set(0, 1);
        }
        DataBlock zc = z.drop(1, 0);
        ssf_.Z(pos, zc);
        zc.mul(weight(pos));
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        if (pos % conversion == 0) {
            x.set(0);
        } else {
            x.copy(m.row(0));
        }
        double w = weight(pos);
        SubMatrix q = m.extract(1, m.getRowsCount(), 0, m.getColumnsCount());
        DataBlockIterator c = q.columns();
        DataBlock col = c.getData();
        do {
            x.add(c.getPosition(), w * ssf_.ZX(pos, col));
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
        SubMatrix v = vm.extract(1, vm.getRowsCount(), 1, vm.getColumnsCount());
        if (pos % conversion == 0) {
            return w * w * ssf_.ZVZ(pos, v);
        } else {
            double r = vm.get(0, 0);
            r += 2 * w * ssf_.ZX(pos, vm.row(0).drop(1, 0));
            r += w * w * ssf_.ZVZ(pos, v);
            return r;
        }
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(int pos, DataBlock x) {
        double r = (pos % conversion == 0) ? 0 : x.get(0);
        return r + mweight(pos, ssf_.ZX(pos, x.drop(1, 0)));
    }

    private double mweight(int pos, double m) {
        return w_ == null ? m : w_[pos] * m;
    }

    private double mweight2(int pos, double m) {
        return w_ == null ? m : w_[pos] * w_[pos] * m;
    }

    private double weight(int pos) {
        return w_ == null ? 1 : w_[pos];
    }

    private double weight2(int pos) {
        return w_ == null ? 1 : w_[pos] * w_[pos];
    }
}

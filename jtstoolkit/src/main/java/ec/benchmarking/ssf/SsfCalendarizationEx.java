/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.benchmarking.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;
import java.util.HashSet;

/**
 * See "Calendarization with splines and state space models"
 * B. Quenneville, F. Picard and S.Fortier
 * Appl. Statistics (2013)
 * 62, part 3, pp 371-399
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfCalendarizationEx implements ISsf {

    /**
     * State vector: Cumulative sum, relative to the observation constraints
     * Cumulative sum, relative to the aggregation constraints (Unweighted)
     * component *
     */
    public final double[] weights;
    public final HashSet<Integer> starts = new HashSet<>(), ends = new HashSet<>();
    public final HashSet<Integer> astarts = new HashSet<>(), aends = new HashSet<>();
    private int curpos = -1, curtype = -1, apos = -1, aggtype = -1;
    private static final int LAST = 1, FIRST = 2, DEF = 0;

    private int posType(int pos) {
        if (curpos == pos) {
            return curtype;
        }
        curpos = pos;
        // 1 = last
        // 2 = first
        // 0 others
        if (starts.contains(pos)) {
            curtype = FIRST;
        } else if (ends.contains(pos)) {
            curtype = LAST;
        } else {
            curtype = DEF;
        }
        return curtype;
    }

    private int aggType(int pos) {
        if (apos == pos) {
            return aggtype;
        }
        apos = pos;
        // 1 = last
        // 2 = first
        // 0 others
        if (astarts.contains(pos)) {
            aggtype = FIRST;
        } else if (aends.contains(pos)) {
            aggtype = LAST;
        } else {
            aggtype = DEF;
        }
        return aggtype;
    }

    /**
     *
     * @param conv
     * @param w
     */
    public SsfCalendarizationEx(int[] starts, int[] astarts, double[] w) {
        weights = w;
        for (int i = 0; i < starts.length; ++i) {
            int cur = starts[i];
            this.starts.add(cur);
            if (cur > 0) {
                this.ends.add(cur - 1);
            }
        }
        for (int i = 0; i < astarts.length; ++i) {
            int cur = astarts[i];
            this.astarts.add(cur);
            if (cur > 0) {
                this.aends.add(cur - 1);
            }
        }
    }

    /**
     *
     *
     * /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        b.set(2, 0, 1);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(2, 2, 1);
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return 1;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return 3;
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
        return true;
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
        return true;
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
        return true;
    }

    /**
     * L = T - k*Z
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        // TODO: optimization
        T(pos, lm);
        int postype = posType(pos);
        if (postype != FIRST) {
            lm.column(0).addAY(-1, k);
        }
        double a = weight(pos);
        lm.column(2).addAY(-a, k);
    }

    private double mweight(int pos, double m) {
        return weights == null ? m : weights[pos] * m;
    }

    private double mweight2(int pos, double m) {
        return weights == null ? m : weights[pos] * weights[pos] * m;
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        pf0.set(2, 2, 1);
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        pi0.set(2, 2, 1);
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
        rv.set(0, 2);
    }

    /**
     * case I: pos+1 % c = 0. Last pos T=| 0 0 | | 0 1 | case II: pos % c = 0.
     * First pos T=| 0 w | | 0 1 | case III: others. Inside T=| 1 w | | 0 1 |
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(2, 2, 1);
        int postype = posType(pos);
        if (postype != LAST) {
            tr.set(0, 2, weight(pos));
            if (postype != FIRST) {
                tr.set(0, 0, 1);
            }
        }
        int atype = aggType(pos);
        if (atype != LAST) {
            tr.set(1, 2, weight(pos));
            if (atype != FIRST) {
                tr.set(1, 1, 1);
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
        // TODO. Optimize
        // TVT' = T (TV)'
        DataBlockIterator columns = vm.columns();
        DataBlock column = columns.getData();
        do {
            TX(pos, column);
        } while (columns.next());
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
        // case I
        int postype = posType(pos);
        double s = mweight(pos, x.get(2));
        if (postype == LAST) {
            x.set(0, 0);
        } else if (postype == FIRST) {
            // case II.
            x.set(0, s);
        } else {
            // case III
            x.add(0, s);
        }
        // case I
        int atype = aggType(pos);
        if (atype == LAST) {
            x.set(1, 0);
        } else if (atype == FIRST) {
            // case II.
            x.set(1, s);
        } else {
            // case III
            x.add(1, s);
        }
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {

        vm.add(2, 2, mweight2(pos, d));
        int postype = posType(pos);
        if (postype != FIRST) {
            double w = mweight(pos, d);
            vm.add(0, 0, d);
            vm.add(0, 2, w);
            vm.add(2, 0, w);
        }
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(int pos, SubMatrix wv) {
    }

    private double weight(int pos) {
        return weights == null ? 1 : weights[pos];
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        x.add(2, mweight(pos, d));
        int postype = posType(pos);
        if (postype != FIRST) {
            x.add(0, d);
        }
    }

    /**
     * |a0 0 a1| |0 b0 b1| |0 0 1|
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
        // case I: 0, x1
        int postype = posType(pos);
        if (postype == LAST) {
            x.set(0, 0);
        } // case II: 0, w x0 + x1
        else if (postype == FIRST) {
            x.add(2, mweight(pos, x.get(0)));
            x.set(0, 0);
        } // case III: x0, w x0 + x1
        else {
            x.add(2, mweight(pos, x.get(0)));
        }
        int atype = aggType(pos);
        if (atype == LAST) {
            x.set(1, 0);
        } // case II: 0, w x0 + x1
        else if (postype == FIRST) {
            x.add(2, mweight(pos, x.get(1)));
            x.set(1, 0);
        } // case III: x0, w x0 + x1
        else {
            x.add(2, mweight(pos, x.get(1)));
        }
    }

    /**
     * Z(t) = [ 0, w(t)] for t%c == 0. [ 1, w(t)] for t%c != 0.
     *
     * @param pos
     * @param z
     */
    @Override
    public void Z(int pos, DataBlock z) {
        int postype = posType(pos);
        if (postype == FIRST) {
            z.set(0, 0);
        } else {
            z.set(0, 1);
        }
        z.set(2, weight(pos));
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.product(m.row(2), weight(pos));
        int postype = posType(pos);
        if (postype != FIRST) {
            x.add(m.row(0));
        }
    }

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        int postype = posType(pos);
        if (postype == FIRST) {
            return mweight2(pos, vm.get(2, 2));
        } else {
            double r = vm.get(0, 0);
            r += mweight(pos, 2 * vm.get(2, 0));
            r += mweight2(pos, vm.get(2, 2));
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
        int postype = posType(pos);
        double r = (postype == FIRST) ? 0 : x.get(0);
        return r + mweight(pos, x.get(2));
    }
}

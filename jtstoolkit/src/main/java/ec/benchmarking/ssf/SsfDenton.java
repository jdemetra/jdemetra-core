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
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfDenton extends BaseDisaggregation implements ISsf {

    /**
     *
     */
    public final double[] weights;

    /**
     * 
     * @param conv
     * @param w
     */
    public SsfDenton(int conv, double[] w) {
        super(conv);
        weights = w;
    }

    /**
     * 
    
    /**
     * 
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        b.set(1, 0, 1);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(1, 1, 1);
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
        return 2;
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
     * 
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        double k0=k.get(0), k1=k.get(1);
        double w=weight(pos);
        if ((pos+1)% conversion  == 0){
        //case I:
            lm.set(0,0, -k0);
            lm.set(0,1, -w*k0);
            lm.set(1,0, -k1);
            lm.set(1,1, 1-w*k1);
        }
        else if (pos% conversion  == 0){
        //case II:
            lm.set(0,0, 0);
            lm.set(0,1, w-w*k0);
            lm.set(1,0, 0);
            lm.set(1,1, 1-w*k1);
        }else{
        //case III:
            lm.set(0,0, 1-k0);
            lm.set(0,1, w-w*k0);
            lm.set(1,0, -k1);
            lm.set(1,1, 1-w*k1);
            
        }
//        lm.set(1, 1, 1 - k.get(1));
//
//        DataBlock c0 = lm.column(0);
//        c0.set(0);
//        double s = -k.get(0);
//        if ((pos + 1) % conversion == 0) {
//            if ((pos + 1) % conversion != 0) {
//                c0.set(0, 1);
//            }
//            c0.sub(k);
//        }
//        // if ((pos + 1) % conversion != 0)
//        // s += 1;
//        lm.set(1, 0, mweight(pos, s));
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
        pf0.set(1, 1, 1);
    }

    /**
     * 
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        pi0.set(1, 1, 1);
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
        rv.set(0, 1);
    }

    /**
     * case I: pos+1 % c = 0
     * T=| 0 0 |
     *   | 0 1 |
     * case II: pos % c = 0
     * T=| 0 w |
     *   | 0 1 |
     * case III: others
     * T=| 1 w |
     *   | 0 1 |
     * 
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(1, 1, 1);
        if ((pos + 1) % conversion != 0) {
            tr.set(0, 1, weight(pos));
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
        if ((pos + 1) % conversion == 0) {
            vm.set(0, 0, 0);
            vm.set(1, 0, 0);
            vm.set(0, 1, 0);
        } else if (pos % conversion == 0) {
            double w = weight(pos);
            double v = w * vm.get(1, 1);
            vm.set(0, 0, w * v);
            vm.set(1, 0, v);
            vm.set(0, 1, v);
        } else {
            double w = weight(pos);
            double wV = w * vm.get(1, 1);
            double wv = w * vm.get(0, 1);
            vm.add(0, 1, wV);
            vm.add(1, 0, wV);
            vm.add(0, 0, 2 * wv + w * wV);
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        // case I
        if ((pos + 1) % conversion == 0) {
            x.set(0, 0);
        }else if ( pos % conversion == 0){
            // case II.
            double s = x.get(1);
            x.set(0, mweight(pos, s));
        }else{
            // case III
            double s = x.get(1);
            x.add(0, mweight(pos, s));
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

        vm.add(1, 1, mweight2(pos, d));
        if (pos % conversion != 0) {
            double w = mweight(pos, d);
            vm.add(0, 0, d);
            vm.add(0, 1, w);
            vm.add(1, 0, w);
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
        x.add(1, mweight(pos, d));
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
        // case I: 0, x1
        if ((pos + 1) % conversion == 0) {
            x.set(0, 0);
        } // case II: 0, w x0 + x1
        else if (pos % conversion == 0) {
            x.add(1, mweight(pos, x.get(0)));
            x.set(0, 0);
        } // case III: x0, w x0 + x1
        else {
            x.add(1, mweight(pos, x.get(0)));
        }
    }

    /**
     * Z(t) = 
     * [ 0, w(t)] for t%c == 0.
     * [ 1, w(t)] for t%c != 0.
     * @param pos
     * @param z
     */
    @Override
    public void Z(int pos, DataBlock z) {
        if (pos % conversion == 0) {
            z.set(0, 0);
        } else {
            z.set(0, 1);
        }
        z.set(1, weight(pos));
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.product(m.row(1), weight(pos));
        if (pos % conversion != 0) {
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
        if (pos % conversion == 0) {
            return mweight2(pos, vm.get(1, 1));
        } else {
            double r = vm.get(0, 0);
            r += mweight(pos, 2 * vm.get(1, 0));
            r += mweight2(pos, vm.get(1, 1));
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
        return r + mweight(pos, x.get(1));
    }
}

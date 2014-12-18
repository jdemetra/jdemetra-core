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
public class SsfFernandez extends BaseDisaggregation implements ISsf {

    /**
     * 
     */
    public SsfFernandez() {
    }

    /**
     * 
     * @param conv
     */
    public SsfFernandez(int conv) {
        super(conv);
    }

    /**
     * 
     * @param expander
     */
    public SsfFernandez(SsfFernandez expander) {
        super(expander);
    }

    /**
     * 
     * @param b
     */
    public void diffuseConstraints(SubMatrix b) {
        b.set(1, 0, 1);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(1, 1, 1);
    }

    /**
     * 
     * @return
     */
    public int getNonStationaryDim() {
        return 1;
    }

    /**
     * 
     * @return
     */
    public int getStateDim() {
        return 2;
    }

    /**
     * 
     * @return
     */
    public int getTransitionResCount() {
        return 1;
    }

    /**
     * 
     * @return
     */
    public int getTransitionResDim() {
        return 1;
    }

    /**
     * 
     * @return
     */
    public boolean hasR() {
        return true;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean hasW() {
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean isDiffuse() {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isMeasurementEquationTimeInvariant() {
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean isTimeInvariant() {
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionEquationTimeInvariant() {
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionResidualTimeInvariant() {
        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isValid() {
        return true;
    }

    /**
     * 
     * @param pos
     * @param k
     * @param lm
     */
    public void L(int pos, DataBlock k, SubMatrix lm) {
        if ((pos + 1) % conversion == 0) {
            lm.set(0, 0, -k.get(0));
            lm.set(0, 1, -k.get(0));
            lm.set(1, 0, -k.get(1));
            lm.set(1, 1, 1 - k.get(1));
        } else if (pos % conversion == 0) {
            //lm.set(0, 0, 0);
            lm.set(0, 1, 1 - k.get(0));
            //lm.set(1, 0, 0);
            lm.set(1, 1, 1 - k.get(1));
        } else {
            lm.set(0, 0, 1 - k.get(0));
            lm.set(0, 1, -k.get(0));
            lm.set(1, 0, 1 - k.get(1));
            lm.set(1, 1, 1 - k.get(1));
        }
    }

    /**
     * 
     * @param pf0
     */
    public void Pf0(SubMatrix pf0) {
        pf0.set(1, 1, 1);
    }

    /**
     * 
     * @param pi0
     */
    public void Pi0(SubMatrix pi0) {
        pi0.set(1, 1, 1);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void Q(int pos, SubMatrix qm) {
        qm.set(0, 0, 1);
    }

    /**
     * 
     * @param pos
     * @param rv
     */
    public void R(int pos, SubArrayOfInt rv) {
        rv.set(0, 1);
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    public void T(int pos, SubMatrix tr) {
        tr.set(1, 1, 1);
        if ((pos + 1) % conversion != 0) {
            tr.set(0, 1, 1);
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
    public void TVT(int pos, SubMatrix vm) {
        if ((pos + 1) % conversion == 0) {
            vm.set(0, 0, 0);
            vm.set(0, 1, 0);
            vm.set(1, 0, 0);
        } else if (pos % conversion == 0) {
            double v = vm.get(1, 1);
            vm.set(0, 0, v);
            vm.set(0, 1, v);
            vm.set(1, 0, v);
        } else {
            double v = vm.get(1, 1);
            vm.add(0, 0, 2 * vm.get(0, 1) + v);
            vm.add(0, 1, v);
            vm.add(1, 0, v);
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void TX(int pos, DataBlock x) {
        if ((pos + 1) % conversion == 0) {
            x.set(0, 0);
        } else if (pos % conversion == 0) {
            x.set(0, x.get(1));
        } else {
            x.add(0, x.get(1));
        }
    }

    /**
     * 
     * @param pos
     * @param vm
     * @param d
     */
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        if (pos % conversion == 0) {
            vm.add(1, 1, d);
        } else {
            vm.add(d);
        }
    }

    /**
     * 
     * @param pos
     * @param wv
     */
    public void W(int pos, SubMatrix wv) {
    }

    /**
     * 
     * @param pos
     * @param x
     * @param d
     */
    public void XpZd(int pos, DataBlock x, double d) {
        x.add(1, d);
        if (pos % conversion != 0) {
            x.add(0, d);
        }
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void XT(int pos, DataBlock x) {
        if ((pos + 1) % conversion == 0) {
            x.set(0, 0);
        } else if (pos % conversion == 0) {
            x.add(1, x.get(0));
            x.set(0, 0);
        } else {
            x.add(1, x.get(0));
        }
    }

    /**
     * 
     * @param pos
     * @param z
     */
    public void Z(int pos, DataBlock z) {
        z.set(1, 1);
        if (pos % conversion != 0){
            z.set(0, 1);
        }
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.copy(m.row(1));
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
    public double ZVZ(int pos, SubMatrix vm) {
        if (pos % conversion == 0) {
            return vm.get(1, 1);
        } else {
            return vm.get(0, 0) + vm.get(1, 1) + 2 * vm.get(0, 1);
        }
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    public double ZX(int pos, DataBlock x) {
        if (pos % conversion == 0) {
            return x.get(1);
        } else {
            return x.get(0) + x.get(1);
        }
    }
}

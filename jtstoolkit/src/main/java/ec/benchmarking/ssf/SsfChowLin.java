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
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.SingleParameter;
import ec.tstoolkit.ssf.ISsf;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfChowLin extends BaseDisaggregation implements ISsf {

    /**
     *
     */
    public static class Mapper implements IParametricMapping<SsfChowLin> {
        
        public static final String RHO="rho";

        /**
         *
         */
        public final int conversion;
        /**
         *
         */
        /**
         *
         */
        public static final double BOUND = .999999, EPS = 0.0000005;

        /**
         *
         * @param conv
         */
        public Mapper(int conv) {
            this.conversion = conv;
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            double p = inparams.get(0);
            return p > -BOUND && p < BOUND;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
	    return inparams.get(0) > 0 ? -EPS / 2 : EPS/2;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return -BOUND;
        }

        @Override
        public SsfChowLin map(IReadDataBlock p) {
            if (p.getLength() != 1) {
                return null;
            }
            return new SsfChowLin(conversion, p.get(0));
        }

        @Override
        public IReadDataBlock map(SsfChowLin t) {
            return new SingleParameter(t.ro_);
        }

        @Override
        public double ubound(int idx) {
            return BOUND;
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            double p = ioparams.get(0);
            ParamValidation rslt = ParamValidation.Valid;
            if (p <= -BOUND) {
                p = -BOUND;
                ioparams.set(0, p);
                rslt = ParamValidation.Changed;
            } else if (p >= BOUND) {
                p = BOUND;
                ioparams.set(0, p);
                rslt = ParamValidation.Changed;
            }
            return rslt;
        }
        
        @Override
        public String getDescription(int idx){
            return RHO;
        }
    }
    private double ro_;

    /**
     *
     */
    public SsfChowLin() {
    }

    /**
     *
     * @param conv
     */
    public SsfChowLin(int conv) {
        super(conv);
    }

    /**
     *
     * @param conv
     * @param ro
     */
    public SsfChowLin(int conv, double ro) {
        super(conv);
        ro_ = ro;
    }

    /**
     *
     * @param b
     */
    public void diffuseConstraints(SubMatrix b) {
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
        return 0;
    }

    /**
     *
     * @return
     */
    public double getRo() {
        return ro_;
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
        return false;
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
        return Math.abs(ro_) < 1;
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
        } else if (pos % conversion == 0) {
            //lm.set(0, 0, 0);
            lm.set(0, 1, 1 - k.get(0));
            //lm.set(1, 0, 0);
        } else {
            lm.set(0, 0, 1 - k.get(0));
            lm.set(0, 1, -k.get(0));
            lm.set(1, 0, 1 - k.get(1));
        }
        lm.set(1, 1, ro_ - k.get(1));
    }

    /**
     *
     * @param pf0
     */
    public void Pf0(SubMatrix pf0) {
        pf0.set(1, 1, 1 / (1 - ro_ * ro_));
    }

    /**
     *
     * @param pi0
     */
    public void Pi0(SubMatrix pi0) {
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
     * @param value
     */
    public void setRo(double value) {
        ro_ = value;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    public void T(int pos, SubMatrix tr) {
        tr.set(1, 1, ro_);
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
            v *= ro_;
            vm.set(0, 1, v);
            vm.set(1, 0, v);
        } else {
            double v11 = vm.get(1, 1), v01 = vm.get(0, 1);
            vm.add(0, 0, 2 * v01 + v11);
            double v = ro_ * (v01 + v11);
            vm.set(0, 1, v);
            vm.set(1, 0, v);
        }
        vm.mul(1, 1, ro_ * ro_);
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
        x.mul(1, ro_);
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
            x.mul(1, ro_);
        } else if (pos % conversion == 0) {
            x.set(1, x.get(1) * ro_ + x.get(0));
            x.set(0, 0);
        } else {
            x.set(1, x.get(1) * ro_ + x.get(0));
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
            return vm.sum();
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
            return x.sum();
        }
    }
}

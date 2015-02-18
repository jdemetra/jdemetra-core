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
 * Ssf for (1 1 0) ARIMA models.
 * 
 * y(t)-y(t-1) = rho*(y(t-1)-y(t-2)) + e(t) or
 * y(t)=(1+rho)*y(t-1) - rho*y(t-2) + e(t)
 * 
 * The class is designed to handle models initialized by zero.
 * State: a(t) = [y(t-1) y(t)-y(t-1)]'
 * Measurement: Z(t) = 1 1
 * Transition: T(t) = | 1 1  |
 *                    | 0 rho|
 * Innovations: V(t) = | 0 0 |
 *                     | 0 1 |
 * Initialization: default:
 * Pi0 = | 1 0 |
 *       | 0 0 |
 * Pf0 = | 0 0            |
 *       | 0 1/(1-rho*rho)|
 * 0-initialization
 * Pi0 = | 0 0 |
 *       | 0 0 |
 * Pf0 = | 0 0 |
 *       | 0 1 |
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfRwAr1 implements ISsf {

    /**
     *
     */
    public static class Mapping implements IParametricMapping<SsfRwAr1> {

        public static final String RHO = "rho";
        /**
         *
         */
        public final boolean zeroInit;
        public final double a_, b_;

        /**
         *
         */
        /**
         *
         */
        public static final double EPS = Math.sqrt(2.220446e-16), BOUND = 1-1e-8;
        //public static final double EPS = 1e-5, BOUND = 1-2*EPS;

        /**
         *
         * @param zeroInit
         */
        public Mapping(boolean zeroInit) {
            this.zeroInit = zeroInit;
            a_=-BOUND;
            b_=BOUND;
        }

        public Mapping(boolean zeroInit, double a, double b) {
            this.zeroInit = zeroInit;
            a_=Math.max(a, -BOUND);
            b_=Math.min(b, BOUND);
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            double p = inparams.get(0);
            return p > a_ && p < b_;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return EPS;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return a_;
        }

        @Override
        public SsfRwAr1 map(IReadDataBlock p) {
            if (p.getLength() != 1) {
                return null;
            }
            SsfRwAr1 ssf = new SsfRwAr1(p.get(0));
            ssf.m_zeroinit = zeroInit;
            return ssf;
        }

        @Override
        public IReadDataBlock map(SsfRwAr1 t) {
            return new SingleParameter(t.getRho());
        }

        @Override
        public double ubound(int idx) {
            return b_;
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            double p = ioparams.get(0);
            ParamValidation rslt = ParamValidation.Valid;
            if (p < a_) {
                p = a_;
                ioparams.set(0, p);
                rslt = ParamValidation.Changed;
            } else if (p > b_) {
                p = b_;
                ioparams.set(0, p);
                rslt = ParamValidation.Changed;
            }
            return rslt;
        }

        @Override
        public String getDescription(int idx) {
            return RHO;
        }
    }

    private double m_rho = .2;

    private boolean m_zeroinit;

    /**
     *
     */
    public SsfRwAr1() {
    }

    /**
     *
     * @param ro
     */
    public SsfRwAr1(double rho) {
        m_rho = rho;
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
        qm.set(1, 1, 1);
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
    public double getRho() {
        return m_rho;
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
    public boolean isUseZeroInitialization() {
        return m_zeroinit;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return Math.abs(m_rho) < 1;
    }

    /**
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        double k0 = k.get(0), k1 = k.get(1);
        lm.set(0, 0, 1 - k0);
        lm.set(0, 1, 1 - k0);
        lm.set(1, 0, -k1);
        lm.set(1, 1, m_rho - k1);
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        if (!m_zeroinit) {
            pf0.set(1, 1, 1 / (1 - m_rho * m_rho));
        } else {
            pf0.set(1, 1, 1);
        }
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
        rv.set(0, 1);
    }

    /**
     *
     * @param value
     */
    public void setRho(double value) {
        m_rho = value;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(0, 0, 1);
        tr.set(0, 1, 1);
        tr.set(1, 1, m_rho);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(int pos, SubMatrix vm) {
        double v00 = vm.get(0, 0);
        double v01 = vm.get(0, 1);
        double v10 = vm.get(1, 0);
        double v11 = vm.get(1, 1);
        vm.set(0, 0, v00 + v01 + v10 + v11);
        vm.set(0, 1, m_rho * (v01 + v11));
        vm.set(1, 0, m_rho * (v10 + v11));
        vm.set(1, 1, m_rho * m_rho * v11);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        x.set(0, x.sum());
        x.mul(1, m_rho);
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
        x.set(1, x.get(0) + m_rho * x.get(1));
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void Z(int pos, DataBlock x) {
        x.set(1);
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.sum(m.row(0), m.row(1));
    }

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        return vm.sum();
    }

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(int pos, DataBlock x) {
        return x.sum();
    }
}

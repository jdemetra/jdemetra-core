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
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfAr1 implements ISsf {

    /**
     * 
     */
    public static class Mapping implements IParametricMapping<SsfAr1>
    {
        public static final String RHO="rho";
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
        public static final double EPS = Math.sqrt(2.220446e-16),BOUND = 1-1e-8;
        //public static final double EPS = 1e-5, BOUND = 1-2*EPS;

        /**
         * 
         * @param zeroInit
         */
        public Mapping(boolean zeroInit)
        {
	    this.zeroInit = zeroInit;
            a_=-BOUND;
            b_=BOUND;
	}

        public Mapping(boolean zeroInit, double a, double b)
        {
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
	    return inparams.get(0) > 0 ? -EPS / 2 : EPS/2;
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
	public SsfAr1 map(IReadDataBlock p) {
	    if (p.getLength() < 1)
		return null;
	    else {
		SsfAr1 ar1 = new SsfAr1(p.get(0));
		ar1.m_zeroinit = zeroInit;
		return ar1;
	    }
	}

	@Override
	public IReadDataBlock map(SsfAr1 t) {
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

    private boolean m_zeroinit;

    private double m_rho;

    public SsfAr1()
    {
	m_rho = .1;
    }

    /**
     * 
     * @param rho
     */
    public SsfAr1(double rho)
    {
	m_rho = rho;
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
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
	return 0;
    }

    /**
     * 
     * @return
     */
    public double getRho()
    {
	return m_rho;
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
	return false;
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
    public boolean isUsingZeroInitialization()
    {
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
	lm.set(0, 0, m_rho - k.get(0));
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
	if (m_zeroinit)
	    pf0.set(0, 0, 1);
	else
	    pf0.set(0, 0, 1 / (1 - m_rho * m_rho));
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
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
        rv.set(0);
    }

    /**
     * 
     * @param value
     */
    public void setRho(double value)
    {
	m_rho = value;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
	tr.set(0, 0, m_rho);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(int pos, SubMatrix vm) {
	vm.mul(m_rho * m_rho);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
	x.mul(m_rho);
    }

    /**
     * 
     * @param value
     */
    public void useZeroInitialization(boolean value)
    {
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
        wv.set(1);
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
	x.mul(m_rho);
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
	x.set(0, m.get(0, 0));
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

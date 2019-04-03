/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package demetra.maths.functions;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.data.DoubleSeq;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ParametersRange implements IParametersDomain {

    private final int m_np;

    private final double a, b;

    private final boolean m_excluded;

    private double m_eps = 1e-6;

    /**
     * 
     * @param a
     * @param b
     * @param excluded
     */
    public ParametersRange(double a, double b, boolean excluded)
    {
	m_np = 1;
	this.a = a;
	this.b = b;
	m_excluded = excluded;
    }

    /**
     * 
     * @param nparams
     * @param a
     * @param b
     * @param excluded
     */
    public ParametersRange(int nparams, double a, double b, boolean excluded)
    {
	m_np = nparams;
	this.a = a;
	this.b = b;
	m_excluded = excluded;
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
	for (int i = 0; i < m_np; ++i) {
	    double v = inparams.get(i);
	    if (isOpen()) {
		if (v <= getA() || v >= getB())
		    return false;
	    } else if (v < getA() || v > getB())
		return false;
	}
	return true;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
	double eps = (getB() - getA()) * m_eps;
	if (inparams.get(idx) + eps >= getB())
	    eps = -eps;
	return eps;
    }

    /**
     * 
     * @return
     */
    public double getDelta()
    {
	return m_eps;
    }

    @Override
    public int getDim() {
	return m_np;
    }

    @Override
    public double lbound(int idx) {
	return isOpen() ? getA() + m_eps : getA();
    }

    /**
     * 
     * @param value
     */
    public void setDelta(double value)
    {
	m_eps = value;
    }

    @Override
    public double ubound(int idx) {
	return isOpen() ? getB() - m_eps : getB();
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
	ParamValidation rslt = ParamValidation.Valid;
	for (int i = 0; i < m_np; ++i) {
	    double v = ioparams.get(i);
	    if (isOpen()) {
		if (v <= getA()) {
		    double eps = (getB() - getA()) * m_eps;
		    ioparams.set(i, getA() + eps);
		    rslt = ParamValidation.Changed;
		} else if (v >= getB()) {
		    double eps = (getB() - getA()) * m_eps;
		    ioparams.set(i, getB() - eps);
		    rslt = ParamValidation.Changed;
		}
	    } else if (v < getA()) {
		ioparams.set(i, getA());
		rslt = ParamValidation.Changed;
	    } else if (v > getB()) {
		ioparams.set(i, getB());
		rslt = ParamValidation.Changed;
	    }
	}
	return rslt;
    }
    
    @Override
    public String getDescription(int idx) {
        return PARAM+idx; 
    }

    /**
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * @return the b
     */
    public double getB() {
        return b;
    }

    /**
     * @return the m_excluded
     */
    public boolean isOpen() {
        return m_excluded;
    }

}

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
import demetra.data.DoubleSequence;


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
    public boolean checkBoundaries(DoubleSequence inparams) {
	for (int i = 0; i < m_np; ++i) {
	    double v = inparams.get(i);
	    if (m_excluded) {
		if (v <= a || v >= b)
		    return false;
	    } else if (v < a || v > b)
		return false;
	}
	return true;
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
	double eps = (b - a) * m_eps;
	if (inparams.get(idx) + eps >= b)
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
	return m_excluded ? a + m_eps : a;
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
	return m_excluded ? b - m_eps : b;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
	ParamValidation rslt = ParamValidation.Valid;
	for (int i = 0; i < m_np; ++i) {
	    double v = ioparams.get(i);
	    if (m_excluded) {
		if (v <= a) {
		    double eps = (b - a) * m_eps;
		    ioparams.set(i, a + eps);
		    rslt = ParamValidation.Changed;
		} else if (v >= b) {
		    double eps = (b - a) * m_eps;
		    ioparams.set(i, b - eps);
		    rslt = ParamValidation.Changed;
		}
	    } else if (v < a) {
		ioparams.set(i, a);
		rslt = ParamValidation.Changed;
	    } else if (v > b) {
		ioparams.set(i, b);
		rslt = ParamValidation.Changed;
	    }
	}
	return rslt;
    }
    
    @Override
    public String getDescription(int idx) {
        return PARAM+idx; 
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        return DataBlock.ofInternal(new double[]{(b+a)/2});
    }

     
}

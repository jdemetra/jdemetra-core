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

package ec.tstoolkit.maths.realfunctions;

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ParametersRange implements IParametersDomain {

    private int m_np;

    private double m_a, m_b;

    private boolean m_excluded;

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
	m_a = a;
	m_b = b;
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
	m_a = a;
	m_b = b;
	m_excluded = excluded;
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
	for (int i = 0; i < m_np; ++i) {
	    double v = inparams.get(i);
	    if (m_excluded) {
		if (v <= m_a || v >= m_b)
		    return false;
	    } else if (v < m_a || v > m_b)
		return false;
	}
	return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
	double eps = (m_b - m_a) * m_eps;
	if (inparams.get(idx) + eps >= m_b)
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
	return m_excluded ? m_a + m_eps : m_a;
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
	return m_excluded ? m_b - m_eps : m_b;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
	ParamValidation rslt = ParamValidation.Valid;
	for (int i = 0; i < m_np; ++i) {
	    double v = ioparams.get(i);
	    if (m_excluded) {
		if (v <= m_a) {
		    double eps = (m_b - m_a) * m_eps;
		    ioparams.set(i, m_a + eps);
		    rslt = ParamValidation.Changed;
		} else if (v >= m_b) {
		    double eps = (m_b - m_a) * m_eps;
		    ioparams.set(i, m_b - eps);
		    rslt = ParamValidation.Changed;
		}
	    } else if (v < m_a) {
		ioparams.set(i, m_a);
		rslt = ParamValidation.Changed;
	    } else if (v > m_b) {
		ioparams.set(i, m_b);
		rslt = ParamValidation.Changed;
	    }
	}
	return rslt;
    }
    
    @Override
    public String getDescription(int idx) {
        return PARAM+idx; 
    }
    
}

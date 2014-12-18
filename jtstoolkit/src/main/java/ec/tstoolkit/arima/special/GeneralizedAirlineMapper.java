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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.utilities.Ref;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class GeneralizedAirlineMapper implements
	IParametricMapping<GeneralizedAirlineModel> {


    private enum P_Type {

	P_ZERO, R_ZERO, H_SEL, H_NSEL;
    }

    private final int[] m_c;
    private final boolean[] m_fixed;
    private final int m_pow, m_freq;
    private final P_Type[] m_ptypes;

    private double m_ubound;

    private boolean m_strict = false;

    /**
     * 
     * @param t
     */
    public GeneralizedAirlineMapper(GeneralizedAirlineModel t)
    {
        m_c=t.m_c;
        m_fixed=t.m_fixed;
	m_ptypes = new P_Type[t.getParametersCount()];
	m_pow = t.m_pow;
	m_freq = t.m_freq;
	m_ubound = 1.1;
	for (int i = 0, j = 0; i < t.m_q.length; ++i)
	    if (!t.m_fixed[i])
		if (t.m_c[i] < 0)
		    m_ptypes[j++] = P_Type.P_ZERO;
		else if (i < 2)
		    m_ptypes[j++] = P_Type.R_ZERO;
		else if (i == 2)
		    m_ptypes[j++] = P_Type.H_SEL;
		else
		    m_ptypes[j++] = P_Type.H_NSEL;
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock p) {
	int np = m_ptypes.length;
	for (int i = 0; i < np; ++i)
	    if (Double.isNaN(p.get(i)))
		return false;
	int idx = 0;
	if (m_ptypes[0] == P_Type.P_ZERO)
	    if (np > 1 && m_ptypes[1] == P_Type.P_ZERO) {
		double ro = p.get(0) * p.get(0) - 4 * p.get(1);
		if (ro < 0) {
		    if (p.get(1) > m_ubound * m_ubound)
			return false;
		} else {
		    ro = Math.sqrt(ro);
		    double r = (-p.get(0) + ro) / (2 * p.get(1));
		    if (Math.abs(r) < 1 / m_ubound)
			return false;
		    r = (-p.get(0) - ro) / (2 * p.get(1));
		    if (Math.abs(r) < 1 / m_ubound)
			return false;
		}
		idx = 2;
	    } else {
		if (Math.abs(p.get(0)) > 2)
		    return false;
		idx = 1;
	    }
	else if (m_ptypes[0] == P_Type.R_ZERO) {
	    if (Math.abs(p.get(0)) > m_ubound)
		return false;
	    idx = 1;
	}

	for (int i = idx; i < np; ++i)
	    if (p.get(i) < 0 || p.get(i) > m_ubound)
		return false;
	return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
	if (m_ptypes[idx] == P_Type.R_ZERO || m_ptypes[idx] == P_Type.P_ZERO)
	    return GeneralizedAirlineModel.EPS;
	else if (m_ptypes[idx] == P_Type.H_SEL)
	    return GeneralizedAirlineModel.EPS / m_pow;
	else
	    return GeneralizedAirlineModel.EPS / (m_freq - m_pow - 1);
    }

    @Override
    public int getDim() {
	return m_ptypes.length;
    }

    @Override
    public String getDescription(int idx) {
        // TO DO: improve the description of the parameters
        return PARAM+idx; 
    }
    /**
     * 
     * @return
     */
    public boolean isStrict()
    {
	return m_strict;
    }

    @Override
    public double lbound(int idx) {
        int l=0;
        if (m_ptypes.length>1 && m_ptypes[0]== P_Type.P_ZERO && m_ptypes[1]== P_Type.P_ZERO)
            l=2;
        if (idx < l)
            return Double.NEGATIVE_INFINITY;
        else
            return 0;
    }

    @Override
    public IReadDataBlock map(GeneralizedAirlineModel t) {
	return t.getParameters();
    }

    @Override
    public GeneralizedAirlineModel map(IReadDataBlock p) {
        GeneralizedAirlineModel model = new GeneralizedAirlineModel();
        model.m_c=m_c;
        model.m_fixed=m_fixed;
        model.m_freq=m_freq;
        model.m_pow=m_pow;
        model.m_q=new double[m_fixed.length];
        for (int i=0; i<m_fixed.length; ++i)
            if (m_fixed[i])
                model.m_q[i]=1;
	model.setParameters(p); 
        return model;
    }

    /**
     * 
     * @param value
     */
    public void setStrict(boolean value)
    {
	m_strict = value;
    }

    @Override
    public double ubound(int idx) {
        int l=0;
        if (m_ptypes.length>1 && m_ptypes[0]== P_Type.P_ZERO && m_ptypes[1]== P_Type.P_ZERO)
            l=2;
        if (idx < l)
            return Double.POSITIVE_INFINITY;
        else
            return m_ubound;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
	ParamValidation rslt = ParamValidation.Valid;
	int idx = 0;
	// polynomial...
	if (m_ptypes[0] == P_Type.P_ZERO)
	    if (m_ptypes.length > 1 && m_ptypes[1] == P_Type.P_ZERO) {
		Polynomial p = Polynomial.valueOf(1, ioparams.get(0), ioparams.get(1));
		Ref<Polynomial> np = new Ref<>(null);
		if (ec.tstoolkit.maths.linearfilters.Utilities.stabilize(p, np)) {
		    ioparams.set(0, np.val.get(1));
		    ioparams.set(1, np.val.get(2));
		    rslt = ParamValidation.Changed;
		}
		idx = 2;
	    } else {
		if (ioparams.get(0) > 2) {
		    rslt = ParamValidation.Changed;
		    ioparams.set(0, 2);
		} else if (ioparams.get(0) < -2) {
		    rslt = ParamValidation.Changed;
		    ioparams.set(0, -2);
		}
		idx = 1;
	    }
	else if (m_ptypes[0] == P_Type.R_ZERO) {
	    if (Math.abs(ioparams.get(0)) > 1) {
		ioparams.set(0, 1 / ioparams.get(0));
		rslt = ParamValidation.Changed;
	    }
	    idx = 1;
	}

	for (int i = idx; i < m_ptypes.length; ++i)
	    if (ioparams.get(i) < 0) {
		ioparams.set(i, 0);
		rslt = ParamValidation.Changed;
	    }
	    // return ParamValidation.Invalid;
	    else if (ioparams.get(i) > 1)
		if (m_strict || ioparams.get(i) > m_ubound) {
		    ioparams.set(i, 1 / ioparams.get(i));
		    rslt = ParamValidation.Changed;
		}
	return rslt;
    }
}

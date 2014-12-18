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


package ec.tstoolkit.maths.realfunctions.bfgs;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class IterativeLineSearch implements ILineSearch {
    // / <summary>
    // / Creates a new SimpleLineSearch
    // / </summary>

    private double m_eps = 1e-9, m_dfac = .5, m_ifac = 2;

    /**
     * 
     */
    public IterativeLineSearch()
    {
    }

    /**
     *
     * @return
     */
    @Override
    public IterativeLineSearch exemplar() {
	IterativeLineSearch ls = new IterativeLineSearch();
	ls.m_dfac = m_dfac;
	ls.m_ifac = m_ifac;
	ls.m_eps = m_eps;
	return ls;
    }

    /**
     *
     * @param fn
     * @param start
     * @return
     */
    @Override
    public boolean optimize(ILineFunction fn, double start) {
	double stpmax = fn.getStepMax();
	if (stpmax < m_eps)
	    return true;
	double stp = start;
	if (stp > stpmax)
	    stp = stpmax;

	double f0 = fn.getValue();

	fn.setStep(stp);
	double fprev, fcur = fn.getValue();
	if (fn.getDerivative() > 0) {
	    double smin = fn.getStepMin();
	    do {
		fprev = fcur;
		stp *= m_dfac;
		if (stp < smin)
		    break;
		fn.setStep(stp);
		fcur = fn.getValue();
	    } while (fcur < fprev || fprev > f0);
	    if (fprev > f0)
		return false;
	    else {
		fn.setStep(stp / m_dfac);
		return true;
	    }
	} else if (stp != stpmax) {
	    do {
		fprev = fcur;
		stp *= m_ifac;
		if (stp >= stpmax)
		    break;
		fn.setStep(stp);
		fcur = fn.getValue();
	    } while (fcur < fprev || fprev > f0);
	    if (fprev > f0)
		return false;
	    else {
		fn.setStep(stp / m_ifac);
		return true;
	    }
	} else
	    return fcur < f0;
    }
}

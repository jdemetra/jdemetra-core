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
public class QuadraticLineSearch implements ILineSearch {

    private ILineFunction m_fn;

    private double m_stpmax, m_stpopt, m_fopt, m_expand = 4, m_d0, m_f0,
	    m_eps = 1e-15;

    private boolean m_opt = true;

    int m_iter, m_maxiter = 20, m_miniter = 5;

    /**
     * 
     */
    public QuadraticLineSearch()
    {
    }

    /**
     *
     * @return
     */
    @Override
    public ILineSearch exemplar() {
	QuadraticLineSearch ls = new QuadraticLineSearch();
	ls.m_expand = m_expand;
	ls.m_opt = m_opt;
	ls.m_maxiter = m_maxiter;
	ls.m_miniter = m_miniter;
	return ls;
    }

    private void expand(double stp) {
	if (!m_opt)
	    return;
	try {
	    stp *= m_expand;
	    do {
		if (stp > m_stpmax)
		    return;
		m_fn.setStep(stp);
		double f = m_fn.getValue();
		if (f > m_fopt)
		    return;
		else {
		    m_fopt = f;
		    m_stpopt = stp;
		    double nstp = pmin(stp, f);
		    if (nstp < stp)
			break;
		    else
			stp = nstp * m_expand;
		}
	    } while (m_iter++ < m_miniter);
	} catch (RuntimeException err) {
	}
    }

    /**
     *
     * @param fn
     * @param start
     * @return
     */
    @Override
    public boolean optimize(ILineFunction fn, double start) {
	m_fn = fn;
	m_stpmax = fn.getStepMax();
	if (m_stpmax < m_eps)
	    return true;
	double stp = start;
	if (stp > m_stpmax)
	    stp = m_stpmax;
	m_d0 = fn.getDerivative();
	m_f0 = fn.getValue();
	m_stpopt = 0;
	m_fopt = m_f0;
	m_iter = 0;

	m_fn.setStep(stp);
	double f = m_fn.getValue();
	if (!quadOK(stp, f)) {
	    m_stpopt = stp;
	    m_fopt = f;
	    expand(stp);
	} else {
	    double mstp = pmin(stp, f);
	    if (mstp >= stp) {
		m_stpopt = stp;
		m_fopt = f;
		expand(stp);
	    } else
		shrink(mstp);
	}

	m_fn.setStep(m_stpopt);
	return m_iter < m_maxiter;
    }

    private double pmin(double stp, double f) {
	double ds = m_d0 * stp, df = f - m_f0 - ds;
	return -.5 * ds * stp / df;
    }

    private boolean quadOK(double stp, double f) {
	double ds = m_d0 * stp, df = f - m_f0 - ds;
	return df > 0;
    }

    private void shrink(double stp) {
	try {
	    do {
		m_fn.setStep(stp);
		double f = m_fn.getValue();
		if (f < m_fopt) {
		    m_fopt = f;
		    m_stpopt = stp;
		    if (m_iter > m_miniter)
			break;
		} else if (m_fopt < m_f0)
		    break;
		double nstp = pmin(stp, f);
		if (nstp > stp)
		    break;
		else
		    stp = nstp;
	    } while (m_iter++ < m_maxiter);
	} catch (RuntimeException err) {
	}
    }
}

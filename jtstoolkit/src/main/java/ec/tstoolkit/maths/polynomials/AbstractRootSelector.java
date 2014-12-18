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

package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.design.Development;

import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractRootSelector implements IRootSelector {

    protected Polynomial m_sel, m_nsel;

    /**
     *
     */
    public AbstractRootSelector() {
    }

    /**
     * 
     * @param root
     * @return
     */
    public abstract boolean accept(final Complex root);

    @Override
    public Polynomial getOutofSelection() {
	return m_nsel;
    }

    @Override
    public Polynomial getSelection() {
	return m_sel;
    }

    @Override
    public boolean select(final Polynomial p) {
	if (p.getDegree() == 0) {
	    m_nsel = p;
	    m_sel = null;
	    return false;
	} else {
	    Complex[] roots = p.roots();
	    List<Complex> sel = new ArrayList<>(), nsel = new ArrayList<>();
	    for (int i = 0; i < roots.length; ++i)
		if (accept(roots[i]))
		    sel.add(roots[i]);
		else
		    nsel.add(roots[i]);
	    if (sel.isEmpty()) {
		m_sel = null;
		m_nsel = p;
		return false;
	    } else if (nsel.isEmpty()) {
		m_nsel = null;
		m_sel = p;
		return true;
	    } else {
		Complex[] rs = Jdk6.Collections.toArray(sel, Complex.class);
		Complex[] rn = Jdk6.Collections.toArray(nsel, Complex.class);

		Complex.lejaOrder(rs);
		Complex.lejaOrder(rn);
		m_sel = Polynomial.fromComplexRoots(rs);
		m_sel = m_sel.times(p.get(0) / m_sel.get(0));
		m_nsel = Polynomial.fromComplexRoots(rn);
		m_nsel = m_nsel.divide(m_nsel.get(0));
		return true;
	    }
	}
    }
}

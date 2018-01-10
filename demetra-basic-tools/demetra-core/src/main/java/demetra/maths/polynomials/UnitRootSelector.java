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
package demetra.maths.polynomials;

import demetra.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UnitRootSelector  {

    private Polynomial m_sel, m_nsel;

    private int m_freq = 12;

    /**
     *
     */
    public UnitRootSelector() {
    }

    /**
     * 
     * @param freq
     */
    public UnitRootSelector(final int freq) {
	m_freq = freq;
    }

    /**
     * 
     * @return
     */
     public Polynomial getOutofSelection() {
	return m_nsel;
    }

    /**
     * 
     * @return
     */
    public Polynomial getSelection() {
	return m_sel;
    }

    /**
     * 
     * @param p
     * @return
     */
    public boolean select(final Polynomial p) {
	UnitRootsSolver urs = new UnitRootsSolver(m_freq);
	if (urs.factorize(p)) {
	    m_sel = urs.getUnitRoots().toPolynomial();
	    m_nsel = urs.remainder();
	    return true;
	} else
	    return false;
    }
}

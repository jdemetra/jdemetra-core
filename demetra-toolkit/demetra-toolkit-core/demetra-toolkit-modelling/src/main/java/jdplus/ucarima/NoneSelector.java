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
package jdplus.ucarima;

import demetra.design.Development;
import jdplus.maths.polynomials.Polynomial;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NoneSelector implements IRootSelector {

    private Polynomial m_p;

    /**
     *
     */
    public NoneSelector() {
    }

    @Override
    public Polynomial getOutofSelection() {
	return m_p;
    }

    /**
     * 
     * @return
     */
    @Override
    public Polynomial getSelection() {
	return null;
    }

    /**
     * 
     * @param p
     * @return
     */
    @Override
    public boolean select(final Polynomial p) {
	m_p = p;
	return false;
    }

     @Override
    public boolean selectUnitRoots(final Polynomial p) {
	return select(p);
    }

}

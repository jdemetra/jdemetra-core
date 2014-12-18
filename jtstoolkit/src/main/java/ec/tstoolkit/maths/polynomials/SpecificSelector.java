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

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SpecificSelector extends AbstractRootSelector {
    private Complex[] m_roots;

    private double m_epsilon = .03;

    /**
	 *
	 */
    public SpecificSelector() {
    }

    /**
     * 
     * @param roots
     */
    public SpecificSelector(final Complex[] roots) {
	m_roots = roots.clone();
    }


    /**
     * 
     * @param root
     * @return
     */
    @Override
    public boolean accept(final Complex root) {
	for (Complex element : m_roots)
	    if (root.equals(element, m_epsilon))
		return true;
	return false;
    }

    /**
     * 
     * @return
     */
    public double getEpsilon() {
	return m_epsilon;
    }

    /**
     * 
     * @return
     */
    public Complex[] getRoots() {
	return m_roots;
    }

    /**
     * 
     * @param value
     */
    public void setEpsilon(final double value) {
	m_epsilon = value;

    }

    /**
     * 
     * @param value
     */
    public void setRoots(final Complex[] value) {
	m_roots = value;

    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        m_sel=null;
        m_nsel=p;
        return false;
    }

}

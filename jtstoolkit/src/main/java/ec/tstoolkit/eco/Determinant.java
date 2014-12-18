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
package ec.tstoolkit.eco;

/**
 * 
 * @author Jean Palate
 */
public class Determinant {
    private double m_detcar, m_detman = 1;

    /**
     *
     */
    public Determinant() {
    }

    /**
     * 
     * @param var
     */
    public void add(final double var) {
 	update(var);
    }

    /**
         *
         */
    public void clear() {
	m_detman = 1;
	m_detcar = 0;
    }

    /**
     * 
     * @param n
     * @return
     */
    public double factor(final int n) {
	double det = Math.pow(m_detman, (1.0 / n));
	det *= Math.pow(2.0, (m_detcar / n));
	return det;
    }

    /**
     * 
     * @return
     */
    public double getLogDeterminant() {
	return Math.log(m_detman) + m_detcar * Math.log(2.0);
    }

    /**
     * 
     * @param var
     */
    public void remove(final double var) {
	update(1 / var);
    }

    private void update(final double var) {
	if (Double.isInfinite(var) || Double.isNaN(var) || var <= 0)
	    throw new EcoException(EcoException.LDet);
	/*
	 * if (var <= 0) { m_detman = 0; return; }
	 */

	m_detman *= var;
	while (m_detman >= 1) {
	    m_detman *= .0625;
	    m_detcar += 4.0;
	}

	while (m_detman != 0 && m_detman <= 0.0625) {
	    m_detman *= 16;
	    m_detcar -= 4.0;
	}
    }

}

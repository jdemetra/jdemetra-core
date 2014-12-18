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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.Jdk6;
import java.io.Serializable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsObservation implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5296836912344003330L;

    private final TsPeriod m_p;

    private final double m_val;

    /**
     * 
     * @param p
     * @param v
     */
    public TsObservation(final TsPeriod p, final double v) {
	m_p = p;
	m_val = v;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsObservation && equals((TsObservation) obj));
    }
    
    private boolean equals(TsObservation other) {
        return m_p.equals(other.m_p) && (m_val == other.m_val);
    }

    /**
     * 
     * @return
     */
    public TsPeriod getPeriod()
    {
	return m_p;
    }

    /**
     * 
     * @return
     */
    public double getValue() {
	return m_val;
    }

    @Override
    public int hashCode() {
        return 31 * m_p.hashCode() + Jdk6.Double.hashCode(m_val);
    }

    @Override
    public String toString() {
	StringBuilder buffer = new StringBuilder(64);
	buffer.append(m_p.toString());
	buffer.append(": ");
	buffer.append(m_val);
	return buffer.toString();
    }

}

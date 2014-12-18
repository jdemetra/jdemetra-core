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
package ec.tstoolkit.dstats;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DistInterval implements IInterval {
    private double m_r0;
    private double m_r1;

    /**
     * Default constructor; lower and upper bounds are set to 0,0
     */
    public DistInterval() {
	this(0, 0);
    }

    /**
     * The constructor initializes the bounds of the interval
     * 
     * @param r0
     *            The lower bound
     * @param r1
     *            The upper bound
     */
    public DistInterval(final double r0, final double r1) {
	setLBound(r0);
	setUBound(r1);
    }

    public double getLBound() {
	return m_r0;
    }

    public double getLength() {
	return getUBound() - getLBound();
    }

    public double getUBound() {
	return m_r1;
    }

    public boolean isEmpty() {
	return getUBound() != getLBound();
    }

    public boolean isValid() {
	return getUBound() >= getLBound();
    }

    public void setLBound(final double bound) {
	m_r0 = bound;
    }

    public void setUBound(final double bound) {
	m_r1 = bound;
    }

}

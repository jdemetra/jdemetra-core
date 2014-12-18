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
 * Default interval
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Interval implements IInterval {
    private double r0_;
    private double r1_;

    /**
     * Default constructor; lower and upper bounds are set to 0,0
     */
    public Interval() {
	this(0, 0);
    }

    /**
     * The constructor initializes the bounds of the interval.
     * r0 should be lower or equal r1.
     * 
     * @param r0 The lower bound
     * @param r1 The upper bound
     */
    public Interval(final double r0, final double r1) {
	r0_=r0;
	r1_=r1;
    }

    @Override
    public double getLBound() {
	return r0_;
    }

    @Override
    public double getLength() {
	return getUBound() - getLBound();
    }

    @Override
    public double getUBound() {
	return r1_;
    }

    @Override
    public boolean isEmpty() {
	return getUBound() == getLBound();
    }

    @Override
    public boolean isValid() {
	return getUBound() >= getLBound();
    }

    @Override
    public void setLBound(final double bound) {
	r0_ = bound;
    }

    @Override
    public void setUBound(final double bound) {
	r1_ = bound;
    }

}

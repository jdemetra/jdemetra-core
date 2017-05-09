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
package demetra.data;

import demetra.design.Development;
import demetra.design.Immutable;


/**
 * Default interval
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public class Interval {
    private final double lbound;
    private final double ubound;

    /**
     * Default constructor; lower and upper bounds are set to 0,0
     */
    public Interval() {
	this(0, 0);
    }

    /**
     * The constructor initializes the bounds of the interval.
     * r0 should be lower or equal r1. Not checked
     * 
     * @param r0 The lower bound
     * @param r1 The upper bound
     */
    public Interval(final double r0, final double r1) {
	lbound=r0;
	ubound=r1;
    }

    public double getLowerBound() {
	return lbound;
    }

    public double getLength() {
	return ubound-lbound;
    }

    public double getUpperBound() {
	return ubound;
    }

    public boolean isPoint() {
	return ubound == lbound;
    }

    public boolean isValid() {
	return ubound >= lbound;
    }

    public boolean contains(final double pt){
        return lbound<=pt && pt<=ubound;
    }

}

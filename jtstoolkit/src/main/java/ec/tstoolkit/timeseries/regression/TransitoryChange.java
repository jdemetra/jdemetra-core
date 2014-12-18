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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TransitoryChange extends AbstractOutlierVariable {

    double coefficient;

    static double ZERO = 1e-15;

    /**
     * 
     * @param p
     */
    public TransitoryChange(TsPeriod p)
    {
	super(p);
	coefficient = TransitoryChangeFactory.DEF_TCRATE;
    }

    /**
     * 
     * @param p
     * @param c
     */
    public TransitoryChange(TsPeriod p, double c)
    {
	super(p);
	coefficient = c;
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
	data.set(0);
	double cur = 1;
	int n = data.getLength();
	int i = position.minus(start);
	for (; i < 0; ++i) {
	    cur *= coefficient;
	    if (Math.abs(cur) < ZERO)
		return;
	}

	for (; i < n; ++i) {
	    data.set(i, cur);
	    cur *= coefficient;
	    if (Math.abs(cur) < ZERO)
		return;
	}
    }

    /**
     * 
     * @return
     */
    public double getCoefficient()
    {
	return coefficient;
    }

    @Override
    public OutlierType getOutlierType() {
	return OutlierType.TC;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
	if (domain.getFrequency() != position.getFrequency())
	    return false;
	return domain.getLast().minus(position) > 0;
    }

    /**
     * 
     * @param value
     */
    public void setCoefficient(double value)
    {
	coefficient = value;
    }

    @Override
    public FilterRepresentation getFilterRepresentation(int freq){

        return new FilterRepresentation(new RationalBackFilter(
            BackFilter.ONE, BackFilter.of(new double[]{1, -coefficient})), 0);
    }
}

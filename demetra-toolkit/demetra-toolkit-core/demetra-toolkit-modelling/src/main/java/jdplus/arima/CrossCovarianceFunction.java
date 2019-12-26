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
package jdplus.arima;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.design.SkipProcessing;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.ForeFilter;
import jdplus.math.linearfilters.IRationalFilter;
import jdplus.math.linearfilters.RationalFilter;


/**
 * 
  * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
@SkipProcessing(target = Immutable.class, reason = "field m_c is not final")
public final class CrossCovarianceFunction {

    /**
     * 
     * @param m1
     * @param m2
     * @return
     */
    public static CrossCovarianceFunction create(final IArimaModel m1, final IArimaModel m2) {
	try {
	    return new CrossCovarianceFunction(new RationalFilter(FiniteFilter.multiply(new FiniteFilter(
		    m1.getMa()), m2.getMa().mirror()), m1.getAr(), m2.getAr()
		    .mirror()), Math.sqrt(m1.getInnovationVariance()
		    * m2.getInnovationVariance()));
	} catch (ArimaException ex) {
	    return null;
	}
    }

    private final RationalFilter m_r;

    private double m_c = 1;

    /**
     * 
     * @param m1
     * @param m2
     */
    public CrossCovarianceFunction(final ILinearProcess m1, final ILinearProcess m2) {
	IRationalFilter f1 = m1.getFilter();
	IRationalFilter f2 = m2.getFilter();
	// m_r = f1*f2.Mirror...
	FiniteFilter n1 = new FiniteFilter(f1.getNumerator()), n2 = new FiniteFilter(
		f2.getNumerator()).mirror();
	BackFilter db1 = f1.getRationalBackFilter().getDenominator(), db2 = f2.getRationalBackFilter()
		.getDenominator();
	ForeFilter df1 = f1.getRationalForeFilter().getDenominator(), df2 = f2.getRationalForeFilter()
		.getDenominator();

	m_r = new RationalFilter(FiniteFilter.multiply(n1, n2), db1
		.times(df2.mirror()), df1.times(db2.mirror()));
	m_c = Math
		.sqrt(m1.getInnovationVariance() * m2.getInnovationVariance());

    }

    private CrossCovarianceFunction(RationalFilter r, double c) {
	m_r = r;
	m_c = c;
    }

    /**
     * 
     * @param k
     * @return
     */
    public double get(final int k) {
	return m_r.weight(k) * m_c;
    }

    /**
     * 
     * @return
     */
    public int getLBound() {
	return m_r.getLBound();
    }

    /**
     * 
     * @return
     */
    public int getUBound() {
	return m_r.getUBound();
    }

    /**
     * 
     * @return
     */
    public boolean hasLBound() {
	return m_r.hasLowerBound();
    }

    /**
     * 
     * @return
     */
    public boolean hasUBound() {
	return m_r.hasUpperBound();
    }

    /**
     * 
     * @param n
     * @param m
     */
    public void prepare(final int n, final int m) {
	m_r.prepare(n, m);
    }
}

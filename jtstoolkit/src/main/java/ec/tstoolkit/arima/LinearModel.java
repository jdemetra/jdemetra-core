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
package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.ForeFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.maths.linearfilters.RationalForeFilter;
import ec.tstoolkit.maths.linearfilters.RationalFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class LinearModel extends AbstractLinearModel {

    private final RationalFilter m_rf;

    private double m_var = 1;

    /**
     * 
     * @param lm
     */
    public LinearModel(final LinearModel lm) {
	m_rf = lm.m_rf;
	m_var = lm.m_var;
    }

    /**
     * 
     * @param rf
     * @param var
     */
    public LinearModel(final RationalFilter rf, final double var) {
	m_rf = rf;
	m_var = var;
    }

    /**
     * 
     * @return
     */
    public LinearModel doStationary() {
	RationalBackFilter rb = m_rf.getRationalBackFilter();
	BackFilter bdenom = rb.getDenominator();
	BackFilter.StationaryTransformation bst = new BackFilter.StationaryTransformation();
	if (bst.transform(bdenom))
	    bdenom = bst.stationaryFilter;

	RationalForeFilter rf = m_rf.getRationalForeFilter();
	ForeFilter fdenom = rf.getDenominator();
	ForeFilter.StationaryTransformation fst = new ForeFilter.StationaryTransformation();
	if (fst.transform(fdenom))
	    fdenom = fst.stationaryFilter;

	RationalFilter stfilter = new RationalFilter(m_rf.getNumerator(), bdenom, fdenom);
	return new LinearModel(stfilter, m_var);
    }

    /**
     * 
     * @return
     */
    @Override
    public RationalFilter getFilter() {
	return m_rf;
    }

    /**
     * 
     * @return
     */
    @Override
    public double getInnovationVariance() {
	return m_var;
    }

    @Override
    protected AutoCovarianceFunction initAcgf() {
	RationalFilter f = getFilter();
	Polynomial n = Polynomial.copyOf(f.getNumerator().getWeights());
	Polynomial bd = f.getRationalBackFilter().getDenominator().getPolynomial();
	Polynomial fd = f.getRationalForeFilter().getDenominator().getPolynomial();

	return new AutoCovarianceFunction(n, bd.times(fd), getInnovationVariance());
    }

    @Override
    public boolean isInvertible() {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isNull() {
	return m_var == 0;
    }

    @Override
    public boolean isStationary() {
	return true;
    }

}

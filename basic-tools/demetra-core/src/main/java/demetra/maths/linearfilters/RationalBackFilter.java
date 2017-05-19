/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/
package demetra.maths.linearfilters;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import java.util.function.IntToDoubleFunction;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class RationalBackFilter implements IRationalFilter {

    private final RationalFunction m_rfe;

    /**
     *
     */
    public RationalBackFilter() {
	m_rfe = RationalFunction.zero();
    }

    /**
     * 
     * @param num
     * @param denom
     */
    public RationalBackFilter(final BackFilter num, final BackFilter denom) {
	m_rfe = RationalFunction.of(num.getPolynomial(), denom.getPolynomial());
    }

    RationalBackFilter(final RationalFunction rfe) {
	m_rfe = rfe;
    }

    /**
     * 
     * @param nterms
     * @return
     */
    public RationalBackFilter drop(final int nterms) {
	RationalFunction rfe = m_rfe.drop(nterms);
	return new RationalBackFilter(rfe);
    }

    /**
     * 
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(double freq) {
	Complex n = Utilities.frequencyResponse(m_rfe.getNumerator().asFunction(),0, m_rfe.getNumerator().length(), -freq);
	Complex d = Utilities.frequencyResponse(m_rfe.getDenominator().asFunction(), 0, m_rfe.getDenominator().length(), -freq);
	return n.div(d);
    }

    @Override
    public BackFilter getDenominator() {
	Polynomial p = m_rfe.getDenominator();
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public int getLBound() {
	if (m_rfe.isFinite())
	    return -m_rfe.getNumerator().getDegree();
	else
	    return Integer.MIN_VALUE;
    }

    /**
     * 
     * @return
     */
    @Override
    public BackFilter getNumerator() {
	Polynomial p = m_rfe.getNumerator();
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public RationalFunction getRationalFunction()
    {
	return m_rfe;
    }

    /**
     * 
     * @return
     */
    public int getUBound() {
	return 0;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public double weight(int pos) {
	return m_rfe.get(-pos);
    }

    public IntToDoubleFunction weights() {
        return pos->weight(pos);
    }

    /**
     * 
     * @param n
     * @return
     */
    public double[] getWeights(final int n) {
	return m_rfe.coefficients(n);
    }

    @Override
    public boolean hasLowerBound() {
	return m_rfe.isFinite();
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasUpperBound() {
	return true;
    }

    /**
     * 
     * @return
     */
    public RationalForeFilter mirror() {
	return new RationalForeFilter(m_rfe);
    }

    /**
     * 
     * @param n
     */
    public void prepare(final int n) {
	m_rfe.prepare(n);
    }

    /**
     * 
     * @param r
     * @return
     */
    public RationalBackFilter times(final RationalBackFilter r) {
	Polynomial ln = m_rfe.getNumerator(), rn = r.m_rfe.getNumerator();
	Polynomial ld = m_rfe.getDenominator(), rd = r.m_rfe.getDenominator();
	Polynomial.SimplifyingTool psmp = new Polynomial.SimplifyingTool();
	if (psmp.simplify(ln, rd)) {
	    ln = psmp.getLeft();
	    rd = psmp.getRight();
	}
	if (psmp.simplify(rn, ld)) {
	    rn = psmp.getLeft();
	    ld = psmp.getRight();
	}

	Polynomial n = ln.times(rn), d = ld.times(rd);

	RationalFunction rfe = RationalFunction.of(n, d);
	return new RationalBackFilter(rfe);
    }
}

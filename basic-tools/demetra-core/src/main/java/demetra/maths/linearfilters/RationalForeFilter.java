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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class RationalForeFilter implements IRationalFilter {

    private final RationalFunction m_rfe;

    /**
     *
     */
    public RationalForeFilter() {
        m_rfe = RationalFunction.zero();
    }

    /**
     *
     * @param num
     * @param denom
     */
    public RationalForeFilter(final ForeFilter num, final ForeFilter denom) {
        m_rfe = RationalFunction.of(num.getPolynomial(), denom.getPolynomial());
    }

    RationalForeFilter(final RationalFunction rfe) {
        m_rfe = rfe;
    }

    /**
     * Returns the filter obtained by eliminating the first n terms See the
     * "RationalFunction class for further information
     *
     * @param n The number ofInternal terms being dropped
     * @return A new rational filter is returned
     */
    public RationalForeFilter drop(final int n) {
        RationalFunction rfe = m_rfe.drop(n);
        return new RationalForeFilter(rfe);
    }

    /**
     *
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        Complex n = Utilities.frequencyResponse(m_rfe.getNumerator().asFunction(), 0, m_rfe.getNumerator().length(), freq);
        Complex d = Utilities.frequencyResponse(m_rfe.getDenominator().asFunction(), 0, m_rfe.getDenominator().length(), freq);
        return n.div(d);
    }

    /**
     *
     * @return
     */
    @Override
    public ForeFilter getDenominator() {
        Polynomial p = m_rfe.getDenominator();
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return 0;
    }

    /**
     *
     * @return
     */
    public RationalBackFilter getMirror() {
        return new RationalBackFilter(m_rfe);
    }

    @Override
    public ForeFilter getNumerator() {
        Polynomial p = m_rfe.getNumerator();
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    public RationalFunction getRationalFunction() {
        return m_rfe;
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        if (m_rfe.isFinite()) {
            return m_rfe.getNumerator().getDegree();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * 
     * @param pos
     * @return
     */
    public double weight(int pos) {
	return m_rfe.get(pos);
    }
    /**
     *
     * @param n
     * @return
     */
    public double[] getWeights(final int n) {
        return m_rfe.coefficients(n);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasLowerBound() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasUpperBound() {
        return m_rfe.isFinite();
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
    public RationalForeFilter times(final RationalForeFilter r) {
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
        // normalize the filter...
        double d0 = d.get(0);
        if (d0 != 1) {
            n = n.divide(d0);
            d = d.divide(d0);
        }
        RationalFunction rfe = RationalFunction.of(n, d);
        return new RationalForeFilter(rfe);
    }
}

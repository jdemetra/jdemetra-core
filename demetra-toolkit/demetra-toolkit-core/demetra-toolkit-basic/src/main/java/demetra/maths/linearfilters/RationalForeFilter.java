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
public final class RationalForeFilter implements IRationalFilter {

    public static final RationalForeFilter ZERO=new RationalForeFilter();

    private final RationalFunction rationalFunction;
    private final int fshift;

    /**
     *
     */
    public RationalForeFilter() {
        rationalFunction = RationalFunction.zero();
        fshift=0;
    }

    /**
     *
     * @param num
     * @param denom
     */
    public RationalForeFilter(final ForeFilter num, final ForeFilter denom, final int fshift) {
        rationalFunction = RationalFunction.of(num.getPolynomial(), denom.getPolynomial());
        this.fshift=fshift;
    }

    RationalForeFilter(final RationalFunction rfe, final int fshift) {
        rationalFunction = rfe;
        this.fshift=fshift;
    }

    /**
     * Returns the filter obtained by eliminating the first n terms See the
     * "RationalFunction class for further information
     *
     * @param n The number ofInternal terms being dropped
     * @return A new rational filter is returned
     */
    public RationalForeFilter drop(final int n) {
        RationalFunction rfe = rationalFunction.drop(n);
        return new RationalForeFilter(rfe, fshift+n);
    }

    /**
     *
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        IntToDoubleFunction fn = rationalFunction.getNumerator()::get;
        Complex n = FilterUtility.frequencyResponse(i->fn.applyAsDouble(i-fshift), fshift, fshift+rationalFunction.getNumerator().degree(), freq);
        Complex d = FilterUtility.frequencyResponse(rationalFunction.getDenominator()::get, 0, rationalFunction.getDenominator().degree(), freq);
        return n.div(d);
    }

    /**
     *
     * @return
     */
    @Override
    public ForeFilter getDenominator() {
        Polynomial p = rationalFunction.getDenominator();
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
        return new RationalBackFilter(rationalFunction, fshift);
    }

    @Override
    public ForeFilter getNumerator() {
        Polynomial p = rationalFunction.getNumerator();
        return new ForeFilter(p);
    }

    /**
     *
     * @return
     */
    public RationalFunction getRationalFunction() {
        return rationalFunction;
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        if (rationalFunction.isFinite()) {
            return rationalFunction.getNumerator().degree();
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
	return rationalFunction.get(pos);
    }
    /**
     *
     * @param n
     * @return
     */
    public double[] getWeights(final int n) {
        return rationalFunction.coefficients(n);
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
        return rationalFunction.isFinite();
    }

    /**
     *
     * @param n
     */
    public void prepare(final int n) {
        rationalFunction.prepare(n);
    }

    /**
     *
     * @param r
     * @return
     */
    public RationalForeFilter times(final RationalForeFilter r) {
        Polynomial ln = rationalFunction.getNumerator(), rn = r.rationalFunction.getNumerator();
        Polynomial ld = rationalFunction.getDenominator(), rd = r.rationalFunction.getDenominator();
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
        return new RationalForeFilter(rfe, fshift+r.fshift);
    }

    @Override
    public RationalBackFilter getRationalBackFilter() {
        return RationalBackFilter.ZERO;
    }

    @Override
    public RationalForeFilter getRationalForeFilter() {
        return this;
    }
}

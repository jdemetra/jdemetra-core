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

import demetra.data.Doubles;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.Complex;
import demetra.maths.Simplifying;
import demetra.maths.polynomials.IRootsSolver;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.PolynomialException;
import java.util.function.IntToDoubleFunction;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class ForeFilter extends AbstractFiniteFilter {

     /**
     *
     */
    public static final ForeFilter ZERO = new ForeFilter(Polynomial.ZERO);

    /**
     *
     */
    public static final ForeFilter ONE = new ForeFilter(Polynomial.ONE);

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static ForeFilter add(final double d, final ForeFilter l) {
	Polynomial p = l.m_p.plus(d);
	return new ForeFilter(p);
    }

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static ForeFilter multiply(final double d, final ForeFilter l) {
	Polynomial p = l.m_p.times(d);
	return new ForeFilter(p);
    }

    private final Polynomial m_p;

     /**
     * Create a new BackFilter from the specified coefficients.<br>
     * Note that a cached one can be returned if available (ONE, ZERO, ...)
     * @param coefficients
     * @return 
     */
    public static ForeFilter ofInternal(double[] coefficients) {
        if (coefficients.length == 1) {
            if (coefficients[0] == 1.0)
                return ForeFilter.ONE;
            else if (coefficients[0] == 0.0)
                return ForeFilter.ZERO;
        }
        return new ForeFilter(Polynomial.ofInternal(coefficients));
    }
   /**
     * 
     * @param p
     */
    public ForeFilter(final Polynomial p) {
	m_p = p;
    }

    /**
     * 
     * @param r
     * @return
     */
    public ForeFilter divide(final ForeFilter r) {
	Polynomial.Division div = Polynomial.divide(m_p, r.m_p);
	if (!div.getRemainder().isZero())
	    throw new PolynomialException(PolynomialException.DIVISION);
	return new ForeFilter(div.getQuotient());
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double get(final int idx) {
	return m_p.get(idx);
    }

    public Polynomial getPolynomial() {
         return m_p;
    }

    /**
     * 
     * @return
     */
    public int getDegree() {
	return m_p.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public int length() {
	return m_p.getDegree() + 1;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getLowerBound() {
	return 0;
    }

    @Override
    public int getUpperBound() {
	return m_p.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
	return i->m_p.get(i);
    }
    
    @Override
    public Polynomial asPolynomial(){
        return m_p;
    }

    /**
     * 
     * @return
     */
    public boolean isIdentity() {
	return m_p.isIdentity(); 
    }

    /**
     * 
     * @return
     */
    public boolean isNull() {
	return m_p.isZero();
    }

    /**
     * 
     * @param d
     * @return
     */
    public ForeFilter minus(final double d) {
	Polynomial p = m_p.minus(d);
	return new ForeFilter(p);
    }

    /**
     * 
     * @param r
     * @return
     */
    public ForeFilter minus(final ForeFilter r) {
	Polynomial p = m_p.minus(r.m_p);
	return new ForeFilter(p);
    }

    /**
     * 
     * @return
     */
    @Override
    public BackFilter mirror() {
	return new BackFilter(m_p);
    }

    /**
     * 
     * @return
     */
    public ForeFilter negate() {
	Polynomial p = m_p.negate();
	return new ForeFilter(p);
    }

    /**
     * 
     * @return
     */
    public ForeFilter normalize() {
	double r = m_p.get(0);
	if (r == 0 || r == 1)
            return this;
	else 
	    return new ForeFilter(m_p.times(1 / r));
    }

    /**
     * 
     * @param d
     * @return
     */
    public ForeFilter plus(final double d) {
	Polynomial p = m_p.plus(d);
	return new ForeFilter(p);
    }

    /**
     * 
     * @param r
     * @return
     */
    public ForeFilter plus(final ForeFilter r) {
	Polynomial p = m_p.plus(r.m_p);
	return new ForeFilter(p);
    }

    /**
     * 
     * @return
     */
    public Complex[] roots() {
	return m_p.roots();
    }

    /**
     * 
     * @param searcher
     * @return
     */
    public Complex[] roots(final IRootsSolver searcher)
    {
	return m_p.roots(searcher);
    }

    /**
     * 
     * @param d
     * @return
     */
    public ForeFilter times(final double d) {
	Polynomial p = m_p.times(d);
	return new ForeFilter(p);
    }

    /**
     * 
     * @param r
     * @return
     */
    public ForeFilter times(final ForeFilter r) {
	Polynomial p = m_p.times(r.m_p);
	return new ForeFilter(p);
    }

    @Override
    public String toString() {
	return m_p.toString('B', true);
    }
}

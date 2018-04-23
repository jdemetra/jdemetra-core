/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
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
import demetra.maths.Simplifying;
import demetra.maths.polynomials.IRootsSolver;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRootSelector;
import demetra.maths.polynomials.UnitRoots;
import demetra.maths.polynomials.UnitRootsSolver;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSequence;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class BackFilter extends AbstractFiniteFilter {
    
    /**
     * BackFilter(0)
     */
    public static final BackFilter ZERO = new BackFilter(Polynomial.ZERO);

    /**
     * BackFilter(1)
     */
    public static final BackFilter ONE = new BackFilter(Polynomial.ONE);
    
    /**
     * BackFilter(1 - x)
     */
    public static final BackFilter D1 = new BackFilter(UnitRoots.D1);

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static BackFilter add(final double d, final BackFilter l) {
	Polynomial p = l.polynomial.plus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static BackFilter multiply(final double d, final BackFilter l) {
	Polynomial p = l.polynomial.times(d);
	return new BackFilter(p);
    }

    /**
     * Create a new BackFilter from the specified coefficients.<br>
     * Note that a cached one can be returned if available (ONE, ZERO, ...)
     * @param coefficients
     * @return 
     */
    public static BackFilter ofInternal(double... coefficients) {
        if (coefficients.length == 1) {
            if (coefficients[0] == 1.0)
                return BackFilter.ONE;
            else if (coefficients[0] == 0.0)
                return BackFilter.ZERO;
        }
        return new BackFilter(Polynomial.ofInternal(coefficients));
    }
    
    private final Polynomial polynomial;

    /**
     * 
     * @param p
     */
    public BackFilter(final Polynomial p) {
	polynomial = p;
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter divide(final BackFilter r) {
	Polynomial.Division div = Polynomial.divide(polynomial, r.polynomial);
	// if (!div.getRemainder().isNull())
	// throw new PolynomialException(PolynomialException.Division);
	return new BackFilter(div.getQuotient());
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final BackFilter other = (BackFilter) obj;
	return this.polynomial.equals(other.polynomial);
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double get(final int idx) {
	return polynomial.get(idx);
    }

    /**
     * Gets the coefficients of the filter, in the form of a polynomial.
     * The coefficients are in the order of the powers of the back shift operator:
     * <br>
     * (1 a1 a2 ...)
     * @return
     */
    public Polynomial asPolynomial() {
	return polynomial;
    }
  

    /**
     * 
     * @return
     */
    @Override
    public int length() {
	return polynomial.getDegree() + 1;
    }

    @Override
    public int getLowerBound() {
	return -polynomial.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getUpperBound() {
	return 0;
    }

    public int getDegree(){
        return polynomial.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public IntToDoubleFunction weights() {
	return i->polynomial.get(-i);
   }

    @Override
    public int hashCode() {
	return polynomial.hashCode();
    }

    /**
     * 
     * @return
     */
    public boolean isIdentity() {
	return polynomial.isIdentity();
    }

    /**
     * 
     * @return
     */
    public boolean isNull() {
	return polynomial.isZero();
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter minus(final BackFilter r) {
	Polynomial p = polynomial.minus(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter minus(final double d) {
	Polynomial p = polynomial.minus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    @Override
    public ForeFilter mirror() {
	return new ForeFilter(polynomial);
    }

    /**
     * 
     * @return
     */
    public BackFilter negate() {
	Polynomial p = polynomial.negate();
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public BackFilter normalize() {
	double r = polynomial.get(0);
	if (r == 0 || r == 1)
            return this;
        else
	    return new BackFilter(polynomial.times(1 / r));
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter plus(final BackFilter r) {
	Polynomial p = polynomial.plus(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter plus(final double d) {
	Polynomial p = polynomial.plus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public Complex[] roots() {
	return polynomial.roots();
    }

    /**
     * 
     * @param solver
     * @return
     */
    public Complex[] roots(final IRootsSolver solver) {
	return polynomial.roots(solver);
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter times(final BackFilter r) {
	Polynomial p = polynomial.times(r.polynomial);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter times(final double d) {
	Polynomial p = polynomial.times(d);
	return new BackFilter(p);
    }

    @Override
    public String toString() {
	return polynomial.toString('B', true);
    }
    
        /**
     * if some simplification is done, l[0] and r[0] must be unchanged; c[0]=1;
     * 
     */
    public static class SimplifyingTool extends Simplifying<BackFilter> {

	private final boolean m_sur;

	/**
	 * 
	 * @param simplifyUR
	 */
	public SimplifyingTool(final boolean simplifyUR) {
	    m_sur = simplifyUR;
	}

	@Override
	public boolean simplify(final BackFilter left, final BackFilter right) {
	    clear();
	    if (left.getDegree() == 0 || right.getDegree() == 0)
		return false;
	    Polynomial lp = left.polynomial, rp = right.polynomial, p;
	    double l0 = lp.get(0), r0 = rp.get(0);
	    Polynomial.SimplifyingTool psimp = new Polynomial.SimplifyingTool();
	    if (psimp.simplify(lp, rp)) {
		lp = psimp.getLeft();
		rp = psimp.getRight();
		lp = lp.times(l0 / lp.get(0));
		rp = rp.times(r0 / rp.get(0));
		p = psimp.getCommon();
		p = p.divide(p.get(0));

		if (m_sur || p.getDegree() == 0) {

		    common = new BackFilter(p);
		    simplifiedLeft = new BackFilter(lp);
		    simplifiedRight = new BackFilter(rp);
		    return true;
		} else {
		    UnitRootSelector ursel = new UnitRootSelector();
		    if (ursel.select(p)){
			Polynomial pnur = ursel.getOutofSelection();
			Polynomial pur = ursel.getSelection();

			pur = pur.divide(pur.get(0));
			pnur = pnur.divide(pnur.get(0));

			common = new BackFilter(pnur);
			simplifiedLeft = new BackFilter(lp.times(pur));
			simplifiedRight = new BackFilter(rp.times(pur));
			return true;
		    } else // no unit roots
		    {
			common = new BackFilter(p);
			simplifiedLeft = new BackFilter(lp);
			simplifiedRight = new BackFilter(rp);
			return true;
		    }
		}
	    }
	    return false;
	}
    }

    /**
     * 
     */
    public static class StationaryTransformation
    {

        /**
         *
         */
        /**
         *
         */
        public BackFilter unitRoots, stationaryFilter;

	private int freq;

        /**
         * 
         */
        public StationaryTransformation()
        {
	    this.freq = 0;
	}

        /**
         * 
         * @param freq
         */
        public StationaryTransformation(int freq)
        {
	    this.freq = freq;
	}

        /**
         * 
         * @param f
         * @return
         */
        public boolean transform(BackFilter f)
        {
	    UnitRootsSolver urs = freq == 0 ? new UnitRootsSolver()
		    : new UnitRootsSolver(freq);
	    urs.factorize(f.polynomial);
	    unitRoots = new BackFilter(urs.getUnitRoots().toPolynomial());
	    if (unitRoots.getDegree() == 0) {
		stationaryFilter = f;
		return false;
	    } else {
		stationaryFilter = new BackFilter(urs.remainder());
		return true;
	    }
	}
    }

}

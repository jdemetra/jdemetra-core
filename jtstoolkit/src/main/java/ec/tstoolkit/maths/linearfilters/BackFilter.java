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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.Simplifying;
import ec.tstoolkit.maths.polynomials.IRootsSolver;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRootSelector;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.polynomials.UnitRootsSolver;
import ec.tstoolkit.utilities.Arrays2;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class BackFilter extends AbstractFiniteFilter {

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
	    if (left.getLength() == 1 || right.getLength() == 1)
		return false;
	    Polynomial lp = left.m_p, rp = right.m_p, p;
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

		    m_common = new BackFilter(p);
		    m_left = new BackFilter(lp);
		    m_right = new BackFilter(rp);
		    return true;
		} else {
		    UnitRootSelector ursel = new UnitRootSelector();
		    if (ursel.select(p))
		    // remove the ur from the factorization and add it
		    // to the components
		    {
			Polynomial pnur = ursel.getOutofSelection();
			Polynomial pur = ursel.getSelection();

			pur = pur.divide(pur.get(0));
			pnur = pnur.divide(pnur.get(0));

			m_common = new BackFilter(pnur);
			m_left = new BackFilter(lp.times(pur));
			m_right = new BackFilter(rp.times(pur));
			return true;
		    } else // no unit roots
		    {
			m_common = new BackFilter(p);
			m_left = new BackFilter(lp);
			m_right = new BackFilter(rp);
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
	    urs.factorize(f.m_p);
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
	Polynomial p = l.m_p.plus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @param l
     * @return
     */
    public static BackFilter multiply(final double d, final BackFilter l) {
	Polynomial p = l.m_p.times(d);
	return new BackFilter(p);
    }

    /**
     * Create a new BackFilter from the specified coefficients.<br>
     * Note that a cached one can be returned if available (ONE, ZERO, ...)
     * @param coefficients
     * @return 
     */
    public static BackFilter of(double[] coefficients) {
        if (coefficients.length == 1) {
            if (coefficients[0] == 1.0)
                return BackFilter.ONE;
            else if (coefficients[0] == 0.0)
                return BackFilter.ZERO;
        }
        return new BackFilter(Polynomial.of(coefficients));
    }
    
    private final Polynomial m_p;

    /**
     * 
     * @param p
     */
    public BackFilter(final Polynomial p) {
	m_p = p.adjustDegree();
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter divide(final BackFilter r) {
	Polynomial.Division div = Polynomial.divide(m_p, r.m_p);
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
	return this.m_p.equals(other.m_p);
    }

    /**
     * 
     * @param idx
     * @return
     */
    public double get(final int idx) {
	return m_p.get(idx);
    }

    /**
     * 
     * @return
     */
    public double[] getCoefficients() {
	return m_p.getCoefficients();
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
    public int getLength() {
	return m_p.getDegree() + 1;
    }

    @Override
    public int getLowerBound() {
	return -m_p.getDegree();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getUpperBound() {
	return 0;
    }

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public double getWeight(int pos) {
	return m_p.get(-pos);
    }

    /**
     * 
     * @return
     */
    @Override
    public double[] getWeights() {
	double[] w = m_p.getCoefficients();
	Arrays2.reverse(w);
	return w;
    }

    @Override
    public int hashCode() {
	return m_p.hashCode();
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
     * @param r
     * @return
     */
    public BackFilter minus(final BackFilter r) {
	Polynomial p = m_p.minus(r.m_p);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter minus(final double d) {
	Polynomial p = m_p.minus(d);
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public ForeFilter mirror() {
	return new ForeFilter(m_p);
    }

    /**
     * 
     * @return
     */
    public BackFilter negate() {
	Polynomial p = m_p.negate();
	return new BackFilter(p);
    }

    /**
     * 
     * @return
     */
    public BackFilter normalize() {
	/*
	 * int idx = 0; double[] c = m_p.getCoefficients(); while (idx <
	 * c.length && c[idx] == 0) ++idx; if (idx == c.length) throw new
	 * LinearFilterException("Illegal operation", "BFilter.normalize"); if
	 * (idx != 0) { double[] nc = new double[c.length - idx]; for (int i =
	 * 0; i < nc.length; ++i) nc[i] = c[idx + i]; m_p =
	 * Polynomial.promote(nc); }
	 */

	double r = m_p.get(0);
	if (r == 0 || r == 1)
            return this;
        else
	    return new BackFilter(m_p.times(1 / r));
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter plus(final BackFilter r) {
	Polynomial p = m_p.plus(r.m_p);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter plus(final double d) {
	Polynomial p = m_p.plus(d);
	return new BackFilter(p);
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
     * @param solver
     * @return
     */
    public Complex[] roots(final IRootsSolver solver) {
	return m_p.roots(solver);
    }

    /**
     * 
     * @param r
     * @return
     */
    public BackFilter times(final BackFilter r) {
	Polynomial p = m_p.times(r.m_p);
	return new BackFilter(p);
    }

    /**
     * 
     * @param d
     * @return
     */
    public BackFilter times(final double d) {
	Polynomial p = m_p.times(d);
	return new BackFilter(p);
    }

    @Override
    public String toString() {
	return m_p.toString('B', true);
    }
}

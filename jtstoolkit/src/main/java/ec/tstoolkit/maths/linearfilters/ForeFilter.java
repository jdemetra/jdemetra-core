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
import ec.tstoolkit.maths.polynomials.PolynomialException;
import ec.tstoolkit.maths.polynomials.UnitRootSelector;
import ec.tstoolkit.maths.polynomials.UnitRootsSolver;

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
    public static class SimplifyingTool extends Simplifying<ForeFilter> {

	private final boolean m_sur;

	/**
	 * 
	 * @param simplifyUR
	 */
	public SimplifyingTool(final boolean simplifyUR) {
	    m_sur = simplifyUR;
	}

	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	@Override
	public boolean simplify(final ForeFilter left, final ForeFilter right) {
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

		    m_common = new ForeFilter(p);
		    m_left = new ForeFilter(lp);
		    m_right = new ForeFilter(rp);
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

			m_common = new ForeFilter(pnur);
			m_left = new ForeFilter(lp.times(pur));
			m_right = new ForeFilter(rp.times(pur));
			return true;
		    } else // no unit roots
		    {
			m_common = new ForeFilter(p);
			m_left = new ForeFilter(lp);
			m_right = new ForeFilter(rp);
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
        public ForeFilter unitRoots, stationaryFilter;

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
        public boolean transform(ForeFilter f)
        {
	    UnitRootsSolver urs = freq == 0 ? new UnitRootsSolver()
		    : new UnitRootsSolver(freq);
	    urs.factorize(f.m_p);
	    unitRoots = new ForeFilter(urs.getUnitRoots().toPolynomial());
	    if (unitRoots.getDegree() == 0) {
		stationaryFilter = f;
		return false;
	    } else {
		stationaryFilter = new ForeFilter(urs.remainder());
		return true;
	    }
	}
    }

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
     * 
     * @param w
     */
    public ForeFilter(final double[] w) {
	m_p = Polynomial.of(w).adjustDegree();
    }

    /**
     * 
     * @param p
     */
    public ForeFilter(final Polynomial p) {
	m_p = p.adjustDegree();
    }

    /**
     * 
     * @param r
     * @return
     */
    public ForeFilter divide(final ForeFilter r) {
	Polynomial.Division div = Polynomial.divide(m_p, r.m_p);
	if (!div.getRemainder().isZero())
	    throw new PolynomialException(PolynomialException.Division);
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
    public int getLength() {
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
     * @param pos
     * @return
     */
    @Override
    public double getWeight(final int pos) {
	return m_p.get(pos);
    }

    /**
     * 
     * @return
     */
    @Override
    public double[] getWeights() {
	return m_p.getCoefficients();
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

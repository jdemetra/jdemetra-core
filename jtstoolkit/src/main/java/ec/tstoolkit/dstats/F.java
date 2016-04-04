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
package ec.tstoolkit.dstats;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.StochasticRandomizer;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class F implements IContinuousDistribution {

    private int m_k1 = 1;
    private int m_k2 = 1;

    @Override
    public double getDensity(final double x) {
	return SpecialFunctions.FDensity(x, m_k1, m_k2);
    }

    /**
     * 
     * @return
     */
    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("F with ");
	sb.append(m_k1);
	sb.append(" degrees of freedom in the nominator and ");
	sb.append(m_k2);
	sb.append(" degrees of freedom in the denominator");
	return sb.toString();
    }

    /**
     * Gets the value of K2
     * @return
     */
    public int getDFDenom() {
	return m_k2;
    }

    /**
     * Gets the value of K1
     * @return
     */
    public int getDFNum() {
	return m_k1;
    }

    /**
     * Gets the expectation of the F-distribution; it is defined as k2/(k2-2)
     * 
     * @return
     * @see be.nbb.dstats.IDistribution#getExpectation()
     */
    @Override
    public double getExpectation() {
	if (m_k2 <= 2)
	    throw new DStatException("Expectation undefined for k2 <= 2", "F");
	return (m_k2 / (m_k2 - 2.0));
    }

    @Override
    public double getLeftBound() {
	return 0.0;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return 0;
	double res = SpecialFunctions.FProbability(x, m_k1, m_k2);
	if (pt == ProbabilityType.Lower)
	    res = 1.0 - res;
	return res;
    }

    @Override
    public double getProbabilityForInterval(final double x, final double y) {
	double py = getProbability(y, ProbabilityType.Lower);
	double px = getProbability(x, ProbabilityType.Lower);

	return y > x ? py - px : px - py;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
	if (pt == ProbabilityType.Upper)
	    p = 1.0 - p;
	if (p < EPS_P || 1 - p < EPS_P)
	    throw new DStatException(DStatException.ERR_INV_SMALL, "F");
	double start = m_k2 <= 2 ? 1 : m_k2 / (m_k2 - 2.0);
	return ProbInvFinder.find(p, start, EPS_P, EPS_X, this);
    }

    @Override
    public double getRightBound() {
	return Double.POSITIVE_INFINITY;
    }

    /**
     * Gets the variance of the Beta distribution. It is defined as
     * (2*k2*(k1+k2-2))/(k1*(k2-2)^2*(k2-4)); it throws an exception for k2 .LE.
     * 4.
     * 
     * @throws DStatException
     * @see be.nbb.dstats.IDistribution#getVariance()
     */
    @Override
    public double getVariance() throws DStatException {
	if (m_k2 <= 4)
	    throw new DStatException("Variance undefined for k2 <= 4", "F");

	double top = (2 * m_k2 * m_k2 * (m_k1 + m_k2 - 2));
	double bot = (m_k1 * (m_k2 - 2) * (m_k2 - 2) * (m_k2 - 4));
	return top / bot;
    }

    @Override
    public BoundaryType hasLeftBound() {
	return (m_k1 <= 2) ? BoundaryType.Asymptotical : BoundaryType.Finite;
    }

    @Override
    public BoundaryType hasRightBound() {
	return BoundaryType.None;
    }

    @Override
    public boolean isSymmetrical() {
	return false;
    }

    @Override
    public double random(IRandomNumberGenerator rng) {
	if (rng == null)
	    throw new DStatException("No valid Random Number Generator", "F");

	return StochasticRandomizer.F(rng, m_k1, m_k2);
    }

    /**
     * Sets the value of K2, will throw an exception when K2 .LT. 1
     * 
     * @param m_k2
     * @throws DStatException
     *             K2 .LT. 1
     */
    public void setDFDenom(final int m_k2) {
	if (m_k2 < 1)
	    throw new DStatException(
		    "Degrees of freedom must be larger or equal to 1", "F");
	this.m_k2 = m_k2;
    }

    /**
     * Sets the value of K1, will throw an exception when K1 .LT. 1
     * 
     * @param m_k1
     * @throws DStatException
     *             K1 .LT. 1
     */
    public void setDFNum(final int m_k1) {
	if (m_k1 < 1)
	    throw new DStatException(
		    "Degrees of freedom must be larger or equal to 1", "F");
	this.m_k1 = m_k1;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("F(");
	sb.append(m_k1);
	sb.append(',');
	sb.append(m_k2);
	sb.append(')');
	return sb.toString();
    }
}

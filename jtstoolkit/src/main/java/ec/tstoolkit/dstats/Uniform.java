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
public class Uniform implements IContinuousDistribution {

    private double m_max;
    private double m_min;

    @Override
    public double getDensity(final double x) throws DStatException {
	if (m_min >= m_max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	return 1.0 / (m_max - m_min);
    }

    /**
     * 
     * @return
     */
    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("Uniform distribution with Lower bound = ");
	sb.append(m_min);
	sb.append(" and Upper bound = ");
	sb.append(m_max);
	return sb.toString();
    }

    /**
     * Gets the expectation of the t-distribution; it is defined as (U-L)/2
     * 
     * @return
     * @see be.nbb.dstats.IDistribution#getExpectation()
     */
    @Override
    public double getExpectation() {
	return (m_max - m_min) / 2.0;
    }

    @Override
    public double getLeftBound() {
	return m_min;
    }

    /**
     * Gets the upper bound of the uniform distribution
     * 
     * @return
     */
    public double getMax() {
	return m_max;
    }

    /**
     * Gets the lower bound of the uniform distribution
     * 
     * @return
     */
    public double getMin() {
	return m_min;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt)
	    throws DStatException {
	if (m_min >= m_max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	if ((x < m_min) || (x > m_max))
	    throw new DStatException(
		    "The argument must lie between Min and Max", "Uniform");

	double res = (x - m_min) / (m_max - m_min);
	if (pt == ProbabilityType.Upper)
	    res = 1.0 - res;
	return res;
    }

    @Override
    public double getProbabilityForInterval(final double x, final double y)
	    throws DStatException {
	double px = getProbability(x, ProbabilityType.Lower);
	double py = getProbability(y, ProbabilityType.Lower);

	return x < y ? py - px : px - py;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt)
	    throws DStatException {
	if (p < 0.0 || p > 1.0)
	    throw new DStatException("x mus respect 0.0 <= x <= 1.0", "Uniform");
	if (m_min >= m_max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	if (pt == ProbabilityType.Upper)
	    p = 1.0 - p;
	return p * (m_max - m_min) + m_min;
    }

    @Override
    public double getRightBound() {
	return m_max;
    }

    /**
     * Gets the variance of the t-distribution; t is defined as
     * ((U-L+1)*(U-L-1))/12
     * 
     * @return
     * @see be.nbb.dstats.IDistribution#getVariance()
     */
    @Override
    public double getVariance() {
	return ((m_max - m_min + 1) * (m_max - m_min) - 1) / 12.0;
    }

    @Override
    public BoundaryType hasLeftBound() {
	return BoundaryType.Finite;
    }

    @Override
    public BoundaryType hasRightBound() {
	return BoundaryType.Finite;
    }

    @Override
    public boolean isSymmetrical() {
	return true;
    }

    @Override
    public double random(IRandomNumberGenerator rng) throws DStatException {
	if (rng == null)
	    throw new DStatException("No valid Random Number Generator",
		    "Uniform");

	return StochasticRandomizer.uniform(rng, m_min, m_max);
    }

    /**
     * Sets the upper bound of the uniform distribution
     * 
     * @param max
     */
    public void setMax(final double max) {
	m_max = max;
    }

    /**
     * Sets the lower bound of the uniform distribution
     * 
     * @param min
     */
    public void setMin(final double min) {
	m_min = min;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("U(");
	sb.append(m_min);
	sb.append(',');
	sb.append(m_max);
	sb.append(')');
	return sb.toString();

    }
}

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
 * Chi2 Distribution
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Chi2 implements IContinuousDistribution {
    
    public static final String NAME="Chi2";

    private int df_;

    /**
     * Default constructors; 2 degrees of freedom unless otherwise set by
     * setDegreesofFreedom(int)
     */
    public Chi2() {
	df_ = 2;
    }

    /**
     * Gets the degrees of freedom of the distribution
     * @return A strictly positive number
     */
    public int getDegreesofFreedom() {
	return df_;
    }

    @Override
    public double getDensity(final double x) {
	if (x < 0.0)
	    return 0;

	return SpecialFunctions.chiSquareDensity(x, df_);
    }

    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("Chi2 with ");
	sb.append(df_);
	sb.append(" degrees of freedom ");
	return sb.toString();
    }

    /**
     * Gets the expectation of the Chi2 distribution; it is defined as degrees
     * of freedom if df &gt 2, otherwise an exception is thrown
     * 
     * @return
     * @throws DStatException An exception is thrown if the expectation doesn't exist
     * (df &lt= 2)
     */
    @Override
    public double getExpectation() {
	if (df_ <= 2)
	    throw new DStatException("Expectation not defined for df <= 2",
		    NAME);
	return df_;
    }

    @Override
    public double getLeftBound() {
	return 0.0;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return 0;

	double res;
	res = SpecialFunctions.chiSquare(x, df_);
	if (pt == ProbabilityType.Upper)
	    res = 1 - res;
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
	if (p < EPS || 1 - p < EPS)
	    throw new DStatException(DStatException.ERR_INV_SMALL, NAME);
	double start = df_;
	return ProbInvFinder.find(p, start, EPS * .1, 1e-4, this);
    }

    @Override
    public double getRightBound() {
	return Double.POSITIVE_INFINITY;
    }

    /**
     * Gets the variance of the Beta distribution; it is defined as degrees of
     * freedom *2 if df &gt 2, otherwise an exception is thrown
     * 
     * @return
      * @throws DStatException An exception is thrown if the variance doesn't exist
    */
    @Override
    public double getVariance() {
	if (df_ <= 2)
	    throw new DStatException("Variance not defined for df <= 2", NAME);
	return df_ * 2.0;
    }

    @Override
    public BoundaryType hasLeftBound() {
	return df_ <= 2 ? BoundaryType.Asymptotical : BoundaryType.Finite;
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
	return StochasticRandomizer.chi2(rng, df_);
    }

    /**
     * Sets the degrees of freedom. 
     * @param df A number &gt= 1
     * @throws DStatException An exception is thrown if the degrees of freedom
     * is &lt 1
     */
    public void setDegreesofFreedom(final int df) {
	if (df < 1)
	    throw new DStatException("Degrees of freedom must be >= 1", NAME);
	df_ = df;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(NAME).append('(');
	sb.append(df_);
	sb.append(')');
	return sb.toString();
    }

}

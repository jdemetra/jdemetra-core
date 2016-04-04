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
import ec.tstoolkit.dstats.Utility.calcProbDelegate;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.StochasticRandomizer;
import java.util.Formatter;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Normal implements IContinuousDistribution {

    private double mean_;
    private double stdev_;

    /**
     * Default constructor; mean = 0.0 and variance = 1.0 i.e. the standard
     * normal distribution
     */
    public Normal() {
	mean_ = 0.0;
	stdev_ = 1.0;
    }

    double evaluate(final double x) {
	return getProbability(x, ProbabilityType.Lower);
    }

    /**
     * Returns the centered (around the mean) confidence interval for the
     * given probability p.
     * @param p  The probability. Must belongs to ]0, 1[.  
     * @return The requested interval (I). P(N in I) = p.
     * @throws DStatException  
     */
    public IInterval getConfidenceInterval(final double p){
	double r1 = getProbabilityInverse((1.0 - p) / 2.0,
		ProbabilityType.Upper);
	double r0 = 2 * mean_ - r1;

	if (r1 < r0) {
	    double tmp = r0;
	    r0 = r1;
	    r1 = tmp;
	}

	Interval ni = new Interval(r0 * stdev_ + mean_, r1 * stdev_
		+ mean_);
	return ni;
    }

    @Override
    public double getDensity(final double x) {
	return SpecialFunctions.normalDensity(x, mean_, stdev_);
    }

    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("Normal with Mean = ");
	sb.append(mean_);
	sb.append(" and Stdev = ");
	sb.append(stdev_);
	return sb.toString();
    }

    @Override
    public double getExpectation() {
	return mean_;
    }

    /**
     * @return Double.NEGATIVE_INFINITY
     */
    @Override
    public double getLeftBound() {
	return Double.NEGATIVE_INFINITY;
    }

    /**
     * Gets the mean of the distribution
     * @return The mean
     */
    public double getMean()
    {
	return mean_;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return 0;
	double zx = (x - mean_) / stdev_;
	return Utility.intProbability(zx, pt);
    }

    @Override
    public double getProbabilityForInterval(final double x, final double y) {
	double py = getProbability(y, ProbabilityType.Lower);
	double px = getProbability(x, ProbabilityType.Lower);

	return y > x ? py - px : px - py;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return Double.NaN;

	if (pt == ProbabilityType.Upper)
	    p = 1 - p;

	if (p < EPS_P || 1 - p < EPS_P)
	    throw new DStatException(DStatException.ERR_INV_SMALL, "Normal");

	calcProbDelegate cb = new calcProbDelegate() {

	    @Override
	    public double calcProb(double x) {
		return evaluate(x);
	    }
	};

	double zp= Utility.intProbabilityInverse(p, cb);
	return zp * stdev_ + mean_;
    }

    @Override
    public double getRightBound() {
	return Double.POSITIVE_INFINITY;
    }

    /**
     * Returns an IInterval reference defining a significance interval for the
     * given probability p
     * 
     * @param x
     * @return
     */
    public IInterval getSignificanceInterval(final double x) {
	double r1 = getProbabilityInverse(x / 2.0, ProbabilityType.Upper);
	double r0 = 2 * mean_ - r1;

	if (r1 < r0) {
	    double tmp = r0;
	    r0 = r1;
	    r1 = tmp;
	}

	Interval ni = new Interval(r0 * stdev_ + mean_, r1 * stdev_
		+ mean_);
	return ni;
    }

    /**
     * 
     * @return
     */
    public double getStdev() {
	return stdev_;
    }

    @Override
    public double getVariance() {
	return stdev_ * stdev_;
    }

    @Override
    public BoundaryType hasLeftBound() {
	return BoundaryType.None;
    }

    @Override
    public BoundaryType hasRightBound() {
	return BoundaryType.None;
    }

    @Override
    public boolean isSymmetrical() {
	return true;
    }

    @Override
    public double random(IRandomNumberGenerator rng) {
	if (rng == null)
	    throw new DStatException("No valid Random Number Generator",
		    "Normal");

	return StochasticRandomizer.normal(rng, mean_, stdev_);
    }

    /**
     * Sets the mean of the distribution
     * @param mean Mean of the distribution (any real number)
     */
    public void setMean(final double mean) {
	mean_ = mean;
    }

    /**
     * Sets the standard deviation of the distribution
     * @param stdev The standard distribution. Should be strictly positive
     */
    public void setStdev(final double stdev)
    {
	if (stdev <=0)
	    throw new DStatException(DStatException.ERR_PARAM, "Normal");
	stdev_ = stdev;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("N(");
	sb.append(new Formatter().format("%g4", mean_));
	sb.append(',');
	sb.append(new Formatter().format("%g4", stdev_));
	sb.append(')');
	return sb.toString();
    }
}

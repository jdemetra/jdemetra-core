/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import nbbrd.design.Development;
import lombok.NonNull;
import jdplus.random.RandomNumberGenerator;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class Uniform implements ContinuousDistribution {

    private final double min, max;
    
    public Uniform(final double min, final double max){
        this.min=min;
        this.max=max;
    }
 
    @Override
    public double getDensity(final double x) throws DStatException {
	if (min >= max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	return 1.0 / (max - min);
    }

    /**
     * 
     * @return
     */
    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("Uniform distribution with Lower bound = ");
	sb.append(min);
	sb.append(" and Upper bound = ");
	sb.append(max);
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
	return (max - min) / 2.0;
    }

    @Override
    public double getLeftBound() {
	return min;
    }

    /**
     * Gets the upper bound of the uniform distribution
     * 
     * @return
     */
    public double getMax() {
	return max;
    }

    /**
     * Gets the lower bound of the uniform distribution
     * 
     * @return
     */
    public double getMin() {
	return min;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt)
	    throws DStatException {
	if (min >= max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	if ((x < min) || (x > max))
	    throw new DStatException(
		    "The argument must lie between Min and Max", "Uniform");

	double res = (x - min) / (max - min);
	if (pt == ProbabilityType.Upper)
	    res = 1.0 - res;
	return res;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt)
	    throws DStatException {
	if (p < 0.0 || p > 1.0)
	    throw new DStatException("x mus respect 0.0 <= x <= 1.0", "Uniform");
	if (min >= max)
	    throw new DStatException("Min must be strictly smaller than Max",
		    "Uniform");
	if (pt == ProbabilityType.Upper)
	    p = 1.0 - p;
	return p * (max - min) + min;
    }

    @Override
    public double getRightBound() {
	return max;
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
	return ((max - min + 1) * (max - min) - 1) / 12.0;
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
    public double random(@NonNull RandomNumberGenerator rng) throws DStatException {
	return min + rng.nextDouble() * (max - min);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("U(");
	sb.append(min);
	sb.append(',');
	sb.append(max);
	sb.append(')');
	return sb.toString();

    }
}

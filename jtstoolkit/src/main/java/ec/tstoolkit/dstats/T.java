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
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class T implements IContinuousDistribution {

    static final double[][] coeff = new double[][] {
	    new double[] { 1.0, 1.0, 0.0, 0.0, 0.0 },
	    new double[] { 3.0, 16.0, 5.0, 0.0, 0.0 },
	    new double[] { -15.0, 17.0, 19.0, 3.0, 0.0 },
	    new double[] { -945.0, -1920.0, 1482.0, 776.0, 79.0 } };
    static final double[] denom = { 4.0, 96.0, 384.0, 92160.0 };
    static final int[] ideg = { 2, 3, 4, 5 };

    static double calcInitT(final double p, final double df) {
	Normal nl = new Normal();

	double x = nl.getProbabilityInverse(p, ProbabilityType.Lower);
	double xx = x * x;
	double sum = x;
	double denpow = 1.0;
	for (int i = 0; i < 4; i++) {
	    double term = Utility.calcPoly(coeff[i], ideg[i], xx) * x;
	    denpow *= df;
	    sum += term / (denpow * denom[i]);
	}

	return sum;
    }

    private int m_df;

    /**
     * Default constructor; sets the degrees of freedom to 2
     */
    public T() {
	m_df = 2;
    }

    /**
     * 
     * @return
     */
    public int getDegreesofFreedom() {
	return m_df;
    }

    @Override
    public double getDensity(final double x) {
	return SpecialFunctions.studentDensity(x, m_df);
    }

    /**
     * 
     * @return
     */
    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("T with ");
	sb.append(m_df);
	sb.append(" degrees of freedom");
	return sb.toString();
    }

    @Override
    public double getExpectation() {
	return 0.0;
    }

    @Override
    public double getLeftBound() {
	return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return 0;
        if (x == 0)
            return .5;

	double res = SpecialFunctions.studentProbability(x, m_df);
	if (pt == ProbabilityType.Upper)
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
	    throw new DStatException(DStatException.ERR_INV_SMALL);
        if (Math.abs(p-.5)<EPS_P)
            return 0;
	double start = calcInitT(p, m_df);
	return ProbInvFinder.find(p, start, EPS_P, EPS_X, this);
    }

    @Override
    public double getRightBound() {
	return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getVariance() {
	if (m_df < 2)
	    throw new DStatException("No valid variance defined for df < 2",
		    "T");
	return (m_df) / (m_df - 2.0);
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
	return StochasticRandomizer.T(rng, m_df);
    }

    /**
     * 
     * @param df
     */
    public void setDegreesofFreedom(final int df)
    {
	if (df <= 0)
	    throw new DStatException(
		    "Degrees of freedom for TStudent must be strictly positive",
		    "T");
	m_df = df;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("T(");
	sb.append(m_df);
	sb.append(')');
	return sb.toString();
    }
}

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
package demetra.dstats;

import demetra.dstats.internal.SpecialFunctions;
import demetra.dstats.internal.ProbInvFinder;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.random.IRandomNumberGenerator;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public class F implements IContinuousDistribution {

    private final double k1;
    private final double k2;
    private final AtomicReference<Chi2> num, denom;
    
    public F(final double ndf, final double ddf){
        k1=ndf;
        k2=ddf;
        num=new AtomicReference<>();
        denom=new AtomicReference<>();
    }

    @Override
    public double getDensity(final double x) {
	return SpecialFunctions.FDensity(x, k1, k2);
    }

    /**
     * 
     * @return
     */
    @Override
    public String getDescription() {
	StringBuilder sb = new StringBuilder();
	sb.append("F with ");
	sb.append(k1);
	sb.append(" degrees of freedom in the nominator and ");
	sb.append(k2);
	sb.append(" degrees of freedom in the denominator");
	return sb.toString();
    }

    /**
     * Gets the value of K2
     * @return
     */
    public double getDFDenom() {
	return k2;
    }

    /**
     * Gets the value of K1
     * @return
     */
    public double getDFNum() {
	return k1;
    }

    /**
     * Gets the expectation of the F-distribution; it is defined as k2/(k2-2)
     * 
     * @return
     * @see be.nbb.dstats.IDistribution#getExpectation()
     */
    @Override
    public double getExpectation() {
	if (k2 <= 2)
	    throw new DStatException(DStatException.ERR_UNDEFINED, "F");
	return (k2 / (k2 - 2.0));
    }

    @Override
    public double getLeftBound() {
	return 0.0;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
	if (pt == ProbabilityType.Point)
	    return 0;
	double res = SpecialFunctions.FProbability(x, k1, k2);
	if (pt == ProbabilityType.Lower)
	    res = 1.0 - res;
	return res;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
	if (pt == ProbabilityType.Upper)
	    p = 1.0 - p;
	if (p < EPS_P || 1 - p < EPS_P)
	    throw new DStatException(DStatException.ERR_INV_SMALL, "F");
	double start = k2 <= 2 ? 1 : k2 / (k2 - 2.0);
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
	if (k2 <= 2)
	    throw new DStatException(DStatException.ERR_UNDEFINED, "F");
        if (k2 <= 4)
            return Double.POSITIVE_INFINITY;
	double top = (2 * k2 * k2 * (k1 + k2 - 2));
	double bot = (k1 * (k2 - 2) * (k2 - 2) * (k2 - 4));
	return top / bot;
    }

    @Override
    public BoundaryType hasLeftBound() {
	return (k1 <= 2) ? BoundaryType.Asymptotical : BoundaryType.Finite;
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
    public double random(@NonNull IRandomNumberGenerator rng) {
        Chi2 cnum=num.get(), cdenom=denom.get();
        if (cnum ==null){
            cnum=new Chi2(k1);
            num.set(cnum);
        }
        if (cdenom == null){
            cdenom=new Chi2(k2);
            denom.set(cdenom);
        }
	return (cnum.random(rng)/k1)/(cdenom.random(rng)/k2);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("F(");
	sb.append(k1);
	sb.append(',');
	sb.append(k2);
	sb.append(')');
	return sb.toString();
    }
}

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

/*
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
final class ProbInvFinder {
    private final static int maxiter = 1000;

    /**
     * Given a continuous distribution cdist and a probability p, the method
     * looks for a value x such that cdist.prob(x, ProbType.Liwer) = p. Lower
     * bound and upper bound for x a given.
     * 
     * @param cdist
     *            Interface of type IContinuousDistribution
     * @param a
     *            The lower bound of the search interval
     * @param b
     *            The upper bound of the search interval The value of pa for the
     *            evaluation function ev
     * @param ptol
     *            A tolerance threshold
     * @return A double x such that cdist.prob(x, ProbType.Lower) = p
     * @throws MaxIterSpecFuncException
     *             No solution can be found within the current number of
     *             iterations
     * @throws SpecFuncException
     *             Thrown when fa and fb do not have opposite signs
     */
    static double find(final double p, final double a, final double ptol,
	    final double xtol, final IContinuousDistribution cdist) {
	double x = a;
	double fx = cdist.getProbability(x, ProbabilityType.Lower) - p;

	// search the root of cprob(x)-p...

	double xprev = x;
	int niter = 0;
	while (++niter < maxiter
		&& (Math.abs(fx) > ptol || Math.abs(xprev - x) > xtol)) {
	    xprev = x;
	    // Newton - raphson
	    // a= aprev - f(aprev)/df(aprev)
	    // a = aprev - (cdist.prob(aprev, lower)-p)/cdist.density(aprev)
	    double dfx = 0;
	    try {
		dfx = cdist.getDensity(xprev);
	    } catch (DStatException e) {
	    }
	    if (dfx == 0)
		dfx = Math.sqrt(ptol);
	    x = xprev - fx / dfx;
	    BoundaryType lbound = cdist.hasLeftBound();
	    BoundaryType rbound = cdist.hasRightBound();
	    switch (lbound) {
	    case Asymptotical: {
		double lb = cdist.getLeftBound();
		if (x <= lb)
		    x = (xprev + lb) / 2;
		break;
	    }
	    case Finite: {
		double lb = cdist.getLeftBound();
		if (x < lb)
		    x = lb;
		break;
	    }
	    }
	    switch (rbound) {
	    case Asymptotical: {
		double rb = cdist.getRightBound();
		if (x >= rb)
		    x = (xprev + rb) / 2;
		break;
	    }
	    case Finite: {
		double rb = cdist.getRightBound();
		if (x > rb)
		    x = rb;
		break;
	    }
	    }
	    fx = cdist.getProbability(x, ProbabilityType.Lower) - p;
	}
	if (niter < maxiter)
	    return x;
	else
	    throw new DStatException(DStatException.ERR_ITER, cdist
		    .getDescription());
    }

    private ProbInvFinder() {
    }

}

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
package jdplus.dstats.internal;

import nbbrd.design.Development;
import nbbrd.design.Internal;
import demetra.stats.ProbabilityType;
import demetra.dstats.BoundaryType;
import demetra.dstats.ContinuousDistribution;
import demetra.dstats.DStatException;

/*
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Internal
@lombok.experimental.UtilityClass
public final class ProbInvFinder {
    private final int MAXITER = 100;

    /**
     * Given a continuous distribution cdist and a probability p, the method
     * looks for a value x such that cdist.prob(x, ProbType.Lower) = p. Lower
     * bound and upper bound for x a given.
     * 
     * @param p
     * @param cdist
     *            Interface of type IContinuousDistribution
     * @param a
     *            The initial value
     * @param xtol
     *            Tolerance threshold on x
     * @param ptol
     *            Tolerance threshold on p
     * @return A double x such that cdist.prob(x, ProbType.Lower) = p
     */
    public double find(final double p, final double a, final double ptol,
	    final double xtol, final ContinuousDistribution cdist) {
	double x = a;
	double fx = cdist.getProbability(x, ProbabilityType.Lower) - p;

	// search the root of cprob(x)-p...

	double xprev = x;
	int niter = 0;
	while (++niter < MAXITER
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
        return finish(p, x, xtol, cdist);
    }

    private double finish(final double p, final double x,
            final double xtol, final ContinuousDistribution cdist) {
        // search limits
        double step = xtol;
        double a = x;
        double K = 4;
        do {
            double pcur = cdist.getProbability(a, ProbabilityType.Lower);
            if (pcur == p) {
                return a;
            }
            if (pcur < p) {
                break;
            }
            a = remove(a, step, xtol, cdist);
            step *= K;

        } while (true);
        step = xtol;
        double b = x;
        do {
            double pcur = cdist.getProbability(b, ProbabilityType.Lower);
            if (pcur == p) {
                return b;
            }
            if (pcur > p) {
                break;
            }
            b = add(b, step, xtol, cdist);
            step *= K;

        } while (true);

        // simple bissection
        double m;
        do {
            m = (b + a) / 2;
            double pcur = cdist.getProbability(m, ProbabilityType.Lower);
            if (pcur == p) {
                return m;
            } else if (pcur < p) {
                a = m;
            } else {
                b = m;
            }
        } while (b - a > xtol);
        return m;
    }

    private double remove(final double x, final double d, final double xtol, final ContinuousDistribution cdist) {
        double nx = x - d;
        BoundaryType lb = cdist.hasLeftBound();
        if (lb == BoundaryType.None) {
            return nx;
        }
        double l = cdist.getLeftBound();
        if (nx > l) {
            return nx;
        } else if (lb == BoundaryType.Asymptotical) {
            return l + xtol;
        } else {
            return l;
        }
    }

    private double add(final double x, final double d, final double xtol, final ContinuousDistribution cdist) {
        double nx = x + d;
        BoundaryType rb = cdist.hasRightBound();
        if (rb == BoundaryType.None) {
            return nx;
        }
        double r = cdist.getRightBound();
        if (nx < r) {
            return nx;
        } else if (rb == BoundaryType.Asymptotical) {
            return r - xtol;
        } else {
            return r;
        }
    }
    
}

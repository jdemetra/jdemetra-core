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

import jdplus.dstats.internal.ProbInvFinder;
import jdplus.dstats.internal.SpecialFunctions;
import demetra.stats.ProbabilityType;
import demetra.design.Development;
import jdplus.random.RandomNumberGenerator;


/**
 * Chi2 Distribution
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class Chi2 implements ContinuousDistribution {

    public static final String NAME = "Chi2";

    private final double df;


    /**
     * @param df Degrees of freedom
     */
    public Chi2(final double df) {
        if (df <= 0)
            throw new IllegalArgumentException("The degrees of freedom should be strictly positive");
        this.df = df;
        fillHelpers();
    }

    /**
     * Gets the degrees of freedom of the distribution
     *
     * @return A strictly positive number
     */
    public double getDegreesofFreedom() {
        return df;
    }

    @Override
    public double getDensity(final double x) {
        if (x < 0.0) {
            return 0;
        }

        return SpecialFunctions.chiSquareDensity(x, df);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chi2 with ");
        sb.append(df);
        sb.append(" degrees of freedom ");
        return sb.toString();
    }

    /**
     * Gets the expectation of the Chi2 distribution.
     *
     * @return
     */
    @Override
    public double getExpectation() {
        return df;
    }

    /**
     * Gets the variance of the Beta distribution.
     *
     * @return
     */
    @Override
    public double getVariance() {
        return df * 2.0;
    }
    
    public double getSkewness() {
        return 2 * Math.sqrt(2 / df);
    }

    public double getKurtosis() {
        return 3 + 12 / df;
    }

    @Override
    public double getLeftBound() {
        return 0.0;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
        if (pt == ProbabilityType.Point) {
            return 0;
        }
        double res;
        res = SpecialFunctions.chiSquare(x, df);
        if (pt == ProbabilityType.Upper) {
            res = 1 - res;
        }
        return res;
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
        if (pt == ProbabilityType.Upper) {
            p = 1.0 - p;
        }
        if (p < EPS_P || 1 - p < EPS_P) {
            throw new DStatException(DStatException.ERR_INV_SMALL, NAME);
        }
        double start = df;
        return ProbInvFinder.find(p, start, EPS_P, EPS_X, this);
    }

    @Override
    public double getRightBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public BoundaryType hasLeftBound() {
        return df <= 2 ? BoundaryType.Asymptotical : BoundaryType.Finite;
    }

    @Override
    public BoundaryType hasRightBound() {
        return BoundaryType.None;
    }

    @Override
    public boolean isSymmetrical() {
        return false;
    }

    private double b, vm, vd, vp;

    @Override
    /**
     * REFERENCE : - J.F. Monahan (1987): An algorithm for * generating chi
     * random variables, ACM Trans. * Math. Software 13, 168-172. 
     * Based on "cern.jet.random.ChiSquare", from the Colt library
     * Copyright (c) 1999 CERN - European Organization for Nuclear Research.
     */
    public double random(RandomNumberGenerator rng) {

        if (df == 1) {
            while (true) {
                double u = rng.nextDouble();
                double v = rng.nextDouble() * 0.857763884960707;
                double z = v / u;
                if (z < 0) {
                    continue;
                }
                double zz = z * z;
                double r = 2.5 - zz;
                if (z < 0.0) {
                    r = r + zz * z / (3.0 * z);
                }
                if (u < r * 0.3894003915) {
                    return (z * z);
                }
                if (zz > (1.036961043 / u + 1.4)) {
                    continue;
                }
                if (2.0 * Math.log(u) < (-zz * 0.5)) {
                    return (z * z);
                }
            }
        } else {
            while (true) {
                double u = rng.nextDouble();
                double v = rng.nextDouble() * vd + vm;
                double z = v / u;
                if (z < -b) {
                    continue;
                }
                double zz = z * z;
                double r = 2.5 - zz;
                if (z < 0.0) {
                    r = r + zz * z / (3.0 * (z + b));
                }
                if (u < r * 0.3894003915) {
                    return ((z + b) * (z + b));
                }
                if (zz > (1.036961043 / u + 1.4)) {
                    continue;
                }
                if (2.0 * Math.log(u) < (Math.log(1.0 + z / b) * b * b - zz * 0.5 - z * b)) {
                    return ((z + b) * (z + b));
                }
            }
        }
    }

     private void fillHelpers() {
        b = Math.sqrt(df - 1.0);
        vm = -0.6065306597 * (1.0 - 0.25 / (b * b + 1.0));
        vm = (-b > vm) ? -b : vm;
        vp = 0.6065306597 * (0.7071067812 + b) / (0.5 + b);
        vd = vp - vm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(NAME).append('(');
        sb.append(df);
        sb.append(')');
        return sb.toString();
    }

}

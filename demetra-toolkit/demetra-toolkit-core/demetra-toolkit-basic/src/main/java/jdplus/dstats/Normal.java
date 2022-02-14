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

import demetra.dstats.BoundaryType;
import demetra.dstats.ContinuousDistribution;
import demetra.dstats.DStatException;
import internal.jdplus.dstats.Utility;
import internal.jdplus.dstats.SpecialFunctions;
import demetra.stats.ProbabilityType;
import demetra.data.Interval;
import nbbrd.design.Development;
import nbbrd.design.Immutable;
import java.util.Formatter;
import lombok.NonNull;
import demetra.dstats.RandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public final class Normal implements ContinuousDistribution {

    private final double mean;
    private final double stdev;

    /**
     * Default constructor; mean = 0.0 and variance = 1.0 i.e. the standard
     * normal distribution
     */
    public Normal() {
        mean = 0.0;
        stdev = 1.0;
    }

    public Normal(final double mean, final double stdev) {
        this.mean = mean;
        this.stdev = stdev;
    }

    double evaluate(final double x) {
        return getProbability(x, ProbabilityType.Lower);
    }

    /**
     * Returns the centered (around the mean) confidence interval for the given
     * probability p.
     *
     * @param p The probability. Must belongs to ]0, 1[.
     * @return The requested interval (I). P(N in I) = p.
     * @throws DStatException
     */
    public Interval getConfidenceInterval(final double p) {
        double r1 = getProbabilityInverse((1.0 - p) / 2.0,
                ProbabilityType.Upper);
        double r0 = 2 * mean - r1;

        if (r1 < r0) {
            double tmp = r0;
            r0 = r1;
            r1 = tmp;
        }

        Interval ni = new Interval(r0 * stdev + mean, r1 * stdev
                + mean);
        return ni;
    }

    @Override
    public double getDensity(final double x) {
        return SpecialFunctions.normalDensity(x, mean, stdev);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Normal with Mean = ");
        sb.append(mean);
        sb.append(" and Stdev = ");
        sb.append(stdev);
        return sb.toString();
    }

    /**
     *
     * @return
     */
    public double getStdev() {
        return stdev;
    }

    // Moments
    @Override
    public double getExpectation() {
        return mean;
    }

    @Override
    public double getVariance() {
        return stdev * stdev;
    }

    public double getSkewness() {
        return 0;
    }

    public double getKurtosis() {
        if (stdev == 1) {
            return 3;
        }
        double var = stdev * stdev;
        return 3 * var * var;
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
     *
     * @return The mean
     */
    public double getMean() {
        return mean;
    }

    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
        if (pt == ProbabilityType.Point) {
            return 0;
        }
        double zx = (x - mean) / stdev;
        return Utility.intProbability(zx, pt);
    }

    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
        if (pt == ProbabilityType.Point) {
            return Double.NaN;
        }

        if (pt == ProbabilityType.Upper) {
            p = 1 - p;
        }

        if (p < EPS_P || 1 - p < EPS_P) {
            throw new DStatException(DStatException.ERR_INV_SMALL, "Normal");
        }

        double zp = Utility.intProbabilityInverse(p, x -> Utility.intProbability(x, ProbabilityType.Lower));
        return zp * stdev + mean;
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
    public Interval getSignificanceInterval(final double x) {
        double r1 = getProbabilityInverse(x / 2.0, ProbabilityType.Upper);
        double r0 = 2 * mean - r1;

        if (r1 < r0) {
            double tmp = r0;
            r0 = r1;
            r1 = tmp;
        }

        Interval ni = new Interval(r0 * stdev + mean, r1 * stdev
                + mean);
        return ni;
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
    public double random(@NonNull RandomNumberGenerator rng) {
        return random(rng, mean,stdev);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("N(");
        sb.append(new Formatter().format("%g4", mean));
        sb.append(',');
        sb.append(new Formatter().format("%g4", stdev));
        sb.append(')');
        return sb.toString();
    }
    
    public static double random(@NonNull RandomNumberGenerator rng, double mean, double stdev) {
        double x1, x2, w;
        do {
            x1 = 2 * rng.nextDouble() - 1;
            x2 = 2 * rng.nextDouble() - 1;
            w = x1 * x1 + x2 * x2;
        } while (w >= 1 || w < 1E-30);
        w = Math.sqrt((-2 * Math.log(w)) / w);
        x1 *= w;
        return x1 * stdev + mean;
    }

}

/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import static jdplus.dstats.Gamma.logGamma;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author PALATEJ
 */
public class InverseGamma implements ContinuousDistribution {

    // shape, scale
    private final double alpha, beta;
    private final Gamma gamma;

    public InverseGamma(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = new Gamma(alpha, 1 / beta);
    }

    public double getShape() {
        return alpha;
    }

    public double getScale() {
        return beta;
    }

    @Override
    public double getDensity(double x) throws DStatException {
        if (x < 0) {
            throw new IllegalArgumentException();
        }
        if (x == 0) {
            return 0.0;
        }
        if (alpha == 1.0) {
            return beta / (x * x) * Math.exp(-beta / x);
        }

        return Math.exp((alpha + 1.0) * Math.log(beta / x) - beta / x - logGamma(alpha)) / beta;
    }

    @Override
    public double getLeftBound() {
        return 0;
    }

    @Override
    public double getRightBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Inverse Gamma with shape = ");
        sb.append(alpha);
        sb.append(" and scale = ");
        sb.append(beta);
        return sb.toString();
    }

    @Override
    public double getExpectation() throws DStatException {
        if (alpha <= 1) {
            return Double.POSITIVE_INFINITY;
        } else {
            return beta / (alpha - 1);
        }
    }

    @Override
    public double getProbability(double x, ProbabilityType pt) throws DStatException {
        switch (pt) {
            case Upper:
                return gamma.getProbability(1 / x, ProbabilityType.Lower);
            case Lower:
                return gamma.getProbability(1 / x, ProbabilityType.Upper);
        }
        return 0;
    }

    @Override
    public double getProbabilityInverse(double p, ProbabilityType pt) throws DStatException {
        switch (pt) {
            case Upper:
                return 1 / gamma.getProbabilityInverse(p, ProbabilityType.Lower);
            case Lower:
                return 1 / gamma.getProbability(p, ProbabilityType.Upper);
        }
        return Double.NaN;
    }

    @Override
    public double getVariance() throws DStatException {
        if (alpha <= 2) {
            return Double.POSITIVE_INFINITY;
        } else {
            return beta * beta / ((alpha - 1) * (alpha - 1) * (alpha - 2));
        }
    }

    @Override
    public BoundaryType hasLeftBound() {
        return BoundaryType.Asymptotical;
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
    public double random(RandomNumberGenerator rng) throws DStatException {
        double z = gamma.random(rng);
        return 1 / z;
    }

    public static double random(RandomNumberGenerator rng, double shape, double scale) {
        return 1/Gamma.random(rng, shape, 1/scale);
    }
}

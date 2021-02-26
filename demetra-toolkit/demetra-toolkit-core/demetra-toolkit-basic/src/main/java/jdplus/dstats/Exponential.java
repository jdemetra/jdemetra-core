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
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author PALATEJ
 */
public class Exponential implements ContinuousDistribution {

    private final double lambda;

    public Exponential(double rate) {
        this.lambda = rate;
    }

    public double getRate() {
        return lambda;
    }

    @Override
    public double getDensity(double x) throws DStatException {
        if (x < 0.0) {
            return 0.0;
        }
        return lambda * Math.exp(-x * lambda);
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
        sb.append("Exponential with rate = ");
        sb.append(lambda);
        return sb.toString();
    }

    @Override
    public double getExpectation() throws DStatException {
        return 1 / lambda;
    }

    @Override
    public double getProbability(double x, ProbabilityType pt) throws DStatException {
        if (x <= 0.0) {
            return 0.0;
        }
        switch (pt) {
            case Lower:
                return 1.0 - Math.exp(-x * lambda);
            case Upper:
                return Math.exp(-x * lambda);
            default:
                return 0;
        }
    }

    @Override
    public double getProbabilityInverse(double p, ProbabilityType pt) throws DStatException {
        if (p <= 0) {
            return 0;
        } else if (p >= 1) {
            return Double.POSITIVE_INFINITY;
        }

        switch (pt) {
            case Lower:
                return -Math.log(1 - p) / lambda;
            case Upper:
                return -Math.log(p) / lambda;
            default:
                return Double.NaN;
        }
    }

    @Override
    public double getVariance() throws DStatException {
        return 1 / (lambda * lambda);
    }

    @Override
    public BoundaryType hasLeftBound() {
        return BoundaryType.Finite;
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
        return random(rng, lambda);
    }

    public static double random(RandomNumberGenerator rng, double lambda) {
        return -Math.log(rng.nextDouble()) / lambda;
    }

}

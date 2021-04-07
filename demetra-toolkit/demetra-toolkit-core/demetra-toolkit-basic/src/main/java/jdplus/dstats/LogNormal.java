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

import demetra.dstats.BoundaryType;
import demetra.dstats.ContinuousDistribution;
import demetra.dstats.DStatException;
import demetra.stats.ProbabilityType;
import java.util.Formatter;
import jdplus.dstats.internal.SpecialFunctions;
import demetra.dstats.RandomNumberGenerator;

/**
 *
 * @author PALATEJ
 */
public class LogNormal implements ContinuousDistribution {

    private final Normal N;

    public LogNormal() {
        N = new Normal();
    }

    /**
     *
     * @param mean Mean of the underlying normal distribution
     * @param stdev Standard deviation of the underlying normal distribution
     */
    public LogNormal(final double mean, final double stdev) {
        N = new Normal(mean, stdev);
    }

    @Override
    public double getDensity(double x) throws DStatException {
        if (x == 0) {
            return 0;
        }
        return SpecialFunctions.logNormalDensity(x, N.getMean(), N.getStdev());
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
        sb.append("Lognormal with normal mean = ");
        sb.append(N.getMean());
        sb.append(" and normal stdev = ");
        sb.append(N.getStdev());
        return sb.toString();
    }

    @Override
    public double getExpectation() throws DStatException {
        return Math.exp(N.getMean() + N.getVariance() / 2);
    }

    @Override
    public double getProbability(double x, ProbabilityType pt) throws DStatException {
        return N.getProbability(Math.log(x), pt);
    }

    @Override
    public double getProbabilityInverse(double p, ProbabilityType pt) throws DStatException {
        return Math.exp(N.getProbabilityInverse(p, pt));
    }

    @Override
    public double getVariance() throws DStatException {
        double v = N.getVariance();
        return (Math.exp(v) - 1) * (Math.exp(2 * N.getMean() + v));
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
        return Math.exp(N.random(rng));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LogN(");
        sb.append(new Formatter().format("%g4", N.getMean()));
        sb.append(',');
        sb.append(new Formatter().format("%g4", N.getStdev()));
        sb.append(')');
        return sb.toString();
    }

    /**
     * mean of a lognormal distribution defined by the parameters of the
     * underlying normal distribution
     *
     * @param mu Mean of the normal distribution
     * @param stdev Stdev of the normaldistribution
     * @return
     */
    public static double mean(double mu, double stdev) {
        return Math.exp(mu + stdev * stdev / 2);
    }

    /**
     * standard deviation of a lognormal distribution defined by the parameters
     * of the underlying normal distribution
     *
     * @param mu Mean of the normal distribution
     * @param stdev Stdev of the normaldistribution
     * @return
     */
    public static double stdev(double mu, double stdev) {
        double v = stdev * stdev;
        return Math.exp(mu + 0.5 * v) * Math.sqrt((Math.exp(v) - 1));
    }

    /**
     * standard deviation of a lognormal distribution defined by the parameters
     * of the underlying normal distribution
     *
     * @param emu Exponential of the mean of the normal distribution
     * @param stdev Stdev of the normaldistribution
     * @return
     */
    public static double stdev2(double emu, double stdev) {
        double v = stdev * stdev;
        return emu * Math.exp(0.5 * v) * Math.sqrt((Math.exp(v) - 1));
    }
}

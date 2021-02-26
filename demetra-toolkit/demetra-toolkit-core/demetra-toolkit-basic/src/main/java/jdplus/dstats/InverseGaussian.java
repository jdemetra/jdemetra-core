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

import static demetra.math.Constants.TWOPI;
import demetra.stats.ProbabilityType;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author PALATEJ
 */
public class InverseGaussian implements ContinuousDistribution {

    private final double mu, lambda;

    public InverseGaussian(double mu, double lambda) {
        this.mu = mu;
        this.lambda = lambda;
    }

    @Override
    public double getDensity(double x) throws DStatException {
        if (x == 0) {
            return 0;
        }
//        double ld=.5*(Math.log(lambda)-Math.log(TWOPI)-3*Math.log(x))-lambda*(x-mu)*(x-mu)/(2*mu*mu*x);
//        return Math.exp(ld);
        return Math.sqrt(lambda / (TWOPI * x * x * x)) * Math.exp(-lambda * (x - mu) * (x - mu) / (2 * mu * mu * x));
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
        sb.append("Inverse Gaussian with mean = ");
        sb.append(mu);
        sb.append(" and shape = ");
        sb.append(lambda);
        return sb.toString();
    }

    @Override
    public double getExpectation() throws DStatException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getProbability(double x, ProbabilityType pt) throws DStatException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getProbabilityInverse(double p, ProbabilityType pt) throws DStatException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getVariance() throws DStatException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return random(rng, mu, lambda);
    }

    public static double random(RandomNumberGenerator rng, double mu, double lambda) {
        double nu = Normal.random(rng, 0, 1);
        double y = nu * nu;
        double x = mu + (mu * mu * y) / (2 * lambda) - (mu / (2 * lambda)) * Math.sqrt(4 * mu * lambda * y + mu * mu * y * y);
        double z = rng.nextDouble();
        if (z > mu / (mu + x)) {
            x = mu * mu / x;
        }
        return x;
    }
}

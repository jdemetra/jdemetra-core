/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.arima;

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Immutable;
import jdplus.dstats.Normal;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.polynomials.Polynomial;
import demetra.random.XorshiftRNG;
import javax.annotation.Nonnull;
import jdplus.dstats.Distribution;
import jdplus.maths.matrices.CanonicalMatrix;
import demetra.random.RandomNumberGenerator;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class ArimaSeriesGenerator {
    
    @BuilderPattern(ArimaSeriesGenerator.class)
    public static class Builder {

    private int ndrop = 0;
    private double startMean = 100;
    private double startStdev = 10;
    private final RandomNumberGenerator rng;
    private Distribution dist=new Normal();
    
    private Builder(){
        rng = XorshiftRNG.fromSystemNanoTime();
    }
    
    private Builder(RandomNumberGenerator rng){
        this.rng = rng;
    }
    /**
     * Number of initial random numbers that are dropped
     * @param n
     * @return 
     */
    public Builder initialWarmUp(int n){
        ndrop=n;
        return this;
    }
    
    /**
     * Distribution used for generating the innovations
     * @param distribution
     * @return 
     */
    public Builder distribution(Distribution distribution){
        dist=distribution;
        return this;
    }
    
    public Builder startMean(double mean){
        this.startMean=mean;
        return this;
    }

    public Builder startStdev(double stdev){
        this.startStdev=stdev;
        return this;
    }

        public ArimaSeriesGenerator build() {
            return new ArimaSeriesGenerator(this);
        }
    }
    
    public static Builder builder(){
        return new Builder();
    }
    
    public static Builder builder(@Nonnull RandomNumberGenerator rng){
        return new Builder(rng);
    }

    private final int initialdrop;
    private final double startMean;
    private final double startStdev;
    private final RandomNumberGenerator rng;
    private final Distribution distribution;
    
    public ArimaSeriesGenerator(){
        this(new Builder());
    }

    private ArimaSeriesGenerator(final Builder builder){
        initialdrop=builder.ndrop;
        startMean=builder.startMean;
        startStdev=builder.startStdev;
        this.rng=builder.rng.synchronize();
        this.distribution=builder.dist;
    }
    
     /**
     * 
     * @param arima
     * @param n
     * @return 
     */
    public double[] generate(final IArimaModel arima, final int n) {
        return generate(arima, 0, n);
    }
    
    /**
     *
     * @param arima
     * @param mean
     * @param n
     * @return
     */
    public double[] generate(final IArimaModel arima, final double mean, final int n) {
         try {
            StationaryTransformation stm = arima.stationaryTransformation();
            double[] tmp = generateStationary((IArimaModel) stm.getStationaryModel(), mean, n + initialdrop);
            double[] w;
            if (initialdrop == 0) {
                w = tmp;
            } else {
                w = new double[n];
                System.arraycopy(tmp, initialdrop, w, 0, n);
            }
            if (stm.getUnitRoots().isIdentity()) {
                return w;
            } else {
                Polynomial P = stm.getUnitRoots().asPolynomial();
                double[] yprev = new double[P.degree()];
                if (startStdev != 0) {
                    Normal normal=new Normal(startMean, startStdev);
                    
                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = normal.random(rng);
                    }
                } else if (startMean != 0) {
                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = startMean;
                    }
                }

                for (int i = 0; i < n; ++i) {
                    double y = w[i];
                    for (int j = 1; j <= P.degree(); ++j) {
                        y -= yprev[j - 1] * P.get(j);
                    }
                    w[i] = y;
                    for (int j = yprev.length - 1; j > 0; --j) {
                        yprev[j] = yprev[j - 1];
                    }
                    if (yprev.length > 0) {
                        yprev[0] = y;
                    }
                }
                return w;
            }
        } catch (ArimaException ex) {
            return null;
        }
    }

    public double[] generateStationary(final IArimaModel starima, final int n) {
        return generateStationary(starima, 0, n);
    }
    
    public double[] generateStationary(final IArimaModel starima, final double mean, final int n) {
    
        BackFilter ar = starima.getAr(), ma = starima.getMa();
        int p = ar.getDegree(), q = ma.getDegree();
        double[] y = new double[p], e = new double[q];
        if (p == 0) {
            for (int i = 0; i < q; ++i) {
                e[i] = distribution.random(rng);
            }
        } else {
            CanonicalMatrix ac = CanonicalMatrix.square(p + q);
            AutoCovarianceFunction acf = starima.getAutoCovarianceFunction();
            acf.prepare(p);
            // fill the p part
            FastMatrix pm = ac.extract(0, p, 0, p);
            pm.diagonal().set(acf.get(0));
            for (int i = 1; i < p; ++i) {
                pm.subDiagonal(-i).set(acf.get(i));
            }
            if (q > 0) {
                FastMatrix qm = ac.extract(p, q, p, q);
                qm.diagonal().set(starima.getInnovationVariance());
                FastMatrix qp = ac.extract(p, q, 0, p);
                RationalBackFilter psi = starima.getPsiWeights();
                int nw = Math.min(q, p);
                psi.prepare(q);
                DataBlock w = DataBlock.of(psi.getWeights(q));
                for (int i = 0; i < nw; ++i) {
                    qp.column(i).drop(i, 0).copy(w.drop(0, i));
                }
                qp.mul(starima.getInnovationVariance());
            }
            SymmetricMatrix.fromLower(ac);
            SymmetricMatrix.lcholesky(ac);
            double[] x = new double[p + q];
            for (int i = 0; i < x.length; ++i) {
                x[i] = distribution.random(rng);
            }
            LowerTriangularMatrix.lmul(ac, DataBlock.of(x));
            System.arraycopy(x, 0, y, 0, p);
            if (q > 0) {
                System.arraycopy(x, p, e, 0, q);
            }
        }

        double[] z = new double[n];
        double std = Math.sqrt(starima.getInnovationVariance());
        Polynomial theta = ma.asPolynomial(), phi = ar.asPolynomial();
        for (int i = 0; i < n; ++i) {
            double u = distribution.random(rng)*std;
            double t = mean + u * theta.get(0);
            for (int j = 1; j <= q; ++j) {
                t += e[j - 1] * theta.get(j);
            }
            for (int j = 1; j <= p; ++j) {
                t -= y[j - 1] * phi.get(j);
            }

            t /= phi.get(0);
            z[i] = t;

            if (e.length > 0) {
                for (int j = e.length - 1; j > 0; --j) {
                    e[j] = e[j - 1];
                }
                e[0] = u;
            }
            for (int j = y.length - 1; j > 0; --j) {
                y[j] = y[j - 1];
            }
            if (y.length > 0) {
                y[0] = t;
            }
        }
        return z;
    }
   
}

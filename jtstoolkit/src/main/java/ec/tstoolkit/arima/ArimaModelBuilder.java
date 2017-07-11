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
package ec.tstoolkit.arima;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.IDistribution;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.StochasticRandomizer;
import ec.tstoolkit.random.XorshiftRNG;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class ArimaModelBuilder {

    private int m_ndrop = 0;
    private double m_startmean = 100;
    private double m_startstdev = 10;
    private static IRandomNumberGenerator RNG = XorshiftRNG.fromSystemNanoTime();
    private IDistribution m_dist=new Normal();

    private IRandomNumberGenerator rng = RNG;

    /**
     *
     */
    public ArimaModelBuilder() {
    }
    
    public void setStartMean(double m){
        m_startmean=m;
    }

    public void setStartStdev(double e){
        m_startstdev=e;
    }
    
    public void setDropCount(int n){
        m_ndrop=n;
    }
    
    public double getStartMean(){
        return m_startmean;
    }

    public double getStartStdev(){
        return m_startstdev;
    }
    
    public int getDropCount(){
        return m_ndrop;
    }

    public void setRandomNumberGenerator(IRandomNumberGenerator rng) {
        this.rng = rng;
    }

    public IRandomNumberGenerator getRandomNumberGenerator() {
        return rng;
    }
    
    public IDistribution getDistribution(){
        return m_dist;
    }
    
    public void setDistribution(IDistribution dist){
        m_dist=dist;
    }

    /**
     *
     * @param AR
     * @param MA
     * @param var
     * @return
     */
    public ArimaModel createModel(final Polynomial AR, final Polynomial MA,
            final double var) {
        try {
            double x = AR.get(0), y = MA.get(0);

            BackFilter ar = new BackFilter(AR);
            if (x != 1) {
                ar = ar.normalize();
            }
            BackFilter ur = BackFilter.ONE;
            BackFilter.StationaryTransformation st = new BackFilter.StationaryTransformation();
            if (st.transform(ar)) {
                ar = st.stationaryFilter;
                ur = st.unitRoots;
            }
            BackFilter ma = new BackFilter(MA);
            if (y != 1) {
                ma = ma.normalize();
            }
            return new ArimaModel(ar, ur, ma, var * y / x * y / x);
        } catch (BaseException ex) {
            return null;
        }
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
            double[] tmp = generateStationary((IArimaModel) stm.stationaryModel, mean, n + m_ndrop);
            double[] w;
            if (m_ndrop == 0) {
                w = tmp;
            } else {
                w = new double[n];
                System.arraycopy(tmp, m_ndrop, w, 0, n);
            }
            if (stm.unitRoots.isIdentity()) {
                return w;
            } else {
                Polynomial P = stm.unitRoots.getPolynomial();
                double[] yprev = new double[P.getDegree()];
                if (m_startstdev != 0) {
                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = StochasticRandomizer.normal(rng, m_startmean, m_startstdev);
                    }
                } else if (m_startmean != 0) {
                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = m_startmean;
                    }
                }

                for (int i = 0; i < n; ++i) {
                    double y = w[i];
                    for (int j = 1; j <= P.getDegree(); ++j) {
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
    
        BackFilter ar = starima.getAR(), ma = starima.getMA();
        int p = ar.getDegree(), q = ma.getDegree();
        double[] y = new double[p], e = new double[q];
        if (p == 0) {
            for (int i = 0; i < q; ++i) {
                e[i] = m_dist.random(rng);
            }
        } else {
            Matrix ac = new Matrix(p + q, p + q);
            AutoCovarianceFunction acf = starima.getAutoCovarianceFunction();
            acf.prepare(p);
            // fill the p part
            SubMatrix pm = ac.subMatrix(0, p, 0, p);
            pm.diagonal().set(acf.get(0));
            for (int i = 1; i < p; ++i) {
                pm.subDiagonal(-i).set(acf.get(i));
            }
            if (q > 0) {
                SubMatrix qm = ac.subMatrix(p, p + q, p, p + q);
                qm.diagonal().set(starima.getInnovationVariance());
                SubMatrix qp = ac.subMatrix(p, p + q, 0, p);
                RationalBackFilter psi = starima.getPsiWeights();
                int nw = Math.min(q, p);
                psi.prepare(q);
                DataBlock w = new DataBlock(psi.getWeights(q));
                for (int i = 0; i < nw; ++i) {
                    qp.column(i).drop(i, 0).copy(w.drop(0, i));
                }
                qp.mul(starima.getInnovationVariance());
            }
            SymmetricMatrix.fromLower(ac);
            SymmetricMatrix.lcholesky(ac);
            double[] x = new double[p + q];
            for (int i = 0; i < x.length; ++i) {
                x[i] = m_dist.random(rng);
            }
            LowerTriangularMatrix.lmul(ac, x);
            System.arraycopy(x, 0, y, 0, p);
            if (q > 0) {
                System.arraycopy(x, p, e, 0, q);
            }
        }

        double[] z = new double[n];
        double std = Math.sqrt(starima.getInnovationVariance());
        Polynomial theta = ma.getPolynomial(), phi = ar.getPolynomial();
        for (int i = 0; i < n; ++i) {
            double u = m_dist.random(rng)*std;
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

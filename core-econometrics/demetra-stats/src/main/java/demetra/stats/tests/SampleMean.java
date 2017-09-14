/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package demetra.stats.tests;

import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.dstats.F;
import demetra.dstats.Normal;
import demetra.dstats.T;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SampleMean implements IBuilder<StatisticalTest> {

    public static final double SMALL = 1e-38;

    private final double smean, pmean, svariance, pvariance;
    private final boolean pnormal;
    private final int n;

    private SampleMean(final double smean, final double svariance, final double pmean, final double pvariance, final int n, final boolean pnormal) {
        this.smean = smean;
        this.pmean = pmean;
        this.svariance = svariance;
        this.pvariance = pvariance;
        this.n = n;
        this.pnormal = pnormal;
    }

    @Override
    public StatisticalTest build() {
        // case I: pmean and pvariance are known
        if (Double.isFinite(pvariance)) {
            return fromKnownPopulation();
        } else {
            return fromKnownMean();
        }
    }

    private StatisticalTest fromKnownPopulation() {
        double val = (smean - pmean) / Math.sqrt(pvariance/n);
        return new StatisticalTest(new Normal(), val, TestType.TwoSided, !pnormal);
    }

    private StatisticalTest fromKnownMean() {
        double val = (smean - pmean) / Math.sqrt(svariance/n);
        return new StatisticalTest(new T(n - 1), val, TestType.TwoSided, !pnormal);
    }


    /**
     *
     * @return
     */
    public double getSampleMean() {
        return smean;
    }

    /**
     *
     * @return
     */
    public double getPopulationMean() {
        return pmean;
    }

    public double getSampleVariance() {
        return svariance;
    }

    public double getPopulationVariance() {
        return pvariance;
    }

    public int getSampleSize() {
        return n;
    }

    public boolean isPopulationNormal() {
        return pnormal;
    }

    /**
     *
     * @param data
     * @param populationMean
     * @param populationVariance
     * @return
     */
    public static SampleMean fromSample(final DoubleSequence data,
            final double populationMean, final double populationVariance, final boolean normalPopulation) {
        int sn = data.length();
        double mean = Doubles.average(data);
        double variance = (Doubles.ssq(data) - sn * mean * mean) / (sn - 1);
        if (variance < SMALL) {
            variance = SMALL;
        }
        return new SampleMean(Doubles.average(data), variance, populationMean, populationVariance, data.length(), normalPopulation);
    }

    // Unknown variance
    /**
     *
     * @param data
     * @param populationMean
     * @param normalPopulation
     * @return
     */
    public static SampleMean fromSample(final DoubleSequence data,
            final double populationMean, final boolean normalPopulation) {
        return fromSample(data, populationMean, Double.NaN, normalPopulation);
    }
    
    public static StatisticalTest compareVariances(SampleMean m0, SampleMean m1) {
        F f = new F(m1.getSampleSize() - 1, m0.getSampleSize() - 1);
        return new StatisticalTest(f, m1.getSampleVariance() / m0.getSampleVariance(), TestType.TwoSided, false);
    }

    public static StatisticalTest compareMeans(SampleMean m0, SampleMean m1, boolean samevar) {
        int n0 = m0.getSampleSize(), n1 = m1.getSampleSize();
        double v0 = m0.getSampleVariance(), v1 = m1.getSampleVariance();
        double t;
        int df;
        if (samevar) {
            double v = (v0 * (n0 - 1) + v1 * (n1 - 1)) / (n0 + n1 - 2);
            t = (m1.getSampleMean() - m0.getSampleMean()) / Math.sqrt(v / n0 + v / n1);
            df = n0 + n1 - 2;
        } else {
            t = (m1.getSampleMean() - m0.getSampleMean()) / Math.sqrt(v0 / n0 + v1 / n1);
            df = Math.min(n0 - 1, n1 - 1);
//            double f=v1/v0;
//            double z=1.0/n0+f/n1;
//            df=(z*z)/(1.0/(n0*n0*(n0-1))+f*f/(n1*n1*(n1-1)));
        }
        T dist = new T(df);
        return new StatisticalTest(dist, t, TestType.TwoSided, false);
    }

}

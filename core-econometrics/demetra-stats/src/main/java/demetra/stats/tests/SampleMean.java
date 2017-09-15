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
import demetra.stats.samples.Population;
import demetra.stats.samples.Sample;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SampleMean implements IBuilder<StatisticalTest> {

    public static final double SMALL = 1e-38;

    private final Sample sample;

    public SampleMean(final Sample sample) {
        this.sample=sample;
    }

    @Override
    public StatisticalTest build() {
        // case I: pmean and pvariance are known
        if (Double.isFinite(sample.population().getVariance())) {
            return fromKnownPopulation();
        } else {
            return fromKnownMean();
        }
    }

    private StatisticalTest fromKnownPopulation() {
        Population population=sample.population();
        double val = (sample.mean() - population.getMean()) / Math.sqrt(population.getVariance()/sample.size());
        return new StatisticalTest(new Normal(), val, TestType.TwoSided, ! population.isNormal());
    }

    private StatisticalTest fromKnownMean() {
        Population population=sample.population();
        double val = (sample.mean() - population.getMean()) / Math.sqrt(sample.variance()/sample.size());
        return new StatisticalTest(new T(sample.size() - 1), val, TestType.TwoSided, ! population.isNormal());
    }


    /**
     *
     * @return
     */
    public Sample getSample() {
        return sample;
    }

   
    public static StatisticalTest compareVariances(Sample s0, Sample s1) {
        F f = new F(s1.size()- 1, s0.size()- 1);
        return new StatisticalTest(f, s1.variance()/ s0.variance(), TestType.TwoSided, false);
    }

    public static StatisticalTest compareMeans(Sample s0, Sample s1, boolean samevar) {
        int n0 = s0.size(), n1 = s1.size();
        double v0 = s0.variance(), v1 = s1.variance();
        double t;
        int df;
        if (samevar) {
            double v = (v0 * (n0 - 1) + v1 * (n1 - 1)) / (n0 + n1 - 2);
            t = (s1.mean() - s0.mean()) / Math.sqrt(v / n0 + v / n1);
            df = n0 + n1 - 2;
        } else {
            t = (s1.mean() - s0.mean()) / Math.sqrt(v0 / n0 + v1 / n1);
            df = Math.min(n0 - 1, n1 - 1);
//            double f=v1/v0;
//            double z=1.0/n0+f/n1;
//            df=(z*z)/(1.0/(n0*n0*(n0-1))+f*f/(n1*n1*(n1-1)));
        }
        T dist = new T(df);
        return new StatisticalTest(dist, t, TestType.TwoSided, false);
    }

}

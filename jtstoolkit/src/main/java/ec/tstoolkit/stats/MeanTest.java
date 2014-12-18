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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MeanTest extends StatisticalTest {

    public static final double SMALL = 1e-38;

    private double mean_, emean_, var_;
    
    private int n_;

    /**
     *
     */
    public MeanTest() {
    }

    /**
     *
     * @return
     */
    public double getExpectedMean() {
        return emean_;
    }

    /**
     *
     * @return
     */
    public double getMean() {
        return mean_;
    }

    public double getVariance() {
        return var_;
    }
    
    public int getSampleSize(){
        return n_;
    }

    /**
     *
     * @param stats
     * @param expectedMean
     * @param stdev
     * @param alternative
     */
    public void sampleMean(final DescriptiveStatistics stats,
            final double expectedMean, final double stdev,
            final TestType alternative) {
        n_=stats.getObservationsCount();
        mean_ = stats.getAverage();
        var_=stdev*stdev;
        m_val = Math.sqrt(n_)
                * (mean_ - expectedMean) / stdev;
        m_dist = new Normal();
        m_type = alternative;
        m_asympt = false;
        emean_ = expectedMean;
    }

    // Unknown variance
    /**
     *
     * @param stats
     * @param expectedMean
     * @param alternative
     */
    public void sampleMean(final DescriptiveStatistics stats,
            final double expectedMean, final TestType alternative) {
        n_=stats.getObservationsCount();
        mean_ = stats.getSum() / (n_-1);
        var_ = stats.getSumSquare() / (n_-1);
        var_ -= mean_ * mean_;
        if (var_ < SMALL) {
            var_ = SMALL;
        }
        m_val = (mean_ - expectedMean) / Math.sqrt(var_ / (n_-1));
        T tstat = new T();
        tstat.setDegreesofFreedom(n_-1);
        m_dist = tstat;
        m_type = alternative;
        m_asympt = false;
        emean_ = expectedMean;
    }

    public void zeroMean(final DescriptiveStatistics stats) {
        n_=stats.getObservationsCount();
        mean_ = stats.getSum() / n_;
        var_ = stats.getSumSquare() / n_;
        m_val = mean_ / Math.sqrt(var_ / n_);
        T tstat = new T();
        tstat.setDegreesofFreedom(n_);
        m_dist = tstat;
        m_type = TestType.TwoSided;
        m_asympt = false;
        emean_ = 0;
    }

    /**
     *
     * @param data
     * @param expectedMean
     * @param stdev
     * @param alternative
     */
    public void sampleMean(final IReadDataBlock data,
            final double expectedMean, final double stdev,
            final TestType alternative) {
        DescriptiveStatistics bs = new DescriptiveStatistics(data);
        sampleMean(bs, expectedMean, stdev,alternative);
    }

    // Unknown variance
    /**
     *
     * @param data
     * @param expectedMean
     * @param alternative
     */
    public void sampleMean(final IReadDataBlock data,
            final double expectedMean, final TestType alternative) {
        DescriptiveStatistics bs = new DescriptiveStatistics(data);
        sampleMean(bs, expectedMean, alternative);
    }

    /**
     *
     * @param data
     * @param expectedMean
     * @param alternative
     */
    public void zeroMean(final IReadDataBlock data) {
        DescriptiveStatistics bs = new DescriptiveStatistics(data);
        zeroMean(bs);
    }

    public void sampleMean(final IReadDataBlock data,
            final double expectedMean, final double stdev, final int nsample, final TestType alternative) {
        DescriptiveStatistics bs = new DescriptiveStatistics(data);
        n_=bs.getObservationsCount();
        mean_ = bs.getAverage();
        var_=stdev*stdev;
        m_val = Math.sqrt(n_) * (mean_ - expectedMean)
                / stdev;
        T tstat = new T();
        tstat.setDegreesofFreedom(nsample - 1);
        m_dist = tstat;
        m_type = alternative;
        m_asympt = false;
        emean_ = expectedMean;
    }
    
    public static StatisticalTest compareVariances(MeanTest m0, MeanTest m1){
        F f = new F();
        f.setDFNum(m1.getSampleSize()-1);
        f.setDFDenom(m0.getSampleSize() - 1);
        return new StatisticalTest(f, m1.getVariance() / m0.getVariance(), TestType.TwoSided, false);
    }
    public static StatisticalTest compareMeans(MeanTest m0, MeanTest m1){
        return compareMeans(m0, m1, !compareVariances(m0, m1).isSignificant());
    }

    public static StatisticalTest compareMeans(MeanTest m0, MeanTest m1, boolean samevar){
        int n0=m0.getSampleSize(), n1=m1.getSampleSize();
        double v0=m0.getVariance(), v1=m1.getVariance();
        double t;
        int df;
        if (samevar){
            double v=(v0*(n0-1)+v1*(n1-1))/(n0+n1-2);
            t=(m1.getMean()-m0.getMean())/Math.sqrt(v/n0+v/n1);
            df=n0+n1-2;
        }else{
            t=(m1.getMean()-m0.getMean())/Math.sqrt(v0/n0+v1/n1);
            df=Math.min(n0-1, n1-1);
//            double f=v1/v0;
//            double z=1.0/n0+f/n1;
//            df=(z*z)/(1.0/(n0*n0*(n0-1))+f*f/(n1*n1*(n1-1)));
        }
        T dist=new T();
        dist.setDegreesofFreedom(df);
        return new StatisticalTest(dist, t, TestType.TwoSided, false);
    }
}

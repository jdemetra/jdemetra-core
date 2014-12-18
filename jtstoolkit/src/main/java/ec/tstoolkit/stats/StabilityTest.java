/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
public class StabilityTest {

    private DescriptiveStatistics stats_, stats0_, stats1_;
    private MeanTest m_, m0_, m1_;
    private StatisticalTest f_, msame_;
    private double prob_ = .01;

    public boolean process(IReadDataBlock data) {
        stats_ = new DescriptiveStatistics(data);
        int n = data.getLength();
        int n2 = (1 + n) / 2;
        stats0_ = new DescriptiveStatistics(data.rextract(0, n2));
        stats1_ = new DescriptiveStatistics(data.rextract(n2, n - n2));
        m_ = new MeanTest();
        m_.sampleMean(stats_, 0, TestType.TwoSided);
        m_.setSignificanceThreshold(prob_);
        m0_ = new MeanTest();
        m0_.sampleMean(stats0_, 0, TestType.TwoSided);
        m0_.setSignificanceThreshold(prob_);
        m1_ = new MeanTest();
        m1_.sampleMean(stats1_, 0, TestType.TwoSided);
        m1_.setSignificanceThreshold(prob_);
        f_ = MeanTest.compareVariances(m0_, m1_);
        f_.setSignificanceThreshold(prob_);

        return true;
    }

    public double getSignificance() {
        return prob_;
    }

    public void setSignificance(double p) {
        prob_ = p;
        if (f_ != null) {
            f_.setSignificanceThreshold(prob_);
        }
        if (m_ != null) {
            m_.setSignificanceThreshold(prob_);
        }
        if (m0_ != null) {
            m0_.setSignificanceThreshold(prob_);
        }
        if (m1_ != null) {
            m1_.setSignificanceThreshold(prob_);
        }
    }
    
    public StatisticalTest getVariancesTest(){
        return f_;
    }

    public StatisticalTest getMeansTest(){
        if (msame_==null){
            msame_=MeanTest.compareMeans(m_, m_, !f_.isSignificant());
        }
        return msame_;
    }

    public boolean isSameVariance(){
        return !f_.isSignificant();
    }
    
    public boolean isSameMean(){
        if (is0StartMean() && is0EndMean())
            return true;
        else if (!is0StartMean() && !is0EndMean()){
            return !getMeansTest().isSignificant();
        }else
            return false;
    }
    
    public MeanTest getFullMeanTest() {
        return m_;
    }

    public MeanTest getStartMeanTest() {
        return m0_;
    }

    public MeanTest getEndMeanTest() {
        return m1_;
    }

    public boolean is0FullMean() {
        return m_ != null ? !m_.isSignificant() : false;
    }

    public boolean is0StartMean() {
        return m0_ != null ? !m0_.isSignificant() : false;
    }

    public boolean is0EndMean() {
        return m1_ != null ? !m1_.isSignificant() : false;
    }
}

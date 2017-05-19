/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.Doubles;
import demetra.design.IBuilder;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface OrderedSample extends Sample {

    public static class Builder implements IBuilder<Sample> {

        private final Doubles data;
        private double mean = Double.NaN;
        private boolean checkMissing=true;

        private Builder(Doubles data) {
            this.data = data;
        }

        public Builder mean(double mean) {
            this.mean = mean;
            return this;
        }

       public Builder checkMissing(boolean check) {
            this.checkMissing=check;
            return this;
        }

       @Override
        public Sample build() {
            if (!Double.isFinite(mean)) {
                return new DefaultOrderedSample(data);
            } else if (mean == 0) {
                return new OrderedSampleWithZeroMean(data, checkMissing);
            } else {
                return new OrderedSampleWithMean(data, mean);
            }
        }
    }

    default double[] autoCovariancces(int maxlag) {
        double[] cov = new double[maxlag];
        for (int i = 0; i < cov.length; ++i) {
            cov[i] = autoCovariance(i + 1);
        }
        return cov;
    }

    default double[] autoCorrelations(int maxlag) {
        double v = variance();
        double[] cov = new double[maxlag];
        if (v != 0) {
            for (int i = 0; i < cov.length; ++i) {
                cov[i] = autoCovariance(i + 1) / v;
            }
        }
        return cov;
    }
    
    default double variance(){
        return autoCovariance(0);
    }

    double autoCovariance(int lag);
}

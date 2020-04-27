/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.revisions.parametric.Bias;
import demetra.stats.ProbabilityType;
import java.util.Random;
import jdplus.dstats.T;
import jdplus.stats.AutoCovariances;
import jdplus.stats.samples.Population;
import jdplus.stats.samples.Sample;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class BiasComputer {

    /**
     * Revisions should not contain missing values
     *
     * @param revisions
     * @return
     */
    public Bias of(DoubleSeq revisions) {

        Sample sample = Sample.build(revisions, true, Population.UNKNOWN);
        int n = sample.observationsCount();
        if (n < 2) {
            return null;
        }

        double mu = sample.mean();
        double v = sample.variance();
        // stdev of the mean
        double sigma = Math.sqrt(v / n);
        double t = mu / sigma;
        T tstat = new T(n - 1);
        // two-sided
        double pval = 2 * tstat.getProbability(Math.abs(t), ProbabilityType.Upper);

        Bias.Builder builder = Bias.builder()
                .n(n)
                .mu(mu)
                .sigma(sigma)
                .t(t)
                .tPvalue(pval);
        if (n > 2) {
            double rho = AutoCovariances.autoCovariance(revisions, mu, 1) / v;
            builder
                    .ar(rho);
            if (Math.abs(rho) < 1) {
//                double neff = n * n*(1 - rho)*(1-rho) / (n-2*rho-n*rho*rho+2*Math.pow(rho, n+1));
                double neff = n * (1 - rho) / (1 + rho); // asymptotic number of obs 
                // adjusted stdev of the mean
                double sigmac = Math.sqrt(v / neff);
                tstat = new T(neff);
                t = mu / sigmac;
                pval = 2 * tstat.getProbability(Math.abs(t), ProbabilityType.Upper);
                builder.adjustedSigma(sigmac)
                        .adjustedT(t)
                        .adjustedTPvalue(pval);
            } else {
                builder.adjustedSigma(Double.NaN)
                        .adjustedT(Double.NaN)
                        .adjustedTPvalue(Double.NaN);

            }
        } else {
            builder.ar(Double.NaN)
                    .adjustedSigma(Double.NaN)
                    .adjustedT(Double.NaN)
                    .adjustedTPvalue(Double.NaN);
        }
        return builder.build();
    }

}

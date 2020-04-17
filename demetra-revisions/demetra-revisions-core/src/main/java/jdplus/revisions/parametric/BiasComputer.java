/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.revisions.parametric.Bias;
import demetra.stats.ProbabilityType;
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

        int n = revisions.length();
        if (n < 2) {
            return null;
        }

        double mu = revisions.average();
        double sigma = Math.sqrt(revisions.ssqc(mu) / (n - 1));
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
            double gamma = AutoCovariances.autoCovariance(revisions, mu, 1) / sigma;
            if (Math.abs(gamma) < 1) {
                // TODO Check the correction factor. Differences between Eurostat's document in literature
                double corr = Math.sqrt((1 - gamma) / (1 + gamma));
                double nc = n * corr;
                tstat = new T(nc);
                double sigmac = Math.sqrt(revisions.ssqc(mu) / nc);
                t=mu/sigmac;
                pval = 2 * tstat.getProbability(Math.abs(t), ProbabilityType.Upper);
                builder
                        .ar(gamma)
                        .adjustedSigma(sigmac)
                        .adjustedT(t)
                        .adjustedTPvalue(pval);
            }
        }else
            builder.ar(Double.NaN);
        return builder.build();
    }

}

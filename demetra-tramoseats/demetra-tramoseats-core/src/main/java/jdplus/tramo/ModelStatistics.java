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
package jdplus.tramo;

import nbbrd.design.Development;
import jdplus.stats.AutoCovariances;
import jdplus.stats.samples.Sample;
import jdplus.stats.tests.LjungBox;
import jdplus.stats.tests.Skewness;
import jdplus.tramo.internal.TramoUtility;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import jdplus.likelihood.ConcentratedLikelihood;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.regsarima.regular.ModelDescription;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder
public class ModelStatistics {

    private int outliersCount;
    private int observationsCount;
    private int effectiveObservationsCount;
    private double bic;
    private double se;
    private double ljungBox;
    private double ljungBoxPvalue;
    private double seasonalLjungBox;
    private double seasonalLjungBoxPvalue;
    private double skewnessAbsvalue;
    private double skewnessPvalue;
    private double stableMean;
    private double stableMeanPvalue;
    private double stableVariance;
    private double stableVariancePvalue;

    private static Builder builder() {
        return new Builder();
    }

    public static ModelStatistics of(ModelDescription m, ConcentratedLikelihood ll) {
        DoubleSeq e = ll.e();
        int p = m.getAnnualFrequency();
        int n = TramoUtility.calcLBLength(p);
        int nres = e.length();
        int nhp = m.getArimaSpec().freeParametersCount();
        IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(e, 0);
        StatisticalTest lb = new LjungBox(acf, nres)
                .autoCorrelationsCount(n)
                .hyperParametersCount(nhp)
                .build();
        StatisticalTest sk = new Skewness(e).build();
        int nobs = ll.dim();
        double bic = Math.log(ll.sigma2()) + nhp * Math.log(nobs) / nobs; // TRAMO-like
        Builder builder = builder()
                .outliersCount((int) m.variables().filter(var -> ModellingUtility.isOutlier(var, true)).count())
                .observationsCount(m.getEstimationDomain().getLength())
                .effectiveObservationsCount(ll.dim())
                .bic(bic)
                .se(Math.sqrt(ll.ssq() / (ll.degreesOfFreedom() - nhp)))
                .ljungBox(lb.getValue())
                .ljungBoxPvalue(lb.getPvalue())
                .skewnessAbsvalue(Math.abs(sk.getValue()))
                .skewnessPvalue(sk.getPvalue());
        if (p > 1) {
            StatisticalTest lbs = p == 1 ? null : new LjungBox(acf, nres)
                    .autoCorrelationsCount(2)
                    .lag(p)
                    .build();
            builder.seasonalLjungBox(lbs.getValue())
                    .seasonalLjungBoxPvalue(lbs.getPvalue());
        }
        int nres2 = (1 + nres) / 2;
        int nlast = Math.min(nres2, 10 * p);
        DoubleSeq data0 = e.range(0, nres - nlast);
        DoubleSeq data1 = e.range(nlast, nres);

        Sample s0 = Sample.ofResiduals(data0);
        Sample s1 = Sample.ofResiduals(data1);
        StatisticalTest means = Sample.compareMeans(s0, s1, true);
        StatisticalTest vars = Sample.compareVariances(s0, s1);

        return builder
                .stableMean(means.getValue())
                .stableMeanPvalue(means.getPvalue())
                .stableVariance(vars.getValue())
                .stableVariancePvalue(vars.getPvalue())
                .build();

    }

}

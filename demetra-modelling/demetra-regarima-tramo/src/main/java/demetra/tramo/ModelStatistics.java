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
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.likelihood.LikelihoodStatistics;
import demetra.regarima.regular.PreprocessingModel;
import demetra.stats.AutoCovariances;
import demetra.stats.samples.Sample;
import demetra.stats.tests.LjungBox;
import demetra.stats.tests.SampleMean;
import demetra.stats.tests.Skewness;
import demetra.stats.tests.StatisticalTest;
import demetra.tramo.internal.TramoUtility;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

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

    private static ModelStatisticsBuilder builder() {
        return new ModelStatisticsBuilder();
    }

    public static ModelStatistics of(PreprocessingModel m) {
        LikelihoodStatistics stats = m.getEstimation().getStatistics();
        DoubleSequence e = m.getEstimation().getConcentratedLikelihood().e();
        int p = m.getDescription().getAnnualFrequency();
        int n = TramoUtility.calcLBLength(p);
        int nres = e.length();
        IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(e, 0);
        StatisticalTest lb = new LjungBox(acf, nres)
                .autoCorrelationsCount(n)
                .build();
        StatisticalTest sk = new Skewness(e).build();
        ModelStatisticsBuilder builder = builder()
                .outliersCount((int) m.getDescription().variables().filter(var -> var.isOutlier(false)).count())
                .observationsCount(stats.getObservationsCount())
                .effectiveObservationsCount(stats.getEffectiveObservationsCount())
                .bic(stats.getBICC())
                .se(Math.sqrt(stats.getSsqErr() / (stats.getEffectiveObservationsCount() - stats.getEstimatedParametersCount() + 1)))
                .ljungBox(lb.getValue())
                .ljungBoxPvalue(lb.getPValue())
                .skewnessAbsvalue(Math.abs(sk.getValue()))
                .skewnessPvalue(sk.getPValue());
        if (p > 1) {
            StatisticalTest lbs = p == 1 ? null : new LjungBox(acf, nres)
                    .autoCorrelationsCount(2)
                    .lag(p)
                    .build();
            builder.seasonalLjungBox(lbs.getValue())
                    .seasonalLjungBoxPvalue(lbs.getPValue());
        }
        int nres2 = (1 + nres) / 2;
        int nlast = Math.min(nres2, 10 * p);
        DoubleSequence data0 = e.range(0, nres - nlast);
        DoubleSequence data1 = e.range(nlast, nres);
        Sample s0 = Sample.ofResiduals(data0);
        Sample s1 = Sample.ofResiduals(data1);
        StatisticalTest means = SampleMean.compareMeans(s0, s1, true);
        StatisticalTest vars = SampleMean.compareVariances(s0, s1);

        return builder
                .stableMean(means.getValue())
                .stableMeanPvalue(means.getPValue())
                .stableVariance(vars.getValue())
                .stableVariancePvalue(vars.getPValue())
                .build();

    }

}

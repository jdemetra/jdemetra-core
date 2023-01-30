/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tempdisagg.univariate;

import static demetra.data.AggregationType.Average;
import demetra.data.DoubleSeq;
import demetra.tempdisagg.univariate.ModelBasedDentonSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.util.Map.Entry;
import jdplus.ssf.benchmarking.SsfCumulator;
import jdplus.benchmarking.univariate.BenchmarkingUtility;
import jdplus.data.DataBlock;
import jdplus.data.normalizer.AbsMeanNormalizer;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.basic.Coefficients;
import jdplus.ssf.basic.Loading;
import jdplus.ssf.basic.Measurements;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.multivariate.MultivariateSsf;
import jdplus.ssf.multivariate.SsfMatrix;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModelBasedDentonProcessor {

    public ModelBasedDentonResults process(TsData aggregatedSeries, TsData indicator, ModelBasedDentonSpec spec) {
        if (spec.getFixedBiRatios().isEmpty()) {
            return estimate(aggregatedSeries, indicator, spec);
        } else {

        }
        return estimateWithConstraints(aggregatedSeries, indicator, spec);
    }

    private ModelBasedDentonResults estimate(TsData aggregatedSeries, TsData indicator, ModelBasedDentonSpec spec) {
        int ratio = indicator.getTsUnit().ratioOf(aggregatedSeries.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData naggregatedSeries;
        switch (spec.getAggregationType()) {
            case Sum ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries);
            case Average ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries).multiply(ratio);
            default ->
                throw new TsException(TsException.INVALID_OPERATION);
        }

        DataBlock x = DataBlock.of(indicator.getValues());
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        double fx = normalizer.normalize(x);
        ISsf ssf;
        ISsfLoading loading = Loading.regression(x);
        if (spec.getShockVariances().isEmpty()) {
            StateComponent c = Coefficients.timeVaryingCoefficients(DoubleSeq.of(1));
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        } else {
            double[] stde = new double[indicator.length()];
            for (int i = 0; i < stde.length; ++i) {
                stde[i] = 1;
            }
            TsDomain domain = indicator.getDomain();
            for (Entry<LocalDate, Double> entry : spec.getShockVariances().entrySet()) {
                int idx = domain.indexOf(entry.getKey().atStartOfDay()) - 1;
                if (idx >= 0 && idx < stde.length) {
                    stde[idx] = Math.sqrt(entry.getValue());
                }
            }
            StateComponent c = Coefficients.timeVaryingCoefficient(DoubleSeq.of(stde), 1);
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        }

        DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, new SsfData(naggregatedSeries.getValues()), true, true);
        TsData biratios = TsData.of(indicator.getStart(), srslts.getComponent(1).times(fx));
        TsData ebiratios = TsData.of(indicator.getStart(), srslts.getComponentVariance(1).fn(q -> Math.sqrt(q) * fx));

        // Not optimal
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, new SsfData(naggregatedSeries.getValues()), true, true);
        DoubleSeq e = ll.e();
        int del=x.length()-e.length();
        TsData res=TsData.of(indicator.getStart().plus(del), e);
        TsData lbi = TsData.divide(aggregatedSeries, indicator.aggregate(aggregatedSeries.getTsUnit(), spec.getAggregationType(), true));

        return ModelBasedDentonResults.builder()
                .target(aggregatedSeries)
                .indicator(indicator)
                .aggregatedBiRatios(lbi)
                .disaggregatedSeries(TsData.multiply(biratios, indicator))
                .stdevDisaggregatedSeries(TsData.multiply(ebiratios, indicator.abs()))
                .biRatios(biratios)
                .stdevBiRatios(ebiratios)
                .likelihood(ll.stats(0, 0))
                .residuals(res)
                .build();
    }

    private ModelBasedDentonResults estimateWithConstraints(TsData aggregatedSeries, TsData indicator, ModelBasedDentonSpec spec) {

        int ratio = indicator.getTsUnit().ratioOf(aggregatedSeries.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData naggregatedSeries;
        switch (spec.getAggregationType()) {
            case Sum ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries);
            case Average ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries).multiply(ratio);
            default ->
                throw new TsException(TsException.INVALID_OPERATION);
        }

        DataBlock x = DataBlock.of(indicator.getValues());
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        double fx = normalizer.normalize(x);
        ISsfLoading loading = Loading.regression(x);
        ISsfLoading cloading = SsfCumulator.defaultLoading(loading, ratio, 0);
        ISsfLoading floading = Loading.fromPosition(1);
        DoubleSeq values = naggregatedSeries.getValues();
        int n=values.length();
        FastMatrix M=FastMatrix.make(n, 2);
        M.column(1).copy(values);
        M.column(0).set(Double.NaN);
        TsPeriod start = naggregatedSeries.getStart();
        spec.getFixedBiRatios().forEach((LocalDate d,Double v)->{
            TsPeriod p=start.withDate(d.atStartOfDay());
            int del=start.until(p);
            if (del>=0 && del<M.getRowsCount())
                M.set(del, 0, v/fx);
        });
        SsfMatrix Q=new SsfMatrix(M, 1);
        StateComponent cc;
        if (spec.getShockVariances().isEmpty()) {
            StateComponent c = Coefficients.timeVaryingCoefficients(DoubleSeq.of(1));
            cc = SsfCumulator.of(c, loading, ratio, 0);
       } else {
            double[] stde = new double[indicator.length()];
            for (int i = 0; i < stde.length; ++i) {
                stde[i] = 1;
            }
            TsDomain domain = indicator.getDomain();
            for (Entry<LocalDate, Double> entry : spec.getShockVariances().entrySet()) {
                int idx = domain.indexOf(entry.getKey().atStartOfDay()) - 1;
                if (idx >= 0 && idx < stde.length) {
                    stde[idx] = Math.sqrt(entry.getValue());
                }
            }
            StateComponent c = Coefficients.timeVaryingCoefficient(DoubleSeq.of(stde), 1);
            cc = SsfCumulator.of(c, loading, ratio, 0);
         }
        IMultivariateSsf ssf=new MultivariateSsf(cc, Measurements.of(new ISsfLoading[]{floading, cloading}, null));
                
        DefaultSmoothingResults srslts = DkToolkit.smooth(M2uAdapter.of(ssf), M2uAdapter.of(Q), true, true);
        TsData biratios = TsData.of(indicator.getStart(), srslts.getComponent(1).extract(0, n, 2).times(fx));
        TsData ebiratios = TsData.of(indicator.getStart(), srslts.getComponentVariance(1).extract(0, n, 2).fn(q -> Math.sqrt(q) * fx));

        // Not optimal
        DiffuseLikelihood ll = DkToolkit.likelihood(M2uAdapter.of(ssf), M2uAdapter.of(Q), true, true);
        DoubleSeq e = ll.e();
        int del=x.length()-e.length();
        TsData res=TsData.of(indicator.getStart().plus(del), e);
        TsData lbi = TsData.divide(aggregatedSeries, indicator.aggregate(aggregatedSeries.getTsUnit(), spec.getAggregationType(), true));

        return ModelBasedDentonResults.builder()
                .target(aggregatedSeries)
                .indicator(indicator)
                .aggregatedBiRatios(lbi)
                .disaggregatedSeries(TsData.multiply(biratios, indicator))
                .stdevDisaggregatedSeries(TsData.multiply(ebiratios, indicator.abs()))
                .biRatios(biratios)
                .stdevBiRatios(ebiratios)
                .likelihood(ll.stats(0, 0))
                .residuals(res)
                .build();
    }

}

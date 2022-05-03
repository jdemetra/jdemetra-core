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

import demetra.data.AggregationType;
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
import jdplus.arima.ssf.Rw;
import jdplus.benchmarking.ssf.SsfCumulator;
import jdplus.benchmarking.univariate.BenchmarkingUtility;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.Coefficients;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.likelihood.DiffuseLikelihood;
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

        int ratio = indicator.getTsUnit().ratioOf(aggregatedSeries.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData naggregatedSeries;
        switch (spec.getAggregationType()) {
            case Sum ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries);
            case  Average ->
                naggregatedSeries = BenchmarkingUtility.highFreqConstraints(indicator, aggregatedSeries).multiply(ratio);
//            case Last ->
//                naggregatedSeries = BenchmarkingUtility.highFreqConstraintsByPosition(indicator, aggregatedSeries, ratio - 1);
//            case First ->
//                naggregatedSeries = BenchmarkingUtility.highFreqConstraintsByPosition(indicator, aggregatedSeries, 0);
//            case UserDefined ->
//                naggregatedSeries = BenchmarkingUtility.highFreqConstraintsByPosition(indicator, aggregatedSeries, spec.getObservationPosition());
            default ->
                throw new TsException(TsException.INVALID_OPERATION);
        }

//        TsPeriod sh = indicator.getStart();
//        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregatedSeries.getStart().start());
//        int offset = sh.until(sl);
//        switch (spec.getAggregationType()) {
//            case Last ->
//                offset += ratio - 1;
//            case UserDefined ->
//                offset += spec.getObservationPosition();
//        }
//
        ISsf ssf;
        ISsfLoading loading = Loading.regression(indicator.getValues());
        if (spec.getOutlierVariances().isEmpty()) {
            StateComponent c = Coefficients.timeVaryingCoefficients(DoubleSeq.of(1));
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        } else {
            double[] stde = new double[indicator.length()];
            for (int i = 0; i < stde.length; ++i) {
                stde[i] = 1;
            }
            TsDomain domain = indicator.getDomain();
            for (Entry<LocalDate, Double> entry : spec.getOutlierVariances().entrySet()) {
                int idx = domain.indexOf(entry.getKey().atStartOfDay());
                if (idx >= 0 && idx < stde.length) {
                    stde[idx] = Math.sqrt(entry.getValue());
                }
            }
            StateComponent c = Coefficients.timeVaryingCoefficient(DoubleSeq.of(stde), 1);
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        }

        DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(ssf, new SsfData(naggregatedSeries.getValues()), true, true);
        TsData biratios = TsData.of(indicator.getStart(), srslts.getComponent(1));
        TsData ebiratios = TsData.of(indicator.getStart(), srslts.getComponentVariance(1).sqrt());
        
        // Not optimal
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, new SsfData(naggregatedSeries.getValues()), true, true);
        DoubleSeq e = ll.e();
        //TsData res=TsData.of(start, DoubleSeq.ONE)

        return ModelBasedDentonResults.builder()
                .disaggregatedSeries(TsData.multiply(biratios, indicator))
                .stdevDisaggregatedSeries(TsData.multiply(biratios, indicator.abs()))
                .biRatios(biratios)
                .stdevBiRatios(ebiratios)
                .likelihood(ll.stats(0, 0))
 //               .residuals()
                .build();
    }

 
}

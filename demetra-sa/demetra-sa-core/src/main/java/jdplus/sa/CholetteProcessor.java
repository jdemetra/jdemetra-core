/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa;

import demetra.data.AggregationType;
import demetra.timeseries.TsException;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.TsUnit;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.sa.benchmarking.SaBenchmarkingSpec.BiasCorrection;
import static jdplus.timeseries.simplets.TsDataToolkit.multiply;

/**
 *
 * @author Jean Palate
 */
public class CholetteProcessor  {

    public static final CholetteProcessor PROCESSOR = new CholetteProcessor();

    public TsData benchmark(TsData source, TsData target, SaBenchmarkingSpec spec) {
        TsData ytarget=target.aggregate(TsUnit.YEAR, AggregationType.Average, true);
        TsData s = correctBias(source, ytarget, spec);
        return cholette(s, ytarget, spec);
    }

    private TsData correctBias(TsData s, TsData ytarget, SaBenchmarkingSpec spec) {
        // No bias correction when we use pure interpolation
        BiasCorrection bias = spec.getBiasCorrection();
        if (bias == BiasCorrection.None) {
            return s;
        }
        TsData sy=s.aggregate(TsUnit.YEAR, AggregationType.Average, true);
        // TsDataBlock.all(target).data.sum() is the sum of the aggregation constraints
        //  TsDataBlock.all(sy).data.sum() is the sum of the averages or sums of the original series
        if (bias == BiasCorrection.Multiplicative) {
            return multiply(s, ytarget.getValues().sum() / sy.getValues().sum());
        } else {
            double b = ytarget.getValues().average() - sy.getValues().average();
            return TsDataToolkit.add(s, b);
        }
    }

    /**
     *
     * @param length
     * @param ratio
     * @param agg
     * @param offset
     * @return
     */
    public static double[] expand(int length, int ratio, DoubleSeq agg, int offset) {
        // expand the data;
        double[] y = new double[length];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }
        // search the first non missing value
        int pos = offset, j = 0, m = agg.length();
        DoubleSeqCursor cursor = agg.cursor();

        while (j++ < m) {
            y[pos] = cursor.getAndNext();
            pos += ratio;
        }
        return y;
    }

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    private TsData cholette(TsData highFreqSeries, TsData aggregationConstraint, SaBenchmarkingSpec spec) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

//        TsData naggregationConstraint, agg;
//                naggregationConstraint = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint);
//                agg = highFreqSeries.aggregate(aggregationConstraint.getTsUnit(), AggregationType(), true);
//
//        TsPeriod sh = highFreqSeries.getStart();
//        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
//        int offset = sh.until(sl);
//        if (spec.getAggregationType() == AggregationType.Average) {
//            naggregationConstraint = multiply(naggregationConstraint, ratio);
//            agg = multiply(agg, ratio);
//        }
//        switch (spec.getAggregationType()) {
//            case First:
//                break;
//            case UserDefined:
//                offset += spec.getObservationPosition();
//                break;
//            default:
//                offset += ratio - 1;
//
//        }
//
//        naggregationConstraint = TsData.subtract(naggregationConstraint, agg);
//        double[] y = expand(highFreqSeries.length(), ratio, naggregationConstraint.getValues(), offset);
//
//        double[] w = null;
//        if (spec.getLambda() != 0) {
//            w = highFreqSeries.getValues().toArray();
//            if (spec.getLambda() != 1) {
//                for (int i = 0; i < w.length; ++i) {
//                    w[i] = Math.pow(Math.abs(w[i]), spec.getLambda());
//                }
//            }
//        }
//        TsPeriod start = highFreqSeries.getStart();
//        int head = (int) (start.getId() % ratio);
//        if (spec.getAggregationType() == AggregationType.Average
//                || spec.getAggregationType() == AggregationType.Sum) {
//            ISsf ssf = SsfCholette.builder(ratio)
//                    .start(head)
//                    .rho(spec.getRho())
//                    .weights(w == null ? null : DoubleSeq.of(w))
//                    .build();
//            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);
//
//            double[] b = new double[highFreqSeries.length()];
//            if (w != null) {
//                for (int i = 0; i < b.length; ++i) {
//                    b[i] = w[i] * (rslts.a(i).get(1));
//                }
//            } else {
//                rslts.getComponent(1).copyTo(b, 0);
//            }
//            return TsData.add(highFreqSeries, TsData.ofInternal(start, b));
//        } else {
//            ISsfLoading loading;
//            StateComponent cmp;
//            if (spec.getRho() == 1) {
//                loading = Rw.defaultLoading();
//                cmp = Rw.DEFAULT;
//            } else {
//                loading = AR1.defaultLoading();
//                cmp = AR1.of(spec.getRho());
//            }
//            if (w != null) {
//                double[] weights = w;
//                loading = WeightedLoading.of(loading, i -> weights[i]);
//            }
//            ISsf ssf = Ssf.of(cmp, loading);
//            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);
//            double[] b = new double[highFreqSeries.length()];
//            for (int i = 0; i < b.length; ++i) {
//                b[i] = loading.ZX(i, rslts.a(i));
//            }
//            return TsData.add(highFreqSeries, TsData.ofInternal(start, b));
return null; 
    }

}

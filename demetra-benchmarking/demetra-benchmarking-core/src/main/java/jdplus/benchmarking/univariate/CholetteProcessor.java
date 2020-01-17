/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.benchmarking.univariate;

import jdplus.benchmarking.ssf.SsfCholette;
import demetra.benchmarking.univariate.Cholette;
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.CholetteSpec.BiasCorrection;
import demetra.data.AggregationType;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.SsfData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import nbbrd.service.ServiceProvider;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.SeqCursor;
import demetra.timeseries.TsObs;
import jdplus.arima.ssf.AR1;
import jdplus.arima.ssf.Rw;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.implementations.WeightedLoading;
import jdplus.ssf.univariate.Ssf;
import static jdplus.timeseries.simplets.TsDataToolkit.add;
import static jdplus.timeseries.simplets.TsDataToolkit.multiply;
import static jdplus.timeseries.simplets.TsDataToolkit.subtract;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(Cholette.Processor.class)
public class CholetteProcessor implements Cholette.Processor {

    public static final CholetteProcessor PROCESSOR = new CholetteProcessor();

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, CholetteSpec spec) {
        TsData s = correctBias(highFreqSeries, aggregationConstraint, spec);
        return cholette(s, aggregationConstraint, spec);
    }

    private TsData correctBias(TsData s, TsData target, CholetteSpec spec) {
        // No bias correction when we use pure interpolation
        AggregationType agg = spec.getAggregationType();
        if (spec.getBias() == BiasCorrection.None
                || (agg != AggregationType.Average && agg != AggregationType.Sum)) {
            return s;
        }
        TsData sy = s.aggregate(target.getTsUnit(), agg, true);
        sy = TsDataToolkit.fitToDomain(sy, target.getDomain());
        // TsDataBlock.all(target).data.sum() is the sum of the aggregation constraints
        //  TsDataBlock.all(sy).data.sum() is the sum of the averages or sums of the original series
        BiasCorrection bias = spec.getBias();
        if (bias == BiasCorrection.Multiplicative) {
            return multiply(s, target.getValues().sum() / sy.getValues().sum());
        } else {
            double b = (target.getValues().sum() - sy.getValues().sum()) / target.length();
            if (agg == AggregationType.Average) {
                b *= s.getTsUnit().ratioOf(target.getTsUnit());
            }
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
        int pos = offset, j=0, m=agg.length();
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
    private TsData cholette(TsData highFreqSeries, TsData aggregationConstraint, CholetteSpec spec) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData naggregationConstraint;
        switch (spec.getAggregationType()) {
            case Sum:
            case Average:
                naggregationConstraint = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint);
                break;
            case Last:
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio - 1);
                break;
            case First:
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, 0);
                break;
            case UserDefined:
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, spec.getObservationPosition());
                break;
            default:
                throw new TsException(TsException.INVALID_OPERATION);
        }

        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
        int offset = sh.until(sl);
        if (spec.getAggregationType() == AggregationType.Average) {
            naggregationConstraint = multiply(naggregationConstraint, ratio);
        }
        switch (spec.getAggregationType()) {
            case First:
                break;
            case UserDefined:
                offset += spec.getObservationPosition();
                break;
            default:
                offset += ratio - 1;

        }

        double[] y = expand(highFreqSeries.length(), ratio, naggregationConstraint.getValues(), offset);

        double[] w = null;
        if (spec.getLambda() != 0) {
            w = highFreqSeries.getValues().toArray();
            if (spec.getLambda() != 1) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.pow(Math.abs(w[i]), spec.getLambda());
                }
            }
        }
        TsPeriod start = highFreqSeries.getStart();
        int head = (int) (start.getId() % ratio);
        if (spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum) {
            ISsf ssf = SsfCholette.builder(ratio)
                    .start(head)
                    .rho(spec.getRho())
                    .weights(w == null ? null : DoubleSeq.of(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);

            double[] b = new double[highFreqSeries.length()];
            if (w != null) {
                for (int i = 0; i < b.length; ++i) {
                    b[i] = w[i] * (rslts.a(i).get(1));
                }
            } else {
                rslts.getComponent(1).copyTo(b, 0);
            }
            return TsData.ofInternal(start, b);
        } else {
            ISsfLoading loading;
            StateComponent cmp;
            if (spec.getRho() == 1) {
                loading = Rw.defaultLoading();
                cmp = Rw.DEFAULT;
            } else {
                loading = AR1.defaultLoading();
                cmp = AR1.of(spec.getRho());
            }
            if (w != null) {
                double[] weights = w;
                loading = WeightedLoading.of(loading, i -> weights[i]);
            }
            ISsf ssf = Ssf.of(cmp, loading);
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);
            double[] b = new double[highFreqSeries.length()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = loading.ZX(i, rslts.a(i));
            }
            return TsData.ofInternal(start, b);
        }
    }

}

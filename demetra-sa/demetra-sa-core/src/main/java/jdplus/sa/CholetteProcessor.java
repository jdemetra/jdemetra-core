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
package jdplus.sa;

import demetra.data.AggregationType;
import demetra.timeseries.TsException;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.TsUnit;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.information.Explorable;
import demetra.modelling.SeriesInfo;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.sa.benchmarking.SaBenchmarkingSpec.BiasCorrection;
import demetra.timeseries.TsPeriod;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import jdplus.data.DataBlock;
import jdplus.data.normalizer.AbsMeanNormalizer;
import jdplus.ssf.benchmarking.SsfCholette;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.SsfData;
import static jdplus.timeseries.simplets.TsDataToolkit.multiply;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
public class CholetteProcessor {

    private final SaBenchmarkingSpec spec;

    public static final CholetteProcessor PROCESSOR = new CholetteProcessor(SaBenchmarkingSpec.DEFAULT_ENABLED);

    public static CholetteProcessor of(SaBenchmarkingSpec spec) {
        if (!spec.isEnabled()) {
            return null;
        } else {
            return new CholetteProcessor(spec);
        }
    }

    private CholetteProcessor(SaBenchmarkingSpec spec) {
        this.spec = spec;
    }

    public SaBenchmarkingResults process(@NonNull TsData y, TsData sa, Explorable preprocessing) {
        if (sa == null || sa.equals(y)) {
            return new SaBenchmarkingResults(y, y, y);
        }
        TsData orig = preprocessing == null ? y : preprocessing.getData(RegressionDictionaries.YC, TsData.class);
        TsData cal = preprocessing == null ? null : preprocessing.getData(RegressionDictionaries.YCAL, TsData.class);
        if (preprocessing != null && spec.isForecast()) {
            TsData origf = preprocessing.getData(RegressionDictionaries.Y_F, TsData.class);
            TsData calf = preprocessing.getData(RegressionDictionaries.YCAL + SeriesInfo.F_SUFFIX, TsData.class);
            orig = TsData.concatenate(orig, origf);
            cal = TsData.concatenate(cal, calf);
        }
        TsData target = (cal == null || spec.getTarget() == SaBenchmarkingSpec.Target.Original) ? orig : cal;

        // computes the benchmarking...
        TsData benchSa = benchmark(sa, target);
        return new SaBenchmarkingResults(sa, target, benchSa);
    }

    public TsData benchmark(TsData source, TsData target) {
        TsData ytarget = target.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        TsData s = correctBias(source, ytarget);
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        DataBlock ns = DataBlock.of(s.getValues());
        double factor = normalizer.normalize(ns);
        TsData tmp = TsData.of(s.getStart(), ns);
        TsData btmp = cholette(tmp, ytarget.fn(z -> z * factor));
        if (btmp != null) {
            btmp = btmp.fn(z -> z / factor);
        }
        return btmp;
    }

    private TsData correctBias(TsData s, TsData ytarget) {
        BiasCorrection bias = spec.getBiasCorrection();
        if (bias == BiasCorrection.None) {
            return s;
        }
        TsData sy = s.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
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
    private TsData cholette(TsData highFreqSeries, TsData aggregationConstraint) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData agg = highFreqSeries.aggregate(aggregationConstraint.getTsUnit(), AggregationType.Sum, true);

        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), aggregationConstraint.getStart().start());
        int offset = sh.until(sl) + ratio - 1;
        aggregationConstraint = TsData.subtract(aggregationConstraint, agg);
        double[] y = expand(highFreqSeries.length(), ratio, aggregationConstraint.getValues(), offset);

        double[] w = null;
        if (spec.getLambda() != 0) {
            w = highFreqSeries.getValues().toArray();
            if (spec.getLambda() != 1) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.pow(Math.abs(w[i]), spec.getLambda());
                }
            } else {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.abs(w[i]);
                }
            }
        }
        TsPeriod start = highFreqSeries.getStart();
        int head = (int) (start.getId() % ratio);
        ISsf ssf = SsfCholette.builder(ratio)
                .start(head)
                .rho(spec.getRho())
                .weights(w == null ? null : DoubleSeq.of(w))
                .build();
        DefaultSmoothingResults rslts = DkToolkit.sqrtSmooth(ssf, new SsfData(y), false, false);

        double[] b = new double[highFreqSeries.length()];
        if (w != null) {
            for (int i = 0; i < b.length; ++i) {
                b[i] = w[i] * (rslts.a(i).get(1));
            }
        } else {
            rslts.getComponent(1).copyTo(b, 0);
        }
        return TsData.add(highFreqSeries, TsData.ofInternal(start, b));
    }

}

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
package demetra.benchmarking.univariate;

import demetra.benchmarking.spi.ICholette;
import demetra.benchmarking.ssf.SsfCholette;
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.CholetteSpec.BiasCorrection;
import demetra.data.AggregationType;
import demetra.data.DeprecatedDoubles;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import org.openide.util.lookup.ServiceProvider;
import demetra.benchmarking.univariate.Cholette;
import static demetra.timeseries.simplets.TsDataToolkit.add;
import static demetra.timeseries.simplets.TsDataToolkit.multiply;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ICholette.class)
public class CholetteProcessor implements ICholette {
    
    public static final CholetteProcessor PROCESSOR=new CholetteProcessor();

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
            return multiply(s, DeprecatedDoubles.sum(target.getValues()) / DeprecatedDoubles.sum(sy.getValues()));
        } else {
            double b = (DeprecatedDoubles.sum(target.getValues()) - DeprecatedDoubles.sum(sy.getValues())) / target.length();
            if (agg == AggregationType.Average) {
                b *= s.getTsUnit().ratioOf(target.getTsUnit());
            }
            return TsDataToolkit.add(s, b);
        }
    }

    /**
     *
     * @param d
     * @param agg
     * @param type
     * @return
     */
    public static double[] expand(TsDomain d, TsData agg, AggregationType type) {
        int ratio = d.getTsUnit().ratioOf(agg.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // expand the data;
        double[] y = new double[d.getLength()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }
        // search the first non missing value
        TsPeriod aggstart = agg.getStart();
        TsPeriod first = aggstart.withUnit(d.getTsUnit());
        if (type != AggregationType.First) {
            first = first.plus(ratio - 1);
        }
        int pos = d.indexOf(first);
        if (pos < 0) {
            return null;
        }
        int p = 0;
        while (p < agg.length()) {
            y[pos] = agg.getValue(p++);
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
    private TsData cholette(TsData s, TsData target, CholetteSpec spec) {
        int ratio = s.getTsUnit().ratioOf(target.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData obj = subtract(target, s.aggregate(target.getTsUnit(), spec.getAggregationType(), true));
        if (spec.getAggregationType() == AggregationType.Average) {
            obj = multiply(obj, ratio);
        }

        double[] y = expand(s.getDomain(), obj, spec.getAggregationType());

        double[] w = null;
        if (spec.getLambda() != 0) {
            w = s.getValues().toArray();
            if (spec.getLambda() != 1) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.pow(Math.abs(w[i]), spec.getLambda());
                }
            }
        }
        TsPeriod start = s.getStart();
        int head = (int) (start.getId() % ratio);
        if (spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum) {
            ISsf ssf = SsfCholette.builder(ratio)
                    .start(head)
                    .rho(spec.getRho())
                    .weights(w == null ? null : DoubleSeq.of(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);

            double[] b = new double[s.length()];
            if (w != null) {
                for (int i = 0; i < b.length; ++i) {
                    b[i] = w[i] * (rslts.a(i).get(1));
                }
            } else {
                rslts.getComponent(1).copyTo(b, 0);
            }
            return add(s, TsData.ofInternal(s.getStart(), b));
        } else {
            ISsf ssf = SsfCholette.builder(ratio)
                    .start(head)
                    .rho(spec.getRho())
                    .weights(w == null ? null : DoubleSeq.of(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false, false);
            double[] b = new double[s.length()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = ssf.loading().ZX(i, rslts.a(i));
            }
            return add(s, TsData.ofInternal(start, b));
        }
    }

}

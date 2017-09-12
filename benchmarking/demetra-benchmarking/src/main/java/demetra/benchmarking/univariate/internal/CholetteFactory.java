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
package demetra.benchmarking.univariate.internal;

import demetra.benchmarking.spi.CholetteAlgorithm;
import demetra.benchmarking.univariate.CholetteSpecification;
import demetra.benchmarking.univariate.CholetteSpecification.BiasCorrection;
import demetra.data.AggregationType;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import org.openide.util.lookup.ServiceProvider;
import static demetra.timeseries.simplets.TsDataToolkit.add;
import static demetra.timeseries.simplets.TsDataToolkit.multiply;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = CholetteAlgorithm.class)
public class CholetteFactory implements CholetteAlgorithm {

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, CholetteSpecification spec) {
        TsData s = correctBias(highFreqSeries, aggregationConstraint, spec);
        return cholette(s, aggregationConstraint, spec);
    }

    private TsData correctBias(TsData s, TsData target, CholetteSpecification spec) {
        // No bias correction when we use pure interpolation
        AggregationType agg = spec.getAggregationType();
        if (spec.getBias() == BiasCorrection.None
                || (agg != AggregationType.Average && agg != AggregationType.Sum)) {
            return s;
        }
        TsData sy = TsDataConverter.changeTsUnit(s, target.getUnit(), agg, true);
        sy = TsDataToolkit.fitToDomain(sy, target.domain());
        // TsDataBlock.all(target).data.sum() is the sum of the aggregation constraints
        //  TsDataBlock.all(sy).data.sum() is the sum of the averages or sums of the original series
        BiasCorrection bias = spec.getBias();
        if (bias == BiasCorrection.Multiplicative) {
            return multiply(s, Doubles.sum(target.values()) / Doubles.sum(sy.values()));
        } else {
            double b = (Doubles.sum(target.values()) - Doubles.sum(sy.values())) / target.length();
            if (agg == AggregationType.Average) {
                b *= s.getUnit().ratio(target.getUnit());
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
    public static double[] expand(RegularDomain d, TsData agg, AggregationType type) {
        int ratio = d.getUnit().ratio(agg.getUnit());
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
        TsPeriod first = aggstart.withUnit(d.getUnit());
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
    private TsData cholette(TsData s, TsData target, CholetteSpecification spec) {
        int ratio = s.getUnit().ratio(target.getUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        TsData obj = subtract(target, TsDataConverter.changeTsUnit(s, target.getUnit(), spec.getAggregationType(), true));
        if (spec.getAggregationType() == AggregationType.Average) {
            obj = multiply(obj, ratio);
        }

        double[] y = expand(s.domain(), obj, spec.getAggregationType());

        double[] w = null;
        if (spec.getLambda() != 0) {
            w = s.values().toArray();
            if (spec.getLambda() != 1) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.pow(Math.abs(w[i]), spec.getLambda());
                }
            }
        }

        if (spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum) {
            ISsf ssf = SsfCholette.builder(ratio)
                    .start(s.getStart().getPosition(target.getUnit()) % ratio)
                    .rho(spec.getRho())
                    .weights(w == null ? null : DoubleSequence.ofInternal(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false);

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
                    .start(s.getStart().getPosition(target.getUnit()) % ratio)
                    .rho(spec.getRho())
                    .weights(w == null ? null : DoubleSequence.ofInternal(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false);
            double[] b = new double[s.length()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = ssf.getMeasurement().ZX(i, rslts.a(i));
            }
            return add(s, TsData.ofInternal(s.getStart(), b));
        }
    }

}

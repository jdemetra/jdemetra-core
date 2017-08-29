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
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriodSelector;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.timeseries.simplets.TsDataToolkit;
import static demetra.timeseries.simplets.TsDataToolkit.multiply;
import static demetra.timeseries.simplets.TsDataToolkit.subtract;
import demetra.timeseries.simplets.TsFrequency;
import demetra.timeseries.simplets.TsPeriod;
import java.time.LocalDate;
import org.openide.util.lookup.ServiceProvider;
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
        if (spec.getRho() == 1) {
            return rwcholette(s, aggregationConstraint, spec);
        } else {
            return archolette(s, aggregationConstraint, spec);
        }
    }

    private TsData correctBias(TsData s, TsData target, CholetteSpecification spec) {
        // No bias correction when we use pure interpolation
        AggregationType agg = spec.getAggregationType();
        if (spec.getBias() == BiasCorrection.None
                || (agg != AggregationType.Average && agg != AggregationType.Sum)) {
            return s;
        }
        TsData sy = TsDataConverter.changeFrequency(s, target.getFrequency(), agg, true);
        sy = TsDataToolkit.fitToDomain(sy, target.domain());
        // TsDataBlock.all(target).data.sum() is the sum of the aggregation constraints
        //  TsDataBlock.all(sy).data.sum() is the sum of the averages or sums of the original series
        BiasCorrection bias = spec.getBias();
        if (bias == BiasCorrection.Multiplicative) {
            return multiply(s, Doubles.sum(target.values()) / Doubles.sum(sy.values()));
        } else {
            double b = (Doubles.sum(target.values()) - Doubles.sum(sy.values())) / target.length();
            if (agg == AggregationType.Average) {
                int hfreq = s.getFrequency().getAsInt(), lfreq = target.getFrequency().getAsInt();
                b *= hfreq / lfreq;
            }
            return TsDataToolkit.add(s, b);
        }
    }

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    private TsData archolette(TsData s, TsData target, CholetteSpecification spec) {
        int lfreq = target.getFrequency().intValue(), hfreq = s.getFrequency().intValue();
        int c = hfreq / lfreq;

        TsData obj = s.changeFrequency(target.getFrequency(), getAggregationType(), true).minus(target);
        if (getAggregationType() == TsAggregationType.Average) {
            obj = obj.times(c);
        }

        double[] y = expand(s.getDomain(), obj, getAggregationType());

        double[] w = null;
        if (lambda_ == 1) {
            w = s.internalStorage();
        } else {
            w = new double[s.getLength()];
            TsDataBlock.all(s).data.copyTo(w, 0);
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(w[i]), lambda_);
            }
        }

        if (spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum) {
            ISsf ssf = SsfCholette.builder(c)
                    .start(s.getStart().getPosition() % c)
                    .rho(spec.getRho())
                    .weights(DoubleSequence.ofInternal(w))
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
            return subtract(s, TsData.ofInternal(s.getStart(), b));
        } else {
            ISsf ssf = SsfCholette.builder(c)
                    .start(s.getStart().getPosition() % c)
                    .rho(spec.getRho())
                    .weights(DoubleSequence.ofInternal(w))
                    .build();
            DefaultSmoothingResults rslts = DkToolkit.smooth(ssf, new SsfData(y), false);
            double[] b = new double[s.length()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = ssf.getMeasurement().ZX(i, rslts.a(i));
            }

            return subtract(s, TsData.ofInternal(s.getStart(), b));
        }
    }

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    private TsData rwcholette(TsData s, TsData target, CholetteSpecification spec) {
        int lfreq = target.getFrequency().getAsInt(), hfreq = s.getFrequency().getAsInt();
        int c = hfreq / lfreq;

        TsData obj = subtract(TsDataConverter.changeFrequency(s, target.getFrequency(), spec.getAggregationType(), true), target);
        if (spec.getAggregationType() == AggregationType.Average) {
            obj = multiply(obj, c);
        }

        double[] y = expand(s.domain(), obj, spec.getAggregationType());

        double[] w = null;
        if (spec.getLambda() == 1) {
            w = s.internalStorage();
        } else {
            w = new double[s.getLength()];
            TsDataBlock.all(s).data.copyTo(w, 0);
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(w[i]), lambda_);
            }
        }

        if (spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum) {
            SsfDenton denton = new SsfDenton(c, s.getStart().getPosition() % c, w);
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(denton);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b;
            if (w != null) {
                b = new double[s.getLength()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = w[i] * (drslts.A(i).get(1));
                }
            } else {
                b = drslts.component(1);
            }
            return s.minus(new TsData(s.getStart(), b, false));
        } else {
            WeightedSsf<SsfRw> denton = new WeightedSsf<>(w, new SsfRw());
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(denton);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b = new double[s.getLength()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = denton.ZX(i, drslts.A(i));
            }

            return s.minus(new TsData(s.getStart(), b, false));

        }
    }

}

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

import demetra.benchmarking.univariate.CubicSpline;
import demetra.benchmarking.univariate.CubicSplineSpec;
import demetra.benchmarking.univariate.Denton;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import java.time.LocalDateTime;
import java.util.function.DoubleUnaryOperator;
import jdplus.timeseries.simplets.TsDataToolkit;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(CubicSpline.Processor.class)
public class CubicSplineProcessor implements CubicSpline.Processor {

    public static final CubicSplineProcessor PROCESSOR=new CubicSplineProcessor();

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, CubicSplineSpec spec) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        LocalDateTime hstart = highFreqSeries.getStart().start();
        LocalDateTime hend = highFreqSeries.getPeriod(highFreqSeries.length()).start();
        TimeSelector qsel = TimeSelector.between(hstart, hend);
        TsData naggregationConstraint = TsDataToolkit.select(aggregationConstraint, qsel);
                // if sum or average, remove incomplete periods
        if (spec.getAggregationType() == AggregationType.Average || spec.getAggregationType() == AggregationType.Sum){
            throw new IllegalArgumentException();
        }

        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
        int offset = sh.until(sl);
        double[] r = process(spec, ratio, offset, highFreqSeries.getValues(), naggregationConstraint.getValues());
        return TsData.ofInternal(sh, r);
    }

    @Override
    public TsData benchmark(TsUnit highFreq, TsData aggregationConstraint, CubicSplineSpec spec) {
        int ratio = highFreq.ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TsPeriod sh = TsPeriod.of(highFreq, aggregationConstraint.getStart().start());
        double[] r = process(spec, ratio,aggregationConstraint.getValues());
        return TsData.ofInternal(sh, r);
    }

    private double[] process(CubicSplineSpec spec, int ratio, int offset, DoubleSeq hvals, DoubleSeq lvals) {
        double[] fxi=lvals.toArray();
        double[] obs=hvals.toArray();
        int n=fxi.length;
        double[] xi=new double[n];
        int start;
        switch (spec.getAggregationType()){
            case Last:
                start=ratio-1; break;
            case UserDefined:
                start=Math.min(ratio-1, spec.getObservationPosition());break;
            default:
                start=0;
        }
        for (int i=0, j=start+offset; i<xi.length; ++i, j+=ratio){
            xi[i]=j;
            fxi[i]/=obs[j];
        }
        DoubleUnaryOperator cs = jdplus.math.functions.CubicSpline.of(xi, fxi);
        for (int i=0; i<obs.length; ++i){
            double r=cs.applyAsDouble(i);
            obs[i]*=r;
        }
        return obs;
    }

    private double[] process(CubicSplineSpec spec, int ratio, DoubleSeq lvals) {
        double[] fxi=lvals.toArray();
        int n=fxi.length;
        double[] xi=new double[n];
        int start;
        switch (spec.getAggregationType()){
            case Last:
                start=ratio-1; break;
            case UserDefined:
                start=Math.min(ratio-1, spec.getObservationPosition());break;
            default:
                start=0;
        }
        for (int i=0, j=start; i<xi.length; ++i, j+=ratio){
            xi[i]=j;
        }
        DoubleUnaryOperator cs = jdplus.math.functions.CubicSpline.of(xi, fxi);
        double[] rslt=new double[n*ratio];
        for (int i=0; i<rslt.length; ++i){
            rslt[i]=cs.applyAsDouble(i);
        }
        return rslt;
    }


}

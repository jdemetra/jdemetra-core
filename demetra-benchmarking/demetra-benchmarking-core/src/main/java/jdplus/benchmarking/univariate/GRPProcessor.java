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

import demetra.benchmarking.univariate.GrowthRatePreservation;
import demetra.benchmarking.univariate.GrpSpec;
import demetra.data.AggregationType;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import java.time.LocalDateTime;
import jdplus.timeseries.simplets.TsDataToolkit;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(GrowthRatePreservation.Processor.class)
public class GRPProcessor implements GrowthRatePreservation.Processor {
    
    public static final GRPProcessor PROCESSOR=new GRPProcessor();
    

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, GrpSpec spec) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        LocalDateTime hstart = highFreqSeries.getStart().start();
        LocalDateTime hend = highFreqSeries.getPeriod(highFreqSeries.length()).start();
        TimeSelector qsel = TimeSelector.between(hstart, hend);
        TsData naggregationConstraint = TsDataToolkit.select(aggregationConstraint, qsel);
        
        // if sum or average, remove incomplete periods
        if (spec.getAggregationType() == AggregationType.Average || spec.getAggregationType() == AggregationType.Sum){
            int nbeg=0, nend=0;
            if (hstart.isAfter(naggregationConstraint.getStart().start()))
                    nbeg=1;
            if (hend.isBefore(naggregationConstraint.getPeriod(naggregationConstraint.length()).start())){
                nend=1;
            }
            naggregationConstraint=naggregationConstraint.drop(nbeg, nend);
        }
 
        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
        int offset = sh.until(sl);
        // exclude incomplete low frequency periods at the beginning and at the end
        
        GRP grp = new GRP(spec, ratio, offset);
        
        double[] r = grp.process(highFreqSeries.getValues(), naggregationConstraint.getValues());
        TsData rslt = TsData.ofInternal(sh, r);
        if (spec.getAggregationType() == AggregationType.Average)
            rslt=rslt.multiply(ratio);
        return rslt;
    }
}

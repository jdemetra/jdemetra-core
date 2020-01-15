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
import demetra.data.AggregationType;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
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
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, AggregationType type) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TimeSelector qsel = TimeSelector.between(highFreqSeries.getStart().start(), highFreqSeries.getPeriod(highFreqSeries.length()).start());
        TsData naggregationConstraint = TsDataToolkit.select(aggregationConstraint, qsel);
        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
        int offset = sh.until(sl);
        GRP grp = new GRP(type, ratio, offset);
        double[] r = grp.process(highFreqSeries.getValues(), naggregationConstraint.getValues());
        TsData rslt = TsData.ofInternal(sh, r);
        if (type == AggregationType.Average)
            rslt=rslt.multiply(ratio);
        return rslt;
    }
}

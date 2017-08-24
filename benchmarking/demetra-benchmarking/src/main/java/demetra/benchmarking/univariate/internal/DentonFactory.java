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

import demetra.benchmarking.spi.DentonAlgorithm;
import demetra.benchmarking.univariate.DentonSpecification;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriodSelector;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.simplets.TsFrequency;
import demetra.timeseries.simplets.TsPeriod;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = DentonAlgorithm.class)
public class DentonFactory implements DentonAlgorithm {

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, DentonSpecification spec) {
        int hfreq = highFreqSeries.getFrequency().getAsInt(), lfreq = aggregationConstraint.getFrequency().getAsInt();
        if (hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TsPeriodSelector qsel = TsPeriodSelector.between(highFreqSeries.getStart().start(), highFreqSeries.getPeriod(highFreqSeries.length()).start());
        TsData naggregationConstraint = TsDataToolkit.select(aggregationConstraint, qsel);
        TsPeriod sh = highFreqSeries.getStart(), sl = TsPeriod.of(sh.getFrequency(), naggregationConstraint.getStart().firstDay());
        int offset = sl.minus(sh);
        Denton denton = new Denton(spec, hfreq / lfreq, offset);
        double[] r = denton.process(highFreqSeries.values(), naggregationConstraint.values());
        return TsData.ofInternal(highFreqSeries.getStart(), r);
    }

    @Override
    public TsData benchmark(TsFrequency highFreq, TsData aggregationConstraint, DentonSpecification spec) {
       int hfreq = highFreq.getAsInt(), lfreq = aggregationConstraint.getFrequency().getAsInt();
        if (hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TsPeriod sh = TsPeriod.of(highFreq, aggregationConstraint.getStart().firstDay());
        Denton denton = new Denton(spec, hfreq / lfreq, 0);
        double[] r = denton.process(aggregationConstraint.values());
        return TsData.ofInternal(sh, r);
    }

}

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
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriodSelector;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.simplets.TsFrequency;
import demetra.timeseries.simplets.TsPeriod;
import java.time.LocalDate;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = CholetteAlgorithm.class)
public class CholetteFactory implements CholetteAlgorithm {

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, CholetteSpecification spec) {
        TsData s = correctBias(s, aggregationConstraint);
        if (spec.getRho() == 1) {
            return rwcholette(s, aggregationConstraint);
        } else {
            return archolette(s, aggregationConstraint);
        }
    }

    private TsData correctBias(TsData s, TsData target, CholetteSpecification spec) {
        // No bias correction when we use pure interpolation
        AggregationType agg=spec.getAggregationType();
        if (spec.getBias()== BiasCorrection.None || 
                (agg != AggregationType.Average && agg != AggregationType.Sum)) {
            return s;
        }
        TsData sy = TsDataConverter.changeFrequency(s, target.getFrequency(), agg, true);
        sy = TsDataToolkit.fitToDomain(sy, target.domain());
        // TsDataBlock.all(target).data.sum() is the sum of the aggregation constraints
        //  TsDataBlock.all(sy).data.sum() is the sum of the averages or sums of the original series 
        if (bias_ == BiasCorrection.Multiplicative) {
            return s.times(TsDataBlock.all(target).data.sum() / TsDataBlock.all(sy).data.sum());
        } else {
            double b = (TsDataBlock.all(target).data.sum() - TsDataBlock.all(sy).data.sum())/target.getLength();
            if (agg == TsAggregationType.Average){
                int hfreq=s.getFrequency().intValue(), lfreq=target.getFrequency().intValue();
                b*=hfreq/lfreq;
            }
            return s.plus(b);
        }
    }

}

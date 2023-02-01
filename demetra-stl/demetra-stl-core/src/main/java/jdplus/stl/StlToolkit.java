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
package jdplus.stl;

import demetra.data.DoubleSeq;
import demetra.stl.IStlSpec;
import demetra.stl.MStlSpec;
import demetra.stl.SeasonalSpec;
import demetra.stl.StlSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class StlToolkit {

    public StlResults process(TsData data, StlSpec spec) {
        RawStlKernel stl = new RawStlKernel(spec);
        RawStlResults decomp = stl.process(data.getValues());

        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa()),
                seasonal = TsData.of(start, decomp.getSeasonal());

        return StlResults.builder()
                .multiplicative(spec.isMultiplicative())
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights)
                .seasonal(spec.getSeasonalSpec().getPeriod(), seasonal)
                .build();
    }

    public StlResults process(TsData data, MStlSpec spec) {
        // We should add pre-processing
        MStlKernel stl = new MStlKernel(spec);
        MStlResults decomp = stl.process(data.getValues());
        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa());

        StlResults.Builder builder = StlResults.builder()
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights);
        
        Iterator<DoubleSeq> seasons = decomp.getSeasons().iterator();
        List<SeasonalSpec> seasonalSpecs = spec.getSeasonalSpecs();
        for (SeasonalSpec sspec : seasonalSpecs){
            builder.seasonal(sspec.getPeriod(), TsData.of(start, seasons.next()));
        }
         return builder.build();
    }
    
    public StlResults process(TsData data, IStlSpec spec) {
        // We should add pre-processing
        
        MStlResults decomp = IStlKernel.process(data.getValues(), spec);
        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa());

        StlResults.Builder builder = StlResults.builder()
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights);
        
        Iterator<DoubleSeq> seasons = decomp.getSeasons().iterator();
        List<IStlSpec.PeriodSpec> perodSpec = spec.getPeriodSpecs();
        for (IStlSpec.PeriodSpec sspec : perodSpec){
            builder.seasonal(sspec.getSeasonalSpec().getPeriod(), TsData.of(start, seasons.next()));
        }
         return builder.build();
    }
    
}

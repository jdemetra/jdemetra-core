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

import demetra.stl.SeasonalSpecification;
import demetra.stl.StlSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author PALATEJ
 */
public class StlPlusKernel {

    public StlPlusKernel(StlSpecification spec) {
        this.spec = spec;
    }

    private final StlSpecification spec;

    public StlPlusResults process(TsData data) {
        // We should add pre-processing
        StlKernel stl = new StlKernel(spec);
        StlResults decomp = stl.process(data.getValues());

        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = spec.isMultiplicative() ? TsData.multiply(trend, irr) : TsData.add(trend, irr);

        StlPlusResults.Builder builder = StlPlusResults.builder()
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights);
        
        int i=0;
        TsData seas=null;
        for (SeasonalSpecification sspec : spec.getSeasonalSpecs()){
            TsData cur= TsData.of(start, decomp.getSeasons().get(i++));
            seas=spec.isMultiplicative() ? TsData.multiply(seas, cur) : TsData.add(seas, cur);
            builder.season(sspec.getPeriod(), cur);
        }
        builder.seasonal(seas);

        return builder.build();
    }

}

/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this fit except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.stl.IStlSpec;
import demetra.stl.StlSpec;

/**
 * Iterative version of STL for multiple frequencies
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class IStlKernel {

    public MStlResults process(DoubleSeq data, IStlSpec spec) {
        MStlResults.Builder builder = MStlResults.builder().series(data);
        RawStlResults curRslt = null;
        DoubleSeq seasonal = null;
        boolean mul = spec.isMultiplicative();
        for (IStlSpec.PeriodSpec pspec : spec.getPeriodSpecs()) {
            StlSpec curSpec = StlSpec.builder()
                    .multiplicative(mul)
                    .seasonalSpec(pspec.getSeasonalSpec())
                    .trendSpec(pspec.getTrendSpec())
                    .innerLoopsCount(spec.getInnerLoopsCount())
                    .outerLoopsCount(spec.getOuterLoopsCount())
                    .robustWeightFunction(spec.getRobustWeightFunction())
                    .robustWeightThreshold(spec.getRobustWeightThreshold())
                    .build();
            RawStlKernel kernel = new RawStlKernel(curSpec);
            curRslt = kernel.process(data);
            DoubleSeq seas = curRslt.getSeasonal();
            builder.season(seas);
            if (mul) {
                seasonal = DoublesMath.multiply(seasonal, seas);
            } else {
                seasonal = DoublesMath.add(seasonal, seas);
            }
            data = curRslt.getSa();
        }
        if (curRslt == null) {
            return null;
        }
        DoubleSeq fit = mul ? DoublesMath.multiply(curRslt.getTrend(), seasonal) 
                : DoublesMath.add(curRslt.getTrend(), seasonal);
        return builder
                .trend(curRslt.getTrend())
                .sa(data)
                .fit(fit)
                .irregular(curRslt.getIrregular())
                .seasonal(seasonal)
                .weights(curRslt.getWeights())
                .build();
    }

}

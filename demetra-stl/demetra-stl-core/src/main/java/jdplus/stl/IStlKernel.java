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

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.stl.IStlSpec;
import demetra.stl.SeasonalSpec;
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
        StlResults curRslt=null;
        DoubleSeq seasonal=null;
        for (IStlSpec.PeriodSpec pspec : spec.getPeriodSpecs()){
            boolean mul=spec.isMultiplicative();
            StlSpec curSpec = StlSpec.builder()
                    .multiplicative(mul)
                    .seasonalSpec(pspec.getSeasonalSpec())
                    .trendSpec(pspec.getTrendSpec())
                    .innerLoopsCount(spec.getInnerLoopsCount())
                    .outerLoopsCount(spec.getOuterLoopsCount())
                    .robustWeightFunction(spec.getRobustWeightFunction())
                    .robustWeightThreshold(spec.getRobustWeightThreshold())
                    .build();
            StlKernel kernel=new StlKernel(curSpec);
            curRslt = kernel.process(data);
            DoubleSeq seas = curRslt.getSeasonal();
            builder.season(seas);
            DoublesMath.add(seasonal, seas);
            data=curRslt.getSa();
        }
        if (curRslt == null)
            return null;
        return builder
                .trend(curRslt.getTrend())
                .sa(data)
                .fit(DoublesMath.add(curRslt.getTrend(), seasonal))
                .irregular(curRslt.getIrregular())
                .seasonal(seasonal)
                .weights(curRslt.getWeights())
                .build();
    }

}

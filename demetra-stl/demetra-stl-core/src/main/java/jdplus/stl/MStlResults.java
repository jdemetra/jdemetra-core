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
import demetra.data.Doubles;
import demetra.data.DoublesMath;
import demetra.information.GenericExplorable;
import demetra.modelling.highfreq.DataCleaning;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class MStlResults implements GenericExplorable {

    boolean multiplicative;
    DoubleSeq series;
    DoubleSeq sa;
    DoubleSeq trend;
    @lombok.Singular
    Map<Integer, DoubleSeq> seasons;
    DoubleSeq irregular;
    DoubleSeq fit;
    DoubleSeq weights;
    
    public DoubleSeq seasonal(){
        if (seasons.isEmpty())
            return null;
        DoubleSeq all=null;
        for (DoubleSeq s :seasons.values()){
            all=multiplicative ? DoublesMath.multiply(all, s) : DoublesMath.add(all, s);
        }
        return all;            
    }
    
    public SeriesDecomposition asDecomposition(TsPeriod start){
        return SeriesDecomposition.builder(multiplicative ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .add(TsData.of(start, series), ComponentType.Series)
                .add(TsData.of(start, trend), ComponentType.Trend)
                .add(TsData.of(start, sa), ComponentType.SeasonallyAdjusted)
                .add(TsData.of(start, irregular), ComponentType.Irregular)
                .add(TsData.of(start, seasonal()), ComponentType.Seasonal)
                .build();
    }

    public SeriesDecomposition asDecomposition(TsDomain domain, DataCleaning cleaning){
        return SeriesDecomposition.builder(multiplicative ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .add(cleaning.expand(domain, series), ComponentType.Series)
                .add(cleaning.expand(domain, trend), ComponentType.Trend)
                .add(cleaning.expand(domain, sa), ComponentType.SeasonallyAdjusted)
                .add(cleaning.expand(domain, irregular), ComponentType.Irregular)
                .add(cleaning.expand(domain, seasonal()), ComponentType.Seasonal)
                .build();
    }
    
    
}

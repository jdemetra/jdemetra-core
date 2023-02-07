/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.stlplus;

import demetra.sa.SeriesDecomposition;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.timeseries.TsData;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sa.StationaryVarianceComputer;
import jdplus.sa.diagnostics.GenericSaTests;
import jdplus.stl.StlResults;

@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class StlPlusDiagnostics {
    
    private StationaryVarianceDecomposition varianceDecomposition;
    private GenericSaTests genericDiagnostics;
    
    public static StlPlusDiagnostics of(RegSarimaModel preprocessing, StlResults srslts, SeriesDecomposition finals) {
        boolean mul = finals.getMode().isMultiplicative();
        TsData y = srslts.getSeries();
        TsData sa = srslts.getTrend();
        TsData i = srslts.getIrregular();
        TsData s = srslts.seasonal();
        TsData si = mul ? TsData.multiply(s, i) : TsData.add(s, i);
        GenericSaTests gsadiags;
        TsData lsa = mul ? sa.log() : sa;
        TsData li = mul ? i.log() : i;
        if (preprocessing != null) {
            TsData lin = preprocessing.linearizedSeries();
            
            gsadiags = GenericSaTests.builder()
                    .mul(mul)
                    .regarima(preprocessing)
                    .lin(lin)
                    .res(preprocessing.fullResiduals())
                    .y(y)
                    .sa(sa)
                    .irr(i)
                    .si(si)
                    .lsa(lsa)
                    .lirr(li)
                    .build();
        } else {
            gsadiags = GenericSaTests.builder()
                    .mul(mul)
                    .regarima(preprocessing)
                    .res(null)
                    .lin(y)
                    .y(y)
                    .sa(sa)
                    .irr(i)
                    .si(si)
                    .lsa(lsa)
                    .lirr(li)
                    .build();
            
        }
        return new StlPlusDiagnostics(varDecomposition(preprocessing, srslts), gsadiags);
    }
    
    private static StationaryVarianceDecomposition varDecomposition(RegSarimaModel preprocessing, StlResults srslts) {
        StationaryVarianceComputer var = new StationaryVarianceComputer(StationaryVarianceComputer.HP);
        boolean mul = srslts.isMultiplicative();
        if (preprocessing != null) {
            TsData y = preprocessing.interpolatedSeries(false),
                    t = srslts.getTrend(),
                    seas = srslts.seasonal(),
                    irr = srslts.getIrregular(),
                    cal = preprocessing.getCalendarEffect(y.getDomain());
            
            TsData others;
            if (mul) {
                TsData all = TsData.multiply(t, seas, irr, cal);
                others = TsData.divide(y, all);
            } else {
                TsData all = TsData.add(t, seas, irr, cal);
                others = TsData.subtract(y, all);
            }
            return var.build(y, t, seas, irr, cal, others, mul);
        } else {
            TsData y = srslts.getSeries(),
                    t = srslts.getTrend(),
                    seas = srslts.seasonal(),
                    irr = srslts.getIrregular(),
                    cal = null,
                    others = null;
            return var.build(y, t, seas, irr, cal, others, mul);
        }
    }
}

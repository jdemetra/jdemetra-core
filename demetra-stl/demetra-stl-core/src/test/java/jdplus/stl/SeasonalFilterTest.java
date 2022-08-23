/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
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

import demetra.stl.LoessSpec;
import demetra.data.Doubles;
import org.junit.jupiter.api.Test;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.stl.SeasonalSpec;

/**
 *
 * @author Jean Palate
 */
public class SeasonalFilterTest {
    
    public SeasonalFilterTest() {
    }

    @Test
    public void testCustom() {
        DoubleSeq s = Doubles.of(Data.EXPORTS);
        double[] d = s.toArray();
        LoessSpec spec = LoessSpec.of(3, 0, false);
        LoessSpec lspec = LoessSpec.of(5, 1, false);
        SeasonalFilter filter=new SeasonalFilter(spec, lspec, 12);
        double[] sd=new double[d.length];
        filter.filter(IDataGetter.of(d), null, false, IDataSelector.of(sd));
//        System.out.println(DoubleSeq.of(d));
//        System.out.println(DoubleSeq.of(sd));
    }
    
    @Test
    public void testDefault() {
        DoubleSeq s = Doubles.of(Data.EXPORTS);
        double[] d = s.toArray();
        SeasonalSpec spec=new SeasonalSpec(12, 7, false);
        SeasonalFilter filter=SeasonalFilter.of(spec);
        double[] sd=new double[d.length];
        filter.filter(IDataGetter.of(d), null, false, IDataSelector.of(sd));
//        System.out.println(DoubleSeq.of(d));
//        System.out.println(DoubleSeq.of(sd));
    }
    
}

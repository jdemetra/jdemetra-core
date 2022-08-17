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
import demetra.data.Data;
import demetra.data.Doubles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class LoessFilterTest {
    
    public LoessFilterTest() {
    }

    @Test
    @Disabled
    public void testNormal() {
        DoubleSeq s = Doubles.of(Data.EXPORTS);
        double[] d = s.toArray();
        LoessSpec spec = LoessSpec.of(25, 0,1, null);
        LoessFilter filter=new LoessFilter(spec);
        double[] sd=new double[d.length];
        filter.filter(IDataGetter.of(d), null, IDataSelector.of(sd));
//        System.out.println(s);
//        System.out.println(DataBlock.of(sd));
    }
    
    @Test
    public void testBF() {
        DoubleSeq s = Doubles.of(Data.EXPORTS);
        double[] d = s.toArray();
        LoessSpec spec = LoessSpec.of(25, 1, 5, null);
        LoessFilter filter=new LoessFilter(spec);
        int nf=5;
        double[] sd=new double[d.length+2*nf];
        filter.filter(IDataGetter.of(d), null, IDataSelector.of(sd, -nf));
//        System.out.println(DoubleSeq.of(d));
//        System.out.println(DoubleSeq.of(sd));
    }
}

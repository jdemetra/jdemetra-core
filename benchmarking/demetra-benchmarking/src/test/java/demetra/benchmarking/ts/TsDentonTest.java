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
package demetra.benchmarking.ts;

import demetra.benchmarking.univariate.DentonSpecification;
import demetra.benchmarking.univariate.TsDenton;
import demetra.data.DataBlock;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsFrequency;
import demetra.timeseries.simplets.TsPeriod;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TsDentonTest {
    public TsDentonTest(){
        
    }
    
    @Test
    public void test1(){
        DataBlock y=DataBlock.make(20);
        y.set(i->(1+i));
        DataBlock x=DataBlock.make(90);
        x.set(i->(1+i)*(1+i));
        
        DentonSpecification spec=new DentonSpecification();
        spec.setModified(true);
        spec.setMultiplicative(false);
        TsPeriod q=TsPeriod.of(TsFrequency.Quarterly, 1978, 3);
        TsPeriod a=TsPeriod.of(TsFrequency.Yearly, 1980, 0);
        TsData t=TsData.of(a, y);
        TsData s=TsData.of(q, x);
        TsData b = TsDenton.benchmark(s, t, spec);
        System.out.println(b);
    }
    
    @Test
    public void test2(){
        DataBlock y=DataBlock.make(20);
        y.set(i->(1+i));
        
        DentonSpecification spec=new DentonSpecification();
        spec.setModified(true);
        spec.setDifferencing(3);
        TsPeriod a=TsPeriod.of(TsFrequency.Yearly, 1980, 0);
        TsData t=TsData.of(a, y);
        TsData b = TsDenton.benchmark(TsFrequency.QuadriMonthly, t, spec);
        System.out.println(b);
    }
}

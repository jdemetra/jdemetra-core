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
package demetra.benchmarking.r;

import demetra.data.Data;
import jdplus.data.DataBlock;
import demetra.data.Doubles;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class BenchmarkingTest {

    public BenchmarkingTest() {
    }

    @Test
    public void testDenton() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(270);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod q = TsPeriod.monthly(1978, 4);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));
        TsData s = TsData.of(q, Doubles.of(x));

        TsData qs = Benchmarking.denton(s, t, 1, true, true, "Sum");
        assertTrue(qs != null);
        
    }

    @Test
    public void testGRP() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(270);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod q = TsPeriod.monthly(1980, 1);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));
        TsData s = TsData.of(q, Doubles.of(x));

        TsData qs = Benchmarking.grp(s, t, "Sum", 1e-15, 100, true);
        assertTrue(qs != null);
    }
    
    @Test
    public void testCubicSpline() {
        DataBlock y = DataBlock.of(Data.PCRA);
        TsPeriod a = TsPeriod.yearly(1979);
        TsData t = TsData.of(a, Doubles.of(y));
        TsData qs1 = Benchmarking.cubicSpline(4, t, "UserDefined", 2);
        System.out.println(qs1);
        assertTrue(qs1 != null);
        System.out.println();
        DataBlock x = DataBlock.of(Data.IND_PCR);
        TsPeriod q = TsPeriod.quarterly(1978,1);
        TsData s = TsData.of(q, Doubles.of(x));
        TsData qs2 = Benchmarking.cubicSpline(s, t, "UserDefined", 2);
        System.out.println(qs2);
        assertTrue(qs1 != null);
    }
    
}

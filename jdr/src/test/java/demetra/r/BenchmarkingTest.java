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
package demetra.r;

import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.DataBlock;
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
    public void testSomeMethod() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod q = TsPeriod.quarterly(1978, 4);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);

        TsData qs = Benchmarking.denton(s, t, 1, true, true, "Sum");
        
        
    }

}

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
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.Denton;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import demetra.data.Doubles;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class DentonTest {

    public DentonTest() {

    }

    @Test
    public void test1() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(false)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));
        TsData s = TsData.of(q, Doubles.of(x));
        TsData b = Denton.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test2() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));

        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .differencing(3)
                .build();
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));
        TsData b = Denton.benchmark(TsUnit.of(4, ChronoUnit.MONTHS), t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(x -> Math.abs(x) < 1e-9));
    }
}

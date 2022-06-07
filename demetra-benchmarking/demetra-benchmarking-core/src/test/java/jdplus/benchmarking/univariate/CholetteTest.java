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

import demetra.benchmarking.univariate.Cholette;
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.Denton;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import jdplus.timeseries.simplets.TsDataToolkit;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class CholetteTest {

    @Test
    public void test1() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        CholetteSpec spec = CholetteSpec.builder()
                .lambda(0)
                .rho(0.8)
                .build();

        TsPeriod q = TsPeriod.quarterly(1978, 4);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Cholette.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
//        System.out.println(b);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test2() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        CholetteSpec spec = CholetteSpec.builder()
                .lambda(0)
                .rho(1)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Cholette.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
//        System.out.println(b);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test3() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        CholetteSpec spec = CholetteSpec.builder()
                .lambda(-.6)
                .rho(1)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Cholette.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
//        System.out.println(b);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test4() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        CholetteSpec spec = CholetteSpec.builder()
                .lambda(-.60)
                .rho(0)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Cholette.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
//        System.out.println(b);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test5() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

         CholetteSpec spec = CholetteSpec.builder()
                .lambda(0)
                .rho(0.9)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Cholette.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
//        System.out.println(b);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    @Disabled
    public void test1Legacy() {
        ec.tstoolkit.data.DataBlock y = new ec.tstoolkit.data.DataBlock(20);
        y.set(i -> (1 + i));
        ec.tstoolkit.data.DataBlock x = new ec.tstoolkit.data.DataBlock(90);
        x.set(i -> (1 + i) * (1 + i));

        ec.benchmarking.simplets.TsCholette cholette = new ec.benchmarking.simplets.TsCholette();
        cholette.setLambda(.9);
        cholette.setRho(.8);
        ec.tstoolkit.timeseries.simplets.TsPeriod q
                = new ec.tstoolkit.timeseries.simplets.TsPeriod(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 1978, 2);
        ec.tstoolkit.timeseries.simplets.TsPeriod a
                = new ec.tstoolkit.timeseries.simplets.TsPeriod(ec.tstoolkit.timeseries.simplets.TsFrequency.Yearly, 1980, 0);
        ec.tstoolkit.timeseries.simplets.TsData t = new ec.tstoolkit.timeseries.simplets.TsData(a, y);
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(q, x);
        ec.tstoolkit.timeseries.simplets.TsData b = cholette.process(s, t);
        System.out.println(b);
    }

    @Test
    public void test6() {
        DataBlock y = DataBlock.make(5);
        y.set(i -> (1 + i));

        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .differencing(3)
                .build();

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData b = Denton.benchmark(TsUnit.of(3, ChronoUnit.MONTHS), t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(x -> Math.abs(x) < 1e-9));
    }
}

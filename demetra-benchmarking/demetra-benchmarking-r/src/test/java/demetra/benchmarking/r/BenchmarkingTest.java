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

import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import demetra.data.Doubles;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class BenchmarkingTest {

    public BenchmarkingTest() {
    }

    @Test
    public void testCholette() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q;
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "First", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Last", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cholette(s, t, .5, .5, "None", "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

    @Test
    public void testDenton() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "First", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Last", 0);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1,true, true, "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.denton(s, t, 1, true, true, "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

    @Test
    public void testGRP() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "First", 1, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Last", 1, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "UserDefined", 3, 1e-15, 100, true);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Sum", 0, 1e-15, 100, true);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.grp(s, t, "Average", 0, 1e-15, 100, true);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

    @Test
    public void testCubicSpline() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));

        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, Doubles.of(y));

        TsPeriod q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "First", 1);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 0);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Last", 1);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 11);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "UserDefined", 3);
            TsData ta=qs.aggregateByPosition(TsUnit.YEAR, 2);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Sum", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
        q = TsPeriod.monthly(1979, 1);
        for (int i = 0; i < 8; ++i) {
            TsData s = TsData.of(q, Doubles.of(x));
            TsData qs = Benchmarking.cubicSpline(s, t, "Average", 0);
            TsData ta=qs.aggregate(TsUnit.YEAR, AggregationType.Average, true);
            assertEquals(TsData.subtract(t, ta).getValues().norm2(), 0, 1e-6);
            q = q.plus(1);
        }
    }

}

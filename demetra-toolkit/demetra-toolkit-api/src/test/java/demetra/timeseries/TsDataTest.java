/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package demetra.timeseries;

import static demetra.data.AggregationType.*;
import static demetra.timeseries.TsUnit.*;
import nbbrd.design.Demo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static demetra.timeseries.TsPeriod.DEFAULT_EPOCH;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class TsDataTest {

    @Demo
    public static void main(String[] args) {
        TsData ts = TsData.of(TsPeriod.yearly(2001), Doubles.of(new double[]{3.14, 7}));

        System.out.println("\n[Tests ...]");
        System.out.println(ts.toString());
        System.out.println(ts.getDomain());
        System.out.println(ts.getValues());

        System.out.println("\n[Test for]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.get(i));
        }

        System.out.println("\n[Test forEach]");
        ts.forEach(o -> System.out.println(o));

        System.out.println("\n[Test iterator]");
        for (TsObs o : ts) {
            System.out.println(o);
        }

        System.out.println("\n[Test stream]");
        ts.stream()
                .filter(o -> o.getPeriod().start().isAfter(LocalDate.of(2001, 1, 1).atStartOfDay()))
                .forEach(System.out::println);

        System.out.println("\n[Test forEach(k, v)]");
        ts.forEach((k, v) -> System.out.println(k + ":" + v + " "));

        System.out.println("\n[Test getPeriod / getValue]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.getPeriod(i) + " -> " + ts.getValue(i));
        }

        System.out.println("\n[Test ITimeSeries.OfDouble]");
        {
            TimeSeriesData<?, ?> y = ts;
            for (int i = 0; i < y.length(); i++) {
                System.out.println(y.getPeriod(i) + " -> " + y.getValue(i));
            }
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        TsPeriod start = TsPeriod.yearly(2001);
        double[] values = {1, 2, 3};
        String cause = "some text";

        TsData x;

        x = TsData.empty(start, cause);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).isEmpty();
        assertThat(x.getCause()).isEqualTo(cause);

        assertThatNullPointerException().isThrownBy(() -> TsData.empty(null, cause));
        assertThatNullPointerException().isThrownBy(() -> TsData.empty(start, null));

        x = TsData.of(start, Doubles.of(values));
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.of(null, Doubles.of(values)));
        assertThatNullPointerException().isThrownBy(() -> TsData.of(start, null));

        x = TsData.ofInternal(start, DoubleSeq.copyOf(values));
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(null, DoubleSeq.copyOf(values)));
        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(start, (DoubleSeq) null));

        x = TsData.ofInternal(start, values);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(null, values));
        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(start, (double[]) null));
    }

    @Test
    public void testEquals() {
        assertThat(TsData.empty(TsPeriod.yearly(2001), "abc"))
                .isEqualTo(TsData.empty(TsPeriod.yearly(2001), "abc"))
                .isNotEqualTo(TsData.empty(TsPeriod.yearly(2001), "xyz"));

        assertThat(TsData.of(TsPeriod.yearly(2001), Doubles.of(new double[]{1, 2, 3})))
                .isEqualTo(TsData.of(TsPeriod.yearly(2001), Doubles.of(new double[]{1, 2, 3})));
    }

    @Test
    public void testRandom() {
        TsData random = TsData.random(TsUnit.MONTH, 0);
        assertThat(random.getDomain().length() == random.getValues().length()).isTrue();
        assertThat(random.getValues().allMatch(x -> x >= 100)).isTrue();
    }

    @Test
    public void testAggregationNoRatio() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);
        assertThatExceptionOfType(TsException.class).isThrownBy(() ->ts.aggregate(DAY, First, true));
    }

    @Test
    public void testAggregationNoChange() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);
        assertThat(ts.aggregate(MONTH, First, true)).isEqualTo(ts);
    }

    @Test
    public void testAggregationCompleteAfterEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(2010, 1), y(2011, 13));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(2010, 1), y(2011, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(2010, 12), y(2011, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
    }

    @Test
    public void testAggregationIncompleteAfterEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40).plusMonths(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(2010, 2), y(2011, 13), y(2012, 25));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(2011, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24), y(2012, 25));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(2011, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(2010, 2), q2(2010, 4)).hasSize(9);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q2(2010, 4), q3(2010, 7)).hasSize(7);

        TsData ts11 = monthlyTs(LocalDate.of(2010, 1, 1), 11);
        assertThat(ts11.aggregate(YEAR, First, false)).containsExactly(y(2010, 1));
        assertThat(ts11.aggregate(YEAR, First, true)).isEmpty();
    }

    @Test
    public void testAggregationCompleteBeforeEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.minusYears(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(1969, 1), y(1970, 13));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(1969, 1), y(1970, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(1969, 12), y(1970, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
    }

    @Test
    public void testAggregationIncompleteBeforeEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.minusYears(1).plusMonths(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(1969, 2), y(1970, 13), y(1971, 25));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(1970, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24), y(1971, 25));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(1970, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(1969, 2), q2(1969, 4)).hasSize(9);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q2(1969, 4), q3(1969, 7)).hasSize(7);

        TsData ts11 = monthlyTs(DEFAULT_EPOCH.minusYears(1), 11);
        assertThat(ts11.aggregate(YEAR, First, false)).containsExactly(y(1969, 1));
        assertThat(ts11.aggregate(YEAR, First, true)).isEmpty();
    }
    
    @Test
    public void testAggregationByPosition() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusMonths(1), 61);
        assertThat(ts.aggregateByPosition(YEAR, 3)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 0)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 11)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 1)).hasSize(6);
    }
    private static TsData monthlyTs(LocalDateTime start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), Doubles.of(count, i -> i + start.getMonthValue()));
    }

    private static TsData monthlyTs(LocalDate start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), Doubles.of(count, i -> i + start.getMonthValue()));
    }

    private static TsObs y(int year, double val) {
        return TsObs.of(TsPeriod.yearly(year), val);
    }

    private static TsObs q1(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 1), val);
    }

    private static TsObs q2(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 2), val);
    }

    private static TsObs q3(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 3), val);
    }
}

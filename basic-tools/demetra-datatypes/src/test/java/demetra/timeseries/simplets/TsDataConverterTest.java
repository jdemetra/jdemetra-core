/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries.simplets;

import static demetra.data.AggregationType.*;
import demetra.data.DoubleSequence;
import demetra.timeseries.TsPeriod;
import static demetra.timeseries.TsPeriod.EPOCH;
import demetra.timeseries.TsUnit;
import static demetra.timeseries.TsUnit.*;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static demetra.timeseries.simplets.TsDataConverter.changeTsUnit;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 */
public class TsDataConverterTest {

    @Test
    public void testNoRatio() {
        TsData ts = monthlyTs(EPOCH.plusYears(40), 24);
        assertThat(changeTsUnit(ts, DAY, First, true)).isNull();
    }

    @Test
    public void testNoChange() {
        TsData ts = monthlyTs(EPOCH.plusYears(40), 24);
        assertThat(changeTsUnit(ts, MONTH, First, true)).isEqualTo(ts);
    }

    @Test
    public void testCompleteAfterEpoch() {
        TsData ts = monthlyTs(EPOCH.plusYears(40), 24);

        assertThat(changeTsUnit(ts, YEAR, First, false)).containsExactly(y(2010, 1), y(2011, 13));
        assertThat(changeTsUnit(ts, YEAR, First, true)).containsExactly(y(2010, 1), y(2011, 13));

        assertThat(changeTsUnit(ts, YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24));
        assertThat(changeTsUnit(ts, YEAR, Last, true)).containsExactly(y(2010, 12), y(2011, 24));

        assertThat(changeTsUnit(ts, QUARTER, First, false)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
        assertThat(changeTsUnit(ts, QUARTER, First, true)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
    }

    @Test
    public void testIncompleteAfterEpoch() {
        TsData ts = monthlyTs(EPOCH.plusYears(40).plusMonths(1), 24);

        assertThat(changeTsUnit(ts, YEAR, First, false)).containsExactly(y(2010, 2), y(2011, 13), y(2012, 25));
        assertThat(changeTsUnit(ts, YEAR, First, true)).containsExactly(y(2011, 13));

        assertThat(changeTsUnit(ts, YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24), y(2012, 25));
        assertThat(changeTsUnit(ts, YEAR, Last, true)).containsExactly(y(2011, 24));

        assertThat(changeTsUnit(ts, QUARTER, First, false)).startsWith(q1(2010, 2), q2(2010, 4)).hasSize(9);
        assertThat(changeTsUnit(ts, QUARTER, First, true)).startsWith(q2(2010, 4), q3(2010, 7)).hasSize(7);

        TsData ts11 = monthlyTs(LocalDate.of(2010, 1, 1), 11);
        assertThat(changeTsUnit(ts11, YEAR, First, false)).containsExactly(y(2010, 1));
        assertThat(changeTsUnit(ts11, YEAR, First, true)).isEmpty();
    }

    @Test
    public void testCompleteBeforeEpoch() {
        TsData ts = monthlyTs(EPOCH.minusYears(1), 24);

        assertThat(changeTsUnit(ts, YEAR, First, false)).containsExactly(y(1969, 1), y(1970, 13));
        assertThat(changeTsUnit(ts, YEAR, First, true)).containsExactly(y(1969, 1), y(1970, 13));

        assertThat(changeTsUnit(ts, YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24));
        assertThat(changeTsUnit(ts, YEAR, Last, true)).containsExactly(y(1969, 12), y(1970, 24));

        assertThat(changeTsUnit(ts, QUARTER, First, false)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
        assertThat(changeTsUnit(ts, QUARTER, First, true)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
    }

    @Test
    public void testIncompleteBeforeEpoch() {
        TsData ts = monthlyTs(EPOCH.minusYears(1).plusMonths(1), 24);

        assertThat(changeTsUnit(ts, YEAR, First, false)).containsExactly(y(1969, 2), y(1970, 13), y(1971, 25));
        assertThat(changeTsUnit(ts, YEAR, First, true)).containsExactly(y(1970, 13));

        assertThat(changeTsUnit(ts, YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24), y(1971, 25));
        assertThat(changeTsUnit(ts, YEAR, Last, true)).containsExactly(y(1970, 24));

        assertThat(changeTsUnit(ts, QUARTER, First, false)).startsWith(q1(1969, 2), q2(1969, 4)).hasSize(9);
        assertThat(changeTsUnit(ts, QUARTER, First, true)).startsWith(q2(1969, 4), q3(1969, 7)).hasSize(7);

        TsData ts11 = monthlyTs(EPOCH.minusYears(1), 11);
        assertThat(changeTsUnit(ts11, YEAR, First, false)).containsExactly(y(1969, 1));
        assertThat(changeTsUnit(ts11, YEAR, First, true)).isEmpty();
    }

    private static TsData monthlyTs(LocalDateTime start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), DoubleSequence.of(count, i -> i + start.getMonthValue()));
    }

    private static TsData monthlyTs(LocalDate start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), DoubleSequence.of(count, i -> i + start.getMonthValue()));
    }

    private static TsObservation y(int year, double val) {
        return new TsObservation(TsPeriod.yearly(year), val);
    }

    private static TsObservation q1(int year, double val) {
        return new TsObservation(TsPeriod.quarterly(year, 1), val);
    }

    private static TsObservation q2(int year, double val) {
        return new TsObservation(TsPeriod.quarterly(year, 2), val);
    }

    private static TsObservation q3(int year, double val) {
        return new TsObservation(TsPeriod.quarterly(year, 3), val);
    }
}

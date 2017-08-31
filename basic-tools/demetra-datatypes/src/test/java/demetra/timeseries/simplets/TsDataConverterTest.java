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
import demetra.timeseries.TsUnit;
import static demetra.timeseries.TsUnit.*;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static demetra.timeseries.simplets.TsDataConverter.changeTsUnit;

/**
 *
 * @author Philippe Charles
 */
public class TsDataConverterTest {

    @Test
    public void testNoRatio() {
        TsData ts = monthlyTs(LocalDate.of(2010, 1, 1), 24);
        assertThat(changeTsUnit(ts, DAILY, First, true)).isNull();
    }

    @Test
    public void testNoChange() {
        TsData ts = monthlyTs(LocalDate.of(2010, 1, 1), 24);
        assertThat(changeTsUnit(ts, MONTHLY, First, true)).isEqualTo(ts);
    }

    @Test
    public void testCompleteAfterEpoch() {
        TsData ts = monthlyTs(LocalDate.of(2010, 1, 1), 24);

        assertThat(changeTsUnit(ts, YEARLY, First, false)).containsExactly(y(2010, 1), y(2011, 13));
        assertThat(changeTsUnit(ts, YEARLY, First, true)).containsExactly(y(2010, 1), y(2011, 13));

        assertThat(changeTsUnit(ts, YEARLY, Last, false)).containsExactly(y(2010, 12), y(2011, 24));
        assertThat(changeTsUnit(ts, YEARLY, Last, true)).containsExactly(y(2010, 12), y(2011, 24));

        assertThat(changeTsUnit(ts, QUARTERLY, First, false)).startsWith(q(2010, 1, 1), q(2010, 2, 4)).hasSize(8);
        assertThat(changeTsUnit(ts, QUARTERLY, First, true)).startsWith(q(2010, 1, 1), q(2010, 2, 4)).hasSize(8);
    }

    @Test
    public void testIncompleteAfterEpoch() {
        TsData ts = monthlyTs(LocalDate.of(2010, 2, 1), 24);

        assertThat(changeTsUnit(ts, YEARLY, First, false)).containsExactly(y(2010, 2), y(2011, 13), y(2012, 25));
        assertThat(changeTsUnit(ts, YEARLY, First, true)).containsExactly(y(2011, 13));

        assertThat(changeTsUnit(ts, YEARLY, Last, false)).containsExactly(y(2010, 12), y(2011, 24), y(2012, 25));
        assertThat(changeTsUnit(ts, YEARLY, Last, true)).containsExactly(y(2011, 24));

        assertThat(changeTsUnit(ts, QUARTERLY, First, false)).startsWith(q(2010, 1, 2), q(2010, 2, 4)).hasSize(9);
        assertThat(changeTsUnit(ts, QUARTERLY, First, true)).startsWith(q(2010, 2, 4), q(2010, 3, 7)).hasSize(7);
    }

    @Test
    public void testCompleteBeforeEpoch() {
        TsData ts = monthlyTs(TsPeriod.EPOCH.toLocalDate().minusYears(1), 24);

        assertThat(changeTsUnit(ts, YEARLY, First, false)).containsExactly(y(1969, 1), y(1970, 13));
        assertThat(changeTsUnit(ts, YEARLY, First, true)).containsExactly(y(1969, 1), y(1970, 13));

        assertThat(changeTsUnit(ts, YEARLY, Last, false)).containsExactly(y(1969, 12), y(1970, 24));
        assertThat(changeTsUnit(ts, YEARLY, Last, true)).containsExactly(y(1969, 12), y(1970, 24));

        assertThat(changeTsUnit(ts, QUARTERLY, First, false)).startsWith(q(1969, 1, 1), q(1969, 2, 4)).hasSize(8);
        assertThat(changeTsUnit(ts, QUARTERLY, First, true)).startsWith(q(1969, 1, 1), q(1969, 2, 4)).hasSize(8);
    }

    @Test
    public void testIncompleteBeforeEpoch() {
        TsData ts = monthlyTs(TsPeriod.EPOCH.toLocalDate().minusYears(1).plusMonths(1), 24);

        assertThat(changeTsUnit(ts, YEARLY, First, false)).containsExactly(y(1969, 2), y(1970, 13), y(1971, 25));
        assertThat(changeTsUnit(ts, YEARLY, First, true)).containsExactly(y(1970, 13));

        assertThat(changeTsUnit(ts, YEARLY, Last, false)).containsExactly(y(1969, 12), y(1970, 24), y(1971, 25));
        assertThat(changeTsUnit(ts, YEARLY, Last, true)).containsExactly(y(1970, 24));

        assertThat(changeTsUnit(ts, QUARTERLY, First, false)).startsWith(q(1969, 1, 2), q(1969, 2, 4)).hasSize(9);
        assertThat(changeTsUnit(ts, QUARTERLY, First, true)).startsWith(q(1969, 2, 4), q(1969, 3, 7)).hasSize(7);
    }

    private static TsData monthlyTs(LocalDate start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTHLY, start), DoubleSequence.of(count, i -> i + start.getMonthValue()));
    }

    private static TsObservation y(int year, double val) {
        return new TsObservation(TsPeriod.yearly(year), val);
    }

    private static TsObservation q(int year, int quarter, double val) {
        return new TsObservation(TsPeriod.quarterly(year, quarter), val);
    }
}

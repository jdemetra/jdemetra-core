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
package demetra.timeseries;

import static demetra.timeseries.TsDataTable.DistributionType.*;
import demetra.timeseries.TsDataTable.ValueStatus;
import static demetra.timeseries.TsDataTable.ValueStatus.*;
import static demetra.timeseries.TsDataTable.computeDomain;
import static java.lang.Double.NaN;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.stream.Stream;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class TsDataTableTest {

    @Test
    public void testComputeDomain() {
        assertThat(computeUnit("P14M", "P14M"))
                .as("same chrono, same amount")
                .isEqualTo("P14M");

        assertThat(computeUnit("P14M", "P7M"))
                .as("same chrono, compatible amount")
                .isEqualTo("P7M");

        assertThat(computeUnit("P14M", "P12M"))
                .as("same chrono, uncompatible amount")
                .isEqualTo("P2M");

        assertThat(computeUnit("P2Y", "P2M"))
                .as("compatible chrono, same amount")
                .isEqualTo("P2M");

        assertThat(computeUnit("P2Y", "P12M"))
                .as("compatible chrono, compatible amount")
                .isEqualTo("P12M");

        assertThat(computeUnit("P2Y", "P26M"))
                .as("compatible chrono, uncompatible amount")
                .isEqualTo("P2M");

        assertThat(computeUnit("P2M", "P2D"))
                .as("uncompatible chrono, same amount")
                .isEqualTo("P1D");

        assertThat(computeUnit("P2M", "P10D"))
                .as("uncompatible chrono, compatible amount")
                .isEqualTo("P1D");

        assertThat(computeUnit("P2M", "P11D"))
                .as("uncompatible chrono, uncompatible amount")
                .isEqualTo("P1D");
    }

    @Test
    public void testCursorValue() {
        TsDataTable table = TsDataTable.of(asList(
                TsData.ofInternal(P1M_JAN2010, new double[]{1.1, Double.NaN, 1.3}),
                TsData.ofInternal(P3M_OCT2009, new double[]{2.1})
        ));

        assertThat(Cell.toArray(table.cursor(FIRST)))
                .containsExactly(
                        new Cell[][]{
                            {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, PRESENT, 2.1)},
                            {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN)},
                            {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN)},
                            {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN)}
                        });

        assertThat(Cell.toArray(table.cursor(MIDDLE)))
                .containsExactly(
                        new Cell[][]{
                            {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN)},
                            {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, PRESENT, 2.1)},
                            {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN)},
                            {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN)}
                        });

        assertThat(Cell.toArray(table.cursor(LAST)))
                .containsExactly(
                        new Cell[][]{
                            {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN)},
                            {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN)},
                            {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, PRESENT, 2.1)},
                            {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN)},
                            {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN)}
                        });
    }

    private static String computeUnit(String... periods) {
        Iterator<TsDomain> domains = Stream.of(periods).map(o -> TsDomain.of(TsPeriod.of(TsUnit.parse(o), 0), 1)).iterator();
        return computeDomain(domains).getStartPeriod().getUnit().toIsoString();
    }

    private static final TsPeriod P1M_JAN2010 = TsPeriod.monthly(2010, 1);
    private static final TsPeriod P3M_OCT2009 = TsPeriod.quarterly(2009, 4);

    @lombok.Value(staticConstructor = "of")
    private static class Cell {

        private int index;
        private int windowLength;
        private int windowIndex;
        @lombok.NonNull
        private ValueStatus status;
        private double value;

        static Cell[][] toArray(TsDataTable.Cursor c) {
            Cell[][] result = new Cell[c.getPeriodCount()][c.getSeriesCount()];
            for (int i = 0; i < c.getPeriodCount(); i++) {
                for (int j = 0; j < c.getSeriesCount(); j++) {
                    c.moveTo(i, j);
                    result[i][j] = Cell.of(c.getIndex(), c.getWindowLength(), c.getWindowIndex(), c.getStatus(), c.getValue());
                }
            }
            return result;
        }
    }
}

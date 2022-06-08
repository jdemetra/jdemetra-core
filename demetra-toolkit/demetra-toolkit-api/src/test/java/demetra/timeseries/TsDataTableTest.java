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

import demetra.timeseries.TsDataTable.ValueStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static demetra.timeseries.TsDataTable.DistributionType.*;
import static demetra.timeseries.TsDataTable.ValueStatus.*;
import static demetra.timeseries.TsDataTable.computeDomain;
import static java.lang.Double.NaN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;

/**
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
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(null));

        assertThat(TsDataTable.of(Collections.emptyList()))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(TsDomain.DEFAULT_EMPTY, Collections.emptyList());

        assertThat(TsDataTable.of(asList(empty, empty)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(TsDomain.DEFAULT_EMPTY, asList(empty, empty));

        assertThat(TsDataTable.of(asList(p1m_jan2010, empty)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(p1m_jan2010.getDomain(), asList(p1m_jan2010, empty));

        assertThat(TsDataTable.of(asList(empty, p1m_jan2010)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(p1m_jan2010.getDomain(), asList(empty, p1m_jan2010));
    }

    @Test
    @SuppressWarnings("null")
    public void testCursorValue() {
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(Collections.emptyList()).cursor((TsDataTable.DistributionType) null));
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(Collections.emptyList()).cursor((IntFunction<TsDataTable.DistributionType>) null));

        assertThat(Cell.toArray(TsDataTable.of(Collections.emptyList()).cursor(FIRST))).isEmpty();
        assertThat(Cell.toArray(TsDataTable.of(asList(empty)).cursor(FIRST))).isEmpty();

        TsDataTable table = TsDataTable.of(asList(p1m_jan2010, p3m_oct2009, empty));

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(0, -1));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(-1, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(0, 3));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(6, 0));

        assertDeepEqualTo(Cell.toArray(table.cursor(FIRST)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });

        assertDeepEqualTo(Cell.toArray(table.cursor(MIDDLE)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });

        assertDeepEqualTo(Cell.toArray(table.cursor(LAST)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });
    }

    private static String computeUnit(String... periods) {
        Iterator<TsDomain> domains = Stream.of(periods).map(o -> TsDomain.of(TsPeriod.of(TsUnit.parse(o), 0), 1)).iterator();
        return computeDomain(domains).getStartPeriod().getUnit().toISO8601();
    }

    private final TsData p1m_jan2010 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1.1, Double.NaN, 1.3});
    private final TsData p3m_oct2009 = TsData.ofInternal(TsPeriod.quarterly(2009, 4), new double[]{2.1});
    private final TsData empty = TsData.empty("empty");

    @lombok.Value(staticConstructor = "of")
    private static class Cell {

        static final Cell EMPTY = Cell.of(-1, -1, -1, TsDataTable.ValueStatus.EMPTY, NaN);

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

    private void assertDeepEqualTo(Object[][] actual, Object[][] expected) {
        // workaround of bug in assertj 3.17.0
//        assertThat(actual).isDeepEqualTo(expected);
        assertThat(Arrays.deepEquals(actual, expected));
    }
}

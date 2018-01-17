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
    public void testCursor() {
        TsDataTable table = TsDataTable.of(asList(
                TsData.ofInternal(P1M_JAN2010, new double[]{1.1, Double.NaN, 1.3}),
                TsData.ofInternal(P3M_OCT2009, new double[]{2.1})
        ));

        assertThat(toValues(table.cursor(FIRST)))
                .containsExactly(
                        new double[][]{
                            {NaN, 2.1},
                            {NaN, NaN},
                            {NaN, NaN},
                            {1.1, NaN},
                            {NaN, NaN},
                            {1.3, NaN}
                        });

        assertThat(toStatuses(table.cursor(FIRST)))
                .containsExactly(
                        new TsDataTable.ValueStatus[][]{
                            {OUTSIDE, PRESENT},
                            {OUTSIDE, EMPTY},
                            {OUTSIDE, EMPTY},
                            {PRESENT, OUTSIDE},
                            {MISSING, OUTSIDE},
                            {PRESENT, OUTSIDE}
                        });

        assertThat(toValues(table.cursor(LAST)))
                .containsExactly(
                        new double[][]{
                            {NaN, NaN},
                            {NaN, NaN},
                            {NaN, 2.1},
                            {1.1, NaN},
                            {NaN, NaN},
                            {1.3, NaN}
                        });

        assertThat(toStatuses(table.cursor(LAST)))
                .containsExactly(
                        new TsDataTable.ValueStatus[][]{
                            {OUTSIDE, EMPTY},
                            {OUTSIDE, EMPTY},
                            {OUTSIDE, PRESENT},
                            {PRESENT, OUTSIDE},
                            {MISSING, OUTSIDE},
                            {PRESENT, OUTSIDE}
                        });

        assertThat(toValues(table.cursor(MIDDLE)))
                .containsExactly(
                        new double[][]{
                            {NaN, NaN},
                            {NaN, 2.1},
                            {NaN, NaN},
                            {1.1, NaN},
                            {NaN, NaN},
                            {1.3, NaN}
                        });

        assertThat(toStatuses(table.cursor(MIDDLE)))
                .containsExactly(
                        new TsDataTable.ValueStatus[][]{
                            {OUTSIDE, EMPTY},
                            {OUTSIDE, PRESENT},
                            {OUTSIDE, EMPTY},
                            {PRESENT, OUTSIDE},
                            {MISSING, OUTSIDE},
                            {PRESENT, OUTSIDE}
                        });
    }

    static TsDomain dom(String period, int length) {
        return TsDomain.of(TsPeriod.parse(period), length);
    }

    static double[][] toValues(TsDataTable.Cursor c) {
        double[][] result = new double[c.getPeriodCount()][c.getSeriesCount()];
        c.forEachByPeriod((i, j, s, v) -> result[i][j] = v);
        return result;
    }

    static TsDataTable.ValueStatus[][] toStatuses(TsDataTable.Cursor c) {
        TsDataTable.ValueStatus[][] result = new TsDataTable.ValueStatus[c.getPeriodCount()][c.getSeriesCount()];
        c.forEachByPeriod((i, j, s, v) -> result[i][j] = s);
        return result;
    }

    static String computeUnit(String... periods) {
        Iterator<TsDomain> domains = Stream.of(periods).map(o -> TsDomain.of(TsPeriod.of(TsUnit.parse(o), 0), 1)).iterator();
        return computeDomain(domains).getStartPeriod().getUnit().toIsoString();
    }

    static final TsPeriod P1M_JAN2010 = TsPeriod.monthly(2010, 1);
    static final TsPeriod P3M_OCT2009 = TsPeriod.quarterly(2009, 4);
}

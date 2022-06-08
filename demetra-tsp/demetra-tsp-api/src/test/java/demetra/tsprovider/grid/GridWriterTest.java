/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.01 or - as soon they will be approved
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
package demetra.tsprovider.grid;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.tsprovider.util.ObsFormat;
import org.junit.jupiter.api.Test;
import test.tsprovider.grid.ArrayGridOutput;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import static _util.FixAssertj.assertDeepEqualTo;
import static demetra.timeseries.TsUnit.MONTH;
import static demetra.timeseries.TsUnit.QUARTER;
import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import static java.lang.Double.NaN;
import static org.assertj.core.api.Assertions.assertThat;
import static test.tsprovider.grid.Data.*;

/**
 * @author Philippe Charles
 */
public class GridWriterTest {

    @Test
    public void testVertical() throws IOException {
        GridWriter.Builder opts = GridWriter.builder().layout(VERTICAL).cornerLabel("x");

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x", "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {JAN_2010, 1.01, 2.01, 3.01, null},
                        {FEB_2010, null, null, 3.02, 4.02},
                        {MAR_2010, 1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(true).build()),
                new Object[][]{
                        {"x", "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {MAR_2010, 1.03, null, null, 4.03},
                        {FEB_2010, null, null, 3.02, 4.02},
                        {JAN_2010, 1.01, 2.01, 3.01, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {JAN_2010, 1.01, 2.01, 3.01, null},
                        {FEB_2010, null, null, 3.02, 4.02},
                        {MAR_2010, 1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(true).build()),
                new Object[][]{
                        {MAR_2010, 1.03, null, null, 4.03},
                        {FEB_2010, null, null, 3.02, 4.02},
                        {JAN_2010, 1.01, 2.01, 3.01, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(false).build()),
                new Object[][]{
                        {"G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {1.01, 2.01, 3.01, null},
                        {null, null, 3.02, 4.02},
                        {1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(true).build()),
                new Object[][]{
                        {"G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {1.03, null, null, 4.03},
                        {null, null, 3.02, 4.02},
                        {1.01, 2.01, 3.01, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()),
                new Object[][]{
                        {1.01, 2.01, 3.01, null},
                        {null, null, 3.02, 4.02},
                        {1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(true).build()),
                new Object[][]{
                        {1.03, null, null, 4.03},
                        {null, null, 3.02, 4.02},
                        {1.01, 2.01, 3.01, null}
                });

        assertDeepEqualTo(toArray(empty, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x"}
                });

        assertThat(toArray(empty, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()))
                .isEmpty();

        assertDeepEqualTo(toArray(seriesWithoutData, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x", "S1", "S2"}
                });
    }

    @Test
    public void testHorizontal() throws IOException {
        GridWriter.Builder opts = GridWriter.builder().layout(HORIZONTAL).cornerLabel("x");

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x", JAN_2010, FEB_2010, MAR_2010},
                        {"G1\nS1", 1.01, null, 1.03},
                        {"G1\nS2", 2.01, null, null},
                        {"G2\nS1", 3.01, 3.02, null},
                        {"S1", null, 4.02, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(true).build()),
                new Object[][]{
                        {"x", MAR_2010, FEB_2010, JAN_2010},
                        {"G1\nS1", 1.03, null, 1.01},
                        {"G1\nS2", null, null, 2.01},
                        {"G2\nS1", null, 3.02, 3.01},
                        {"S1", 4.03, 4.02, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {JAN_2010, FEB_2010, MAR_2010},
                        {1.01, null, 1.03},
                        {2.01, null, null},
                        {3.01, 3.02, null},
                        {null, 4.02, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(true).build()),
                new Object[][]{
                        {MAR_2010, FEB_2010, JAN_2010},
                        {1.03, null, 1.01},
                        {null, null, 2.01},
                        {null, 3.02, 3.01},
                        {4.03, 4.02, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(false).build()),
                new Object[][]{
                        {"G1\nS1", 1.01, null, 1.03},
                        {"G1\nS2", 2.01, null, null},
                        {"G2\nS1", 3.01, 3.02, null},
                        {"S1", null, 4.02, 4.03}
                });
        assertDeepEqualTo(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(true).build()),
                new Object[][]{
                        {"G1\nS1", 1.03, null, 1.01},
                        {"G1\nS2", null, null, 2.01},
                        {"G2\nS1", null, 3.02, 3.01},
                        {"S1", 4.03, 4.02, null}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()),
                new Object[][]{
                        {1.01, null, 1.03},
                        {2.01, null, null},
                        {3.01, 3.02, null},
                        {null, 4.02, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(true).build()),
                new Object[][]{
                        {1.03, null, 1.01},
                        {null, null, 2.01},
                        {null, 3.02, 3.01},
                        {4.03, 4.02, null}
                });

        assertDeepEqualTo(toArray(empty, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x"}
                });

        assertThat(toArray(empty, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()))
                .isEmpty();

        assertDeepEqualTo(toArray(seriesWithoutData, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()),
                new Object[][]{
                        {"x"},
                        {"S1"},
                        {"S2"}
                });
    }

    @Test
    public void testValueTypes() throws IOException {
        GridWriter opts = GridWriter.builder().format(ObsFormat.builder().dateTimePattern("yyyy-MM-dd").numberPattern("00.00").build()).build();

        assertDeepEqualTo(toArray(sample, opts, EnumSet.allOf(GridDataType.class)),
                new Object[][]{
                        {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {JAN_2010, 1.01, 2.01, 3.01, null},
                        {FEB_2010, null, null, 3.02, 4.02},
                        {MAR_2010, 1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts, EnumSet.of(GridDataType.LOCAL_DATE_TIME)),
                new Object[][]{
                        {null, null, null, null, null},
                        {JAN_2010, null, null, null, null},
                        {FEB_2010, null, null, null, null},
                        {MAR_2010, null, null, null, null}
                });

        assertDeepEqualTo(toArray(sample, opts, EnumSet.of(GridDataType.DOUBLE)),
                new Object[][]{
                        {null, null, null, null, null},
                        {null, 1.01, 2.01, 3.01, null},
                        {null, null, null, 3.02, 4.02},
                        {null, 1.03, null, null, 4.03}
                });

        assertDeepEqualTo(toArray(sample, opts, EnumSet.of(GridDataType.STRING)),
                new Object[][]{
                        {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                        {"2010-01-01", "01.01", "02.01", "03.01", null},
                        {"2010-02-01", null, null, "03.02", "04.02"},
                        {"2010-03-01", "01.03", null, null, "04.03"}
                });

        assertDeepEqualTo(toArray(sample, opts, EnumSet.noneOf(GridDataType.class)),
                new Object[][]{
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                });
    }

    private static Object[][] toArray(TsCollection grid, GridWriter writer) throws IOException {
        return toArray(grid, writer, EnumSet.allOf(GridDataType.class));
    }

    private static Object[][] toArray(TsCollection grid, GridWriter writer, Set<GridDataType> dataTypes) throws IOException {
        ArrayGridOutput result = new ArrayGridOutput(VERTICAL, dataTypes);
        writer.write(grid, result);
        return result.getData().get(grid.getName());
    }

    private final TsCollection sample = TsCollection
            .builder()
            .item(s("G1\nS1", MONTH, 2010, 0, 1.01d, NaN, 1.03d))
            .item(s("G1\nS2", QUARTER, 2010, 0, 2.01d))
            .item(s("G2\nS1", MONTH, 2010, 0, 3.01d, 3.02d))
            .item(s("S1", MONTH, 2010, 1, 4.02d, 4.03d))
            .build();

    private final TsCollection empty = TsCollection.EMPTY;

    private final TsCollection seriesWithoutData = TsCollection
            .builder()
            .item(Ts.builder().name("S1").build())
            .item(Ts.builder().name("S2").build())
            .build();
}

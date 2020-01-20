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

import static demetra.timeseries.TsUnit.*;
import demetra.tsprovider.TsCollection;
import static demetra.tsprovider.grid.GridLayout.*;
import demetra.tsprovider.util.ObsFormat;
import java.io.IOException;
import static java.lang.Double.NaN;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import test.tsprovider.grid.ArrayGridOutput;
import static test.tsprovider.grid.Data.*;

/**
 *
 * @author Philippe Charles
 */
public class GridWriterTest {

    @Test
    public void testVertical() throws IOException {
        GridWriter.Builder opts = GridWriter.builder().layout(VERTICAL);

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {JAN_2010, 1.01, 2.01, 3.01, null},
                            {FEB_2010, null, null, 3.02, 4.02},
                            {MAR_2010, 1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {MAR_2010, 1.03, null, null, 4.03},
                            {FEB_2010, null, null, 3.02, 4.02},
                            {JAN_2010, 1.01, 2.01, 3.01, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {JAN_2010, 1.01, 2.01, 3.01, null},
                            {FEB_2010, null, null, 3.02, 4.02},
                            {MAR_2010, 1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {MAR_2010, 1.03, null, null, 4.03},
                            {FEB_2010, null, null, 3.02, 4.02},
                            {JAN_2010, 1.01, 2.01, 3.01, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {"G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {1.01, 2.01, 3.01, null},
                            {null, null, 3.02, 4.02},
                            {1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {"G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {1.03, null, null, 4.03},
                            {null, null, 3.02, 4.02},
                            {1.01, 2.01, 3.01, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {1.01, 2.01, 3.01, null},
                            {null, null, 3.02, 4.02},
                            {1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {1.03, null, null, 4.03},
                            {null, null, 3.02, 4.02},
                            {1.01, 2.01, 3.01, null}
                        });
    }

    @Test
    public void testHorizontal() throws IOException {
        GridWriter.Builder opts = GridWriter.builder().layout(HORIZONTAL);

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {null, JAN_2010, FEB_2010, MAR_2010},
                            {"G1\nS1", 1.01, null, 1.03},
                            {"G1\nS2", 2.01, null, null},
                            {"G2\nS1", 3.01, 3.02, null},
                            {"S1", null, 4.02, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(false).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {null, MAR_2010, FEB_2010, JAN_2010},
                            {"G1\nS1", 1.03, null, 1.01},
                            {"G1\nS2", null, null, 2.01},
                            {"G2\nS1", null, 3.02, 3.01},
                            {"S1", 4.03, 4.02, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {JAN_2010, FEB_2010, MAR_2010},
                            {1.01, null, 1.03},
                            {2.01, null, null},
                            {3.01, 3.02, null},
                            {null, 4.02, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(false).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {MAR_2010, FEB_2010, JAN_2010},
                            {1.03, null, 1.01},
                            {null, null, 2.01},
                            {null, 3.02, 3.01},
                            {4.03, 4.02, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {"G1\nS1", 1.01, null, 1.03},
                            {"G1\nS2", 2.01, null, null},
                            {"G2\nS1", 3.01, 3.02, null},
                            {"S1", null, 4.02, 4.03}
                        });
        assertThat(toArray(sample, opts.ignoreNames(false).ignoreDates(true).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {"G1\nS1", 1.03, null, 1.01},
                            {"G1\nS2", null, null, 2.01},
                            {"G2\nS1", null, 3.02, 3.01},
                            {"S1", 4.03, 4.02, null}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(false).build()))
                .containsExactly(
                        new Object[][]{
                            {1.01, null, 1.03},
                            {2.01, null, null},
                            {3.01, 3.02, null},
                            {null, 4.02, 4.03}
                        });

        assertThat(toArray(sample, opts.ignoreNames(true).ignoreDates(true).reverseChronology(true).build()))
                .containsExactly(
                        new Object[][]{
                            {1.03, null, 1.01},
                            {null, null, 2.01},
                            {null, 3.02, 3.01},
                            {4.03, 4.02, null}
                        });
    }

    @Test
    public void testValueTypes() throws IOException {
        GridWriter opts = GridWriter.builder().format(ObsFormat.of(Locale.ROOT, "yyyy-MM-dd", "00.00")).build();

        assertThat(toArray(sample, opts, EnumSet.allOf(GridDataType.class)))
                .containsExactly(
                        new Object[][]{
                            {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {JAN_2010, 1.01, 2.01, 3.01, null},
                            {FEB_2010, null, null, 3.02, 4.02},
                            {MAR_2010, 1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts, EnumSet.of(GridDataType.LOCAL_DATE_TIME)))
                .containsExactly(
                        new Object[][]{
                            {null, null, null, null, null},
                            {JAN_2010, null, null, null, null},
                            {FEB_2010, null, null, null, null},
                            {MAR_2010, null, null, null, null}
                        });

        assertThat(toArray(sample, opts, EnumSet.of(GridDataType.DOUBLE)))
                .containsExactly(
                        new Object[][]{
                            {null, null, null, null, null},
                            {null, 1.01, 2.01, 3.01, null},
                            {null, null, null, 3.02, 4.02},
                            {null, 1.03, null, null, 4.03}
                        });

        assertThat(toArray(sample, opts, EnumSet.of(GridDataType.STRING)))
                .containsExactly(
                        new Object[][]{
                            {null, "G1\nS1", "G1\nS2", "G2\nS1", "S1"},
                            {"2010-01-01", "01.01", "02.01", "03.01", null},
                            {"2010-02-01", null, null, "03.02", "04.02"},
                            {"2010-03-01", "01.03", null, null, "04.03"}
                        });

        assertThat(toArray(sample, opts, EnumSet.noneOf(GridDataType.class)))
                .containsExactly(
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
            .data(s("G1\nS1", MONTH, 2010, 0, 1.01d, NaN, 1.03d))
            .data(s("G1\nS2", QUARTER, 2010, 0, 2.01d))
            .data(s("G2\nS1", MONTH, 2010, 0, 3.01d, 3.02d))
            .data(s("S1", MONTH, 2010, 1, 4.02d, 4.03d))
            .build();
}

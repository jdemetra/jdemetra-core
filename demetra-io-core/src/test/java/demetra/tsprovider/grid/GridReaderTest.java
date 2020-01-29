/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.tsprovider.grid;

import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import org.junit.Test;
import static demetra.timeseries.TsUnit.MONTH;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static test.tsprovider.grid.Data.*;

/**
 *
 * @author Philippe Charles
 */
public class GridReaderTest {

    @Test
    public void testReadEmpty() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        assertThat(x.read(EMPTY))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .build());
    }

    @Test
    public void testReadHorizontal() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        for (GridInput o : new GridInput[]{HGRID, HGRID_OVERFLOW, HGRID_NULL_NAME, HGRID_CORNER_LABEL}) {
            assertThat(x.read(o)).isEqualTo(c(HORIZONTAL, s("S1", MONTH, 2010, 0, 3.14, 4.56, 7.89)));
        }

        assertThat(x.read(HGRID_UNDERFLOW))
                .isEqualTo(c(HORIZONTAL, s("S1", MONTH, 2010, 0, 3.14, 4.56)));

        assertThat(x.read(HGRID_NO_NAME))
                .isEqualTo(c(HORIZONTAL, s("Series 0", MONTH, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(HGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, HORIZONTAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .data(s("G1\nS1", MONTH, 2010, 0, 3.14, 4.56, 7.89))
                        .data(s("G1\nS2", MONTH, 2010, 0, 3, 4, 5))
                        .data(s("G2\nS1", MONTH, 2010, 0, 7, 8, 9))
                        .data(s("S1", MONTH, 2010, 0, 0, 1, 2))
                        .build());
    }

    @Test
    public void testReadVertical() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        for (GridInput o : new GridInput[]{VGRID, VGRID_OVERFLOW, VGRID_NULL_NAME, VGRID_CORNER_LABEL}) {
            assertThat(x.read(o)).isEqualTo(c(VERTICAL, s("S1", MONTH, 2010, 0, 3.14, 4.56, 7.89)));
        }

        assertThat(x.read(VGRID_UNDERFLOW))
                .isEqualTo(c(VERTICAL, s("S1", MONTH, 2010, 0, 3.14, 4.56)));

        assertThat(x.read(VGRID_NO_NAME))
                .isEqualTo(c(VERTICAL, s("Series 0", MONTH, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .data(s("G1\nS1", MONTH, 2010, 0, 3.14, 4.56, 7.89))
                        .data(s("G1\nS2", MONTH, 2010, 0, 3, 4, 5))
                        .data(s("G2\nS1", MONTH, 2010, 0, 7, 8, 9))
                        .data(s("S1", MONTH, 2010, 0, 0, 1, 2))
                        .build());
    }

    @Test
    public void testNameSeparator() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().nameSeparator("-").build();

        assertThat(x.read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .data(s("G1-S1", MONTH, 2010, 0, 3.14, 4.56, 7.89))
                        .data(s("G1-S2", MONTH, 2010, 0, 3, 4, 5))
                        .data(s("G2-S1", MONTH, 2010, 0, 7, 8, 9))
                        .data(s("S1", MONTH, 2010, 0, 0, 1, 2))
                        .build());
    }

    @Test
    public void testLayout() throws IOException {
        assertThat(GridReader.builder().layout(HORIZONTAL).build().read(HGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, HORIZONTAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .data(s("G1\nS1", MONTH, 2010, 0, 3.14, 4.56, 7.89))
                        .data(s("G1\nS2", MONTH, 2010, 0, 3, 4, 5))
                        .data(s("G2\nS1", MONTH, 2010, 0, 7, 8, 9))
                        .data(s("S1", MONTH, 2010, 0, 0, 1, 2))
                        .build());

        assertThat(GridReader.builder().layout(VERTICAL).build().read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .name("")
                        .data(s("G1\nS1", MONTH, 2010, 0, 3.14, 4.56, 7.89))
                        .data(s("G1\nS2", MONTH, 2010, 0, 3, 4, 5))
                        .data(s("G2\nS1", MONTH, 2010, 0, 7, 8, 9))
                        .data(s("S1", MONTH, 2010, 0, 0, 1, 2))
                        .build());
    }
}

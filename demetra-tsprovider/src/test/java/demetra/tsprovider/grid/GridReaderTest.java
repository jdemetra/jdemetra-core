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
import static org.assertj.core.api.Assertions.assertThat;
import static test.tsprovider.grid.Data.*;

/**
 *
 * @author Philippe Charles
 */
public class GridReaderTest {

    @Test
    public void testReadHorizontal() {
        GridReader x = GridReader.of(GridImport.DEFAULT, o -> true, i -> "X" + i);

        for (GridInput o : new GridInput[]{HGRID_WITH_HEADER, HGRID_WITH_DATE_HEADER}) {
            assertThat(x.read(o)).isEqualTo(of("hello", HORIZONTAL, "S1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)));
        }

        assertThat(x.read(HGRID_WITHOUT_HEADER))
                .isEqualTo(of("hello", HORIZONTAL, "X1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(HGRID_WITH_HEADERS))
                .isEqualTo(TsCollectionGrid.builder().name("hello").layout(HORIZONTAL)
                        .item(s("G1\nS1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)))
                        .item(s("G1\nS2", data(MONTH, 2010, 0, 3, 4, 5)))
                        .item(s("G2\nS1", data(MONTH, 2010, 0, 7, 8, 9)))
                        .item(s("S1", data(MONTH, 2010, 0, 0, 1, 2)))
                        .build());
    }

    @Test
    public void testReadVertical() {
        GridReader x = GridReader.of(GridImport.DEFAULT, o -> true, i -> "X" + i);

        for (GridInput o : new GridInput[]{VGRID_WITH_HEADER, VGRID_WITH_DATE_HEADER}) {
            assertThat(x.read(o)).isEqualTo(of("hello", VERTICAL, "S1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)));
        }

        assertThat(x.read(VGRID_WITHOUT_HEADER))
                .isEqualTo(of("hello", VERTICAL, "X1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(VGRID_WITH_HEADERS))
                .isEqualTo(TsCollectionGrid.builder().name("hello").layout(VERTICAL)
                        .item(s("G1\nS1", data(MONTH, 2010, 0, 3.14, 4.56, 7.89)))
                        .item(s("G1\nS2", data(MONTH, 2010, 0, 3, 4, 5)))
                        .item(s("G2\nS1", data(MONTH, 2010, 0, 7, 8, 9)))
                        .item(s("S1", data(MONTH, 2010, 0, 0, 1, 2)))
                        .build());
    }
}

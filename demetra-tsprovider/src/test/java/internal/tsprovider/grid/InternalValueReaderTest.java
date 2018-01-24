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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;
import java.io.IOException;
import org.junit.Test;
import test.tsprovider.grid.ArrayGridInput;
import static test.tsprovider.grid.Data.*;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class InternalValueReaderTest {

    @Test
    public void testOnNull() throws IOException {
        InternalValueReader x = InternalValueReader.onNull();

        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                assertThat(x.read(grid, i, j)).isNull();
            }
        }
    }

    @Test
    public void testOnDateTime() throws IOException {
        InternalValueReader x = InternalValueReader.onDateTime();

        assertThat(x.read(grid, 0, 0)).isNull();
        assertThat(x.read(grid, 1, 0)).isNull();
        assertThat(x.read(grid, 0, 1)).isEqualTo(JAN_2010);
        assertThat(x.read(grid, 0, 2)).isEqualTo(FEB_2010);
    }

    private final Object[][] data = {
        {null, JAN_2010, FEB_2010, MAR_2010},
        {"S1", 3.14, 4.56, 7.89}
    };
    private final GridInput grid = ArrayGridInput.of(data);
}

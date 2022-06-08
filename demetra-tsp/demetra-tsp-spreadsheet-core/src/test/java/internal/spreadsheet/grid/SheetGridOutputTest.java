/*
 * Copyright 2020 National Bank of Belgium
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
package internal.spreadsheet.grid;

import demetra.tsprovider.grid.GridOutput;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class SheetGridOutputTest {

    @Test
    public void testCursor() throws IOException {
        SheetGridOutput x = SheetGridOutput.of(type -> true);

        try (GridOutput.Stream c = x.open("empty", 0, 0)) {
        }
        assertThat(x.getResult()).isEqualTo(ArraySheet.copyOf("empty", new Object[][]{}));

        try (GridOutput.Stream c = x.open("normal", 1, 2)) {
            c.writeCell("A1");
            c.writeCell("B1");
            c.writeEndOfRow();
        }
        assertThat(x.getResult()).isEqualTo(ArraySheet.copyOf("normal", new Object[][]{{"A1", "B1"}}));

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> {
            try (GridOutput.Stream c = x.open("ColumnBoundEx", 1, 2)) {
                c.writeCell("A1");
                c.writeCell("B1");
                c.writeCell("C1");
            }
        });

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> {
            try (GridOutput.Stream c = x.open("RowBoundEx", 1, 2)) {
                c.writeCell("A1");
                c.writeCell("B1");
                c.writeEndOfRow();
                c.writeCell("A2");
            }
        });

    }
}

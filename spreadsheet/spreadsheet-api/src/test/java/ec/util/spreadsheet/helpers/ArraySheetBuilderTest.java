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
package ec.util.spreadsheet.helpers;

import static ec.util.spreadsheet.helpers.ArraySheetAssert.assertThat;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArraySheetBuilderTest {

    static final String STRING = "a";
    static final Date DATE = new Date(1234);
    static final Number NUMBER = 12.34;
    static final Object UNKNOWN = new Object() {
        @Override
        public String toString() {
            return STRING;
        }
    };

    @Test
    public void testUnboundedCell() throws IOException {
        ArraySheet.Builder b = ArraySheet.builder();

        // 1. supported types
        assertThat(b.clear().value(0, 0, STRING).value(0, 1, DATE).value(1, 0, NUMBER).value(1, 1, null).build())
                .hasRowCount(2)
                .hasColumnCount(2)
                .hasValue(0, 0, STRING)
                .hasValue(0, 1, DATE)
                .hasValue(1, 0, NUMBER)
                .hasValue(1, 1, null);

        // 2. unknow type
        assertThat(b.clear().value(0, 0, UNKNOWN).build())
                .hasValue(0, 0, UNKNOWN.toString());

        // 3. value overriding
        assertThat(b.clear().value(0, 0, STRING).value(0, 0, DATE).build())
                .hasValue(0, 0, DATE);
    }

    @Test
    public void testUnboundedClear() {
        ArraySheet.Builder b = ArraySheet.builder();

        assertThat(b.clear().name("hello").value(10, 10, STRING).build())
                .hasName("hello")
                .hasRowCount(11)
                .hasColumnCount(11);

        assertThat(b.clear().build())
                .hasName("")
                .hasRowCount(0)
                .hasColumnCount(0);
    }

    @SuppressWarnings("null")
    @Test
    public void testUnboundedName() {
        ArraySheet.Builder b = ArraySheet.builder();

        assertThat(b.clear().build()).hasName("");
        assertThat(b.clear().name("hello").build()).hasName("hello");
        assertThatThrownBy(() -> b.clear().name(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testRow() {
        ArraySheet.Builder b = ArraySheet.builder();

        assertThat(b.clear().row(1, 2, Arrays.asList("A", "B")).build())
                .hasRowCount(2)
                .hasColumnCount(4)
                .hasValue(1, 2, "A")
                .hasValue(1, 3, "B");

        assertThat(b.clear().row(1, 2, Collections.emptyList()).build())
                .hasRowCount(0)
                .hasColumnCount(0);

        assertThat(b.clear().row(1, 2, Arrays.asList("A", "B").iterator()).build())
                .hasRowCount(2)
                .hasColumnCount(4)
                .hasValue(1, 2, "A")
                .hasValue(1, 3, "B");

        assertThat(b.clear().row(1, 2, new Object[]{"A", "B"}).build())
                .hasRowCount(2)
                .hasColumnCount(4)
                .hasValue(1, 2, "A")
                .hasValue(1, 3, "B");

        assertThat(b.clear().row(1, 2, new Object[]{}).build())
                .hasRowCount(0)
                .hasColumnCount(0);

        assertThat(b.clear().row(1, 2, "A", "B").build())
                .hasRowCount(2)
                .hasColumnCount(4)
                .hasValue(1, 2, "A")
                .hasValue(1, 3, "B");
    }

    @Test
    public void testColumn() {
        ArraySheet.Builder b = ArraySheet.builder();

        assertThat(b.clear().column(1, 2, Arrays.asList("A", "B")).build())
                .hasRowCount(3)
                .hasColumnCount(3)
                .hasValue(1, 2, "A")
                .hasValue(2, 2, "B");

        assertThat(b.clear().column(1, 2, Collections.emptyList()).build())
                .hasRowCount(0)
                .hasColumnCount(0);

        assertThat(b.clear().column(1, 2, Arrays.asList("A", "B").iterator()).build())
                .hasRowCount(3)
                .hasColumnCount(3)
                .hasValue(1, 2, "A")
                .hasValue(2, 2, "B");

        assertThat(b.clear().column(1, 2, new Object[]{"A", "B"}).build())
                .hasRowCount(3)
                .hasColumnCount(3)
                .hasValue(1, 2, "A")
                .hasValue(2, 2, "B");

        assertThat(b.clear().column(1, 2, new Object[]{}).build())
                .hasRowCount(0)
                .hasColumnCount(0);

        assertThat(b.clear().column(1, 2, "A", "B").build())
                .hasRowCount(3)
                .hasColumnCount(3)
                .hasValue(1, 2, "A")
                .hasValue(2, 2, "B");
    }
}

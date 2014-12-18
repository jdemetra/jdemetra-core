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

import ec.util.spreadsheet.Sheet;
import static ec.util.spreadsheet.junit.SheetMatcher.cellValueEqualTo;
import static ec.util.spreadsheet.junit.SheetMatcher.dimensionEqualTo;
import static ec.util.spreadsheet.junit.SheetMatcher.nameEqualTo;
import java.io.IOException;
import java.util.Date;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ArraySheetBuilderTest {

    final String STRING = "a";
    final Date DATE = new Date(1234);
    final Number NUMBER = 12.34;

    @Test
    public void testUnboundedCell() throws IOException {
        // 1. supported types
        Sheet s1 = ArraySheet.builder()
                .value(0, 0, STRING)
                .value(0, 1, DATE)
                .value(1, 0, NUMBER)
                .value(1, 1, null)
                .build();
        assertThat(s1, dimensionEqualTo(2, 2));
        assertThat(s1, cellValueEqualTo(0, 0, STRING));
        assertThat(s1, cellValueEqualTo(0, 1, DATE));
        assertThat(s1, cellValueEqualTo(1, 0, NUMBER));
        assertNull(s1.getCell(1, 1));
        // 2. unknow type
        Sheet s2 = ArraySheet.builder().value(0, 0, new Object() {
            @Override
            public String toString() {
                return STRING;
            }
        }).build();
        assertThat(s2, cellValueEqualTo(0, 0, STRING));
        // 3. value overriding
        Sheet s3 = ArraySheet.builder()
                .value(0, 0, STRING)
                .value(0, 0, DATE)
                .build();
        assertThat(s3, cellValueEqualTo(0, 0, DATE));
    }

    @Test
    public void testUnboundedClear() {
        ArraySheet.Builder b = ArraySheet.builder();
        b.name("hello").value(10, 10, STRING);
        assertThat(b.build(), allOf(nameEqualTo("hello"), dimensionEqualTo(11, 11)));
        b.clear();
        assertThat(b.build(), allOf(nameEqualTo(""), dimensionEqualTo(0, 0)));
    }

    @Test
    public void testUnboundedName() {
        assertThat(ArraySheet.builder().build(), nameEqualTo(""));
        assertThat(ArraySheet.builder().name("hello").build(), nameEqualTo("hello"));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testUnboundedNameNullPointerException() {
        ArraySheet.builder().name(null);
    }

}

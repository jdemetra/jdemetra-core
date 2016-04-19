/*
 * Copyright 2015 National Bank of Belgium
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
package ec.util.spreadsheet.html;

import static ec.util.spreadsheet.Assertions.*;
import ec.util.spreadsheet.helpers.ArrayBook;
import java.io.IOException;
import java.util.Optional;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class JsoupBookReaderTest {

    @Test
    public void testRead() throws IOException {
        JsoupBookReader reader = new JsoupBookReader(Optional.empty(), "");

        ArrayBook book;

        book = reader.read("<html><body>");
        assertThat(book).hasSheetCount(0);

        book = reader.read("<table>");
        assertThat(book).hasSheetCount(1);

        book = reader.read("<table></table><table>");
        assertThat(book).hasSheetCount(2);

        book = reader.read("<table><tr><td>A1</td>");
        assertThat(book.getSheet(0)).hasRowCount(1).hasColumnCount(1);

        book = reader.read("<table><tr><td>A1</td><td>B1</td>");
        assertThat(book.getSheet(0))
                .hasRowCount(1)
                .hasColumnCount(2)
                .hasCellValue(0, 0, "A1")
                .hasCellValue(0, 1, "B1");

        book = reader.read("<table><tr><td>A1</td></tr><tr><td>A2</td>");
        assertThat(book.getSheet(0))
                .hasRowCount(2)
                .hasColumnCount(1)
                .hasCellValue(0, 0, "A1")
                .hasCellValue(1, 0, "A2");

        book = reader.read("<table><tr><td></td></tr><tr><td>A2</td>");
        assertThat(book.getSheet(0))
                .hasRowCount(2)
                .hasColumnCount(1)
                .hasCellValue(0, 0, null)
                .hasCellValue(1, 0, "A2");

        book = reader.read("<table><tr><td colspan=2>A1</td><td>C1</td>");
        assertThat(book.getSheet(0))
                .hasRowCount(1)
                .hasColumnCount(3)
                .hasCellValue(0, 0, "A1")
                .hasCellValue(0, 2, "C1");

        book = reader.read("<table><tr><td>A1</td><td rowspan=2>B1</td></tr> <tr><td>A2</td></tr> <tr><td>A3</td><td>B3</td></tr>");
        assertThat(book.getSheet(0))
                .hasRowCount(3)
                .hasColumnCount(2)
                .hasCellValue(0, 0, "A1")
                .hasCellValue(0, 1, "B1")
                .hasCellValue(1, 0, "A2")
                .hasCellValue(1, 1, null)
                .hasCellValue(2, 0, "A3")
                .hasCellValue(2, 1, "B3");
    }
}

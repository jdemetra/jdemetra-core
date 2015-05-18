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
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.jsoup.Jsoup;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class JsoupBookReaderTest {

    private static ArrayBook read(@Nonnull String html) {
        return JsoupBookReader.read(Jsoup.parse(html));
    }

    @Test
    public void testRead() throws IOException {
        ArrayBook book;
        ArraySheet sheet;

        book = read("<html><body>");
        assertThat(book).hasSheetCount(0);

        book = read("<table>");
        assertThat(book).hasSheetCount(1);

        book = read("<table></table><table>");
        assertThat(book).hasSheetCount(2);

        book = read("<table><tr><td>A1</td>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(1).hasColumnCount(1);

        book = read("<table><tr><td>A1</td><td>B1</td>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(1).hasColumnCount(2);
        assertThat(sheet.getCell(0, 0)).hasString("A1");
        assertThat(sheet.getCell(0, 1)).hasString("B1");

        book = read("<table><tr><td>A1</td></tr><tr><td>A2</td>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(2).hasColumnCount(1);
        assertThat(sheet.getCell(0, 0)).hasString("A1");
        assertThat(sheet.getCell(1, 0)).hasString("A2");

        book = read("<table><tr><td></td></tr><tr><td>A2</td>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(2).hasColumnCount(1);
        assertThat(sheet.getCell(0, 0)).isNull();
        assertThat(sheet.getCell(1, 0)).hasString("A2");

        book = read("<table><tr><td colspan=2>A1</td><td>C1</td>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(1).hasColumnCount(3);
        assertThat(sheet.getCell(0, 0)).hasString("A1");
        assertThat(sheet.getCell(0, 2)).hasString("C1");

        book = read("<table><tr><td>A1</td><td rowspan=2>B1</td></tr> <tr><td>A2</td></tr> <tr><td>A3</td><td>B3</td></tr>");
        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(3).hasColumnCount(2);
        assertThat(sheet.getCell(0, 0)).hasString("A1");
        assertThat(sheet.getCell(0, 1)).hasString("B1");
        assertThat(sheet.getCell(1, 0)).hasString("A2");
        assertThat(sheet.getCell(1, 1)).isNull();
        assertThat(sheet.getCell(2, 0)).hasString("A3");
        assertThat(sheet.getCell(2, 1)).hasString("B3");
    }
}

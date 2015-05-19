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
package ec.util.spreadsheet.html;

import static ec.util.spreadsheet.Assertions.*;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.IOException;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jsoup.Jsoup;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamBookWriterTest {

    @Test
    public void test() throws XMLStreamException, IOException {
        ArrayBook book;
        ArraySheet sheet;

        ArrayBook input = ArraySheet.builder().name("hello").table(0, 0, new Object[][]{{"A1", "B1", "C1"}, {"A2", "B2"}}).build().toBook();

        StringWriter html = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter x = xof.createXMLStreamWriter(html);
        XMLStreamBookWriter.write(x, input, new SimpleDateFormat(), NumberFormat.getInstance());

        book = JsoupBookReader.read(Jsoup.parse(html.toString()));
        assertThat(book).hasSheetCount(1);

        sheet = book.getSheet(0);
        assertThat(sheet).hasRowCount(2).hasColumnCount(3).hasName("hello");
        assertThat(sheet.getCell(0, 0)).isString().hasString("A1");
        assertThat(sheet.getCell(0, 1)).isString().hasString("B1");
        assertThat(sheet.getCell(0, 2)).isString().hasString("C1");
        assertThat(sheet.getCell(1, 0)).isString().hasString("A2");
        assertThat(sheet.getCell(1, 1)).isString().hasString("B2");
        assertThat(sheet.getCell(1, 2)).isNull();
    }
}

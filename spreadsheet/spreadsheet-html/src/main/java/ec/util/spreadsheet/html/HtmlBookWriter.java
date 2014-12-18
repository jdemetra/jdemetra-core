/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.CellRefHelper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
class HtmlBookWriter {

    private final DateFormat periodFormatter;
    private final NumberFormat valueFormatter;

    public HtmlBookWriter(DateFormat periodFormatter, NumberFormat valueFormatter) {
        this.periodFormatter = periodFormatter;
        this.valueFormatter = valueFormatter;
    }

    private String getStyleContent() throws IOException {
        StringBuilder result = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(HtmlBookWriter.class.getResourceAsStream("/Excel2007.css"), StandardCharsets.UTF_8)) {
            CharBuffer buf = CharBuffer.allocate(0x800);
            while (reader.read(buf) != -1) {
                buf.flip();
                result.append(buf);
                buf.clear();
            }
        }
        return result.toString();
    }

    public void write(XMLStreamWriter w, Book book) throws IOException, XMLStreamException {
        BasicHtmlWriter f = new BasicHtmlWriter(w);
        f.beginHtml();
        f.beginHead();
        f.writeStyle(getStyleContent());
        f.endHead();
        f.beginBody();
        int sheetCount = book.getSheetCount();
        for (int s = 0; s < sheetCount; s++) {
            Sheet sheet = book.getSheet(s);
            f.beginTable(sheet.getName(), "sheet");
            int rowCount = sheet.getRowCount();
            int columnCount = sheet.getColumnCount();
            // headers
            f.beginRow();
            f.writeCell("", true, "first");
            for (int j = 0; j < columnCount; j++) {
                f.writeCell(CellRefHelper.getColumnLabel(j), true, "col");
            }
            f.endRow();
            // content
            for (int i = 0; i < rowCount; i++) {
                f.beginRow();
                f.writeCell(String.valueOf(i + 1), true, "row");
                for (int j = 0; j < columnCount; j++) {
                    Cell cell = sheet.getCell(i, j);
                    if (cell != null) {
                        if (cell.isDate()) {
                            f.writeCell(periodFormatter.format(cell.getDate()), false, "type-date");
                        } else if (cell.isNumber()) {
                            f.writeCell(valueFormatter.format(cell.getNumber()), false, "type-number");
                        } else if (cell.isString()) {
                            f.writeCell(cell.getString(), false);
                        }
                    } else {
                        f.writeCell("", false);
                    }
                }
                f.endRow();
            }
            f.endTable();
        }
        f.endBody();
        f.endHtml();
    }
}

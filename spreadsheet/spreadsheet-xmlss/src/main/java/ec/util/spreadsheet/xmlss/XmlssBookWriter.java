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
package ec.util.spreadsheet.xmlss;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
final class XmlssBookWriter {

    private final XMLOutputFactory xof;
    private final Charset charset;

    public XmlssBookWriter(@Nonnull XMLOutputFactory xof, @Nonnull Charset charset) {
        this.xof = xof;
        this.charset = charset;
    }

    public void write(@Nonnull OutputStream stream, @Nonnull Book book) throws IOException {
        try {
            XMLStreamWriter w = xof.createXMLStreamWriter(stream, charset.name());
            try {
                write(new BasicXmlssWriter(w), book);
            } finally {
                w.close();
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static void write(BasicXmlssWriter f, Book book) throws IOException, XMLStreamException {
        f.beginWorkbook();
        int sheetCount = book.getSheetCount();
        for (int s = 0; s < sheetCount; s++) {
            write(f, book.getSheet(s));
        }
        f.endWorkbook();
    }

    private static void write(BasicXmlssWriter f, Sheet sheet) throws XMLStreamException {
        f.beginWorksheet(sheet.getName());
        f.beginTable();
        int rowCount = sheet.getRowCount();
        int columnCount = sheet.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            f.beginRow();
            for (int j = 0; j < columnCount; j++) {
                write(f, sheet.getCell(i, j));
            }
            f.endRow();
        }
        f.endTable();
        f.endWorksheet();
    }

    private static void write(BasicXmlssWriter f, Cell cell) throws XMLStreamException {
        if (cell != null) {
            if (cell.isDate()) {
                f.writeCell(cell.getDate());
            } else if (cell.isNumber()) {
                f.writeCell(cell.getDouble());
            } else if (cell.isString()) {
                f.writeCell(cell.getString());
            }
        } else {
            f.writeCell();
        }
    }
    //</editor-fold>
}

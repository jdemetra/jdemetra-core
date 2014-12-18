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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
final class BasicXmlssWriter {

    private final XMLStreamWriter writer;
    private final NumberFormat numberFormat;
    private final DateFormat dateFormat;
    private int cellIndex;
    private boolean needIndex;

    public BasicXmlssWriter(XMLStreamWriter writer) {
        this.writer = writer;
        numberFormat = NumberFormat.getNumberInstance(Locale.ROOT);
        numberFormat.setMaximumFractionDigits(9);
        numberFormat.setMaximumIntegerDigits(12);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public void beginWorkbook() throws XMLStreamException {
//        writer.writeStartDocument("utf-8", "1.0");
        writer.writeStartDocument();
        writer.writeStartElement("Workbook");
        writer.writeAttribute("xmlns", "urn:schemas-microsoft-com:office:spreadsheet");
        writer.writeAttribute("xmlns:o", "urn:schemas-microsoft-com:office:office");
        writer.writeAttribute("xmlns:x", "urn:schemas-microsoft-com:office:excel");
        writer.writeAttribute("xmlns:ss", "urn:schemas-microsoft-com:office:spreadsheet");
        writer.writeAttribute("xmlns:html", "http://www.w3.org/TR/REC-html40");
        writer.writeStartElement("Styles");
        writer.writeStartElement("Style");
        writer.writeAttribute("ss:ID", "s24");
        writer.writeStartElement("NumberFormat");
        writer.writeAttribute("ss:Format", "Short Date");
        writer.writeEndElement(); // NumberFormat
        writer.writeEndElement(); // style
        writer.writeEndElement(); // styles
    }

    public void endWorkbook() throws XMLStreamException {
        writer.writeEndElement(); // workbook
        writer.writeEndDocument();
        writer.flush();
    }

    public void beginWorksheet(String name) throws XMLStreamException {
        writer.writeStartElement("Worksheet");
        writer.writeAttribute("ss:Name", name);
    }

    public void endWorksheet() throws XMLStreamException {
        writer.writeEndElement(); // Worksheet
        writer.flush();
    }

    public void beginTable() throws XMLStreamException {
        writer.writeStartElement("Table");
    }

    public void endTable() throws XMLStreamException {
        writer.writeEndElement(); // table
    }

    public void beginRow() throws XMLStreamException {
        writer.writeStartElement("Row");
        cellIndex = 0;
        needIndex = false;
    }

    public void endRow() throws XMLStreamException {
        writer.writeEndElement(); // row
    }

    public void writeCell(Date date) throws XMLStreamException {
        cellIndex++;
        writer.writeStartElement("Cell");
        if (needIndex) {
            writer.writeAttribute("ss:Index", Integer.toString(cellIndex));
        }
        writer.writeAttribute("ss:StyleID", "s24");
        writer.writeStartElement("Data");
        writer.writeAttribute("ss:Type", "DateTime");
        String sd = dateFormat.format(date);
        writer.writeCharacters(sd);
        writer.writeEndElement(); // data
        writer.writeEndElement(); // cell
        needIndex = false;        
    }

    public void writeCell(double val) throws XMLStreamException {
        cellIndex++;
        writer.writeStartElement("Cell");
        if (needIndex) {
            writer.writeAttribute("ss:Index", Integer.toString(cellIndex));
        }
        writer.writeStartElement("Data");
        if (!Double.isNaN(val)) {
            writer.writeAttribute("ss:Type", "Number");
            writer.writeCharacters(numberFormat.format(val));
        } else {
            writer.writeAttribute("ss:Type", "String");
            writer.writeCharacters("");
        }
        writer.writeEndElement(); // data
        writer.writeEndElement(); // cell
        needIndex = false;        
    }

    public void writeCell(String txt) throws XMLStreamException {
        cellIndex++;
        writer.writeStartElement("Cell");
        if (needIndex) {
            writer.writeAttribute("ss:Index", Integer.toString(cellIndex));
        }
        writer.writeStartElement("Data");
        writer.writeAttribute("ss:Type", "String");
        writer.writeCharacters(txt != null ? txt : "");
        writer.writeEndElement(); // data
        writer.writeEndElement(); // cell
        needIndex = false;        
    }
    
    public void writeCell() {
        cellIndex++;
        needIndex = true;        
    }
}

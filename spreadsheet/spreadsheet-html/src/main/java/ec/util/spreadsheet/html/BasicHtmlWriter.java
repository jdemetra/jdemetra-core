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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
class BasicHtmlWriter {

    private final XMLStreamWriter writer;

    public BasicHtmlWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    private String joinStyles(String... styles) {
        StringBuilder result = new StringBuilder();
        for (String o : styles) {
            result.append(o).append(' ');
        }
        return result.toString();
    }

    public void beginHtml() throws XMLStreamException {
//        writer.writeStartDocument("utf-8", "1.0");
        writer.writeStartDocument();
        writer.writeStartElement("html");
    }

    public void endHtml() throws XMLStreamException {
        writer.writeEndElement(); // html
        writer.writeEndDocument();
        writer.flush();
    }

    public void beginHead() throws XMLStreamException {
        writer.writeStartElement("head");
        writer.writeEmptyElement("meta");
        writer.writeAttribute("charset", "utf-8");
    }

    public void endHead() throws XMLStreamException {
        writer.writeEndElement(); // head
    }

    public void writeStyle(String content) throws XMLStreamException {
        writer.writeStartElement("style");
        writer.writeAttribute("media", "screen");
        writer.writeAttribute("type", "text/css");
        writer.writeCharacters(content);
        writer.writeEndElement();
    }

    public void beginBody() throws XMLStreamException {
        writer.writeStartElement("body");
    }

    public void endBody() throws XMLStreamException {
        writer.writeEndElement(); // body
    }

    public void writeCaption(String text) throws XMLStreamException {
        writer.writeStartElement("caption");
        writer.writeCharacters(text);
        writer.writeEndElement(); // caption
    }

    public void beginTable(String name, String... styles) throws XMLStreamException {
        writer.writeStartElement("table");
        writer.writeAttribute("id", name);
        if (styles.length > 0) {
            writer.writeAttribute("class", joinStyles(styles));
        }
        writeCaption(name);
    }

    public void endTable() throws XMLStreamException {
        writer.writeEndElement(); // table
        writer.flush();
    }

    public void beginRow() throws XMLStreamException {
        writer.writeStartElement("tr");
    }

    public void endRow() throws XMLStreamException {
        writer.writeEndElement(); // row
    }

    public void writeCell(CharSequence characters, boolean header, String... styles) throws XMLStreamException {
        if (characters.length() > 0) {
            writer.writeStartElement(header ? "th" : "td");
            if (styles.length > 0) {
                writer.writeAttribute("class", joinStyles(styles));
            }
            writer.writeCharacters(characters.toString());
            writer.writeEndElement(); // cell
        } else {
            writer.writeEmptyElement(header ? "th" : "td");
            if (styles.length > 0) {
                writer.writeAttribute("class", joinStyles(styles));
            }
        }
    }
}

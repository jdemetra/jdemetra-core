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
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Philippe Charles
 */
final class XmlssBook extends Book {

    private final List<Sheet> sheets;

    public XmlssBook(InputStream stream) throws IOException {
        BookSax2EventHandler handler = new BookSax2EventHandler();
        parse(handler, stream);
        this.sheets = handler.build();
    }

    private static void parse(ContentHandler handler, InputStream stream) throws IOException {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(stream));
        } catch (SAXException ex) {
            throw new RuntimeException("While parsing xml", ex);
        }
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Sheet getSheet(int index) throws IOException {
        return sheets.get(index);
    }

//    @VisibleForTesting
    static final class BookSax2EventHandler extends DefaultHandler /*implements IBuilder<ImmutableList<Sheet>>*/ {

        private static final String SS_URI = "urn:schemas-microsoft-com:office:spreadsheet";
        private static final String WORKSHEET_TAG = "Worksheet";
        private static final String ROW_TAG = "Row";
        private static final String CELL_TAG = "Cell";
        private static final String DATA_TAG = "Data";
        //
        private final List<Sheet> sheets;
        private int rowNum;
        private int colNum;
        private String dataType;
        private String text;
        private final XmlssSheetBuilder builder;

        public BookSax2EventHandler() {
            this.sheets = new ArrayList<>();
            this.rowNum = -1;
            this.colNum = -1;
            this.dataType = null;
            this.text = null;
            this.builder = XmlssSheetBuilder.create();
        }

        public List<Sheet> build() {
            return new ArrayList<>(sheets);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case WORKSHEET_TAG:
                    builder.name(attributes.getValue(SS_URI, "Name"));
                    break;
                case ROW_TAG:
                    String tmpRow = attributes.getValue(SS_URI, "Index");
                    rowNum = tmpRow != null ? Integer.parseInt(tmpRow) - 1 : rowNum + 1;
                    break;
                case CELL_TAG:
                    String tmpCol = attributes.getValue(SS_URI, "Index");
                    colNum = tmpCol != null ? Integer.parseInt(tmpCol) - 1 : colNum + 1;
                    break;
                case DATA_TAG:
                    dataType = attributes.getValue(SS_URI, "Type");
                    text = "";
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case WORKSHEET_TAG:
                    sheets.add(builder.build());
                    rowNum = -1;
                    builder.clear();
                    break;
                case ROW_TAG:
                    colNum = -1;
                    break;
                case CELL_TAG:
                    dataType = null;
                    break;
                case DATA_TAG:
                    builder.put(text, dataType, rowNum, colNum);
                    text = null;
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (text != null) {
                text += new String(ch, start, length);
            }
        }
    }
}

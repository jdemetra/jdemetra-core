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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Philippe Charles
 */
public class XmlssBookFactory extends Book.Factory {

    private final XMLOutputFactory xof;

    public XmlssBookFactory() {
        this.xof = XMLOutputFactory.newInstance();
    }

    @Override
    public String getName() {
        return "XML Spreadsheet (XMLSS)";
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase(Locale.ROOT).endsWith(".xml");
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        return XmlssBook.create(createXMLReader(), stream);
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        newWriter().write(stream, book);
    }

    private XmlssBookWriter newWriter() {
        return new XmlssBookWriter(xof, StandardCharsets.UTF_8);
    }

    @Nonnull
    private static XMLReader createXMLReader() throws IOException {
        try {
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException ex) {
            throw new IOException("While creating XmlReader", ex);
        }
    }
}

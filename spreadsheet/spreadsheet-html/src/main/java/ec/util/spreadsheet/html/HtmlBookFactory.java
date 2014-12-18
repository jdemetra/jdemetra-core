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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
public class HtmlBookFactory extends Book.Factory {

    private final HtmlBookWriter bookWriter;

    public HtmlBookFactory() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ROOT);
        numberFormat.setMaximumFractionDigits(9);
        numberFormat.setMaximumIntegerDigits(12);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.bookWriter = new HtmlBookWriter(dateFormat, numberFormat);
    }

    @Override
    public String getName() {
        return "Basic Html";
    }

    @Override
    public boolean canLoad() {
        return false;
    }

    @Override
    public Book load(InputStream stream) throws IOException {
        throw new IOException("Not supported yet.");
    }

    @Override
    public boolean accept(File pathname) {
        String tmp = pathname.getName().toLowerCase(Locale.ENGLISH);;
        return tmp.endsWith(".html") || tmp.endsWith(".htm");
    }

    @Override
    public void store(OutputStream stream, Book book) throws IOException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter w = xof.createXMLStreamWriter(stream, StandardCharsets.UTF_8.name());
            bookWriter.write(w, book);
            w.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }
}

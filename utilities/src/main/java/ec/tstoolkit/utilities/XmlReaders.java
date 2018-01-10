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
package ec.tstoolkit.utilities;

import com.google.common.io.InputSupplier;
import ioutil.Sax;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Philippe Charles
 */
public final class XmlReaders {

    private XmlReaders() {
        // static class
    }

    @Deprecated
    public static void parse(ContentHandler handler, InputSupplier<? extends InputStream> supplier) throws IOException {
        XMLReader reader = Sax.createReader();
        Sax.preventXXE(reader);
        reader.setContentHandler(handler);
        parse(reader, supplier);
    }

    @Deprecated
    public static void parse(XMLReader xmlReader, InputSupplier<? extends InputStream> supplier) throws IOException {
        try (InputStream stream = supplier.getInput()) {
            parse(xmlReader, stream);
        }
    }

    public static void parse(ContentHandler handler, InputStream stream) throws IOException {
        XMLReader reader = Sax.createReader();
        Sax.preventXXE(reader);
        reader.setContentHandler(handler);
        parse(reader, stream);
    }

    public static void parse(XMLReader xmlReader, InputStream stream) throws IOException {
        try {
            xmlReader.parse(new InputSource(stream));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }
}

/*
 * Copyright 2016 National Bank of Belgium
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
package spreadsheet.xlsx.internal;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class SaxUtil {

    private SaxUtil() {
        // static class
    }

    interface ByteSource {

        InputStream openStream() throws IOException;
    }

    interface Tmp extends ContentHandler {

        default void parse(XMLReader reader, InputStream stream) throws IOException {
            doParse(reader, stream, this);
        }

        default void parse(XMLReader reader, ByteSource byteSource) throws IOException {
            try (InputStream stream = byteSource.openStream()) {
                if (stream != null) {
                    doParse(reader, stream, this);
                }
            }
        }
    }

    interface ValueHandler<T> extends ContentHandler {

        default T parse(XMLReader reader, ByteSource byteSource) throws IOException {
            try (InputStream stream = byteSource.openStream()) {
                doParse(reader, stream, this);
                return build();
            }
        }

        T build();
    }

    private static void doParse(XMLReader reader, InputStream stream, ContentHandler handler) throws IOException {
        try {
            reader.setContentHandler(handler);
            reader.parse(new InputSource(stream));
        } catch (SAXException ex) {
            throw new IOException("While parsing xml", ex);
        }
    }

    static final class SaxStringBuilder {

        private boolean enabled = false;
        private StringBuilder content = new StringBuilder();

        public SaxStringBuilder clear() {
            content = new StringBuilder();
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public SaxStringBuilder enable() {
            this.enabled = true;
            return this;
        }

        public SaxStringBuilder disable() {
            this.enabled = false;
            return this;
        }

        public CharSequence build() {
            // we defer CharSequence@toString()
            return content;
        }

        public SaxStringBuilder appendIfNeeded(char[] ch, int start, int length) {
            if (isEnabled()) {
                content.append(ch, start, length);
            }
            return this;
        }
    }
}

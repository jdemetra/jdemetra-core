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
package spreadsheet.xlsx.internal.util;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import spreadsheet.xlsx.internal.util.IOUtil.ByteSource;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class SaxUtil {

    public interface ContentRunner extends ContentHandler {

        default void runWith(XMLReader reader, InputStream stream) throws IOException, SAXException {
            parse(reader, stream, this);
        }

        default void runWith(XMLReader reader, ByteSource byteSource) throws IOException, SAXException {
            try (InputStream stream = byteSource.openStream()) {
                runWith(reader, stream);
            }
        }
    }

    public interface ContentSupplier<T> extends ContentHandler {

        default T getWith(XMLReader reader, InputStream stream) throws IOException, SAXException {
            parse(reader, stream, this);
            return build();
        }

        default T getWith(XMLReader reader, ByteSource byteSource) throws IOException, SAXException {
            try (InputStream stream = byteSource.openStream()) {
                return getWith(reader, stream);
            }
        }

        T build();
    }

    private void parse(XMLReader reader, InputStream stream, ContentHandler handler) throws IOException, SAXException {
        reader.setContentHandler(handler);
        reader.parse(new InputSource(stream));
    }

    public static final class SaxStringBuilder {

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

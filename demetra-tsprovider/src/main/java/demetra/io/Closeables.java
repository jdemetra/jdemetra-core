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
package demetra.io;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Provides utility methods for the {@link Closeable} class and for related
 * classes.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Closeables {

    public void closeBoth(@Nonnull Closeable first, @Nonnull Closeable second) throws IOException {
        try {
            first.close();
        } catch (IOException ex) {
            try {
                second.close();
            } catch (IOException suppressed) {
                ex.addSuppressed(suppressed);
            }
            throw ex;
        }
        second.close();
    }

    @Nonnull
    public Closeable asCloseable(@Nonnull XMLStreamWriter o) {
        return () -> {
            try {
                o.close();
            } catch (XMLStreamException ex) {
                throw new IOException("While closing XMLStreamWriter", ex);
            }
        };
    }
}

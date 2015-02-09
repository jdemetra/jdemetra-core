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
package ec.util.spreadsheet.poi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//    @VisibleForTesting
abstract class ByteSource {

    @Nullable
    abstract public InputStream openStream() throws IOException;

    @Nonnull
    public static ByteSource fromURL(@Nonnull final URL url) {
        Objects.requireNonNull(url);
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return url.openStream();
            }
        };
    }

    @Nonnull
    public static ByteSource noStream() {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return null;
            }
        };
    }
}

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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class IOUtil {

    public IOException ensureClosed(@Nonnull IOException exception, @Nonnull Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException suppressed) {
            exception.addSuppressed(suppressed);
        }
        return exception;
    }

    @Nonnull
    public Optional<File> getFile(@Nonnull Path file) {
        try {
            return Optional.of(file.toFile());
        } catch (UnsupportedOperationException ex) {
            return Optional.empty();
        }
    }

    public interface ByteSource {

        @Nonnull
        InputStream openStream() throws IOException;

        @Nonnull
        static ByteSource empty() {
            return () -> EmptyInputStream.INSTANCE;
        }

        @Nonnull
        static ByteSource throwing(@Nonnull Supplier<? extends IOException> exceptionSupplier) {
            return () -> {
                throw exceptionSupplier.get();
            };
        }

        @Nonnull
        static ByteSource throwing() {
            return throwing(IOException::new);
        }
    }

    public interface ByteResource extends Closeable {

        @Nonnull
        InputStream openStream(@Nonnull String name) throws IOException;

        @Nonnull
        default ByteSource asSource(@Nonnull String name) {
            return () -> openStream(name);
        }

        @Nonnull
        static ByteResource of(@Nonnull Class<?> type) {
            return new ByteResource() {
                @Override
                public InputStream openStream(String name) throws IOException {
                    return getResourceAsStreamOrElseThrowIO(type, name);
                }

                @Override
                public void close() throws IOException {
                }
            };
        }
    }

    private InputStream getResourceAsStreamOrElseThrowIO(Class<?> type, String name) throws IOException {
        InputStream result = type.getResourceAsStream(name);
        if (result == null) {
            throw new IOException("Resource '" + name + "' not found with type '" + type + "'");
        }
        return result;
    }

    private static final class EmptyInputStream extends InputStream {

        static final EmptyInputStream INSTANCE = new EmptyInputStream();

        private EmptyInputStream() {
        }

        @Override
        public int read() {
            return -1;
        }

        @Override
        public int available() {
            return 0;
        }
    }
}

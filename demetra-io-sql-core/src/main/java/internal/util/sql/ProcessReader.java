/*
 * Copyright 2018 National Bank of Belgium
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
package internal.util.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ProcessReader {

    @NonNull
    public static BufferedReader newReader(@NonNull String... args) throws IOException {
        return newReader(new ProcessBuilder(args).start());
    }

    @NonNull
    public static BufferedReader newReader(@NonNull Process process) throws IOException {
        return new BufferedReader(new InputStreamReader(new ProcessInputStream(process), Charset.defaultCharset()));
    }

    private static final class ProcessInputStream extends ForwardingInputStream {

        private final Process process;

        public ProcessInputStream(Process process) {
            super(process.getInputStream());
            this.process = process;
        }

        @Override
        public void close() throws IOException {
            try {
                // we need the process to end, else we'll get an illegal Thread State Exception
                while (read() != -1) {
                }
                process.waitFor();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            } finally {
                super.close();
            }
        }
    }

    private abstract static class ForwardingInputStream extends InputStream {

        private final InputStream delegate;

        protected ForwardingInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}

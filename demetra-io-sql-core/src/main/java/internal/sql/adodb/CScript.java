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
package internal.sql.adodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
@ThreadSafe
final class CScript implements Wsh {

    static final CScript INSTANCE = new CScript();

    private final ConcurrentMap<String, File> scripts;

    private CScript() {
        this.scripts = new ConcurrentHashMap<>();
    }

    @Override
    public BufferedReader exec(String scriptName, String... args) throws IOException {
        File script = getScript(scriptName);
        Process process = exec(script, args);
        return new BufferedReader(new InputStreamReader(new ProcessInputStream(process), Charset.defaultCharset()));
    }

    private File getScript(String scriptName) throws IOException {
        File result = scripts.get(scriptName);
        if (result == null || !isValidScript(result)) {
            result = extractResource(scriptName + ".vbs", "adodb", ".vbs");
            scripts.put(scriptName, result);
        }
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @Nonnull
    private static Process exec(@Nonnull File script, @Nonnull String... args) throws IOException {
        // http://technet.microsoft.com/en-us/library/ff920171.aspx
        String[] result = new String[3 + args.length];
        result[0] = "cscript";
        result[1] = "/nologo";
        result[2] = "\"" + script.getAbsolutePath() + "\"";
        System.arraycopy(args, 0, result, 3, args.length);
        return new ProcessBuilder(result).start();
    }

    private static boolean isValidScript(@Nonnull File script) {
        return script.exists() && script.isFile() && script.canRead();
    }

    @Nonnull
    private static File extractResource(@Nonnull String resourceName, @Nonnull String filePrefix, @Nonnull String fileSuffix) throws IOException {
        File result = File.createTempFile(filePrefix, fileSuffix);
        result.deleteOnExit();
        try (InputStream in = Wsh.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new FileNotFoundException(resourceName);
            }
            Files.copy(in, result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
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
    //</editor-fold>
}

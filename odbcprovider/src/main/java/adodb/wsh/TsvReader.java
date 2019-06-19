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
package adodb.wsh;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import static java.lang.String.format;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class TsvReader implements Closeable {

    @NonNull
    static TsvReader of(@NonNull BufferedReader reader, int headerRowCount) throws IOException {
        String[][] headers = new String[headerRowCount][];
        for (int i = 0; i < headerRowCount; i++) {
            String header = readNextLine(reader);
            if (header == null) {
                throw new IOException(format("Expected header on row %s", i));
            }
            headers[i] = split(header);
        }

        return new TsvReader(reader, headers);
    }

    private static final String DELIMITER = "\t";

    private final BufferedReader reader;
    private final String[][] headers;

    private TsvReader(BufferedReader reader, String[][] headers) {
        this.reader = reader;
        this.headers = headers;
    }

    @NonNull
    public String[] getHeader(int row) throws IndexOutOfBoundsException {
        return headers[row];
    }

    public boolean readNextInto(@NonNull String[] array) throws IOException {
        String line = readNextLine(reader);
        if (line != null) {
            splitInto(line, array);
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    static final class Err extends IOException {

        private final int number;

        public Err(String description, int number) {
            super(description);
            this.number = number;
        }

        public String getDescription() {
            return getMessage();
        }

        public int getNumber() {
            return number;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static String[] split(String line) {
        return line.split(DELIMITER, -1);
    }

    private static void splitInto(String line, String[] array) {
        int start = 0;
        for (int i = 0; i < array.length - 1; i++) {
            int stop = line.indexOf(DELIMITER, start);
            array[i] = line.substring(start, stop);
            start = stop + DELIMITER.length();
        }
        array[array.length - 1] = line.substring(start);
    }

    @Nullable
    private static String readNextLine(@NonNull BufferedReader reader) throws IOException, Err {
        String line = reader.readLine();
        if (line != null && line.isEmpty()) {
            throw parseError(reader);
        }
        return line;
    }

    @NonNull
    private static Err parseError(@NonNull BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null && !line.isEmpty()) {
            int index = line.indexOf(DELIMITER);
            if (index != -1) {
                try {
                    return new Err(line.substring(index + DELIMITER.length()), Integer.parseInt(line.substring(0, index)));
                } catch (NumberFormatException ex) {
                    throw new IOException("Cannot parse error code", ex);
                }
            }
        }
        throw new IOException("Expected error description on next row");
    }
    //</editor-fold>
}

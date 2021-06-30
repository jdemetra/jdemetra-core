package internal.demetra.tsp.text;

import demetra.tsprovider.grid.GridDataType;
import demetra.tsprovider.grid.GridInput;
import nbbrd.io.BlockSizer;
import nbbrd.io.Resource;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

@lombok.AllArgsConstructor
public class TxtGridInput implements GridInput {

    @lombok.NonNull
    final Path file;

    @lombok.NonNull
    final Charset charset;

    @lombok.NonNull
    final Csv.Format format;

    @lombok.NonNull
    final Csv.ReaderOptions options;

    final int skipLines;

    @Override
    public @NonNull Set<GridDataType> getDataTypes() {
        return EnumSet.of(GridDataType.STRING);
    }

    @Override
    public @NonNull String getName() {
        return file.toString();
    }

    @Override
    public @NonNull Stream open() throws IOException {
        Csv.Reader reader = Csv.Reader.of(format, options, Files.newBufferedReader(file, charset), (int) BlockSizer.INSTANCE.get().getBlockSize(file));
        try {
            skipLines(reader, skipLines);
        } catch (IOException ex) {
            Resource.ensureClosed(ex, reader);
            throw ex;
        }
        return new TxtStream(reader);
    }

    private static void skipLines(Csv.Reader reader, int skipLines) throws IOException {
        for (int i = 0; i < skipLines; i++) {
            if (!reader.readLine()) {
                return;
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class TxtStream implements Stream {

        @lombok.NonNull
        private final Csv.Reader reader;

        @Override
        public boolean readCell() throws IOException {
            return reader.readField();
        }

        @Override
        public boolean readRow() throws IOException {
            return reader.readLine();
        }

        @Override
        public @NonNull Object getCell() {
            return reader.toString();
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }
}

package internal.demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.HasFilePaths;
import demetra.tsprovider.grid.GridDataType;
import demetra.tsprovider.grid.GridInput;
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.grid.GridReader;
import nbbrd.design.MightBePromoted;
import nbbrd.io.Resource;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.TextParser;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

@lombok.experimental.UtilityClass
public class TxtLoader {

    public @NonNull TsCollection load(@NonNull TxtBean bean, @NonNull HasFilePaths paths) throws IOException {
        return getParser(bean).parseFile(paths.resolveFilePath(bean.getFile()), bean.getCharset());
    }

    public static @NonNull TextParser<TsCollection> getParser(@NonNull TxtBean bean) {
        GridReader gridReader = getGridReader(bean);
        Function<Csv.Reader, GridInput> gridInput = getGridInput(bean);
        return Picocsv.Parser
                .builder(csv -> gridReader.read(gridInput.apply(csv)))
                .format(getCsvFormat(bean))
                .options(getCsvOptions(bean))
                .build();
    }

    private static GridReader getGridReader(TxtBean bean) {
        return GridReader
                .builder()
                .format(bean.getFormat())
                .gathering(bean.getGathering())
                .layout(GridLayout.VERTICAL)
                .namePattern("Column ${number}")
                .build();
    }

    private static Function<Csv.Reader, GridInput> getGridInput(TxtBean bean) {
        return reader -> new TxtGridInput(bean.getFile().getName(), bean.getSkipLines(), reader);
    }

    private static Csv.Format getCsvFormat(TxtBean bean) {
        Csv.Format.Builder result = Csv.Format.builder();
        switch (bean.getDelimiter()) {
            case TAB:
                result.delimiter('\t');
                break;
            case COMMA:
                result.delimiter(',');
                break;
            case SPACE:
                result.delimiter(' ');
                break;
            case SEMICOLON:
                result.delimiter(';');
                break;
        }
        switch (bean.getTextQualifier()) {
            case DOUBLE_QUOTE:
                result.quote('"');
                break;
            case NONE:
                result.quote('\0');
                break;
            case QUOTE:
                result.quote('\'');
                break;
        }
        return result.build();
    }

    private static Csv.ReaderOptions getCsvOptions(TxtBean bean) {
        return Csv.ReaderOptions.builder().lenientSeparator(true).build();
    }

    @lombok.AllArgsConstructor
    private static final class TxtGridInput implements GridInput {

        final String name;
        final int skipLines;
        final Csv.Reader reader;

        @Override
        public @NonNull Set<GridDataType> getDataTypes() {
            return EnumSet.of(GridDataType.STRING);
        }

        @Override
        public @NonNull String getName() {
            return name;
        }

        @Override
        public @NonNull Stream open() throws IOException {
            try {
                skipLines(reader, skipLines);
            } catch (IOException ex) {
                Resource.ensureClosed(ex, reader);
                throw ex;
            }
            return new TxtGridInput.TxtStream(reader);
        }

        @MightBePromoted
        private static boolean skipComments(Csv.Reader reader) throws IOException {
            while (reader.readLine()) {
                if (!reader.isComment()) {
                    return true;
                }
            }
            return false;
        }

        @MightBePromoted
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
                return skipComments(reader);
            }

            @Override
            public @NonNull Object getCell() {
                return reader.toString();
            }

            @Override
            public void close() {
            }
        }
    }
}

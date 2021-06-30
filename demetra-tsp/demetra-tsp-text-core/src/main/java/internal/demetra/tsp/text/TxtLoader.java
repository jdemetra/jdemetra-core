package internal.demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.HasFilePaths;
import demetra.tsprovider.grid.GridInput;
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.grid.GridReader;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;

@lombok.AllArgsConstructor
public class TxtLoader {

    @lombok.NonNull
    private final HasFilePaths filePathSupport;

    public @NonNull TsCollection load(@NonNull TxtBean bean) throws IOException {
        return getReader(bean).read(getInput(bean));
    }

    private GridReader getReader(TxtBean bean) {
        return GridReader
                .builder()
                .format(bean.getObsFormat())
                .gathering(bean.getObsGathering())
                .layout(GridLayout.VERTICAL)
                .namePattern("Column ${number}")
                .build();
    }

    private GridInput getInput(TxtBean bean) throws FileNotFoundException {
        return new TxtGridInput(
                filePathSupport.resolveFilePath(bean.getFile()).toPath(),
                bean.getCharset(),
                getCsvFormat(bean),
                getCsvOptions(bean),
                bean.getSkipLines()
        );
    }

    private Csv.Format getCsvFormat(TxtBean bean) {
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

    private Csv.ReaderOptions getCsvOptions(TxtBean bean) {
        return Csv.ReaderOptions.builder().lenientSeparator(true).build();
    }
}

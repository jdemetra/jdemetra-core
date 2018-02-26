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
package internal.spreadsheet.grid;

import demetra.tsprovider.TsCollection;
import demetra.tsprovider.grid.GridImport;
import ec.util.spreadsheet.Book;
import demetra.tsprovider.grid.GridReader;
import internal.spreadsheet.SpreadSheetAccessor;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public class SheetGrid implements SpreadSheetAccessor {

    @lombok.NonNull
    private final File file;

    @lombok.NonNull
    private final Book.Factory factory;

    @lombok.NonNull
    private final GridImport options;

    @Override
    public Optional<TsCollection> getSheetByName(String name) throws IOException {
        Objects.requireNonNull(name);
        try (Book book = factory.load(file)) {
            return getSheetByName(book, name);
        }
    }

    @Override
    public List<String> getSheetNames() throws IOException {
        try (Book book = factory.load(file)) {
            return getSheetNames(book);
        }
    }

    @Override
    public List<TsCollection> getSheets() throws IOException {
        try (Book book = factory.load(file)) {
            return getSheets(book);
        }
    }

    @Override
    public void close() throws IOException {
    }

    private Optional<TsCollection> getSheetByName(Book book, String name) throws IOException {
        for (int i = 0; i < book.getSheetCount(); i++) {
            if (book.getSheetName(i).equals(name)) {
                GridReader reader = GridReader.of(options, SheetGridInfo.of(factory));
                return Optional.of(reader.read(SheetGridInput.of(book.getSheet(i))));
            }
        }
        return Optional.empty();
    }

    private List<String> getSheetNames(Book book) throws IOException {
        String[] result = new String[book.getSheetCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = book.getSheetName(i);
        }
        return Arrays.asList(result);
    }

    private List<TsCollection> getSheets(Book book) throws IOException {
        GridReader reader = GridReader.of(options, SheetGridInfo.of(factory));
        TsCollection[] result = new TsCollection[book.getSheetCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = reader.read(SheetGridInput.of(book.getSheet(i)));
        }
        return Arrays.asList(result);
    }
}

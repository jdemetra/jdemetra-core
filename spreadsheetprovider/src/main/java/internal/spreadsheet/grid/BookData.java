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

import ec.util.spreadsheet.Book;
import demetra.tsprovider.grid.GridReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class BookData {

    @lombok.NonNull
    private Map<String, SheetData> sheets;

    @Nullable
    public SheetData getSheetByName(@Nonnull String name) {
        Objects.requireNonNull(name);
        return sheets.get(name);
    }

    public static BookData of(Book book, GridReader reader) throws IOException {
        int sheetCount = book.getSheetCount();
        Map<String, SheetData> result = new HashMap<>(sheetCount);
        book.forEach((sheet, i) -> {
            SheetData data = SheetData.of(sheet, i, reader);
            result.put(data.getSheetName(), data);
        });
        return BookData.of(result);
    }
}

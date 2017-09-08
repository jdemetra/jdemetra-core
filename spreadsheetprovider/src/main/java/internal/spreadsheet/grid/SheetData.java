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

import ec.util.spreadsheet.Sheet;
import demetra.tsprovider.grid.GridReader;
import demetra.tsprovider.grid.TsGrid;
import demetra.tsprovider.grid.TsCollectionGrid;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class SheetData {

    @lombok.NonNull
    private String sheetName; // unique id; don't use ordering

    @Nonnegative
    private int ordering; // this may change !

    @lombok.NonNull
    private TsCollectionGrid data;

    @Nullable
    public TsGrid getSeriesByName(@Nonnull String name) {
        for (TsGrid o : data.getItems()) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    @Nonnull
    public static SheetData of(@Nonnull Sheet sheet, @Nonnegative int ordering, @Nonnull GridReader reader) {
        TsCollectionGrid data = reader.read(SheetGridInput.of(sheet));
        return SheetData.of(sheet.getName(), ordering, data);
    }
}

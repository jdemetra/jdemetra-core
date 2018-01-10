/*
 * Copyright 2017 National Bank of Belgium
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
package internal.spreadsheet.grid;

import ec.util.spreadsheet.helpers.ArraySheet;
import demetra.tsprovider.grid.GridOutput;
import java.util.Iterator;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SheetGridOutput implements GridOutput {

    @Nonnull
    public static SheetGridOutput of() {
        return new SheetGridOutput(ArraySheet.builder(), false);
    }

    private final ArraySheet.Builder sheet;
    private boolean inv;

    @Override
    public void name(String name) {
        sheet.name(name);
    }

    @Override
    public void inv() {
        inv = !inv;
    }

    @Override
    public void row(int row, int column, Iterator<?> values) {
        sheet.row(row, column, values);
    }

    @Override
    public void column(int row, int column, Iterator<?> values) {
        sheet.column(row, column, values);
    }

    @Override
    public void value(int row, int column, double value) {
        sheet.value(column, column, value);
    }

    @Nonnull
    public ArraySheet build() {
        ArraySheet result = sheet.build();
        return inv ? result.inv() : result;
    }
}

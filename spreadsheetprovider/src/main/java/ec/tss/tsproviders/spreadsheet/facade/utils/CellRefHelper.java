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
package ec.tss.tsproviders.spreadsheet.facade.utils;

import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public class CellRefHelper {

    private final ec.util.spreadsheet.helpers.CellRefHelper adaptee = new ec.util.spreadsheet.helpers.CellRefHelper();

    public boolean read(@Nullable String ref) {
        return adaptee.parse(ref);
    }

    /**
     * Returns a zero-based column index.
     *
     * @return
     */
    public int getCol() {
        return adaptee.getColumnIndex();
    }

    /**
     * Returns a zero-based row index.
     *
     * @return
     */
    public int getRow() {
        return adaptee.getRowIndex();
    }

    public static String getColumnName(int index) {
        return ec.util.spreadsheet.helpers.CellRefHelper.getColumnLabel(index);
    }

    public static String getName(int row, int col) {
        return ec.util.spreadsheet.helpers.CellRefHelper.getCellRef(row, col);
    }
}

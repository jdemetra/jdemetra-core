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
package ec.util.spreadsheet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Facade that represents <b>a sheet in a spreadsheet</b>.
 * <br>Note that you should not store a cell since some implementations may use
 * the flyweight pattern.
 *
 * @see Book
 * @see Cell
 * @author Philippe Charles
 */
//@FacadePattern
public abstract class Sheet {

    @Nonnegative
    abstract public int getRowCount();

    @Nonnegative
    abstract public int getColumnCount();

    @Nullable
    abstract public Cell getCell(@Nonnegative int rowIdx, @Nonnegative int columnIdx) throws IndexOutOfBoundsException;

    @Nullable
    public Object getCellValue(@Nonnegative int rowIdx, @Nonnegative int columnIdx) throws IndexOutOfBoundsException {
        return getCellValueFromCell(this, rowIdx, columnIdx);
    }

    @Nonnull
    abstract public String getName();

    @Nonnull
    public Sheet inv() {
        return invUsingDelegate(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    @Nullable
    private static Object getCellValueFromCell(@Nonnull Sheet sheet, int rowIdx, int columnIdx) {
        Cell cell = sheet.getCell(rowIdx, columnIdx);
        if (cell == null) {
            return null;
        }
        if (cell.isDate()) {
            return cell.getDate();
        }
        if (cell.isNumber()) {
            return cell.getNumber();
        }
        if (cell.isString()) {
            return cell.getString();
        }
        return null;
    }

    @Nonnull
    private static Sheet invUsingDelegate(@Nonnull Sheet sheet) {
        return sheet instanceof InvSheet ? ((InvSheet) sheet).sheet : new InvSheet(sheet);
    }

    private static final class InvSheet extends Sheet {

        private final Sheet sheet;

        public InvSheet(Sheet sheet) {
            this.sheet = sheet;
        }

        @Override
        public int getRowCount() {
            return sheet.getColumnCount();
        }

        @Override
        public int getColumnCount() {
            return sheet.getRowCount();
        }

        @Override
        public Cell getCell(int rowIdx, int columnIdx) {
            return sheet.getCell(columnIdx, rowIdx);
        }

        @Override
        public Object getCellValue(int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
            return sheet.getCellValue(columnIdx, rowIdx);
        }

        @Override
        public String getName() {
            return sheet.getName();
        }
    }
    //</editor-fold>
}

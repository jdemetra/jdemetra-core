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

import java.util.Objects;
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

    /**
     * Returns the number of rows contained in this sheet.
     *
     * @return a row count
     */
    @Nonnegative
    abstract public int getRowCount();

    /**
     * Returns the number of columns contained in this sheet.
     *
     * @return a column count
     */
    @Nonnegative
    abstract public int getColumnCount();

    /**
     * Returns the cell located at the specified position.
     *
     * @param rowIdx a zero-based row index
     * @param columnIdx a zero-based column index
     * @return a cell if available, null otherwise
     * @throws IndexOutOfBoundsException if the position is out of bounds
     */
    @Nullable
    abstract public Cell getCell(@Nonnegative int rowIdx, @Nonnegative int columnIdx) throws IndexOutOfBoundsException;

    /**
     * Returns the cell value located at the specified position.
     *
     * @param rowIdx a zero-based row index
     * @param columnIdx a zero-based column index
     * @return a value if available, null otherwise
     * @throws IndexOutOfBoundsException if the position is out of bounds
     */
    @Nullable
    public Object getCellValue(@Nonnegative int rowIdx, @Nonnegative int columnIdx) throws IndexOutOfBoundsException {
        return getCellValueFromCell(this, rowIdx, columnIdx);
    }

    /**
     * Performs the given action for each non-null cell of the sheet until all
     * cells have been processed or an exception has been thrown.
     *
     * @implSpec
     * <p>
     * The default implementation parses the content by row.
     *
     * @param action The action to be performed for each cell
     * @throws NullPointerException if the specified action is null
     * @since 2.2.0
     */
    public void forEach(@Nonnull SheetConsumer<? super Cell> action) {
        forEachByRow(this, action);
    }

    /**
     * Performs the given action for each non-null cell value of the sheet until
     * all cell values have been processed or an exception has been thrown.
     *
     * @implSpec
     * <p>
     * The default implementation parses the content by row.
     *
     * @param action The action to be performed for each cell
     * @throws NullPointerException if the specified action is null
     * @since 2.2.0
     */
    public void forEachValue(@Nonnull SheetConsumer<? super Object> action) {
        forEachValueByRow(this, action);
    }

    /**
     * Returns the sheet name.
     *
     * @return a non-null name
     */
    @Nonnull
    abstract public String getName();

    @Nonnull
    public Sheet inv() {
        return invUsingDelegate(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    private static void forEachByRow(@Nonnull Sheet sheet, @Nonnull SheetConsumer<? super Cell> action) {
        Objects.requireNonNull(action);
        int rowCount = sheet.getRowCount();
        int columnCount = sheet.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                Cell cell = sheet.getCell(i, j);
                if (cell != null) {
                    action.accept(i, j, cell);
                }
            }
        }
    }

    private static void forEachValueByRow(@Nonnull Sheet sheet, @Nonnull SheetConsumer<? super Object> action) {
        Objects.requireNonNull(action);
        int rowCount = sheet.getRowCount();
        int columnCount = sheet.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                Object value = sheet.getCellValue(i, j);
                if (value != null) {
                    action.accept(i, j, value);
                }
            }
        }
    }

    @Nullable
    private static Object getCellValueFromCell(@Nonnull Sheet sheet, int rowIdx, int columnIdx) {
        Cell cell = sheet.getCell(rowIdx, columnIdx);
        return cell != null ? cell.getValue() : null;
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
        public void forEach(SheetConsumer<? super Cell> action) {
            sheet.forEach((i, j, c) -> action.accept(j, i, c));
        }

        @Override
        public void forEachValue(SheetConsumer<? super Object> action) {
            sheet.forEachValue((i, j, v) -> action.accept(j, i, v));
        }

        @Override
        public String getName() {
            return sheet.getName();
        }
    }
    //</editor-fold>
}

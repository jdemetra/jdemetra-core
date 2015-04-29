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
package ec.tss.tsproviders.spreadsheet.facade;

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
@Deprecated
public abstract class Sheet {

    abstract public int getRowCount();

    abstract public int getColumnCount();

    @Nullable
    abstract public Cell getCell(int rowIdx, int columnIdx) throws IndexOutOfBoundsException;

    @Nonnull
    abstract public String getName();

    @Nonnull
    public Sheet inv() {
        return this instanceof InvSheet ? ((InvSheet) this).sheet : new InvSheet(this);
    }

    @Nonnull
    @Deprecated
    public Sheet memoize() {
        return this instanceof MemoizedSheet ? this : new MemoizedSheet(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Sheet implementations">
    private static class InvSheet extends Sheet {

        final Sheet sheet;

        InvSheet(Sheet sheet) {
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
        public String getName() {
            return sheet.getName();
        }
    }

    private static class MemoizedSheet extends Sheet {

        final Sheet sheet;
        final int rowCount;
        final int columnCount;
        final String name;
        final Cell[][] data;

        MemoizedSheet(Sheet sheet) {
            this.sheet = sheet;
            this.rowCount = sheet.getRowCount();
            this.columnCount = sheet.getColumnCount();
            this.name = sheet.getName();
            this.data = new Cell[rowCount][columnCount];
        }

        @Override
        public int getRowCount() {
            return rowCount;
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        @Override
        public Cell getCell(int rowIdx, int columnIdx) {
            Cell result = data[rowIdx][columnIdx];
            if (result == null) {
                result = sheet.getCell(rowIdx, columnIdx);
                data[rowIdx][columnIdx] = result == null ? NullCell.INSTANCE : result;
            }
            return NullCell.INSTANCE.equals(result) ? null : result;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class NullCell extends Cell {

        static final NullCell INSTANCE = new NullCell();
    }
    //</editor-fold>
}

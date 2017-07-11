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

import com.google.common.io.InputSupplier;
import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.spreadsheet.facade.Cell;
import ec.tss.tsproviders.spreadsheet.facade.Sheet;
import ec.tstoolkit.maths.matrices.Matrix;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple in-memory implementation of book.
 *
 * @author Philippe Charles
 */
@Deprecated
public class MemBook extends Book {

    private final List<ec.util.spreadsheet.Sheet> sheets;

    private MemBook(List<ec.util.spreadsheet.Sheet> sheets) {
        this.sheets = sheets;
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Sheet getSheet(int index) {
        return new BookFactoryAdapter.ToSheetAdapter(sheets.get(index));
    }

    @Override
    public String getFactoryName() {
        return "BookBuilder";
    }

    public static class Builder /*implements IBuilder<MemBook>*/ {

        private final SheetStep sheetBuilder;
        private final List<ec.util.spreadsheet.Sheet> sheets;

        public Builder() {
            this.sheetBuilder = new SheetStep();
            this.sheets = new ArrayList<>();
        }

        public MemBook build() {
            return new MemBook(new ArrayList<>(sheets));
        }

        public Builder clear() {
            sheets.clear();
            return this;
        }

        @Deprecated
        public Builder copy(InputSupplier<Book> bookSupplier) throws IOException {
            try (Book book = bookSupplier.getInput()) {
                return copy(book);
            }
        }

        public Builder copy(Book book) throws IOException {
            for (int s = 0; s < book.getSheetCount(); s++) {
                Sheet sheet = book.getSheet(s);
                sheet(sheet.getName()).copy(0, 0, sheet).add();
            }
            return this;
        }

        public SheetStep sheet(String name) {
            return sheetBuilder.clear().name(name);
        }

        public class SheetStep {

            private final ec.util.spreadsheet.helpers.ArraySheet.Builder data;
            private boolean inv;

            private SheetStep() {
                this.data = ec.util.spreadsheet.helpers.ArraySheet.builder();
                this.inv = false;
            }

            public SheetStep clear() {
                data.clear();
                return this;
            }

            public SheetStep name(String name) {
                data.name(name);
                return this;
            }

            public SheetStep copy(int row, int col, Sheet sheet) {
                for (int i = 0; i < sheet.getRowCount(); i++) {
                    for (int j = 0; j < sheet.getColumnCount(); j++) {
                        copy(row + i, col + j, sheet.getCell(i, j));
                    }
                }
                return this;
            }

            public SheetStep copy(int row, int col, Cell cell) {
                cell(row, col, cell == null ? null : cell.isDate() ? cell.getDate() : cell.isNumber()
                        ? cell.getNumber() : cell.isString() ? cell.getString()
                        : null);
                return this;
            }

            public SheetStep row(int row, int col, Object[] values) {
                for (int j = 0; j < values.length; j++) {
                    cell(row, col + j, values[j]);
                }
                return this;
            }

            public SheetStep column(int row, int col, Object[] values) {
                for (int i = 0; i < values.length; i++) {
                    cell(row + i, col, values[i]);
                }
                return this;
            }

            public SheetStep matrix(int row, int col, Matrix matrix) {
                for (int i = 0; i < matrix.getRowsCount(); i++) {
                    for (int j = 0; j < matrix.getColumnsCount(); j++) {
                        cell(row + i, col + j, matrix.get(i, j));
                    }
                }
                return this;
            }

            public SheetStep cell(int row, int col, Object value) {
                return !inv
                        ? putCellValue(row, col, value)
                        : putCellValue(col, row, value);
            }

            private SheetStep putCellValue(int row, int col, Object value) {
                if (row < 0 || col < 0) {
                    return this;
                }

                Object cellValue = getCellValue(value);
                if (cellValue == null) {
                    return this;
                }

                data.value(row, col, cellValue);
                return this;
            }

            private Object getCellValue(Object value) {
                if (value instanceof Date) {
                    return value;
                } else if (value instanceof Number) {
                    return value;
                } else if (value instanceof String) {
                    return value;
                } else if (value != null) {
                    return value.toString();
                }
                return null;
            }

            public SheetStep inv() {
                inv = !inv;
                return this;
            }

            public Builder add() {
                ec.util.spreadsheet.Sheet sheet = data.build();
                sheets.add(inv ? sheet.inv() : sheet);
                return Builder.this;
            }
        }
    }
}

/*
 * Copyright 2013 National Bank of Belgium
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
package internal.spreadsheet;

import demetra.design.VisibleForTesting;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.grid.GridExport;
import demetra.tsprovider.grid.GridFactory;
import demetra.tsprovider.grid.GridImport;
import demetra.tsprovider.grid.TsCollectionGrid;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.spreadsheet.Fixme.Matrix;
import internal.spreadsheet.Fixme.Table;
import internal.spreadsheet.grid.SheetGridInput;
import internal.spreadsheet.grid.SheetGridOutput;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface SpreadSheetFactory {

    @Nonnull
    TsCollection toTsCollection(@Nonnull Sheet sheet, @Nonnull GridImport options);

    @Nonnull
    Table<?> toTable(@Nonnull Sheet sheet);

    @Nonnull
    ArraySheet fromTsCollection(@Nonnull TsCollection col, @Nonnull GridExport options);

    @Nonnull
    ArraySheet fromMatrix(@Nonnull Matrix matrix);

    @Nonnull
    ArraySheet fromTable(@Nonnull Table<?> table);

    @Nonnull
    static SpreadSheetFactory getDefault() {
        return DefaultImpl.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @VisibleForTesting
    static final class DefaultImpl implements SpreadSheetFactory {

        private static final DefaultImpl INSTANCE = new DefaultImpl();

        private final GridFactory gridFactory = GridFactory.getDefault();

        @Override
        public ArraySheet fromTsCollection(TsCollection col, GridExport options) {
            SheetGridOutput output = SheetGridOutput.of();
            gridFactory.write(TsCollectionGrid.fromTsCollection(col), output, options);
            return output.build();
        }

        @Override
        public TsCollection toTsCollection(Sheet sheet, GridImport options) {
            SheetGridInput input = SheetGridInput.of(sheet);
            return TsCollectionGrid.toTsCollection(gridFactory.read(input, options));
        }

        @Override
        public ArraySheet fromMatrix(Matrix matrix) {
            return newArraySheet("dnd", matrix);
        }

        @Override
        public Table<?> toTable(Sheet sheet) {
            Table<Object> result = Table.of(sheet.getRowCount(), sheet.getColumnCount());
            sheet.forEachValue(result::set);
            return result;
        }

        @Override
        public ArraySheet fromTable(Table<?> table) {
            return newArraySheet("dnd", table);
        }

        private static ArraySheet newArraySheet(String name, Matrix matrix) {
            ArraySheet.Builder result = ArraySheet.builder(matrix.getRowsCount(), matrix.getColumnsCount()).name(name);
            for (int i = 0; i < matrix.getRowsCount(); i++) {
                for (int j = 0; j < matrix.getColumnsCount(); j++) {
                    result.value(i, j, matrix.get(i, j));
                }
            }
            return result.build();
        }

        private static ArraySheet newArraySheet(String name, Table<?> table) {
            ArraySheet.Builder result = ArraySheet.builder(table.getRowsCount(), table.getColumnsCount()).name(name);
            for (int i = 0; i < table.getRowsCount(); i++) {
                for (int j = 0; j < table.getColumnsCount(); j++) {
                    result.value(i, j, table.get(i, j));
                }
            }
            return result.build();
        }
    }
    //</editor-fold>
}

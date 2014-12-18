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
package ec.util.spreadsheet.od;

import ec.util.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;

/**
 *
 * @author Philippe Charles
 */
class OdSheet extends ec.util.spreadsheet.Sheet {

    final static int BUGGED_COLUMN_COUNT = 16384;
    final Sheet sheet;
    final OdCell flyweightCell;
    final int columnCount;

    public OdSheet(Sheet sheet) {
        this.sheet = sheet;
        this.flyweightCell = new OdCell();
        this.columnCount = computeColumnCount(sheet);
    }

    static int computeColumnCount(Sheet sheet) {
        if (sheet.getRowCount() == 0) {
            return 0;
        }
        int result = sheet.getColumnCount();
        if (result != BUGGED_COLUMN_COUNT) {
            return result;
        }
        // dichotomic search
        int min = 0;
        int max = BUGGED_COLUMN_COUNT;
        do {
            result = (min + max) / 2;
            if (!isNullOrEmpty(sheet, 0, result)) {
                min = result + 1;
            } else {
                max = result - 1;
            }
        } while (min <= max);
        return result + 1;
    }

    static boolean isNullOrEmpty(Sheet sheet, int rowIdx, int columnIdx) throws IndexOutOfBoundsException {
        return sheet.getCellAt(columnIdx, rowIdx).getValueType() == null;
    }

    @Override
    public int getRowCount() {
        return sheet.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Cell getCell(int rowIdx, int columnIdx) {
        return flyweightCell.withCell(sheet.getCellAt(columnIdx, rowIdx));
    }

    @Override
    public String getName() {
        return sheet.getName().replace("_", " ");
    }
}

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
package test.tsprovider.grid;

import demetra.tsprovider.grid.GridDataType;
import demetra.tsprovider.grid.GridInput;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public final class ArrayGridInput implements GridInput {

    @lombok.NonNull
    private final Object[][] array;

    @Override
    public Set<GridDataType> getDataTypes() {
        return EnumSet.allOf(GridDataType.class);
    }

    @Override
    public String getName() {
        return "";
    }

    public int getRowCount() {
        return array.length;
    }

    public int getColumnCount(int row) {
        return array[row].length;
    }

    @Deprecated
    public int getColumnCount() {
        return getRowCount() > 0 ? getColumnCount(0) : 0;
    }

    public Object getValue(int row, int column) {
        return array[row][column];
    }

    public Object[][] getArray() {
        return array;
    }

    @Override
    public Stream open() throws IOException {
        return new Stream() {
            private int row = -1;
            private int col = -1;

            @Override
            public boolean readCell() {
                col++;
                return col < getColumnCount(row);
            }

            @Override
            public boolean readRow() {
                col = -1;
                row++;
                return row < getRowCount();
            }

            @Override
            public Object getCell() {
                return getValue(row, col);
            }

            @Override
            public void close() {
            }
        };
    }

    public ArrayGridInput sub(int firstRow, int lastRow, int firstColumn, int lastColumn) {
        Object[][] result = new Object[lastRow - firstRow][];
        for (int row = 0; row < result.length; row++) {
            Object[] tmp = new Object[lastColumn - firstColumn];
            for (int column = 0; column < tmp.length; column++) {
                tmp[column] = array[row + firstRow][column + firstColumn];
            }
            result[row] = tmp;
        }
        return ArrayGridInput.of(result);
    }

    public ArrayGridInput subrows(int firstRow, int lastRow) {
        Object[][] result = new Object[lastRow - firstRow][];
        for (int row = 0; row < result.length; row++) {
            result[row] = array[firstRow + row];
        }
        return ArrayGridInput.of(result);
    }
}

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
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.grid.GridOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class ArrayGridOutput implements GridOutput {

    @lombok.NonNull
    private final GridLayout layout;

    @lombok.NonNull
    private final Set<GridDataType> dataTypes;

    @lombok.Getter
    private final Map<String, Object[][]> data = new HashMap<>();

    @Override
    public Set<GridDataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public Stream open(String name, int rows, int columns) throws IOException {
        return new ArrayGridOutputStream(name, rows, columns);
    }

    @lombok.RequiredArgsConstructor
    private final class ArrayGridOutputStream implements GridOutput.Stream {

        private final String name;
        private final int rows;
        private final int columns;

        private final List<Cell> cells = new ArrayList<>();
        private int row = 0;
        private int column = 0;

        @Override
        public void writeCell(Object value) throws IOException {
            moveTo(row, column);
            cells.add(new Cell(row, column, value));
            column++;
        }

        @Override
        public void writeEndOfRow() throws IOException {
            row++;
            column = 0;
        }

        @Override
        public void close() throws IOException {
            data.put(name, build());
        }

        private void moveTo(int row, int column) {
            if (row >= rows) {
                throw new IndexOutOfBoundsException(row + "/" + rows);
            }
            if (column >= columns) {
                throw new IndexOutOfBoundsException(column + "/" + columns);
            }
            checkLayout(row, column);
            this.row = row;
            this.column = column;
        }

        private void checkLayout(int row, int column) {
            if (!cells.isEmpty()) {
                switch (layout) {
                    case VERTICAL:
                    case UNDEFINED:
                        int prevRow = cells.get(cells.size() - 1).getRow();
                        if (row < prevRow) {
                            throw new IllegalArgumentException(row + " vs " + prevRow);
                        }
                        break;
                    case HORIZONTAL:
                        int prevColumn = cells.get(cells.size() - 1).getColumn();
                        if (column < prevColumn) {
                            throw new IllegalArgumentException(column + " vs " + prevColumn);
                        }
                        break;
                }
            }
        }

        private Object[][] build() {
            int maxRow = 0;
            int maxColumn = 0;
            for (Cell o : cells) {
                if (maxRow <= o.getRow()) {
                    maxRow = o.getRow();
                }
                if (maxColumn <= o.getColumn()) {
                    maxColumn = o.getColumn();
                }
            }
            Object[][] result = new Object[maxRow + 1][maxColumn + 1];
            cells.forEach((o) -> result[o.getRow()][o.getColumn()] = o.getValue());
            return result;
        }
    }

    @lombok.Value
    private static class Cell {

        final int row;
        final int column;
        final Object value;
    }
}

/*
 * Copyright 2020 National Bank of Belgium
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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class MarkableStream implements GridInput.Stream {

    @lombok.NonNull
    private final GridInput.Stream delegate;

    private final RowBuffer buffer = new RowBuffer();
    private boolean marked = false;

    public void mark() {
        if (marked) {
            throw new IllegalStateException();
        }
        this.marked = true;
        buffer.moveToFirst();
    }

    public void reset() {
        if (!marked) {
            throw new IllegalStateException();
        }
        this.marked = false;
    }

    @Override
    public boolean readRow() throws IOException {
        if (marked) {
            if (buffer.nextRow()) {
                return true;
            }
            if (delegate.readRow()) {
                buffer.pushRow(readRowToList());
                return true;
            }
            return false;
        }
        return buffer.pollRow() || delegate.readRow();
    }

    @Override
    public boolean readCell() throws IOException {
        return buffer.hasValues() ? buffer.nextValue() : delegate.readCell();
    }

    @Override
    public Object getCell() throws IOException {
        return buffer.hasValues() ? buffer.getValue() : delegate.getCell();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private List<Object> readRowToList() throws IOException {
        List<Object> result = new ArrayList<>();
        while (delegate.readCell()) {
            result.add(delegate.getCell());
        }
        return result;
    }

    private static final class RowBuffer {

        private final LinkedList<List<Object>> availableRows = new LinkedList<>();
        private int row = -1;
        private int column = -1;
        private List<Object> currentRow = null;

        void moveToFirst() {
            row = -1;
            column = -1;
            currentRow = null;
        }

        void pushRow(List<Object> values) {
            row = availableRows.size();
            column = -1;
            availableRows.addLast(values);
            currentRow = values;
        }

        boolean pollRow() {
            row = -1;
            column = -1;
            return (currentRow = availableRows.pollFirst()) != null;
        }

        boolean nextRow() {
            row++;
            column = -1;
            if (row < availableRows.size()) {
                currentRow = availableRows.get(row);
                return true;
            }
            currentRow = null;
            return false;
        }

        boolean hasValues() {
            return currentRow != null;
        }

        boolean nextValue() {
            column++;
            return column < currentRow.size();
        }

        Object getValue() {
            return currentRow.get(column);
        }
    }
}

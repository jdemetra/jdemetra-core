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
package ec.util.spreadsheet.helpers;

import ec.util.spreadsheet.Cell;
import ec.util.spreadsheet.Sheet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class ArraySheet extends Sheet implements Serializable {

    private final String name;
    private final int rowCount;
    private final int columnCount;
    private final Serializable[] values;
    private final FlyweightCell flyweightCell;
    private final boolean inv;

    // @VisibleForTesting
    ArraySheet(@Nonnull String name, int rowCount, int columnCount, @Nonnull Serializable[] values, boolean inv) {
        this.name = Objects.requireNonNull(name);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.values = Objects.requireNonNull(values);
        this.flyweightCell = new FlyweightCell();
        this.inv = inv;
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
    public Object getCellValue(int rowIndex, int columnIndex) throws IndexOutOfBoundsException {
        return !inv
                ? values[rowIndex * columnCount + columnIndex]
                : values[columnIndex * rowCount + rowIndex];
    }

    @Override
    public Cell getCell(int rowIndex, int columnIndex) throws IndexOutOfBoundsException {
        Object value = getCellValue(rowIndex, columnIndex);
        return value != null ? flyweightCell.withValue(value) : null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArraySheet inv() {
        return new ArraySheet(name, columnCount, rowCount, values, !inv);
    }

    @Nonnull
    public ArraySheet rename(@Nonnull String name) {
        return this.name.equals(name) ? this : new ArraySheet(name, rowCount, columnCount, values, inv);
    }

    @Nonnull
    public ArraySheet copy() {
        // we need a cell by instance of sheet
        return new ArraySheet(name, rowCount, columnCount, values, inv);
    }

    @Nonnull
    public ArrayBook toBook() {
        return new ArrayBook(new ArraySheet[]{copy()});
    }

    @Nonnull
    public static ArraySheet copyOf(@Nonnull Sheet sheet) {
        return sheet instanceof ArraySheet
                ? ((ArraySheet) sheet).copy()
                : new ArraySheet(sheet.getName(), sheet.getRowCount(), sheet.getColumnCount(), copyValuesOf(sheet), false);
    }

    @Nonnull
    public static ArraySheet copyOf(@Nonnull String name, @Nonnull Object[][] table) {
        int rowCount = table.length;
        int columnCount = 0;
        for (Object[] row : table) {
            if (row != null && columnCount < row.length) {
                columnCount = row.length;
            }
        }
        Serializable[] values = new Serializable[rowCount * columnCount];
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                for (int j = 0; j < table[i].length; j++) {
                    values[i * columnCount + j] = unknownToString(table[i][j]);
                }
            }
        }
        return new ArraySheet(name, rowCount, columnCount, values, false);
    }

    @Nonnull
    public static Builder builder() {
        return new UnboundedBuilder();
    }

    @Nonnull
    public static Builder builder(int rowCount, int columnCount) {
        return new BoundedBuilder(rowCount, columnCount);
    }

    @Nonnull
    public static Builder builder(@Nullable String sheetBounds) {
        if (sheetBounds != null) {
            String[] references = sheetBounds.split(":");
            if (references.length == 2) {
                CellRefHelper helper = new CellRefHelper();
                if (helper.parse(references[1])) {
                    return builder(helper.getRowIndex() + 1, helper.getColumnIndex() + 1);
                }
            }
        }
        return builder();
    }

    public abstract static class Builder {

        @Nonnull
        abstract public Builder name(@Nonnull String name);

        @Nonnull
        abstract public Builder clear();

        @Nonnull
        abstract public Builder value(int row, int column, @Nullable Object value) throws IndexOutOfBoundsException;

        @Nonnull
        public Builder value(int row, int column, @Nullable Cell value) throws IndexOutOfBoundsException {
            Object tmp = value == null ? null : value.isDate() ? value.getDate() : value.isNumber() ? value.getNumber() : value.isString() ? value.getString() : null;
            return value(row, column, (Object) tmp);
        }

        @Nonnull
        public Builder row(int row, int column, @Nonnull Object first, @Nonnull Object... rest) throws IndexOutOfBoundsException {
            return value(row, column, first).row(row, column + 1, rest);
        }

        @Nonnull
        public Builder row(int row, int column, @Nonnull Object[] values) throws IndexOutOfBoundsException {
            return row(row, column, Arrays.asList(values));
        }

        @Nonnull
        public Builder row(int row, int column, @Nonnull Iterable<?> values) throws IndexOutOfBoundsException {
            return row(row, column, values.iterator());
        }

        @Nonnull
        public Builder row(int row, int column, @Nonnull Iterator<?> values) throws IndexOutOfBoundsException {
            return rowByValue(this, row, column, values);
        }

        @Nonnull
        public Builder column(int row, int column, @Nonnull Object first, @Nonnull Object... rest) throws IndexOutOfBoundsException {
            return value(row, column, first).column(row + 1, column, rest);
        }

        @Nonnull
        public Builder column(int row, int column, @Nonnull Object[] values) throws IndexOutOfBoundsException {
            return column(row, column, Arrays.asList(values));
        }

        @Nonnull
        public Builder column(int row, int column, @Nonnull Iterable<?> values) throws IndexOutOfBoundsException {
            return column(row, column, values.iterator());
        }

        @Nonnull
        public Builder column(int row, int column, @Nonnull Iterator<?> values) throws IndexOutOfBoundsException {
            return columnByValue(this, row, column, values);
        }

        @Nonnull
        public Builder table(int row, int column, @Nonnull Object[][] values) throws IndexOutOfBoundsException {
            return tableByRow(this, row, column, values);
        }

        @Nonnull
        public Builder table(int row, int column, @Nonnull Sheet values) throws IndexOutOfBoundsException {
            return tableByValue(this, row, column, values);
        }

        @Nonnull
        public Builder map(int row, int column, @Nonnull Map<?, ?> values) throws IndexOutOfBoundsException {
            return mapByRow(this, row, column, values);
        }

        @Nonnull
        abstract public ArraySheet build();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    @Nonnull
    private static Serializable[] copyValuesOf(@Nonnull Sheet sheet) {
        int rowCount = sheet.getRowCount();
        int columnCount = sheet.getColumnCount();
        Serializable[] result = new Serializable[rowCount * columnCount];
        for (int i = 0; i < sheet.getRowCount(); i++) {
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                result[i * columnCount + j] = (Serializable) sheet.getCellValue(i, j);
            }
        }
        return result;
    }

    // @VisibleForTesting
    static final class FlyweightCell extends Cell implements Serializable {

        private transient Object value = null;

        @Nonnull
        public FlyweightCell withValue(@Nonnull Object value) {
            this.value = value;
            return this;
        }

        @Override
        public boolean isDate() {
            return value instanceof Date;
        }

        @Override
        public boolean isNumber() {
            return value instanceof Number;
        }

        @Override
        public boolean isString() {
            return value instanceof String;
        }

        @Override
        public Date getDate() {
            return (Date) value;
        }

        @Override
        public Number getNumber() {
            return (Number) value;
        }

        @Override
        public String getString() {
            return (String) value;
        }

        @Override
        public String toString() {
            return value != null ? value.toString() : "Null";
        }
    }

    private static Builder rowByValue(Builder b, int row, int column, Iterator<?> values) throws IndexOutOfBoundsException {
        int j = 0;
        while (values.hasNext()) {
            b.value(row, column + j, values.next());
        }
        return b;
    }

    private static Builder columnByValue(Builder b, int row, int column, Iterator<?> values) throws IndexOutOfBoundsException {
        int i = 0;
        while (values.hasNext()) {
            b.value(row + i, column, values.next());
        }
        return b;
    }

    private static Builder tableByRow(Builder b, int row, int column, Object[][] values) {
        for (int i = 0; i < values.length; i++) {
            Object[] tmp = values[i];
            if (tmp != null) {
                for (int j = 0; j < tmp.length; j++) {
                    b.value(row + i, column + j, tmp[j]);
                }
            }
        }
        return b;
    }

    private static Builder tableByValue(Builder b, int row, int column, Sheet values) {
        for (int i = 0; i < values.getRowCount(); i++) {
            for (int j = 0; j < values.getColumnCount(); j++) {
                b.value(row + i, column + j, values.getCellValue(i, j));
            }
        }
        return b;
    }

    @Nonnull
    private static Builder mapByRow(Builder b, int rowIndex, int columnIndex, @Nonnull Map<?, ?> map) {
        int i = 0;
        for (Map.Entry o : map.entrySet()) {
            b.row(rowIndex + i++, columnIndex, o.getKey(), o.getValue());
        }
        return b;
    }

    private static Serializable unknownToString(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Date) {
            return (Date) input;
        }
        if (input instanceof Number) {
            return (Number) input;
        }
        if (input instanceof String) {
            return (String) input;
        }
        return input.toString();
    }

    private static final class BoundedBuilder extends Builder {

        private final int rowCount;
        private final int columnCount;
        private final Serializable[] values;
        private String name;

        public BoundedBuilder(int rowCount, int columnCount) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.values = new Serializable[rowCount * columnCount];
            this.name = "";
        }

        @Override
        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        @Override
        public Builder clear() {
            Arrays.fill(values, null);
            name = "";
            return this;
        }

        @Override
        public Builder value(int rowIndex, int columnIndex, Object value) {
            values[rowIndex * columnCount + columnIndex] = unknownToString(value);
            return this;
        }

        @Override
        public ArraySheet build() {
            return new ArraySheet(name, rowCount, columnCount, values.clone(), false);
        }
    }

    private static final class UnboundedBuilder extends Builder {

        private final List<Serializable> valuesAsList;
        private final PrivateIntList rows;
        private final PrivateIntList cols;
        private int maxRowIndex;
        private int maxColumnIndex;
        private String name;

        public UnboundedBuilder() {
            this.valuesAsList = new ArrayList<>();
            this.rows = new PrivateIntList();
            this.cols = new PrivateIntList();
            this.maxRowIndex = -1;
            this.maxColumnIndex = -1;
            this.name = "";
        }

        @Override
        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        @Override
        public Builder clear() {
            valuesAsList.clear();
            rows.clear();
            cols.clear();
            maxRowIndex = -1;
            maxColumnIndex = -1;
            name = "";
            return this;
        }

        @Override
        public Builder value(int rowIndex, int columnIndex, Object value) {
            if (maxRowIndex < rowIndex) {
                maxRowIndex = rowIndex;
            }
            if (maxColumnIndex < columnIndex) {
                maxColumnIndex = columnIndex;
            }
            valuesAsList.add(unknownToString(value));
            rows.add(rowIndex);
            cols.add(columnIndex);
            return this;
        }

        @Override
        public Builder row(int rowIndex, int columnIndex, Object... row) {
            if (maxRowIndex < rowIndex) {
                maxRowIndex = rowIndex;
            }
            int lastColumnIndex = columnIndex + row.length;
            if (maxColumnIndex < lastColumnIndex) {
                maxColumnIndex = lastColumnIndex;
            }
            for (int j = 0; j < row.length; j++) {
                valuesAsList.add(unknownToString(row[j]));
                rows.add(rowIndex);
                cols.add(columnIndex + j);
            }
            return this;
        }

        @Override
        public ArraySheet build() {
            int rowCount = maxRowIndex + 1;
            int columnCount = maxColumnIndex + 1;
            Serializable[] values = new Serializable[rowCount * columnCount];
            for (int i = 0; i < valuesAsList.size(); i++) {
                int index = rows.get(i) * columnCount + cols.get(i);
                values[index] = valuesAsList.get(i);
            }
            return new ArraySheet(name, rowCount, columnCount, values, false);
        }
    }
    //</editor-fold>
}

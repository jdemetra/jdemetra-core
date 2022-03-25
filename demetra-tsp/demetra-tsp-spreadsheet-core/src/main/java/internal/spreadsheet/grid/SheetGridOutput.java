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
package internal.spreadsheet.grid;

import demetra.tsprovider.grid.GridDataType;
import ec.util.spreadsheet.helpers.ArraySheet;
import demetra.tsprovider.grid.GridOutput;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class SheetGridOutput implements GridOutput {

    private final Predicate<Class<?>> isSupportedDataType;
    private final ZoneId zoneId = ZoneId.systemDefault();
    private ArraySheet result = null;

    @Override
    public Set<GridDataType> getDataTypes() {
        EnumSet<GridDataType> dataTypes = EnumSet.noneOf(GridDataType.class);
        if (isSupportedDataType.test(String.class)) {
            dataTypes.add(GridDataType.STRING);
        }
        if (isSupportedDataType.test(Double.class)) {
            dataTypes.add(GridDataType.DOUBLE);
        }
        if (isSupportedDataType.test(Date.class)) {
            dataTypes.add(GridDataType.LOCAL_DATE_TIME);
        }
        return dataTypes;
    }

    @Override
    public Stream open(String name, int rows, int columns) {
        return new SheetGridOutputStream(ArraySheet.builder(rows, columns).name(name));
    }

    public ArraySheet getResult() {
        return result;
    }

    @lombok.RequiredArgsConstructor
    private final class SheetGridOutputStream implements GridOutput.Stream {

        @lombok.NonNull
        private final ArraySheet.Builder sheet;

        private int row = 0;
        private int column = 0;

        @Override
        public void writeCell(Object value) {
            moveTo(row, column);
            sheet.value(row, column, toSheetValue(value));
            column++;
        }

        @Override
        public void writeEndOfRow() {
            row++;
            column = 0;
        }

        @Override
        public void close() {
            result = sheet.build();
        }

        private void moveTo(int row, int column) {
            this.row = row;
            this.column = column;
        }

        private Object toSheetValue(Object value) {
            return value instanceof LocalDateTime ? fromDateTime((LocalDateTime) value) : value;
        }

        private Date fromDateTime(LocalDateTime o) {
            return Date.from(o.atZone(zoneId).toInstant());
        }
    }
}

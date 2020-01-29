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
import ec.util.spreadsheet.Sheet;
import demetra.tsprovider.grid.GridInput;
import java.io.IOException;
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
@lombok.AllArgsConstructor(staticName = "of")
public final class SheetGridInput implements GridInput {

    private final Sheet sheet;
    private final Predicate<Class<?>> isSupportedDataType;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Override
    public Set<GridDataType> getDataTypes() {
        EnumSet<GridDataType> dataTypes = EnumSet.noneOf(GridDataType.class);
        if (isSupportedDataType.test(String.class)) {
            dataTypes.add(GridDataType.STRING);
        }
        if (isSupportedDataType.test(Double.class)) {
            dataTypes.add(GridDataType.DOUBLE);
        }
        if (isSupportedDataType.test(LocalDateTime.class)) {
            dataTypes.add(GridDataType.LOCAL_DATE_TIME);
        }
        return dataTypes;
    }

    @Override
    public String getName() {
        return sheet.getName();
    }

    @Override
    public Stream open() throws IOException {
        return new SheetGridInputStream();
    }

    private final class SheetGridInputStream implements GridInput.Stream {

        private int row = -1;
        private int col = -1;

        @Override
        public boolean readCell() {
            col++;
            return col < sheet.getColumnCount();
        }

        @Override
        public boolean readRow() {
            col = -1;
            row++;
            return row < sheet.getRowCount();
        }

        @Override
        public Object getCell() {
            Object result = sheet.getCellValue(row, col);
            return result instanceof Date ? toDateTime((Date) result) : result;
        }

        @Override
        public void close() {
        }

        private LocalDateTime toDateTime(Date o) {
            return LocalDateTime.ofInstant(o.toInstant(), zoneId);
        }
    }
}

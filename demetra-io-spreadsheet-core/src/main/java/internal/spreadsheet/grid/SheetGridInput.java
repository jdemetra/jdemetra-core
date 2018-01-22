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

import ec.util.spreadsheet.Sheet;
import demetra.tsprovider.grid.GridInput;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class SheetGridInput implements GridInput {

    private final Sheet sheet;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Override
    public int getRowCount() {
        return sheet.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return sheet.getColumnCount();
    }

    @Override
    public Object getValue(int i, int j) {
        Object result = sheet.getCellValue(i, j);
        return result instanceof Date ? toDateTime((Date) result) : result;
    }

    private LocalDateTime toDateTime(Date o) {
        return LocalDateTime.ofInstant(o.toInstant(), zoneId);
    }
}

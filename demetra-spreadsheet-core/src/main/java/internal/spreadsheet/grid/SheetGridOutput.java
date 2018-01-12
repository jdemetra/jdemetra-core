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

import ec.util.spreadsheet.helpers.ArraySheet;
import demetra.tsprovider.grid.GridOutput;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class SheetGridOutput implements GridOutput {

    private final ArraySheet.Builder sheet;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Override
    public void setName(String name) {
        sheet.name(name);
    }

    @Override
    public void setRow(int row, int column, Iterator<?> values) {
        sheet.row(row, column, values);
    }

    @Override
    public void setColumn(int row, int column, Iterator<?> values) {
        sheet.column(row, column, values);
    }

    @Override
    public void setValue(int row, int column, Object value) {
        sheet.value(row, column, value instanceof LocalDateTime ? fromDateTime((LocalDateTime) value) : value);
    }

    private Date fromDateTime(LocalDateTime o) {
        return Date.from(o.atZone(zoneId).toInstant());
    }
}

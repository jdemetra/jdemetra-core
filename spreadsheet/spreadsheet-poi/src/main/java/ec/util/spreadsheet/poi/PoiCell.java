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
package ec.util.spreadsheet.poi;

import java.util.Date;
import javax.annotation.Nonnull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 *
 * @author Philippe Charles
 */
//@FlyweightPattern
final class PoiCell extends ec.util.spreadsheet.Cell {

    private Cell cell = null;

    @Nonnull
    PoiCell withCell(@Nonnull Cell cell) {
        this.cell = cell;
        return this;
    }

    @Override
    public String getString() {
        return cell.getStringCellValue();
    }

    @Override
    public Date getDate() {
        return cell.getDateCellValue();
    }

    @Override
    public Number getNumber() {
        return cell.getNumericCellValue();
    }

    private int getFinalType() {
        int result = cell.getCellType();
        return result != Cell.CELL_TYPE_FORMULA ? result : cell.getCachedFormulaResultType();
    }

    @Override
    public boolean isNumber() {
        return Cell.CELL_TYPE_NUMERIC == getFinalType();
    }

    @Override
    public boolean isString() {
        return Cell.CELL_TYPE_STRING == getFinalType();
    }

    @Override
    public boolean isDate() {
        switch (getFinalType()) {
            case Cell.CELL_TYPE_STRING:
                // would have thrown IllegalStateException in DateUtil#isCellDateFormatted(Cell)
                return false;
            case Cell.CELL_TYPE_BLANK:
                // would have thrown NullPointerException in Cell#getDateCellValue()
                return false;
            default:
                return DateUtil.isCellDateFormatted(cell);
        }
    }
}

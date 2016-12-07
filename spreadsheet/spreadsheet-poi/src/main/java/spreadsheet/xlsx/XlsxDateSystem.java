/*
 * Copyright 2016 National Bank of Belgium
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
package spreadsheet.xlsx;

import java.util.Calendar;
import java.util.Date;
import javax.annotation.Nonnull;
import spreadsheet.xlsx.internal.XlsxDateSystems;

/**
 * Class that deals with the internal representation of dates in Excel.
 *
 * @author Philippe Charles
 * @since 2.2.0
 * @see
 * https://support.office.com/en-us/article/Excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
 */
public interface XlsxDateSystem {

    /**
     * Checks if the specified value may represent an Excel date.
     *
     * @param value the value to check
     * @return true if the value may be an Excel date, false otherwise
     */
    boolean isValidExcelDate(double value);

    /**
     * Convert an Excel date to java date.
     *
     * @param calendar the resource used to compute the java date
     * @param date an Excel date
     * @return a non-null java date
     */
    @Nonnull
    Date getJavaDate(@Nonnull Calendar calendar, double date);

    /**
     * Gets the default implementation for the specified date system.
     *
     * @param date1904
     * @return a non-null default implementation
     */
    @Nonnull
    static XlsxDateSystem getDefault(boolean date1904) {
        return date1904 ? XlsxDateSystems.X1904 : XlsxDateSystems.X1900;
    }
}

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

import java.util.Calendar;
import java.util.Date;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 *
 * @author Philippe Charles
 */
final class DateUtil2 {

    private DateUtil2() {
        // static class
    }

    /**
     * Same as {@link DateUtil#getJavaDate(double, boolean)} but with calendar
     * as first parameter to reduce memory usage.
     *
     * @param calendar
     * @param date
     * @param use1904windowing
     * @return
     */
    public static Date getJavaDate(Calendar calendar, double date, boolean use1904windowing) {
        int wholeDays = (int) Math.floor(date);
        int millisecondsInDay = (int) ((date - wholeDays) * DateUtil.DAY_MILLISECONDS + 0.5);
        DateUtil.setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing);
        return calendar.getTime();
    }
}

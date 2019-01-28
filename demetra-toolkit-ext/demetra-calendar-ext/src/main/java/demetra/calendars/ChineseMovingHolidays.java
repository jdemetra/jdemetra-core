/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import demetra.calendars.internal.Chinese;
import demetra.calendars.internal.Date;
import java.time.LocalDate;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ChineseMovingHolidays {
    
    public MovingHolidayProvider newYear() {
        return (LocalDate start, LocalDate end) -> {
            int y0 = start.getYear();
            int y1 = end.getYear();
            int n = y1 - y0 + 1;
            LocalDate e0 = Date.fixedToLocalDate(Chinese.newYear(y0));
            LocalDate e1 = Date.fixedToLocalDate(Chinese.newYear(y1));
            boolean use0 = true;
            if (start.isAfter(e0)) {
                --n;
                use0 = false;
            }
            boolean use1 = true;
            if (!e1.isBefore(end)) {
                --n;
                use1 = false;
            }
            LocalDate[] ee = new LocalDate[n];
            int i = 0;
            if (use0) {
                ee[i++] = e0;
            }
            while (++y0 < y1) {
                ee[i++] = Date.fixedToLocalDate(Chinese.newYear(y0));
            }
            if (use1) {
                ee[i] = e1;
            }
            return ee;
        };
    }
    
}

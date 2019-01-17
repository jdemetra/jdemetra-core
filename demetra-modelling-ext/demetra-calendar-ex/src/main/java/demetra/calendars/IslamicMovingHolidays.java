/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import demetra.calendars.internal.Islamic;
import demetra.calendars.internal.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class IslamicMovingHolidays {

    public MovingHolidayProvider rasElAm() {
        return (LocalDate start, LocalDate end) -> {
            int y0 = start.getYear();
            int y1 = end.getYear();
            int n = y1 - y0 + 1;
            List<LocalDate> all = new ArrayList<>();
            long[] l = Islamic.rasElAm(y0);
            if (y0 == y1) {
                for (int i = 0; i < l.length; ++i) {
                    LocalDate d = Date.fixedToLocalDate(l[i]);
                    if (!d.isBefore(start) && d.isBefore(end)) {
                        all.add(d);
                    }
                }
            } else {
                for (int i = 0; i < l.length; ++i) {
                    LocalDate d = Date.fixedToLocalDate(l[i]);
                    if (!d.isBefore(start)) {
                        all.add(d);
                    }
                }
                for (int y = y0 + 1; y < y1; ++y) {
                    l = Islamic.rasElAm(y);
                    for (int i = 0; i < l.length; ++i) {
                        all.add(Date.fixedToLocalDate(l[i]));
                    }
                }
                l = Islamic.rasElAm(y1);
                for (int i = 0; i < l.length; ++i) {
                    LocalDate d = Date.fixedToLocalDate(l[i]);
                    if (d.isBefore(end)) {
                        all.add(d);
                    }
                }
            }
            return all.toArray(new LocalDate[all.size()]);
        };
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.specialcalendar;

import demetra.timeseries.calendars.Easter;
import java.time.LocalDate;
import jdplus.modelling.regression.MovingHolidayProvider;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class JulianMovingHolidays {

    public LocalDate[] easter(LocalDate start, LocalDate end) {
        int y0 = start.getYear();
        int y1 = end.getYear();
        int n = y1 - y0 + 1;
        LocalDate e0 = Easter.julianEaster(y0, true);
        LocalDate e1 = Easter.julianEaster(y1, true);
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
        if (n == 0)
            return new LocalDate[0];
        if (e0.equals(e1))
            return new LocalDate[]{e0};
        LocalDate[] ee = new LocalDate[n];
        int i = 0;
        if (use0) {
            ee[i++] = e0;
        }
        while (++y0 < y1) {
            ee[i++] = Easter.julianEaster(y0, true);
        }
        if (use1) {
            ee[i] = e1;
        }
        return ee;
    }

    private final String EASTER = "julian.easter";

    @ServiceProvider(MovingHolidayProvider.class)
    public static class EasterProvider implements MovingHolidayProvider {

        @Override
        public String identifier() {
            return EASTER;
        }

        @Override
        public LocalDate[] holidays(LocalDate start, LocalDate end) {
            return easter(start, end);
        }
    }
}

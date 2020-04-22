/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.specialcalendar.r;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.specialcalendar.ChineseMovingHolidays;
import jdplus.specialcalendar.GregorianMovingHolidays;
import jdplus.specialcalendar.IslamicMovingHolidays;
import jdplus.specialcalendar.JulianMovingHolidays;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {

    public String[] easter(String start, String end) {
        LocalDate pstart = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate pend = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);

        return convert(GregorianMovingHolidays.easter(pstart, pend));
    }

    public String[] julianEaster(String start, String end) {
        LocalDate pstart = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate pend = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);

        return convert(JulianMovingHolidays.easter(pstart, pend));
    }

    public String[] chineseNewYear(String start, String end) {
        LocalDate pstart = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate pend = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);

        return convert(ChineseMovingHolidays.newYear(pstart, pend));
    }

    public String[] rasElAm(String start, String end) {
        LocalDate pstart = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate pend = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);

        return convert(IslamicMovingHolidays.rasElAm(pstart, pend));
    }

    String[] convert(LocalDate[] holidays) {
        String[] all = new String[holidays.length];
        for (int i = 0; i < all.length; ++i) {
            all[i] = holidays[i].format(DateTimeFormatter.ISO_DATE);
        }
        return all;
    }
}

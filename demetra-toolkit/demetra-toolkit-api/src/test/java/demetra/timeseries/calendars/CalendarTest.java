/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import demetra.maths.matrices.MatrixType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class CalendarTest {

    public static final Calendar belgium;

    static {
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new Holiday(new FixedDay(7, 21)));
        holidays.add(new Holiday(FixedDay.ALLSAINTSDAY));
        holidays.add(new Holiday(FixedDay.ARMISTICE));
        holidays.add(new Holiday(FixedDay.ASSUMPTION));
        holidays.add(new Holiday(FixedDay.CHRISTMAS));
        holidays.add(new Holiday(FixedDay.MAYDAY));
        holidays.add(new Holiday(FixedDay.NEWYEAR));
        holidays.add(new Holiday(EasterRelatedDay.ASCENSION));
        holidays.add(new Holiday(EasterRelatedDay.EASTERMONDAY));
        holidays.add(new Holiday(EasterRelatedDay.WHITMONDAY));

        belgium = new Calendar(holidays.toArray(new Holiday[holidays.size()]), true);
    }

    public CalendarTest() {
    }

    @Test
    public void testBelgium() {
        MatrixType holidays = CalendarUtility.holidays(belgium.getHolidays(), TsDomain.of(TsPeriod.monthly(1980, 1), 360));
//        System.out.println(MatrixType.format(holidays));
        double[][] z = CalendarUtility.longTermMean(belgium.getHolidays(), 12);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = CalendarUtility.longTermMean(belgium.getHolidays(), 6);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = CalendarUtility.longTermMean(belgium.getHolidays(), 4);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = CalendarUtility.longTermMean(belgium.getHolidays(), 3);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = CalendarUtility.longTermMean(belgium.getHolidays(), 2);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
    }

    private double sum(double[][] x) {
        double s = 0;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != null) {
                for (int j = 0; j < x[i].length; ++j) {
                    s += x[i][j];
                }
            }
        }
        return s;
    }
}

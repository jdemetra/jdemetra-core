/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.timeseries.calendars;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class HolidaysUtilityTest {

    public static final Calendar belgium;

    static {
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(new FixedDay(7, 21));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ARMISTICE);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(PrespecifiedHoliday.builder()
                .event(DayEvent.Ascension)
                .build());
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);

        belgium = new Calendar(holidays.toArray(new Holiday[holidays.size()]));
    }

    public HolidaysUtilityTest() {
    }

    @Test
    public void testBelgium() {
        Matrix holidays = HolidaysUtility.holidays(belgium.getHolidays(), TsDomain.of(TsPeriod.monthly(1980, 1), 360));
//        System.out.println(Matrix.format(holidays));
        double[][] z = HolidaysUtility.longTermMean(belgium.getHolidays(), 12);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = HolidaysUtility.longTermMean(belgium.getHolidays(), 6);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = HolidaysUtility.longTermMean(belgium.getHolidays(), 4);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = HolidaysUtility.longTermMean(belgium.getHolidays(), 3);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
        z = HolidaysUtility.longTermMean(belgium.getHolidays(), 2);
        assertEquals(sum(z), belgium.getHolidays().length, 1e-9);
    }

    @Test
    public void testDailyBelgium() {
        FastMatrix M = FastMatrix.make(365, belgium.getHolidays().length);
        HolidaysUtility.fillDays(belgium.getHolidays(), M, LocalDate.of(2021, 1, 1), new int[]{6, 7}, false);
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            assertEquals(M.column(i).sum(), 1, 1e-15);
        }
//        System.out.println(M);
//        System.out.println();
        M = FastMatrix.make(365, belgium.getHolidays().length);
        HolidaysUtility.fillNextWorkingDays(belgium.getHolidays(), M, LocalDate.of(2021, 1, 1), new int[]{6, 7});
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            assertEquals(M.column(i).sum(), 1, 1e-15);
        }
//        System.out.println(M);
//        System.out.println();
        M = FastMatrix.make(365, belgium.getHolidays().length);
        HolidaysUtility.fillPreviousWorkingDays(belgium.getHolidays(), M, LocalDate.of(2021, 1, 1), new int[]{6, 7});
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            assertEquals(M.column(i).sum(), 1, 1e-15);
        }
//        System.out.println(M);
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.maths.MatrixType;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holidays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TsUtilityTest {
    
    public TsUtilityTest() {
    }

    @Test
    public void testHolidays() {
        Holidays holidays=new Holidays();
//        addDefault(holidays);
//        TsUtility.add(holidays, "Christmas", -1, 1, false);
//        TsUtility.add(holidays, "Easter", -1, 1, false);
        TsUtility.add(holidays, "Easter", 0, 1, false);
        TsUtility.add(holidays, "EasterMonday", 0, 1, false);
        MatrixType rslt = TsUtility.holidays(holidays, "2002-12-31", 600, "Default");
        System.out.println(MatrixType.format(rslt));
    }
    
    private static void addDefault(Holidays holidays) {
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.WHITMONDAY);
    }
}

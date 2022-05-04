/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.benchmarking.r;

import demetra.calendarization.CalendarizationResults;
import demetra.data.DoubleSeq;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.r.TsUtility;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class CalendarizationTest {

    public CalendarizationTest() {
    }

    @Test
    public void testSomeMethod() {
        String[] starts = new String[]{
            "1980-01-01", "1983-01-01", "1986-01-01", "1988-01-01"
        };
        String[] ends = new String[]{
            "1982-12-31", "1985-12-31", "1987-12-31", "2000-12-31"
        };
        double[] data = new double[]{100, -100, 10, 20};
        CalendarTimeSeries ts = TsUtility.of(starts, ends, data);
        CalendarizationResults rslt = Calendarization.process(ts, 12, null, "2001-11-30", null, true);
//        System.out.println(rslt.getAggregatedSeries());
    }

}

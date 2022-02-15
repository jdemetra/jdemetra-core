/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.fractionalairline;

import jdplus.fractionalairline.FractionalAirlineKernel;
import demetra.data.Data;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import jdplus.math.matrices.FastMatrix;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import jdplus.timeseries.calendars.HolidaysUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jdplus.highfreq.FractionalAirlineEstimation;
import demetra.highfreq.FractionalAirlineSpec;
import java.io.InputStream;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineKernelTest {

    public FractionalAirlineKernelTest() {
    }

    @Test
    public void testDaily() throws IOException {
        InputStream stream = Data.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        Holiday[] france = france();
        FastMatrix hol = FastMatrix.make(edf.getRowsCount(), france.length);
        HolidaysUtility.fillDays(france, hol, LocalDate.of(1996, 1, 1), new int[]{7}, false);
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .X(hol)
                .periodicities(new double[]{7, 365.25})
                .build();
        FractionalAirlineEstimation rslt = FractionalAirlineKernel.process(spec);
        assertTrue(rslt != null);

//        System.out.println(rslt.getCoefficients());
//        System.out.println(DoubleSeq.of(rslt.tstats()));
//        System.out.println(rslt.getLikelihood().getLogLikelihood());

        spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .X(hol)
                .periodicities(new double[]{7, 365})
                //                .outliers(new String[]{"ao", "wo"})
                //                .criticalValue(6)
                .differencingOrder(2)
                .build();
        rslt = FractionalAirlineKernel.process(spec);
        assertTrue(rslt != null);
//        System.out.println(rslt.getCoefficients());
//        System.out.println(DoubleSeq.of(rslt.tstats()));
//        System.out.println(rslt.getLikelihood().getLogLikelihood());

        spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .periodicities(new double[]{7, 365.25})
                .differencingOrder(2)
                .build();
        rslt = FractionalAirlineKernel.process(spec);
//        System.out.println(rslt.getLikelihood().getLogLikelihood());
    }

    @Test
    public void testWeekly() {
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(WeeklyData.US_CLAIMS)
                .periodicities(new double[]{365.25 / 7})
                .precision(1e-7)
                .build();
        FractionalAirlineEstimation rslt = FractionalAirlineKernel.process(spec);
//        System.out.println(rslt.getLikelihood().getLogLikelihood());
        spec = FractionalAirlineSpec.builder()
                .y(WeeklyData.US_CLAIMS)
                .periodicities(new double[]{52})
                .precision(1e-7)
                .build();
        rslt = FractionalAirlineKernel.process(spec);
//        System.out.println(rslt.getLikelihood().getLogLikelihood());
    }

    private static void addDefault(List<Holiday> holidays) {
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.WHITMONDAY);
    }

    public static Holiday[] france() {
        List<Holiday> holidays = new ArrayList<>();
        addDefault(holidays);
        holidays.add(new FixedDay(5, 8));
        holidays.add(new FixedDay(7, 14));
        holidays.add(FixedDay.ARMISTICE);
        return holidays.stream().toArray(i -> new Holiday[i]);
    }
}

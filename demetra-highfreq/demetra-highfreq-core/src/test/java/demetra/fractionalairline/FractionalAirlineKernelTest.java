/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.fractionalairline;

import jdplus.fractionalairline.FractionalAirlineKernel;
import demetra.data.Data;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import jdplus.timeseries.calendars.HolidaysUtility;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.highfreq.FractionalAirlineSpec;
import demetra.math.matrices.MatrixType;
import java.io.InputStream;

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
        MatrixType edf = MatrixSerializer.read(stream);
        Holiday[] france = france();
        Matrix hol = Matrix.make(edf.getRowsCount(), france.length);
        HolidaysUtility.fillDays(france, hol, LocalDate.of(1996, 1, 1), false);
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .X(hol)
                .periodicities(new double[]{7, 365.25})
                .build();
        FractionalAirlineEstimation rslt = FractionalAirlineKernel.process(spec);
        assertTrue(rslt != null);
        
        System.out.println(DoubleSeq.of(rslt.getCoefficients()));
        System.out.println(DoubleSeq.of(rslt.tstats()));
        System.out.println(rslt.getLikelihood().getLogLikelihood());
        
        spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .X(hol)
                .periodicities(new double[]{7, 365.25})
                .outliers(new String[]{"ao", "wo"})
                .criticalValue(6)
                .build();
        rslt = FractionalAirlineKernel.process(spec);
        assertTrue(rslt != null);
        System.out.println(DoubleSeq.of(rslt.getCoefficients()));
        System.out.println(DoubleSeq.of(rslt.tstats()));
        System.out.println(rslt.getLikelihood().getLogLikelihood());
        
        spec = FractionalAirlineSpec.builder()
                .y(edf.column(0).fn(z -> Math.log(z)).toArray())
                .periodicities(new double[]{7, 365.25})
                .precision(1e-12)
                .build();
        rslt = FractionalAirlineKernel.process(spec);
        System.out.println(rslt.getLikelihood().getLogLikelihood());
    }
    
    @Test
    public void testWeekly() {
        FractionalAirlineSpec spec = FractionalAirlineSpec.builder()
                .y(WeeklyData.US_CLAIMS)
                .periodicities(new double[]{365.25 / 7})
                .precision(1e-12)
                .build();
        FractionalAirlineEstimation rslt = FractionalAirlineKernel.process(spec);
        System.out.println(rslt.getLikelihood().getLogLikelihood());
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

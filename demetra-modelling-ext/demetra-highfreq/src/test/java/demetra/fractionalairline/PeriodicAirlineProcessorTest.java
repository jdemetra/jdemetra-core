/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.fractionalairline;

import demetra.arima.ArimaModel;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.regarima.RegArimaEstimation;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holidays;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodicAirlineProcessorTest {

    public PeriodicAirlineProcessorTest() {
    }

    @Test
    public void testDaily() throws URISyntaxException, IOException {
        URI uri = MultiPeriodicAirlineMapping.class.getResource("/edf.txt").toURI();
        MatrixType edf = MatrixSerializer.read(new File(uri));
        Holidays france = france();
        Matrix hol = Matrix.make(edf.getRowsCount(), france.getCount());
        demetra.timeseries.calendars.Utility.fillDays(france, hol, LocalDate.of(1996, 1, 1));
        RegArimaEstimation<ArimaModel> rslt = PeriodicAirlineProcessor.process(Doubles.fastFn(edf.column(0), z->Math.log(z)), hol, new double[]{7, 365.25}, 1e-12);
        assertTrue(rslt != null);
        ConcentratedLikelihood cll = rslt.getConcentratedLikelihood();
        System.out.println(cll.coefficients());
        System.out.println(DoubleSequence.ofInternal(cll.tstats(0, false)));
        System.out.println(cll.logLikelihood());

        rslt = PeriodicAirlineProcessor.process(Doubles.fastFn(edf.column(0), z->Math.log(z)), null, new double[]{7, 365.25}, 1e-12);
        cll = rslt.getConcentratedLikelihood();
        System.out.println(cll.logLikelihood());
    }

    @Test
    public void testWeekly() {
        double ll = PeriodicAirlineProcessor.process(DoubleSequence.of(WeeklyData.US_CLAIMS), null, 365.25 / 7, 1e-9).getConcentratedLikelihood().logLikelihood();
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

    public static Holidays france() {
        Holidays holidays = new Holidays();
        addDefault(holidays);
        holidays.add(new FixedDay(5, 8));
        holidays.add(new FixedDay(7, 14));
        holidays.add(FixedDay.ARMISTICE);
        return holidays;
    }
}

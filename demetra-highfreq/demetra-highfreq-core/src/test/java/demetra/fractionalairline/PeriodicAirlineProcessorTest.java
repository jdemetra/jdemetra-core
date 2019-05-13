/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.fractionalairline;

import demetra.arima.ArimaModel;
import demetra.data.Data;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.regarima.RegArimaEstimation;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.HolidaysUtility;
import demetra.timeseries.calendars.IHoliday;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodicAirlineProcessorTest {

    public PeriodicAirlineProcessorTest() {
    }

    @Test
    @Ignore
    public void testDaily() throws URISyntaxException, IOException {
        URI uri = Data.class.getResource("/edf.txt").toURI();
        Matrix edf = MatrixSerializer.read(new File(uri));
        Holiday[] france = france();
        CanonicalMatrix hol = CanonicalMatrix.make(edf.getRowsCount(), france.length);
        HolidaysUtility.fillDays(france, hol, LocalDate.of(1996, 1, 1), false);
        RegArimaEstimation<ArimaModel> rslt = PeriodicAirlineProcessor.process(edf.column(0).fn(z->Math.log(z)), hol, new double[]{7, 365.25}, 1e-12);
        assertTrue(rslt != null);
        ConcentratedLikelihoodWithMissing cll = rslt.getConcentratedLikelihood();
        System.out.println(cll.coefficients());
        System.out.println(DoubleSeq.of(cll.tstats(0, false)));
        System.out.println(cll.logLikelihood());

        rslt = PeriodicAirlineProcessor.process(edf.column(0).fn(z->Math.log(z)), null, new double[]{7, 365.25}, 1e-12);
        cll = rslt.getConcentratedLikelihood();
        System.out.println(cll.logLikelihood());
    }

    //@Test
    public void testWeekly() {
        double ll = PeriodicAirlineProcessor.process(DoubleSeq.copyOf(WeeklyData.US_CLAIMS), null, 365.25 / 7, 1e-9).getConcentratedLikelihood().logLikelihood();
    }

    private static void addDefault(List<IHoliday> holidays) {
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
        List<IHoliday> holidays = new ArrayList<>();
        addDefault(holidays);
        holidays.add(new FixedDay(5, 8));
        holidays.add(new FixedDay(7, 14));
        holidays.add(FixedDay.ARMISTICE);
        return holidays.stream().map(h->new Holiday(h)).toArray(i->new Holiday[i]);
    }
}

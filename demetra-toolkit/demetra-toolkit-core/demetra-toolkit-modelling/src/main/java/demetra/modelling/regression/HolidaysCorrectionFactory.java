/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.maths.matrices.FastMatrix;
import demetra.modelling.regression.HolidaysCorrectedTradingDays.HolidaysCorrector;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.CalendarUtility;
import demetra.timeseries.calendars.ChainedCalendar;
import demetra.timeseries.calendars.CompositeCalendar;
import demetra.util.WeightedItem;
import java.time.LocalDate;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.maths.matrices.MatrixFactory;
import demetra.maths.matrices.MatrixType;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
public class HolidaysCorrectionFactory implements RegressionVariableFactory<HolidaysCorrectedTradingDays> {

    public static HolidaysCorrectionFactory FACTORY = new HolidaysCorrectionFactory();

    public static HolidaysCorrector corrector(String name, CalendarManager mgr) {
        CalendarDefinition cur = mgr.get(name);
        if (cur == null) {
            return null;
        }
        if (cur instanceof Calendar) {
            return corrector((Calendar) cur);
        } else if (cur instanceof ChainedCalendar) {
            ChainedCalendar ccur = (ChainedCalendar) cur;
            HolidaysCorrector beg = corrector(ccur.getFirst(), mgr);
            HolidaysCorrector end = corrector(ccur.getSecond(), mgr);
            if (beg == null || end == null) {
                return null;
            }
            return corrector(beg, end, ccur.getBreakDate());
        } else if (cur instanceof CompositeCalendar) {
            CompositeCalendar ccur = (CompositeCalendar) cur;
            WeightedItem<String>[] calendars = ccur.getCalendars();
            HolidaysCorrector[] corr=new HolidaysCorrector[calendars.length];
            double[] weights=new double[calendars.length];
            for (int i=0; i<calendars.length; ++i){
                corr[i]=corrector(calendars[i].getItem(), mgr);
                if (corr[i] == null)
                    return null;
                weights[i]=calendars[i].getWeight();
            }
            return corrector(corr, weights);
        } else {
            return null;
        }
    }

    /**
     * Usual corrections: the holidays are considered as Sundays
     *
     * @param calendar
     * @return
     */
    public static HolidaysCorrector corrector(final Calendar calendar) {
        return (TsDomain domain) -> {
            MatrixType M = CalendarUtility.holidays(calendar.getHolidays(), domain);
            CanonicalMatrix Mc = CanonicalMatrix.of(M);
            if (calendar.isMeanCorrection()) {
                TsPeriod start = domain.getStartPeriod();
                int freq = domain.getAnnualFrequency();
                double[][] mean = CalendarUtility.longTermMean(calendar.getHolidays(), freq);
                if (mean != null) {
                    int pstart = start.annualPosition();
                    DataBlock[] Mean = new DataBlock[freq];
                    for (int i = 0; i < freq; ++i) {
                        Mean[i] = mean[i] == null ? null : DataBlock.of(mean[i]);
                    }
                    int n = Mc.getRowsCount();
                    for (int i = 0; i < n; ++i) {
                        DataBlock m = Mean[(i + pstart) % freq];
                        if (m != null) {
                            DataBlock row = Mc.row(i);
                            row.sub(m);
                        }
                    }
                }
            }
            // we put in the last column the sum of all the other days
            // and we change the sign of the other days
            DataBlock cur = Mc.column(0);
            Mc.column(6).copy(cur);
            cur.chs();
            for (int i = 1; i < 6; ++i) {
                cur = Mc.column(i);
                Mc.column(6).add(cur);
                cur.chs();
            }
            return Mc;
        };
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector beg, final HolidaysCorrector end, final LocalDate breakDate) {
        return (TsDomain domain) -> {
            int n = domain.getLength();
            int pos = domain.indexOf(breakDate.atStartOfDay());
            if (pos > 0) {
                MatrixType M1 = beg.holidaysCorrection(domain.range(0, pos));
                MatrixType M2 = end.holidaysCorrection(domain.range(pos, n));
                return MatrixFactory.rowBind(M1, M2);
            } else if (pos >= -1) {
                return end.holidaysCorrection(domain);
            } else {
                return beg.holidaysCorrection(domain);
            }
        };
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector[] correctors, double[] weights) {
        return (TsDomain domain) -> {
            CanonicalMatrix M=CanonicalMatrix.of(correctors[0].holidaysCorrection(domain));
            M.mul(weights[0]);
            for (int i=1; i<correctors.length; ++i){
                CanonicalMatrix cur = CanonicalMatrix.of(correctors[i].holidaysCorrection(domain));
                M.addAY(weights[i], cur);
             }
            return M;
        };
    }

    private HolidaysCorrectionFactory() {
    }

    @Override
    public boolean fill(HolidaysCorrectedTradingDays var, TsPeriod start,FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        CanonicalMatrix days = CanonicalMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTdMatrix(start, days);
        MatrixType corr = var.getCorrector().holidaysCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            days.column(i).apply(corr.column(i), (a, b) -> a + b);
        }
        if (var.isContrast()) {
            GenericTradingDaysFactory.fillContrasts(var.getClustering(), days, buffer);
        } else {
            GenericTradingDaysFactory.fillNoContrasts(var.getClustering(), var.isNormalized() ? start : null, days, buffer);
        }
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(HolidaysCorrectedTradingDays var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

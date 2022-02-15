/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import jdplus.data.DataBlock;
import nbbrd.design.Development;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays.HolidaysCorrector;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.ChainedCalendar;
import demetra.timeseries.calendars.CompositeCalendar;
import demetra.util.WeightedItem;
import java.time.LocalDate;
import demetra.timeseries.calendars.CalendarDefinition;
import jdplus.math.matrices.MatrixFactory;
import jdplus.math.matrices.FastMatrix;
import jdplus.timeseries.calendars.HolidaysUtility;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.math.matrices.Matrix;
import java.time.DayOfWeek;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
public class HolidaysCorrectionFactory implements RegressionVariableFactory<HolidaysCorrectedTradingDays> {

    public static HolidaysCorrectionFactory FACTORY = new HolidaysCorrectionFactory();

    public static HolidaysCorrector corrector(String name, CalendarManager mgr, DayOfWeek hol, boolean meanCorrection) {
        CalendarDefinition cur = mgr.get(name);
        if (cur == null) {
            return null;
        }
        return corrector(cur, mgr, hol, meanCorrection);
    }

    public static HolidaysCorrector corrector(CalendarDefinition cur, CalendarManager mgr, DayOfWeek hol, boolean meanCorrection) {
        if (cur instanceof Calendar) {
            return corrector((Calendar) cur, hol, meanCorrection);
        } else if (cur instanceof ChainedCalendar) {
            ChainedCalendar ccur = (ChainedCalendar) cur;
            HolidaysCorrector beg = corrector(ccur.getFirst(), mgr, hol, meanCorrection);
            HolidaysCorrector end = corrector(ccur.getSecond(), mgr, hol, meanCorrection);
            if (beg == null || end == null) {
                return null;
            }
            return corrector(beg, end, ccur.getBreakDate());
        } else if (cur instanceof CompositeCalendar) {
            CompositeCalendar ccur = (CompositeCalendar) cur;
            WeightedItem<String>[] calendars = ccur.getCalendars();
            HolidaysCorrector[] corr = new HolidaysCorrector[calendars.length];
            double[] weights = new double[calendars.length];
            for (int i = 0; i < calendars.length; ++i) {
                corr[i] = corrector(calendars[i].getItem(), mgr, hol, meanCorrection);
                if (corr[i] == null) {
                    return null;
                }
                weights[i] = calendars[i].getWeight();
            }
            return corrector(corr, weights);
        } else {
            return null;
        }
    }

    /**
     * Usual corrections: the holidays are considered as the specified day
     *
     * @param calendar
     * @param hol
     * @param meanCorrection Apply mean correction on the generated series
     * @return
     */
    public static HolidaysCorrector corrector(final Calendar calendar, DayOfWeek hol, boolean meanCorrection) {
        return (TsDomain domain) -> {
            int phol = hol.getValue() - 1;
            Matrix M = HolidaysUtility.holidays(calendar.getHolidays(), domain);
            FastMatrix Mc = FastMatrix.of(M);
            if (meanCorrection) {
                TsPeriod start = domain.getStartPeriod();
                int freq = domain.getAnnualFrequency();
                double[][] mean = HolidaysUtility.longTermMean(calendar.getHolidays(), freq);
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
            // we put in the hpos column the sum of all the other days
            // and we change the sign of the other days
            DataBlock chol = Mc.column(phol);
            chol.set(0);
            for (int i = 0; i < 7; ++i) {
                if (i != phol) {
                    DataBlock cur = Mc.column(i);
                    chol.add(cur);
                    cur.chs();
                }
            }
            return Mc;
        };
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector beg, final HolidaysCorrector end, final LocalDate breakDate) {
        return (TsDomain domain) -> {
            int n = domain.getLength();
            int pos = domain.indexOf(breakDate.atStartOfDay());
            if (pos > 0) {
                Matrix M1 = beg.holidaysCorrection(domain.range(0, pos));
                Matrix M2 = end.holidaysCorrection(domain.range(pos, n));
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
            FastMatrix M = FastMatrix.of(correctors[0].holidaysCorrection(domain));
            M.mul(weights[0]);
            for (int i = 1; i < correctors.length; ++i) {
                FastMatrix cur = FastMatrix.of(correctors[i].holidaysCorrection(domain));
                M.addAY(weights[i], cur);
            }
            return M;
        };
    }

    private HolidaysCorrectionFactory() {
    }

    @Override
    public boolean fill(HolidaysCorrectedTradingDays var, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        FastMatrix days = FastMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTdMatrix(start, days);
        Matrix corr = var.getCorrector().holidaysCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            days.column(i).apply(corr.column(i), (a, b) -> a + b);
        }
        if (var.getType() == GenericTradingDays.Type.CONTRAST) {
            GenericTradingDaysFactory.fillContrasts(var.getClustering(), days, buffer);
        } else {
            GenericTradingDaysFactory.fillNoContrasts(var.getClustering(), var.getType() == GenericTradingDays.Type.NORMALIZED,
                    var.getType() == GenericTradingDays.Type.MEANCORRECTED ? start : null, days, buffer);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(HolidaysCorrectedTradingDays var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

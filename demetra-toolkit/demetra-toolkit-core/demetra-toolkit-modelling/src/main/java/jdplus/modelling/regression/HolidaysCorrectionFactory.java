/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.data.DoubleSeq;
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
import jdplus.data.DataBlockIterator;

/**
 * The trading days are computed as the sum of the "normal" calendar and the corrections implied by the holidays (holidays are assimilated to a given day)
 * TD(i,t) = D(i, t) + C(i, t)
 * To remove the systematic seasonal component, we must compute the long term averages of each period (Jan..., Q1...)
 * TDc(i,t) = D(i,t) - mean D(i) + C(i,t) - mean C(i) 
 * 
 * 
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
public class HolidaysCorrectionFactory implements RegressionVariableFactory<HolidaysCorrectedTradingDays> {

    public static HolidaysCorrectionFactory FACTORY = new HolidaysCorrectionFactory();

    /**
     * 
     * @param name Name of the calendar
     * @param mgr 
     * @param hol The day to which the holidays correspond (holidays are considered as a "hol")
     * @param meanCorrection Long term mean corrections are applied on the calendar 
     * @return 
     */
    public static HolidaysCorrector corrector(String name, CalendarManager mgr, DayOfWeek hol, boolean meanCorrection) {
        CalendarDefinition cur = mgr.get(name);
        if (cur == null) {
            return null;
        }
        return corrector(cur, mgr, hol, meanCorrection);
    }

    @lombok.AllArgsConstructor
    static class CalendarCorrector implements HolidaysCorrector {

        final Calendar calendar;
        final DayOfWeek hol;
        final boolean meanCorrection;

        /**
         * C(i,t) if meanCorrection is false, C(i,t)-mean C(i) otherwise
         * @param domain
         * @return 
         */
        @Override
        public Matrix holidaysCorrection(TsDomain domain) {
            int phol = hol.getValue() - 1;
            Matrix C = HolidaysUtility.holidays(calendar.getHolidays(), domain);
            FastMatrix Cc = FastMatrix.of(C);
            if (meanCorrection) {
                TsPeriod start = domain.getStartPeriod();
                int freq = domain.getAnnualFrequency();
                double[][] mean = HolidaysUtility.longTermMean(calendar.getHolidays(), freq);
                if (mean != null) {
                    int pos = start.annualPosition();
                    DataBlock[] Mean = new DataBlock[freq];
                    for (int i = 0; i < freq; ++i) {
                        Mean[i] = mean[i] == null ? null : DataBlock.of(mean[i]);
                    }
                    int n = Cc.getRowsCount();
                    DataBlockIterator rows = Cc.rowsIterator();
                    while (rows.hasNext()){
                        DataBlock row = rows.next();
                        DataBlock m = Mean[pos++];
                        if (m != null) {
                             row.sub(m);
                        }
                        if (pos == freq)
                            pos=0;
                    }
                }
            }
            // we put in the hpos column the sum of all the other days
            // and we change the sign of the other days
            DataBlock chol = Cc.column(phol);
            chol.set(0);
            for (int i = 0; i < 7; ++i) {
                if (i != phol) {
                    DataBlock cur = Cc.column(i);
                    chol.add(cur);
                    cur.chs();
                }
            }
            return Cc;
        }

        @Override
        public DoubleSeq longTermYearlyCorrection() {
            if (calendar.isempty()) {
                return DoubleSeq.onMapping(7, i -> 0);
            }
            double[][] corr = HolidaysUtility.longTermMean(calendar.getHolidays(), 1);
            double[] c = corr[0];
            double sum = 0;
            int ihol = hol.getValue() - 1;
            for (int i = 0; i < c.length; ++i) {
                if (i != ihol) {
                    sum += c[i];
                }
            }
            for (int i = 0; i < c.length; ++i) {
                if (i != ihol) {
                    c[i] = -c[i];
                } else {
                    c[i] = sum;
                }
            }

            return DoubleSeq.of(c);
        }

    }

    public static HolidaysCorrector corrector(CalendarDefinition cur, CalendarManager mgr, DayOfWeek hol, boolean meanCorrection) {
        if (cur instanceof Calendar calendar) {
            return corrector(calendar, hol, meanCorrection);
        } else if (cur instanceof ChainedCalendar ccur) {
            HolidaysCorrector beg = corrector(ccur.getFirst(), mgr, hol, meanCorrection);
            HolidaysCorrector end = corrector(ccur.getSecond(), mgr, hol, meanCorrection);
            if (beg == null || end == null) {
                return null;
            }
            return corrector(beg, end, ccur.getBreakDate());
        } else if (cur instanceof CompositeCalendar ccur) {
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
        return new CalendarCorrector(calendar, hol, meanCorrection);
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector beg, final HolidaysCorrector end, final LocalDate breakDate) {
        return new ChainedCorrector(beg, end, breakDate);
    }

    @lombok.AllArgsConstructor
    static class ChainedCorrector implements HolidaysCorrector {

        final HolidaysCorrector beg, end;
        final LocalDate breakDate;

        @Override
        public Matrix holidaysCorrection(TsDomain domain) {
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
        }

        @Override
        public DoubleSeq longTermYearlyCorrection() {
            // no actual solution
            //prefer focusing on the recent part
            return end.longTermYearlyCorrection();
        }
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector[] correctors, double[] weights) {
        return new CompositeCorrector(correctors, weights);
    }

    @lombok.AllArgsConstructor
    static class CompositeCorrector implements HolidaysCorrector {

        final HolidaysCorrector[] correctors;
        final double[] weights;

        @Override
        public Matrix holidaysCorrection(TsDomain domain) {
            FastMatrix M = FastMatrix.of(correctors[0].holidaysCorrection(domain));
            M.mul(weights[0]);
            for (int i = 1; i < correctors.length; ++i) {
                FastMatrix cur = FastMatrix.of(correctors[i].holidaysCorrection(domain));
                M.addAY(weights[i], cur);
            }
            return M;
        }

        @Override
        public DoubleSeq longTermYearlyCorrection() {
            DataBlock all = DataBlock.make(7);
            all.setAY(weights[0], correctors[0].longTermYearlyCorrection());
            for (int i = 1; i < correctors.length; ++i) {
                all.addAY(weights[i], correctors[i].longTermYearlyCorrection());
            }
            return all;
        }
    }

    private HolidaysCorrectionFactory() {
    }

    private double AVG = 1.0 / 7.0;

    @Override
    public boolean fill(HolidaysCorrectedTradingDays var, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        FastMatrix days = FastMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(start, true, days);
        Matrix corr = var.getCorrector().holidaysCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            days.column(i).apply(corr.column(i), (a, b) -> a + b);
        }
        if (var.getType() == GenericTradingDays.Type.CONTRAST) {
            double[] weights = null;
            if (var.isWeighted()) {
                DoubleSeq dc = var.getCorrector().longTermYearlyCorrection();
                weights = dc.toArray();
                for (int j = 0; j < weights.length; ++j) {
                    weights[j] = AVG + weights[j] / 365.25;
                }
            }
            GenericTradingDaysFactory.fillContrasts(var.getClustering(), days, buffer, weights);
        } else {
            GenericTradingDaysFactory.fillNoContrasts(var.getClustering(),
                    var.getType() == GenericTradingDays.Type.MEANCORRECTED ? start : null, days, buffer);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(HolidaysCorrectedTradingDays var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Computes the long term weights to compute contrasts with holidays The
     * weights are defined as (Di+Hi)(Dk+Hk) where Di is the average number of
     * days for period i, Hi is the average number of holidays for period i (+
     * for the contrast group, - for the other groups), k is the contrasting
     * group
     *
     * @param calendar
     * @return
     */
    /**
     *
     * @param calendar
     * @param freq
     * @param contrast
     * @return
     */
}

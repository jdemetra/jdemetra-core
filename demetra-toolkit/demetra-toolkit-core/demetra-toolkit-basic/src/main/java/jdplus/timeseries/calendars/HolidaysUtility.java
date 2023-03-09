/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.timeseries.calendars;

import nbbrd.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.Holiday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jdplus.math.matrices.FastMatrix;
import demetra.math.matrices.Matrix;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.HolidaysOption;
import java.time.temporal.ChronoUnit;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class HolidaysUtility {

    private static boolean match(int[] nonworking, DayOfWeek dow) {
        for (int i = 0; i < nonworking.length; ++i) {
            if (dow.getValue() == nonworking[i]) {
                return true;
            }
        }
        return false;
    }

    public FastMatrix regressionVariables(Holiday[] holidays, TsDomain domain, HolidaysOption option, int[] nonworking, boolean single) {

        // Fill the full matrix
        TsPeriod P0 = domain.getStartPeriod();
        int nhol = holidays.length;

        FastMatrix M = FastMatrix.make(domain.getLength(), single ? 1 : nhol);
        fill(holidays, P0, option, nonworking, M);
        return M;
    }

    public boolean fill(Holiday[] holidays, TsPeriod P0, HolidaysOption option, int[] nonworking, FastMatrix M) {
        int nhol = M.getColumnsCount(), n = M.getRowsCount();
        LocalDate start = P0.start().toLocalDate(), end = P0.plus(n).start().toLocalDate();
        int length = (int) start.until(end, ChronoUnit.DAYS);

        FastMatrix A = FastMatrix.make(length, nhol);
        switch (option) {
            case Previous ->
                fillPreviousWorkingDays(holidays, A, start, nonworking);
            case Next ->
                fillNextWorkingDays(holidays, A, start, nonworking);
            case Skip ->
                fillDays(holidays, A, start, nonworking, true);
            default ->
                fillDays(holidays, A, start, nonworking, false);
        }
        TsPeriod p = P0;
        boolean ok = false;
        for (int i = 0; i < n; ++i) {
            int j0 = (int) start.until(p.start().toLocalDate(), ChronoUnit.DAYS);
            int j1 = (int) start.until(p.end().toLocalDate(), ChronoUnit.DAYS);
            FastMatrix a = A.extract(j0, j1 - j0, 0, nhol);
            // Avoid doubles
            // We just keep the first holiday when several holidays fall the same day
            DataBlockIterator arows = a.rowsIterator();
            if (nhol == 1) {
                double v = 0;
                while (arows.hasNext()) {
                    v += vrow(arows.next());
                }
                if (v > 0) {
                    ok = true;
                    M.set(i, 0, v);
                }
            } else {
                DataBlock mrow = M.row(i);
                while (arows.hasNext()) {
                    if (addrow(arows.next(), mrow)) {
                        ok = true;
                    }
                }
            }
            p = p.next();
        }
        return ok;
    }

    private double vrow(DataBlock row) {
        double[] pdata = row.getStorage();
        double x = 0;
        for (int j = row.getStartPosition(); j < row.getEndPosition(); j += row.getIncrement()) {
            double s = pdata[j];
            if (s > x) {
                x = s;
            }
        }
        return x;
    }

    private boolean addrow(DataBlock row, DataBlock srow) {
        double[] pdata = row.getStorage();
        double x = 0;
        int l = -1;
        for (int j = row.getStartPosition(), k = 0; j < row.getEndPosition(); j += row.getIncrement(), ++k) {
            double s = pdata[j];
            if (s > x) {
                x = s;
                l = k;
            }
        }
        if (x > 0) {
            srow.add(l, x);
            return true;
        }
        return false;
    }

    public String[] names(Holiday[] hol) {
        String[] n = new String[hol.length];
        for (int i = 0; i < hol.length; ++i) {
            n[i] = hol[i].display();
        }
        return n;
    }

    public void fillDays(Holiday[] holidays, final FastMatrix D, final LocalDate start, int[] nonworking, final boolean skip) {
        LocalDate end = start.plusDays(D.getRowsCount());
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay();
                if (!skip || !match(nonworking, date.getDayOfWeek())) {
                    long pos = start.until(date, DAYS);
                    D.set((int) pos, col, item.getWeight());
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillPreviousWorkingDays(Holiday[] holidays, final FastMatrix D, final LocalDate start, int[] nonworking) {
        int n = D.getRowsCount();
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = HolidayInfo.getPreviousWorkingDate(iter.next().getDay(), nonworking);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, item.getWeight());
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillNextWorkingDays(Holiday[] holidays, final FastMatrix D, final LocalDate start, int[] nonworking) {
        int n = D.getRowsCount();
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {

            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = HolidayInfo.getNextWorkingDate(iter.next().getDay(), nonworking);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, item.getWeight());
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    /**
     * Gets the number of days corresponding to the holidays
     *
     * @param holidays
     * @param domain
     * @return The (weighted) number of holidays for each period of the domain.
     * The different columns of the matrix correspond to Mondays...Sundays
     */
    public Matrix holidays(Holiday[] holidays, TsDomain domain) {
        int n = domain.getLength();
        double[] h = new double[7 * n];

        LocalDate dstart = domain.start().toLocalDate(), dend = domain.end().toLocalDate();
        Map<LocalDate, Double> used = new HashMap<>();
        for (int i = 0; i < holidays.length; ++i) {
            Holiday cur = holidays[i];
            LocalDate start = cur.getValidityPeriod().getStart(), end = cur.getValidityPeriod().getEnd();
            if (start.isBefore(dstart)) {
                start = dstart;
            }
            if (end.isAfter(dend)) {
                end = dend;
            }
            if (start.isBefore(end)) {
                for (HolidayInfo info : HolidayInfo.iterable(cur, start, end)) {
                    LocalDate curday = info.getDay();
                    Double Weight = used.get(curday);
                    double weight = cur.getWeight();
                    if (Weight == null || weight > Weight) {
                        used.put(curday, weight);
                        DayOfWeek w = info.getDayOfWeek();
                        int pos = domain.indexOf(curday.atStartOfDay());
                        if (pos >= 0) {
                            int col = w.getValue() - 1;
                            h[n * col + pos] += Weight == null ? weight : weight - Weight;
                        }
                    }
                }
            }
        }
        return Matrix.of(h, n, 7);
    }

    public static double[][] longTermMean(Holiday holiday, int freq) {

        HolidayImpl impl = HolidayImpl.implementationOf(holiday);
        return impl == null ? null : impl.getLongTermMeanEffect(freq);
    }

    /**
     * Computes the long term mean effects
     *
     * @param holidays
     * @param freq
     * @return Returns an array of "annualFrequency" length, corresponding to
     * each period in one year (for instance, Jan, Feb..., Dec). Each item of
     * the result will contain 7 elements, corresponding to the long term
     * average for Mondays...Sundays. The sum of the longTermMean must be equal
     * to the sum of the weights of the different holidays. Some element of the
     * array can be null, which means that there are no effect for the
     * considered period.
     */
    public double[][] longTermMean(Holiday[] holidays, int freq) {
        double[][] rslt = null;
        for (int k = 0; k < holidays.length; ++k) {
            double[][] cur = longTermMean(holidays[k], freq);
            if (cur != null) {
                if (rslt == null) {
                    rslt = cur;
                } else {
                    for (int i = 0; i < cur.length; ++i) {
                        if (cur[i] != null) {
                            if (rslt[i] == null) {
                                rslt[i] = cur[i];
                            } else {
                                for (int j = 0; j < 7; ++j) {
                                    rslt[i][j] += cur[i][j];
                                }
                            }
                        }
                    }
                }
            }
        }
        return rslt != null ? rslt : new double[freq][];
    }

    public FastMatrix longTermMean(Holiday[] holidays, TsDomain domain) {
        int n = domain.length();
        FastMatrix ltm = FastMatrix.make(n, 7);
        int ifreq = domain.getAnnualFrequency();
        LocalDate start = domain.start().toLocalDate(), end = domain.end().toLocalDate();
        for (int k = 0; k < holidays.length; ++k) {
            Holiday hcur = holidays[k];
            HolidayImpl helper = HolidayImpl.implementationOf(hcur);
            TsDomain xdomain = helper.getSignificantDomain(ifreq, start, end);
            if (!xdomain.isEmpty()) {
                double[][] cur = helper.getLongTermMeanEffect(ifreq);
                if (cur != null) {
                    int del = domain.getStartPeriod().until(xdomain.getStartPeriod());
                    for (int p = 0; p < ifreq; ++p) {
                        if (cur[p] != null) {
                            DataBlock mean = DataBlock.of(cur[p]);
                            int i = p - xdomain.getStartPeriod().annualPosition();
                            if (i < 0) {
                                i += ifreq;
                            }
                            while (i < xdomain.getLength()) {
                                DataBlock row = ltm.row(i + del);
                                row.add(mean);
                                i += ifreq;
                            }
                        }
                    }
                }
            }
        }

        return ltm;
    }

    private static final double[] NDAYS = new double[]{31, 28.25, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * Computes the number of days for each period of the given periodicity,
     * corrected for holidays. The holidays are considered as a day of type
     * "hol"
     *
     * @param calendar
     * @param freq
     * @param hol
     * @return A freq x 7 matrix. The first column corresponds to the number of
     * (corrected) Mondays...
     */
    public static FastMatrix days(final Calendar calendar, int freq, DayOfWeek hol) {

        // long term average number of holidays. mean[j][0] for means of Mondays for period j
        double[][] mean = longTermMean(calendar.getHolidays(), freq);
        DataBlock[] Mean = new DataBlock[freq];
        for (int i = 0; i < freq; ++i) {
            if (mean[i] != null) {
                Mean[i] = DataBlock.of(mean[i]);
                mean[i][hol.getValue() - 1] -= Mean[i].sum();
            }
        }
        int c = 12 / freq;
        FastMatrix M = FastMatrix.make(freq, 7);
        for (int i = 0, m = 0; i < freq; ++i) {
            double avg = 0;
            for (int j = 0; j < c; ++m, ++j) {
                avg += NDAYS[m];
            }
            DataBlock row = M.row(i);
            row.set(avg / 7);
            if (Mean[i] != null) {
                row.sub(Mean[i]);
            }
        }
        return M;
    }

    public static FastMatrix clustering(final FastMatrix days, DayClustering clustering) {
        if (days.getColumnsCount() != 7) {
            throw new IllegalArgumentException();
        }
        FastMatrix M = FastMatrix.make(days.getRowsCount(), clustering.getGroupsCount());
        for (int i = 0; i < 7; ++i) {
            M.column(clustering.groupOf(i)).add(days.column(i));
        }
        return M;
    }

}

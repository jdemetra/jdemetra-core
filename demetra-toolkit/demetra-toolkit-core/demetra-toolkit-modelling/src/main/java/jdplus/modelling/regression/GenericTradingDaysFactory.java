/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.modelling.regression;

import demetra.data.DoubleSeqCursor;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.CalendarUtility;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixWindow;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class GenericTradingDaysFactory implements RegressionVariableFactory<GenericTradingDaysVariable> {

    @lombok.Value
    private static class Entry {

        DayClustering clustering;
        int period;
    }

    @lombok.Value
    private static class Data {

        TsPeriod start;
        FastMatrix data;

    }

    private static final Map<Entry, Data> CACHE = new HashMap<>();

    private static FastMatrix dataFor(DayClustering clustering, TsDomain domain) {
        synchronized (CACHE) {
            TsPeriod start = domain.getStartPeriod();
            Entry entry = new Entry(clustering, domain.getAnnualFrequency());
            Data rslt = CACHE.get(entry);
            if (rslt == null) {
                FastMatrix m = generateContrasts(clustering, domain);
                CACHE.put(entry, new Data(start, m));
                return m;
            } else {
                int beg = rslt.start.until(start);
                int n = domain.getLength();
                int ng = rslt.data.getColumnsCount();
                int end = beg + n;
                if (beg >= 0 && end <= rslt.data.getRowsCount()) {
                    return rslt.data.extract(beg, n, 0, ng);
                } else {
                    int n0 = 0, n1 = 0;
                    TsPeriod mstart;
                    int ncur = rslt.data.getRowsCount();
                    if (beg < 0) {
                        n0 = -beg;
                        beg = 0;
                        mstart = start;
                    } else {
                        mstart = rslt.start;
                    }
                    if (end > ncur) {
                        n1 = end - ncur;
                    }
                    int nn = n0 + n1 + ncur;
                    FastMatrix m = FastMatrix.make(nn, ng);
                    MatrixWindow mw = m.top(0);
                    if (n0 > 0) {
                        TsDomain d0 = TsDomain.of(start, n0);
                        mw.vnext(n0).copy(generateContrasts(clustering, d0));
                    }
                    mw.vnext(ncur).copy(rslt.data);
                    if (n1 > 0) {
                        TsDomain d1 = TsDomain.of(mstart.plus(n0 + ncur), n1);
                        mw.vnext(n1).copy(generateContrasts(clustering, d1));
                    }
                    CACHE.put(entry, new Data(mstart, m));
                    return m.extract(beg, n, 0, ng);
                }
            }
        }
    }

    public static GenericTradingDaysFactory FACTORY = new GenericTradingDaysFactory();

    private GenericTradingDaysFactory() {
    }

    public boolean fill(GenericTradingDays var, TsPeriod start, FastMatrix buffer) {

        if (var.getType() == GenericTradingDays.Type.CONTRAST) {
            dataContrast(var.getClustering(), start, buffer);
        } else {
            dataNoContrast(var.getClustering(), var.getType() == GenericTradingDays.Type.NORMALIZED,
                    var.getType() == GenericTradingDays.Type.MEANCORRECTED, start, buffer);
        }
        return true;
    }

    @Override
    public boolean fill(GenericTradingDaysVariable var, TsPeriod start, FastMatrix buffer) {
//        if (start.annualFrequency() == 1)
//            return false;
        if (var.getType() == GenericTradingDays.Type.CONTRAST) {
            dataContrast(var.getClustering(), start, buffer);
        } else {
            dataNoContrast(var.getClustering(), var.getType() == GenericTradingDays.Type.NORMALIZED,
                    var.getType() == GenericTradingDays.Type.MEANCORRECTED, start, buffer);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(GenericTradingDaysVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private static final double[] MDAYS = new double[]{31.0, 28.25, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0};

    private void dataNoContrast(DayClustering clustering, boolean normalized, boolean meanCorrected,
            TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        int[][] days = tdCount(domain);
        double[] mdays = meanCorrected ? meanDays(domain) : null;

        int[][] groups = clustering.allPositions();
        int ng = groups.length;
        DoubleSeqCursor.OnMutable[] cells = new DoubleSeqCursor.OnMutable[ng];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = buffer.column(i).cursor();
        }
        for (int i = 0; i < n; ++i) {
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                int sum = days[group[0]][i];
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += days[group[ip]][i];
                }
                double dsum = sum;
                if (mdays != null) {
                    dsum -= np * mdays[i];
                } else if (normalized) {
                    dsum /= np;
                }

                cells[ig].setAndNext(dsum);
            }
        }
    }

    private void dataContrast(DayClustering clustering, TsPeriod start, FastMatrix buffer) {
        FastMatrix m = dataFor(clustering, TsDomain.of(start, buffer.getRowsCount()));
        buffer.copy(m);
    }

    private static FastMatrix generateContrasts(DayClustering clustering, TsDomain domain) {
        int n = domain.length();
        int[][] days = tdCount(domain);

        int[][] groups = clustering.allPositions();
        rotate(groups);
        int ng = groups.length - 1;
        int[] cgroup = groups[ng];
        DoubleSeqCursor.OnMutable[] cells = new DoubleSeqCursor.OnMutable[ng];
        FastMatrix data = FastMatrix.make(n, ng);
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = data.column(i).cursor();
        }
        for (int i = 0; i < n; ++i) {
            int csum = days[cgroup[0]][i];
            int cnp = cgroup.length;
            for (int ip = 1; ip < cnp; ++ip) {
                csum += days[cgroup[ip]][i];
            }
            double dcsum = csum;
            dcsum /= cnp;
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                int sum = days[group[0]][i];
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += days[group[ip]][i];
                }
                double dsum = sum;
                cells[ig].setAndNext(dsum - np * dcsum);
            }
        }
        return data;
    }

    public static FastMatrix generateContrasts(DayClustering clustering, FastMatrix days) {
        FastMatrix m = FastMatrix.make(days.getRowsCount(), clustering.getGroupsCount() - 1);
        fillContrasts(clustering, days, m);
        return m;
    }

    public static FastMatrix fillContrasts(DayClustering clustering, FastMatrix days, FastMatrix data) {
        int n = days.getRowsCount();
        int[][] groups = clustering.allPositions();
        rotate(groups);
        int ng = groups.length - 1;
        int[] cgroup = groups[ng];
        DoubleSeqCursor.OnMutable[] cells = new DoubleSeqCursor.OnMutable[ng];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = data.column(i).cursor();
        }
        for (int i = 0; i < n; ++i) {
            DataBlock rdays = days.row(i);
            double csum = rdays.get(cgroup[0]);
            int cnp = cgroup.length;
            for (int ip = 1; ip < cnp; ++ip) {
                csum += rdays.get(cgroup[ip]);
            }
            csum /= cnp;
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                double sum = rdays.get(group[0]);
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += rdays.get(group[ip]);
                }
                double dsum = sum;
                double val = dsum - np * csum;
                double ival = Math.round(val);
                if (Math.abs(val - ival) < 1e-9) {
                    val = ival;
                }
                cells[ig].setAndNext(val);
            }
        }
        return data;
    }

    public static FastMatrix generateNoContrast(DayClustering clustering, boolean normalized, TsPeriod start,
            FastMatrix days) {
        FastMatrix m = FastMatrix.make(days.getRowsCount(), clustering.getGroupsCount());
        fillNoContrasts(clustering, normalized, start, days, m);
        return m;
    }

    public static FastMatrix fillNoContrasts(DayClustering clustering, boolean normalized, TsPeriod start, FastMatrix days, FastMatrix data) {
        int n = days.getRowsCount();
        double[] mdays = null;
        if (start != null) {
            TsDomain domain = TsDomain.of(start, n);
            mdays = meanDays(domain);
        }
        int[][] groups = clustering.allPositions();
        int ng = groups.length;
        DoubleSeqCursor.OnMutable[] cells = new DoubleSeqCursor.OnMutable[ng];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = data.column(i).cursor();
        }
        for (int i = 0; i < n; ++i) {
            DataBlock rdays = days.row(i);
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                double sum = rdays.get(group[0]);
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += rdays.get(group[ip]);
                }
                double dsum = sum;
                if (mdays != null) {
                    dsum -= np * mdays[i];
                } else if (normalized) {
                    dsum /= np;
                }

                cells[ig].setAndNext(dsum);
            }
        }
        return data;
    }

    private static void rotate(int[][] groups) {
        // we put the contrast group at the end
        int[] cgroup = groups[0];
        for (int i = 1; i < groups.length; ++i) {
            groups[i - 1] = groups[i];
        }
        groups[groups.length - 1] = cgroup;
    }

    public static int[][] tdCount(TsDomain domain) {
        int[][] rslt = new int[7][];

        int n = domain.length();
        int[] start = new int[n + 1]; // id of the first day for each period
        LocalDate cur = domain.start().toLocalDate();
        int conv = TsUnit.MONTH.ratioOf(domain.getStartPeriod().getUnit());
        int year = cur.getYear(), month = cur.getMonthValue();
        for (int i = 0; i < start.length; ++i) {
            start[i] = calc(year, month, 1);
            month += conv;
            if (month > 12) {
                year++;
                month -= 12;
            }
//            start[i] = (int) EPOCH.until(cur, ChronoUnit.DAYS);
//            start[i] = Utility.calc(cur.getYear(), cur.getMonthValue(), cur.getDayOfMonth());
//            cur=cur.plusMonths(conv);
        }

        for (int j = 0; j < 7; ++j) {
            rslt[j] = new int[n];
        }

        for (int i = 0; i < n; ++i) {
            int ni = start[i + 1] - start[i];
            int dw0 = (start[i] + DAY_OF_WEEK_OF_EPOCH) % 7;
            if (dw0 < 0) {
                dw0 += 7;
            }
            for (int j = 0; j < 7; ++j) {
                int j0 = j - dw0;
                if (j0 < 0) {
                    j0 += 7;
                }
                rslt[j][i] = 1 + (ni - 1 - j0) / 7;
            }
        }
        return rslt;
    }

    public static void fillTdMatrix(TsPeriod start, FastMatrix mtd) {
        int[][] td = tdCount(TsDomain.of(start, mtd.getRowsCount()));
        for (int i = 0; i < 7; ++i) {
            int[] curtd = td[i];
            mtd.column(i).set(k -> curtd[k]);
        }
    }

    public static double[] meanDays(int period) {
        int conv = 12 / period;
        double[] m = new double[period];
        for (int i = 0, k = 0; i < period; ++i) {
            double s = 0;
            for (int j = 0; j < conv; ++j, ++k) {
                s += MDAYS[k];
            }
            m[i] = s / 7;
        }
        return m;
    }

    public static double[] meanDays(TsDomain domain) {
        int conv = TsUnit.MONTH.ratioOf(domain.getStartPeriod().getUnit());
        if (conv <= 0) {
            return null;
        }
        LocalDate cur = domain.start().toLocalDate();
        int month = cur.getMonthValue() - 1;
        double[] m = new double[domain.getLength()];
        int p = 12 / conv, pmax = Math.min(p, m.length);
        for (int i = 0, k = month; i < pmax; ++i) {
            double s = 0;
            for (int j = 0; j < conv; ++j, ++k) {
                s += MDAYS[k % 12];
            }
            m[i] = s / 7;
        }
        for (int i = p; i < m.length; ++i) {
            m[i] = m[i - p];
        }
        return m;
    }

    private static final int DAY_OF_WEEK_OF_EPOCH = TsPeriod.DEFAULT_EPOCH.getDayOfWeek().getValue() - 1;

    private static int calc(int year, final int month, final int day) {

        boolean bLeapYear = CalendarUtility.isLeap(year);

        // make Jan 1, 1AD be 0
        int nDate = year * 365 + year / 4 - year / 100 + year / 400
                + CalendarUtility.getCumulatedMonthDays(month - 1) + day;

        // If leap year and it's before March, subtract 1:
        if ((month < 3) && bLeapYear) {
            --nDate;
        }
        return nDate - 719528; // number of days since 0
    }

}

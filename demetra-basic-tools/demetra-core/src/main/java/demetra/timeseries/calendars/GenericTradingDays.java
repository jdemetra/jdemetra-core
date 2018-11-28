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
package demetra.timeseries.calendars;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import demetra.data.DoubleCell;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class GenericTradingDays {

    @lombok.Value
    private static class Entry {

        DayClustering clustering;
        int period;
    }

    
    @lombok.Value
    private static class Data {

        TsPeriod start;
        Matrix data;
        
    }

    private static final Map<Entry, Data> cache = new HashMap<>();

    private static Matrix dataFor(DayClustering clustering, TsDomain domain) {
        synchronized (cache) {
            TsPeriod start=domain.getStartPeriod();
            Entry entry=new Entry(clustering, domain.getAnnualFrequency());
            Data rslt = cache.get(entry);
            if (rslt == null){
                Matrix m = generateContrasts(clustering, domain);
                cache.put(entry, new Data(start, m));
                return m;
            }else{
                int beg=rslt.start.until(start);
                int n=domain.getLength();
                int ng=rslt.data.getColumnsCount();
                int end=beg+n;
                if (beg>= 0 && end<= rslt.data.getRowsCount()){
                    return rslt.data.extract(beg, n, 0, ng);
                }else{
                    int n0=0, n1=0;
                    int ncur=rslt.data.getRowsCount();
                    if (beg <0){
                        n0=-beg;
                        beg=0;
                    }
                    if (end > ncur){
                        n1=end-ncur;
                    }
                    int nn=n0+n1+ncur;
                    Matrix m=Matrix.make(nn, ng);
                    MatrixWindow mw=m.top(0);
                    if (n0>0){
                        TsDomain d0=TsDomain.of(start, n0);
                        mw.vnext(n0);
                        mw.copy(generateContrasts(clustering, d0));
                    }
                    mw.vnext(ncur);
                    mw.copy(rslt.data);
                    if (n1>0){
                        TsDomain d1=TsDomain.of(start.plus(n0+ncur), n1);
                        mw.vnext(n1);
                        mw.copy(generateContrasts(clustering, d1));
                    }
                    cache.put(entry, new Data(start, m));
                    return m.extract(beg, n, 0, ng);
                }
            }
        }
    }

    private final DayClustering clustering;
    private final boolean contrast;
    private final boolean normalized;

    public static GenericTradingDays contrasts(DayClustering clustering) {
        return new GenericTradingDays(clustering, true, false);
    }

    public static GenericTradingDays of(DayClustering clustering) {
        return new GenericTradingDays(clustering, false, false);
    }

    public static GenericTradingDays normalized(DayClustering clustering) {
        return new GenericTradingDays(clustering, false, true);
    }

    private GenericTradingDays(DayClustering clustering, boolean contrast, boolean normalized) {
        this.clustering = clustering;
        this.contrast = contrast;
        this.normalized = normalized;
    }

    public DayClustering getClustering() {
        return clustering;
    }

    public void data(TsDomain domain, List<DataBlock> buffer) {
        if (contrast) {
            dataContrasts(domain, buffer);
        } else {
            dataNoContrast(domain, buffer);
        }
    }

    private static final double[] MDAYS = new double[]{31.0, 28.25, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0};

    private void dataNoContrast(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.length();
        int[][] days = tdCount(domain);
        double[] mdays = meanDays(domain);

        int[][] groups = clustering.allPositions();
        int ng = groups.length;
        DoubleCell[] cells = new DoubleCell[ng];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = buffer.get(i).cells();
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
                if (normalized) {
                    dsum = dsum / np - mdays[i];
                } else {
                    dsum -= np * mdays[i];
                }

                cells[ig].setAndNext(dsum);
            }
        }
    }

    private void dataContrasts(TsDomain domain, List<DataBlock> buffer) {
        Matrix m=dataFor(clustering, domain);
        for (int i=0; i<m.getColumnsCount(); ++i){
            buffer.get(i).copy(m.column(i));
        }
    }

    private static Matrix generateContrasts(DayClustering clustering, TsDomain domain) {
        int n = domain.length();
        int[][] days = tdCount(domain);

        int[][] groups = clustering.allPositions();
        rotate(groups);
        int ng = groups.length - 1;
        int[] cgroup = groups[ng];
        DoubleCell[] cells = new DoubleCell[ng];
        Matrix data = Matrix.make(n, ng);
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = data.column(i).cells();
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

    public int getCount() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }

    public String getDescription(int idx) {
        return clustering.toString(idx);
    }

    /**
     * @return the contrastGroup
     */
    public boolean isContrast() {
        return contrast;
    }

    /**
     * @return the normalization
     */
    public boolean isNormalized() {
        return normalized;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof GenericTradingDays) {
            GenericTradingDays x = (GenericTradingDays) other;
            return x.normalized == normalized && x.contrast == contrast
                    && x.clustering.equals(clustering);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.clustering);
        hash = 53 * hash + (this.contrast ? 1 : 0);
        hash = 53 * hash + (this.normalized ? 1 : 0);
        return hash;
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

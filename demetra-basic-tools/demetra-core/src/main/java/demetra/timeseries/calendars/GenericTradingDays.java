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

import demetra.data.Cell;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GenericTradingDays {

    private final DayClustering clustering;
    private final int contrastGroup;
    private final boolean normalized;

    public static GenericTradingDays contrasts(DayClustering clustering) {
        return new GenericTradingDays(clustering, 0);
    }

    public static GenericTradingDays of(DayClustering clustering) {
        return new GenericTradingDays(clustering, false);
    }

    public static GenericTradingDays normalized(DayClustering clustering) {
        return new GenericTradingDays(clustering, true);
    }

    private GenericTradingDays(DayClustering clustering, int contrastGroup) {
        this.clustering = clustering;
        this.contrastGroup = contrastGroup;
        normalized = true;
    }

    private GenericTradingDays(DayClustering clustering, boolean normalized) {
        this.clustering = clustering;
        this.contrastGroup = -1;
        this.normalized = normalized;
    }

    public DayClustering getClustering() {
        return clustering;
    }

    public void data(TsDomain domain, List<DataBlock> buffer) {
        if (contrastGroup >= 0) {
            dataContrasts(domain, buffer);
        } else {
            dataNoContrast(domain, buffer);
        }
    }

    private void dataNoContrast(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.length();
        int[][] days = tdCount(domain);

        int[][] groups = clustering.allPositions();
        int ng = groups.length;
        Cell[] cells = new Cell[ng];
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
                    dsum /= np;
                }
                cells[ig].setAndNext(dsum);
            }
        }
    }

    private void dataContrasts(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.length();
        int[][] days = tdCount(domain);

        int[][] groups = clustering.allPositions();
        rotate(groups);
        int ng = groups.length - 1;
        int[] cgroup = groups[ng];
        Cell[] cells = new Cell[ng];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = buffer.get(i).cells();
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
    }

    public int getCount() {
        int n = clustering.getGroupsCount();
        return contrastGroup >= 0 ? n - 1 : n;
    }

    public String getDescription(int idx) {
        return clustering.toString(idx);
    }

    /**
     * @return the contrastGroup
     */
    public int getContrastGroup() {
        return contrastGroup;
    }

    /**
     * @return the normalization
     */
    public boolean isNormalized() {
        return normalized;
    }

    private void rotate(int[][] groups) {
        if (contrastGroup >= 0) {
            // we put the contrast group at the end
            int[] cgroup = groups[contrastGroup];
            for (int i = contrastGroup + 1; i < groups.length; ++i) {
                groups[i - 1] = groups[i];
            }
            groups[groups.length - 1] = cgroup;
        }
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

    @Override
    public boolean equals(Object other){
        if (this == other)
            return true;
        if (other instanceof GenericTradingDays){
           GenericTradingDays x=(GenericTradingDays) other;
           return x.normalized == normalized && x.contrastGroup == contrastGroup
                   && x.clustering.equals(clustering);
        }else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.clustering);
        hash = 53 * hash + this.contrastGroup;
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

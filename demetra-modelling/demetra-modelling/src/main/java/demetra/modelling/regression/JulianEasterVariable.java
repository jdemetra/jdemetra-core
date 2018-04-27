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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.Easter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class JulianEasterVariable implements IEasterVariable {

    private static final int CYCLE = 532;

    private static final int[] C_MAR = new int[]{
        0, 0, 0, 4, 16, 36, 68, 116, 180, 264, 364, 480, 616, 768,
        936, 1124, 1328, 1552, 1796, 2056, 2336, 2632, 2944, 3276, 3624, 3988, 4372, 4772
    };
    private static final int[] C_APR = new int[]{
        452, 924, 1412, 1908, 2408, 2908, 3404, 3888, 4356, 4804, 5236, 5652, 6048, 6428,
        6792, 7136, 7464, 7772, 8060, 8332, 8584, 8820, 9040, 9240, 9424, 9592, 9740, 9872
    };
    private static final int[] C_MAY = new int[]{
        80, 140, 184, 216, 236, 248, 252, 252, 252, 252, 252, 252, 252, 252,
        252, 252, 252, 252, 252, 252, 252, 252, 252, 252, 252, 252, 252, 252
    };

    private static final int TWOCYCLE = CYCLE * 2;

    private static final int[] C_MAR2 = new int[]{
        1, 3, 8, 22, 51, 91, 145, 226, 343, 488, 657, 858, 1099, 1379, 1692, 2036, 2414, 2828,
        3284, 3779, 4307, 4866, 5458, 6092, 6764, 7469, 8204, 8971
    };
    private static final int[] C_APR2 = new int[]{
        878, 1784, 2713, 3664, 4633, 5613, 6586, 7547, 8484, 9400, 10293, 11155, 11978, 12762, 13513, 14233,
        14919, 15569, 16177, 16746, 17282, 17787, 18259, 18689, 19081, 19440, 19769, 20066
    };
    private static final int[] C_MAY2 = new int[]{
        185, 341, 471, 570, 636, 680, 717, 739, 749, 752, 754, 755,
        755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755, 755
    };
    private final int duration;
    private final boolean gregorianDates;
    private final String name;
    
    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(JulianEasterVariable.class)
    public static class Builder {

        private int duration = 6;
        private boolean gregorianDates = true;
        private String name;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder gregorianDates(boolean gd) {
            this.gregorianDates = gd;
            return this;
        }

        public JulianEasterVariable build() {
            return new JulianEasterVariable(duration, gregorianDates, name != null ? name : NAME);
        }

    }

    private JulianEasterVariable(int duration, boolean gregorianDates, String name) {
        this.duration = duration;
        this.gregorianDates = gregorianDates;
        this.name = name;
    }

    /**
     * Check that the dates are expressed in the Gregorian calendar
     *
     * @return True if Easter is expressed in Gregorian calendar dates, false
     * otherwise
     */
    public boolean isGregorianDate() {
        return gregorianDates;
    }

    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Julian Easter [").append(duration).append(']');
        return builder.toString();
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> ldata) {
        // very inefficient code.Should be improved
        DataBlock data=ldata.get(0);
        data.set(0);
        TsPeriod start = domain.getStartPeriod();
        int freq = domain.getTsUnit().getAnnualFrequency();
        if (freq < 3) {
            return;
        }
        double q = TWOCYCLE * duration;
        int n = data.length();
        int y = start.year();
        TsPeriod march = TsPeriod.monthly(y, 3),
                april = TsPeriod.monthly(y, 4),
                may = TsPeriod.monthly(y, 5);
        LocalDate beg = start.start().toLocalDate(), end = start.plus(n).start().toLocalDate();
        while (true) {
            LocalDate easter = Easter.julianEaster(y, gregorianDates);
            if (beg.isBefore(easter)) {
                // computes the number of days in M, A, M
                LocalDate pbeg = easter.minusDays(duration);
                int n0 = (int) pbeg.until(march.end().toLocalDate(), ChronoUnit.DAYS);
                if (n0 < 0) {
                    n0 = 0;
                }
                int n2 = (int) may.start().toLocalDate().until(easter, ChronoUnit.DAYS);
                if (n2 < 0) {
                    n2 = 0;
                } else if (n2 > duration) {
                    n2 = duration;
                }
                int n1 = duration - n0 - n2;
                double dur = duration;
                TsPeriod cur = TsPeriod.of(start.getUnit(), march.start().toLocalDate());
                int ipos = start.until(cur);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n0 / dur - C_MAR2[duration - 1] / q);
                }
                cur = TsPeriod.of(start.getUnit(), april.start().toLocalDate());
                ipos = start.until(cur);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n1 / dur - C_APR2[duration - 1] / q);
                }
                cur = TsPeriod.of(start.getUnit(), may.start().toLocalDate());
                ipos = start.until(cur);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n2 / dur - C_MAY2[duration - 1] / q);
                }
            }
            march=march.plus(12);
            if (march.isAfter(TsPeriod.of(TsUnit.MONTH, end))) {
                break;
            }
            april=april.plus(12);
            may=may.plus(12);
            ++y;
        }

    }

    @Override
    public JulianEasterVariable rename(String newname) {
        return new JulianEasterVariable(duration, gregorianDates, newname);
    }

}

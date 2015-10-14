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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.calendars.Utilities;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.GregorianCalendar;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class JulianEasterVariable extends AbstractSingleTsVariable implements IMovingHolidayVariable {


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

    private int dur_ = 6;

    private boolean m_gc = true;

    public JulianEasterVariable() {
    }

    public int getDuration() {
        return dur_;
    }

    public void setDuration(int value) {
        if (value <= 0 || value >= 29) {
            throw new IllegalArgumentException();
        }
        dur_ = value;
    }

    /**
     * Check that the dates are expressed in the Gregorian calendar
     *
     * @return True if Easter is expressed in Gregorian calendar dates, false
     * otherwise
     */
    public boolean isGregorianDate() {
        return m_gc;
    }

    public void setGregorianDate(boolean gc) {
        m_gc = gc;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("Julian Easter [").append(dur_).append(']');
        return builder.toString();
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        // very inefficient code.Should be improved
        data.set(0);
        int freq = start.getFrequency().intValue();
        if (freq < 3) {
            return;
        }
        double q = CYCLE * dur_;
        int n = data.getLength();
        int y = start.getYear();
        TsPeriod march = new TsPeriod(TsFrequency.Monthly, y, 2),
                april = new TsPeriod(TsFrequency.Monthly, y, 3),
                may = new TsPeriod(TsFrequency.Monthly, y, 4);
        Day beg = start.firstday(), end = start.plus(n).lastday();
        while (true) {
            Day easter = Utilities.julianEaster(y, m_gc);
            if (beg.isBefore(easter)) {
                // computes the number of days in M, A, M
                Day pbeg = easter.minus(dur_);
                int n0 = march.lastday().difference(pbeg) + 1;
                if (n0 < 0) {
                    n0 = 0;
                }
                int n2 = easter.difference(may.firstday());
                if (n2 < 0) {
                    n2 = 0;
                } else if (n2 > dur_) {
                    n2 = dur_;
                }
                int n1 = dur_ - n0 - n2;
                double dur = dur_;
                TsPeriod cur = new TsPeriod(start.getFrequency(), march);
                int ipos = cur.minus(start);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n0 / dur - C_MAR[dur_ - 1] / q);
                }
                cur = new TsPeriod(start.getFrequency(), april);
                ipos = cur.minus(start);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n1 / dur - C_APR[dur_ - 1] / q);
                }
                cur = new TsPeriod(start.getFrequency(), may);
                ipos = cur.minus(start);
                if (ipos >= 0 && ipos < n) {
                    data.add(ipos, n2 / dur - C_MAY[dur_ - 1] / q);
                }
            }
            march.move(12);
            if (march.isAfter(end)) {
                break;
            }
            april.move(12);
            may.move(12);
            ++y;
        }

    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getFrequency().intValue()>2;
    }
}

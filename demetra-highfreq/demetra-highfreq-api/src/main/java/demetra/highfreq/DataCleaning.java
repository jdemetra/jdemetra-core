/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.util.Arrays;

/**
 *
 * @author palatej
 */
public enum DataCleaning {

    NONE,
    SUNDAYS,
    WEEKENDS;

    public static DataCleaning of(TsData data) {
        TsPeriod start = data.getStart();
        if (!start.getUnit().equals(TsUnit.DAY)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue();
        int n = data.length();
        if (n < 7) {
            return NONE;
        }
        // Sundays
        int sun = 7 - pos, sat = 6 - pos;
        if (sat < 0) {
            sat = 6;
        }

        boolean bsun = data.getValues().extract(sun, (n - sun) / 7, 7).allMatch(x -> Double.isNaN(x));
        if (!bsun) {
            return NONE;
        }

        boolean bsat = data.getValues().extract(sat, (n - sat) / 7, 7).allMatch(x -> Double.isNaN(x));
        return bsat ? WEEKENDS : SUNDAYS;
    }

    public double updatePeriodicity(double p) {
        return switch (this) {
            case SUNDAYS ->
                p / 7 * 6;
            case WEEKENDS ->
                p / 7 * 5;
            default ->
                p;
        };
    }

    public double[] updatePeriodicities(double[] p) {
        double[] np = new double[p.length];
        for (int i = 0; i < p.length; ++i) {
            np[i] = updatePeriodicity(p[i]);
        }
        return np;
    }

    public static TsData withMissingSundays(TsDomain domain, DoubleSeq data) {
        double[] tmp = new double[domain.length()];
        TsPeriod start = domain.getStartPeriod();
        if (!start.getUnit().equals(TsUnit.DAY)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue();
        int out = 0, in = 0;

        int n = data.length();
        DoubleSeqCursor cursor = data.cursor();
        while (in < n && out < tmp.length) {
            if (pos != 7) {
                tmp[out++] = cursor.getAndNext();
                ++in;
                ++pos;
            } else {
                tmp[out++] = Double.NaN;
                pos = 1;
            }
        }
        while (out < tmp.length) {
            tmp[out++] = Double.NaN;
        }
        return TsData.ofInternal(start, tmp);
    }

    public static TsData withMissingWeekEnds(TsDomain domain, DoubleSeq data) {
        double[] tmp = new double[domain.length()];
        TsPeriod start = domain.getStartPeriod();
        if (!start.getUnit().equals(TsUnit.DAY)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue();
        int out = 0, in = 0;
        int n = data.length();
        DoubleSeqCursor cursor = data.cursor();

        if (pos == 7) {
            tmp[out++] = Double.NaN;
            pos = 1;
        }

        while (in < n && out < tmp.length) {
            if (pos != 6) {
                tmp[out++] = cursor.getAndNext();
                ++in;
                ++pos;
            } else {
                tmp[out++] = Double.NaN;
                tmp[out++] = Double.NaN;
                pos = 1;
            }
        }
        while (out < tmp.length) {
            tmp[out++] = Double.NaN;
        }
        return TsData.ofInternal(start, tmp);
    }

    public int clean(TsData data, DoubleSeq.Mutable out) {
        return switch (this) {
            case SUNDAYS ->
                cleanSundays(data, out);
            case WEEKENDS ->
                cleanWeekEnds(data, out);
            default ->
                set(data, out);
        };
    }

    public static int cleanSundays(TsData data, DoubleSeq.Mutable out) {
        TsPeriod start = data.getStart();
        if (!start.getUnit().equals(TsUnit.DAY)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue();
        DoubleSeqCursor cursor = data.getValues().cursor();
        DoubleSeqCursor.OnMutable ocursor = out.cursor();
        int nin = data.length();
        int nout = 0;
        for (int i = 0; i < nin; ++i) {
            if (pos != 7) {
                ocursor.setAndNext(cursor.getAndNext());
                ++pos;
                ++nout;
            } else {
                cursor.skip(1);
                pos = 1;
            }
        }
        return nout;
    }

    public static int set(TsData data, DoubleSeq.Mutable out) {
        DoubleSeqCursor cursor = data.getValues().cursor();
        DoubleSeqCursor.OnMutable ocursor = out.cursor();
        int nin = data.length();
        for (int i = 0; i < nin; ++i) {
            ocursor.setAndNext(cursor.getAndNext());
        }
        return nin;
    }

    public static int cleanWeekEnds(TsData data, DoubleSeq.Mutable out) {
        TsPeriod start = data.getStart();
        if (!start.getUnit().equals(TsUnit.DAY)) {
            throw new java.lang.UnsupportedOperationException();
        }
        int pos = start.start().getDayOfWeek().getValue(), i = 0;
        DoubleSeqCursor cursor = data.getValues().cursor();
        DoubleSeqCursor.OnMutable ocursor = out.cursor();
        int nin = data.length();
        int nout = 0;
        if (pos == 7) {
            cursor.skip(1);
            pos = 1;
            i = 1;
        }
        int cur = 0;
        while (i < nin) {
            if (pos != 6) {
                ocursor.setAndNext(cursor.getAndNext());
                ++pos;
                ++i;
                ++nout;
            } else {
                cursor.skip(2);
                pos = 1;
                i += 2;
            }
        }
        return nout;
    }

}

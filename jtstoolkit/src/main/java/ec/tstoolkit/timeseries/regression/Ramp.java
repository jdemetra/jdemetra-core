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
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.StringFormatter;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Ramp extends AbstractSingleTsVariable implements Cloneable {

    private Day start, end;

    public Ramp() {
        this.start = Day.toDay().minus(1);
        this.end = Day.toDay();
        //this(Day.toDay().minus(1),Day.toDay());
    }

    /**
     *
     * @param start
     * @param end
     */
    public Ramp(Day start, Day end) {
        if (end.isNotAfter(start)) {
            throw new TsException("Invalid Ramp");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public Ramp clone() {
        try {
            return (Ramp) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public Day getStart() {
        return start;
    }

    public void setStart(Day start) {
        if (start.isAfter(this.end)) {
            return;
        }
        this.start = start;
    }

    public Day getEnd() {
        return end;
    }

    public void setEnd(Day end) {
        if (end.isBefore(this.start)) {
            return;
        }
        this.end = end;
    }

    /**
     *
     * @param pstart
     * @param data
     */
    @Override
    public void data(TsPeriod pstart, DataBlock data) {
        int t0 = new TsPeriod(pstart.getFrequency(), start.minus(1)).minus(pstart);
        int t1 = new TsPeriod(pstart.getFrequency(), end.plus(1)).minus(pstart);
        int len = data.getLength();
        if (t1 == t0) {
            data.set(0);
            return;
        }

        // set -1 until t0
        if (t0 >= 0) {
            if (t0 >= len) {
                data.set(-1);
                return;
            } else {
                data.range(0, t0 + 1).set(-1);
            }
        }
        // set 0 from t1
        if (t1 < len) {
            if (t1 <= 0) {
                data.set(0);
                return;
            } else {
                data.range(t1, len).set(0);
            }
        }
        int k0 = Math.max(t0 + 1, 0);
        int k1 = Math.min(t1, len);
        double denom = t1 - t0;
        for (int k = k0; k < k1; ++k) {
            data.set(k, (k - t0) / denom - 1);
        }
    }

    @Override
    public String getDescription(TsFrequency context) {
        StringBuilder builder = new StringBuilder();
        if (context != TsFrequency.Undefined) {
            builder.append("rp:").append(new TsPeriod(context, start.minus(1))).append(" - ").append(new TsPeriod(context, end.plus(1)));
        } else {
            builder.append("rp:").append(start).append(" - ").append(end);
        }
        return builder.toString();
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        Day dend = domain.getLast().firstday(), dstart = domain.getStart().lastday();
        return end.isAfter(dstart) && start.isBefore(dend);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Ramp && equals((Ramp) obj));
    }

    private boolean equals(Ramp other) {
        return other.start.equals(start) && other.end.equals(end);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.start);
        hash = 43 * hash + Objects.hashCode(this.end);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append(InformationSet.SEP).append(StringFormatter.convert(start))
                .append(InformationSet.SEP).append(StringFormatter.convert(end));
        return builder.toString();
    }

    public String toString(TsFrequency freq) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append(InformationSet.SEP).append(StringFormatter.write(new TsPeriod(freq, start.minus(1))))
                .append(InformationSet.SEP).append(StringFormatter.write(new TsPeriod(freq, end.plus(1))));
        return builder.toString();
    }

    public static Ramp fromString(String s) {
        String[] ss = InformationSet.split(s);
        if (ss.length != 3) {
            return null;
        }
        if (!ss[0].equals(PREFIX)) {
            return null;
        }
        Day start = StringFormatter.convertDay(ss[1]);
        Day end = StringFormatter.convertDay(ss[2]);
        if (start != null && end != null) {
            return new Ramp(start, end);
        }
        TsPeriod pstart = StringFormatter.readPeriod(ss[1]);
        TsPeriod pend = StringFormatter.readPeriod(ss[2]);
        if (pstart != null && pend != null) {
            return new Ramp(pstart.firstday(), pend.lastday());
        }
        return null;
    }
    public static final String PREFIX = "rp";
}

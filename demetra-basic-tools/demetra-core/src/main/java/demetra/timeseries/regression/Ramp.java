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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Ramp implements ITsVariable<TsDomain>, IUserTsVariable<TsDomain> {

    private final LocalDateTime start, end;
    private final String name;

    /**
     *
     * @param start
     * @param end
     * @param name
     */
    public Ramp(LocalDateTime start, LocalDateTime end, String name) {
        if (!start.isBefore(end)) {
            throw new TsException("Invalid Ramp");
        }
        this.start = start;
        this.end = end;
        this.name = name;
    }

    /**
     *
     * @param start
     * @param end
     * @param name
     */
    public Ramp(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new TsException("Invalid Ramp");
        }
        this.start = start;
        this.end = end;
        this.name = defaultName(start, end);
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    /**
     *
     * @param pstart
     * @param data
     */
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        DataBlock cur = data.get(0);
        TsPeriod pstart = TsPeriod.of(domain.getTsUnit(), start);
        TsPeriod pend = TsPeriod.of(domain.getTsUnit(), end);
        int t0 = domain.position(pstart);
        int t1 = domain.position(pend);
        int len = cur.length(); // =domain.length()
        if (t1 == t0 || t1 <= 0) { // Empty ramp or ramp before the domain: nothing to do
            cur.set(0);
            return;
        }
        if (t0 >= len) { // Ramp after the domain
            cur.set(-1);
            return;
        }

        // set -1 until t0 included
        if (t0 >= 0) {
            cur.range(0, t0 + 1).set(-1);
        }
        // set 0 from t1 included
        if (t1 < len) {
            cur.range(t1, len).set(0);
        }
        if (t1 == t0 + 1) {
            return;
        }
        int k0 = Math.max(t0 + 1, 0);
        int k1 = Math.min(t1, len);
        double denom = t1 - t0;
        for (int k = k0; k < k1; ++k) {
            cur.set(k, (k - t0) / denom - 1);
        }
    }

    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        if (context != null) {
            builder.append("rp:").append(TsPeriod.of(context.getTsUnit(), start).display()).append(" - ")
                    .append(TsPeriod.of(context.getTsUnit(), end).display());
        } else {
            return defaultName(start, end);
        }
        return builder.toString();
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
    public String getName() {
        return name;
    }

    @Override
    public ITsVariable<TsDomain> rename(String nname) {
        return new Ramp(start, end, nname);
    }

    public String toString() {
        return defaultName(start, end);
    }

    public static final String PREFIX = "rp";

    public String defaultName(LocalDateTime start, LocalDateTime end) {
        StringBuilder builder = new StringBuilder();
        builder.append("rp:").append(start).append(" - ").append(end);
        return builder.toString();
    }
}

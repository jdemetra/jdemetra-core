/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries.simplets;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.design.Internal;
import demetra.timeseries.IDateDomain;
import demetra.timeseries.TsException;
import java.time.LocalDate;
import java.time.Period;
import javax.annotation.Nonnegative;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
@lombok.EqualsAndHashCode
public final class TsDomain implements IDateDomain<TsPeriod> {

    /**
     * Creates a new time domain, identified by its frequency, the year and the
     * position of the first period and the length of the domain.
     *
     * @param freq The frequency.
     * @param firstyear Year of the first period
     * @param firstperiod (0-based) position in the year of the first period.
     * @param count Length of the domain (number of periods).
     * @return
     */
    public static TsDomain of(TsFrequency freq, int firstyear, int firstperiod, int count) {
        return new TsDomain(TsPeriod.of(freq, firstyear, firstperiod), count);
    }

    public static TsDomain of(TsPeriod start, int length) {
        return new TsDomain(start, length);
    }

    private final TsPeriod start;
    private final int length;

    private TsDomain(TsPeriod start, int length) {
        this.start = start;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public TsPeriod get(int index) {
        return start.plus(index);
    }

    @Override
    public int search(final LocalDate day) {
        TsPeriod p = TsPeriod.of(start.getFrequency(), day);
        return search(p);
    }

    @Override
    public Period getPeriod() {
        return getFrequency().toPeriod();
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    /**
     * Returns the frequency of each periods.
     *
     * @return The frequency of the domain. Even an empty domain must have a
     * frequency.
     */
    public TsFrequency getFrequency() {
        return start.getFrequency();
    }

    @Override
    public TsPeriod getStart() {
        return start;
    }

    @Override
    public TsPeriod getLast() {
        return start.plus(length - 1);
    }

    @Override
    public TsPeriod getEnd() {
        return start.plus(length);
    }

    /**
     * Searches the place of a given period.
     *
     * @param p The period searched in the domain.
     * @return The index of the period is returned if it is found (exact match),
     * -1 otherwise.
     */
    public int search(final TsPeriod p) {
        if (p.getFrequency() != start.getFrequency()) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        int id = p.id();
        id -= start.id();
        if ((id < 0)) {
            return -1;
        }else if (id >= length){
            return -length;
        } else {
            return id;
        }
    }

    /**
     * Number of periods since 1/1/70 (reference period) of the first period
     *
     * @return
     */
    @Internal
    int id() {
        return start.id();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getStart()).append((" - ")).append(getLast());
        return builder.toString();
    }

    @Override
    public TsDomain range(@Nonnegative int firstPeriod, @Nonnegative int lastPeriod) {
        int len = lastPeriod - firstPeriod;
        if (len < 0) {
            len = 0;
        }
        return TsDomain.of(get(firstPeriod), len);
    }

    @Override
    public TsDomain intersection(IDateDomain<TsPeriod> d2) {
       if (this == d2) {
            return this;
        }
        TsFrequency freq = getFrequency();
        if (freq != ((TsDomain)d2).getFrequency()) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        int n1 = length(), n2 = d2.length();

        int lbeg = id(), rbeg = ((TsDomain)d2).id();

        int lend = lbeg + n1, rend = rbeg + n2;
        int beg = lbeg <= rbeg ? rbeg : lbeg;
        int end = lend >= rend ? rend : lend;

        return TsDomain.of(TsPeriod.ofInternal(freq, beg), Math.max(0, end - beg));
    }

    @Override
    public TsDomain union(IDateDomain<TsPeriod> d2) {
        if (d2 == this) {
            return this;
        }
        Period period = getPeriod();
        if (!period.equals(d2.getPeriod())) {
            return null;
        }

        int ln = length(), rn = d2.length();
        int lbeg = id(), rbeg = ((TsDomain)d2).id();    // FIXME : Solution for that casting
        int lend = lbeg + ln, rend = rbeg + rn;
        int beg = lbeg <= rbeg ? lbeg : rbeg;
        int end = lend >= rend ? lend : rend;

        return TsDomain.of(TsPeriod.ofInternal(getFrequency(), beg), end - beg);
    }

    @Override
    public TsDomain lag(int nperiods) {
        return TsDomain.of(start.plus(nperiods), length);
    }
}

/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries;

import java.time.LocalDateTime;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class TsDomain implements TimeSeriesDomain<TsPeriod> {

    /**
     * Generates a domain which is a splitting of a given period in sub-periods
     *
     * @param period The period which corresponds to the domain
     * @param hUnit The time unit of the sub-periods
     * @param exact Indicates if that the domain must be exactly decomposed into
     * its sub-periods
     * @return The new domain (never null)
     * @throws TsException is thrown when the decomposition is not possible
     */
    public static @NonNull
    TsDomain splitOf(TsPeriod period, TsUnit hUnit, boolean exact) throws TsException {
        LocalDateTime start = period.start(), end = period.end();
        long len = hUnit.getChronoUnit().between(start, end) / hUnit.getAmount();
        TsPeriod pstart = period.withUnit(hUnit);
        if (!exact || end.equals(pstart.plus(len).start())) {
            return of(pstart, (int) len);
        } else {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final TsDomain DEFAULT_EMPTY = TsDomain.of(TsPeriod.of(TsUnit.YEAR, 0), 0);

    @lombok.NonNull
    TsPeriod startPeriod;

    @NonNegative
    int length;

    @Override
    public int length() {
        return getLength();
    }

    @Override
    public TsPeriod get(int index) throws IndexOutOfBoundsException {
        return startPeriod.plus(index);
    }

    public TsUnit getTsUnit() {
        return startPeriod.getUnit();
    }

    public int getAnnualFrequency() {
        return startPeriod.getUnit().getAnnualFrequency();
    }

    public TsPeriod getEndPeriod() {
        checkNonEmpty();
        return startPeriod.plus(length);
    }

    public TsPeriod getLastPeriod() {
        checkNonEmpty();
        return startPeriod.plus(length - 1);
    }

    @Override
    public LocalDateTime start() {
        return startPeriod.start();
    }

    @Override
    public LocalDateTime end() {
        checkNonEmpty();
        return startPeriod.dateAt(startPeriod.getId() + length);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return contains(startPeriod.idAt(date));
    }

    @Override
    public boolean contains(TsPeriod period) {
        startPeriod.checkCompatibility(period);
        return contains(startPeriod.getRebasedId(period));
    }

    @Override
    public int indexOf(LocalDateTime date) {
        return indexOf(startPeriod.idAt(date));
    }

    @Override
    public int indexOf(TsPeriod period) {
        startPeriod.checkCompatibility(period);
        return indexOf(startPeriod.getRebasedId(period));
    }

    public boolean contains(TsDomain other) {
        startPeriod.checkCompatibility(other.startPeriod);
        int index = indexOf(startPeriod.getRebasedId(other.startPeriod));
        return index != -1 && index + other.length <= length;
    }

    /**
     * Returns the position of the given period relative to the starting period
     *
     * @param period A period that should be compatible with the starting period
     * of the domain.
     * @return Could be negative or higher then the length of the domain
     */
    public int position(TsPeriod period) {
        startPeriod.checkCompatibility(period);
        return distance(startPeriod.getRebasedId(period));
    }

    public boolean hasDefaultEpoch() {
        return startPeriod.hasDefaultEpoch();
    }

    public TsDomain move(int count) {
        return count != 0 ? new TsDomain(startPeriod.plus(count), length) : this;
    }

    public TsDomain range(int startIndex, int endIndex) {
        if (endIndex < startIndex || startIndex < 0) {
            throw new IllegalArgumentException(String.format("Invalid bounds: [%s, %s[", startIndex, endIndex));
        }
        if (isEmpty()) {
            return this;
        }
        return startIndex >= length
                ? new TsDomain(get(startIndex), 0)
                : new TsDomain(get(startIndex), Math.min(endIndex, length) - startIndex);
    }

    public TsDomain drop(int nstart, int nend) {
        if (isEmpty()) {
            return this;
        }
        int len = length() - nstart - nend;
        return new TsDomain(get(nstart), len < 0 ? 0 : len);
    }

    public TsDomain intersection(TsDomain other) {
        startPeriod.checkCompatibility(other.startPeriod);

        if (this.equals(other)) {
            return this;
        }
        if (this.isEmpty()) {
            return this;
        }

        int n1 = length(), n2 = other.length();

        long lbeg = startPeriod.getId();
        long rbeg = startPeriod.getRebasedId(other.startPeriod);

        long lend = lbeg + n1, rend = rbeg + n2;
        long beg = lbeg <= rbeg ? rbeg : lbeg;
        long end = lend >= rend ? rend : lend;

        return new TsDomain(startPeriod.withId(beg), Math.max(0, distance(beg, end)));
    }

    public TsDomain union(TsDomain other) {
        startPeriod.checkCompatibility(other.startPeriod);

        if (this.equals(other)) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }

        int ln = length(), rn = other.length();

        long lbeg = startPeriod.getId();
        long rbeg = startPeriod.getRebasedId(other.startPeriod);

        long lend = lbeg + ln, rend = rbeg + rn;
        long beg = lbeg <= rbeg ? lbeg : rbeg;
        long end = lend >= rend ? lend : rend;

        return new TsDomain(startPeriod.withId(beg), distance(beg, end));
    }

    public TsDomain aggregate(@NonNull TsUnit newUnit, boolean complete) {
        int ratio = this.getTsUnit().ratioOf(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            case 1:
                return this;
        }
        if (this.isEmpty()) {
            return TsDomain.of(this.getStartPeriod().withUnit(newUnit), 0);
        }
        int oldLength = length();
        TsPeriod start = getStartPeriod(), nstart = start.withUnit(newUnit);
        int spos = TsDomain.splitOf(nstart, start.getUnit(), false).indexOf(start);
        int head = spos > 0 ? ratio - spos : 0;
        int tail = (oldLength - head) % ratio;
        int nlength = (oldLength - head - tail) / ratio;
        if (head > 0) {
            if (complete) {
                nstart = nstart.next();
            } else {
                nlength++;
            }
        }
        if (tail > 0 && !complete) {
            nlength++;
        }
        return TsDomain.of(nstart, nlength);
    }

    public TsDomain aggregateByPosition(@NonNull TsUnit newUnit, int position) {
        int ratio = this.getTsUnit().ratioOf(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            case 1:
                return this;
        }
        if (position < 0 || position >= ratio) {
            throw new IllegalArgumentException();
        }
        if (this.isEmpty()) {
            return TsDomain.of(this.getStartPeriod().withUnit(newUnit), 0);
        }
        int oldLength = length();
        TsPeriod start = getStartPeriod(), nstart = start.withUnit(newUnit);
        int spos = TsDomain.splitOf(nstart, start.getUnit(), false).indexOf(start);
        int head = position - spos;
        if (head < 0) {
            head += ratio;
            nstart=nstart.next();
        }
        int nlength = 1 + (oldLength - head - 1) / ratio;
        return TsDomain.of(nstart, nlength);
    }

    /**
     * The new domain will only contain complete periods.
     * For instance, if the selector is from 2/1/1980 to 25/5/2000,
     * the selector applied to a yearly domain from 1978 to 2010 will
     * generate a yearly domain from 1981 to 1999
     *
     * @param ps
     * @return
     */
    @Override
    public TsDomain select(TimeSelector ps) {
        if (isEmpty()) {
            return this;
        }

        switch (ps.getType()) {
            case All:
                return this;
            case None:
                return range(0, 0);
            case First:
                return range(0, ps.getN0());
            case Last:
                return range(Math.max(0, length - ps.getN1()), length);
            case Excluding:
                return ps.getN0() <= length - ps.getN1()
                        ? range(ps.getN0(), length - ps.getN1())
                        : range(0, 0);
            case From: {
                long fromId = startPeriod.idAt(ps.getD0());
                return range(Math.max(0, distance(fromId)), Integer.MAX_VALUE);
            }
            case To: {
                long toId = startPeriod.idAt(ps.getD1());
                return range(0, Math.max(0, distance(toId)));
            }
            case Between: {
                long fromId = startPeriod.idAt(ps.getD0());
                long toId = startPeriod.idAt(ps.getD1());
                return range(Math.max(0, distance(fromId)), Math.max(0, distance(toId)));
            }
            default:
                throw new RuntimeException();
        }
    }

    private boolean contains(long id) {
        return startPeriod.getId() <= id && id < startPeriod.getId() + length;
    }

    private int indexOf(long id) {
        int index = distance(id);
        return (length == 0 || index < 0) ? -1 : index < length ? index : -length;
    }

    private void checkNonEmpty() throws IllegalStateException {
        if (length == 0) {
            throw new IllegalStateException();
        }
    }

    private int distance(long endId) {
        return distance(startPeriod.getId(), endId);
    }

    private static int distance(long startId, long endId) {
        return (int) (endId - startId);
    }
}

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
import javax.annotation.Nonnegative;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class RegularDomain implements TsDomain<TsPeriod> {

    @lombok.NonNull
    TsPeriod startPeriod;

    @Nonnegative
    int length;

    @Override
    public int length() {
        return getLength();
    }

    @Override
    public TsPeriod get(int index) throws IndexOutOfBoundsException {
        return startPeriod.plus(index);
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

    public boolean contains(RegularDomain other) {
        startPeriod.checkCompatibility(other.startPeriod);
        int index = indexOf(startPeriod.getRebasedId(other.startPeriod));
        return index != -1 && index + other.length <= length;
    }

    public RegularDomain move(int count) {
        return count != 0 ? new RegularDomain(startPeriod.plus(count), length) : this;
    }

    public RegularDomain range(int startIndex, int endIndex) {
        if (endIndex < startIndex || startIndex < 0) {
            throw new IllegalArgumentException(String.format("Invalid bounds: [%s, %s[", startIndex, endIndex));
        }
        if (isEmpty()) {
            return this;
        }
        return startIndex >= length
                ? new RegularDomain(get(startIndex), 0)
                : new RegularDomain(get(startIndex), Math.min(endIndex, length) - startIndex);
    }

    public RegularDomain intersection(RegularDomain other) {
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

        return new RegularDomain(startPeriod.withId(beg), Math.max(0, distance(beg, end)));
    }

    public RegularDomain union(RegularDomain other) {
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

        return new RegularDomain(startPeriod.withId(beg), distance(beg, end));
    }

    public RegularDomain select(TsPeriodSelector ps) {
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
        return index >= 0 && index < length ? index : -1;
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

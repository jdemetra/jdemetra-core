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

import demetra.data.Range;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TsPeriod implements Range<LocalDateTime>, Comparable<TsPeriod> {

    int offset;

    @lombok.NonNull
    TsUnit unit;

    long id;

    @Override
    public LocalDateTime start() {
        return dateAt(offset, unit, id);
    }

    @Override
    public LocalDateTime end() {
        return dateAt(offset, unit, id + 1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return idAt(offset, unit, date) == id;
    }

    @Override
    public int compareTo(TsPeriod period) {
        checkCompatibility(period);
        return Long.compare(id, getRebasedId(period));
    }

    public boolean isAfter(TsPeriod period) {
        checkCompatibility(period);
        return id > getRebasedId(period);
    }

    public boolean isBefore(TsPeriod period) {
        checkCompatibility(period);
        return id < getRebasedId(period);
    }

    public TsPeriod next() {
        return plus(1);
    }

    public TsPeriod plus(long count) {
        return new TsPeriod(offset, unit, id + count);
    }

    public TsPeriod withOffset(int newOffset) {
        return make(newOffset, unit, start());
    }

    public TsPeriod withUnit(TsUnit newUnit) {
        return make(offset, newUnit, start());
    }

    public TsPeriod withDate(LocalDate date) {
        return withDate(date.atStartOfDay());
    }

    public TsPeriod withDate(LocalDateTime date) {
        return make(offset, unit, date);
    }

    public TsPeriod withId(long id) {
        return new TsPeriod(offset, unit, id);
    }

    public int until(TsPeriod endExclusive) {
        checkCompatibility(endExclusive);
        return (int) (getRebasedId(endExclusive) - id);
    }

    @Override
    public String toString() {
        return toString(offset, unit, id);
    }

    public String toShortString() {
        return toShortString(offset, unit, id);
    }

    public long idAt(LocalDate date) {
        return idAt(date.atStartOfDay());
    }

    public long idAt(LocalDateTime date) {
        return idAt(offset, unit, date);
    }

    public LocalDateTime dateAt(long id) {
        return dateAt(offset, unit, id);
    }

    private boolean hasSameOffset(TsPeriod period) {
        return offset == period.offset;
    }

    long getRebasedId(TsPeriod period) {
        return hasSameOffset(period)
                ? period.id
                : idAt(period.start());
    }

    void checkCompatibility(TsPeriod period) throws IllegalArgumentException {
        if (!unit.equals(period.unit)) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final int DEFAULT_OFFSET = 0;
    public static final LocalDateTime EPOCH = LocalDate.of(1970, 1, 1).atStartOfDay();

    public static TsPeriod of(TsUnit unit, LocalDateTime date) {
        return make(DEFAULT_OFFSET, unit, date);
    }

    public static TsPeriod of(TsUnit unit, LocalDate date) {
        return make(DEFAULT_OFFSET, unit, date);
    }

    public static TsPeriod of(TsUnit unit, long id) {
        return make(DEFAULT_OFFSET, unit, id);
    }

    public static TsPeriod yearly(int year) {
        return make(DEFAULT_OFFSET, TsUnit.YEARLY, LocalDate.of(year, 1, 1));
    }

    public static TsPeriod quarterly(int year, int quarter) {
        return make(DEFAULT_OFFSET, TsUnit.QUARTERLY, LocalDate.of(year, ((quarter - 1) * 3) + 1, 1));
    }

    public static TsPeriod monthly(int year, int month) {
        return make(DEFAULT_OFFSET, TsUnit.MONTHLY, LocalDate.of(year, month, 1));
    }

    public static TsPeriod daily(int year, int month, int dayOfMonth) {
        return make(DEFAULT_OFFSET, TsUnit.DAILY, LocalDate.of(year, month, dayOfMonth));
    }

    public static TsPeriod hourly(int year, int month, int dayOfMonth, int hour) {
        return make(DEFAULT_OFFSET, TsUnit.HOURLY, LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    }

    public static TsPeriod minutely(int year, int month, int dayOfMonth, int hour, int minute) {
        return make(DEFAULT_OFFSET, TsUnit.MINUTELY, LocalDateTime.of(year, month, dayOfMonth, hour, minute));
    }

    public static TsPeriod parse(CharSequence text) throws DateTimeParseException {
        String value = text.toString();

        int idIdx = value.indexOf('#');
        if (idIdx == -1) {
            throw new DateTimeParseException("Text cannot be parsed to a period", text, 0);
        }

        TsUnit unit = TsUnit.parse(value.substring(0, idIdx));

        int offsetIdx = value.indexOf('@', idIdx);
        if (offsetIdx == -1) {
            long id = parseId(value.substring(idIdx + 1));
            return new TsPeriod(DEFAULT_OFFSET, unit, id);
        }

        long id = parseId(value.substring(idIdx + 1, offsetIdx));
        int offset = parseOffset(value.substring(offsetIdx + 1));
        return new TsPeriod(offset, unit, id);
    }

    private static long parseId(String o) {
        try {
            return Long.parseLong(o);
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to an id", o, 0, ex);
        }
    }

    private static int parseOffset(String o) {
        try {
            return Integer.parseInt(o);
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to an offset", o, 0, ex);
        }
    }

    private static TsPeriod make(int offset, TsUnit unit, LocalDate date) {
        return new TsPeriod(offset, unit, idAt(offset, unit, date.atStartOfDay()));
    }

    private static TsPeriod make(int offset, TsUnit unit, LocalDateTime date) {
        return new TsPeriod(offset, unit, idAt(offset, unit, date));
    }

    private static TsPeriod make(int offset, TsUnit unit, long id) {
        return new TsPeriod(offset, unit, id);
    }

    public static long idAt(int offset, TsUnit unit, LocalDateTime date) {
        return unit.getChronoUnit().between(EPOCH, date) / unit.getAmount() - offset;
    }

    public static LocalDateTime dateAt(int offset, TsUnit unit, long id) {
        return EPOCH.plus(unit.getAmount() * (id + offset), unit.getChronoUnit());
    }

    private static String toString(int offset, TsUnit unit, long id) {
        return DEFAULT_OFFSET == offset
                ? String.format("TsPeriod(unit=%s, start=%s)", unit, dateAt(offset, unit, id))
                : String.format("TsPeriod(offset=%s, unit=%s, start=%s)", offset, unit, dateAt(offset, unit, id));
    }

    private static String toShortString(int offset, TsUnit unit, long id) {
        return DEFAULT_OFFSET == offset
                ? String.format("%s#%s", unit, id)
                : String.format("%s#%s@%s", unit, id, offset);
    }

    public static final class Builder implements Range<LocalDateTime> {

        private int offset = DEFAULT_OFFSET;
        private TsUnit unit = TsUnit.MONTHLY;

        private void refreshId(int oldoffset, TsUnit oldUnit, int newoffset, TsUnit newUnit) {
            this.id = idAt(newoffset, newUnit, dateAt(oldoffset, oldUnit, id));
        }

        public Builder offset(int offset) {
            refreshId(this.offset, this.unit, this.offset = offset, this.unit);
            return this;
        }

        public Builder unit(TsUnit unit) {
            refreshId(this.offset, this.unit, this.offset, this.unit = unit);
            return this;
        }

        public Builder date(LocalDate date) {
            return date(date.atStartOfDay());
        }

        public Builder date(LocalDateTime date) {
            this.id = idAt(offset, unit, date);
            return this;
        }

        public Builder plus(int count) {
            this.id += count;
            return this;
        }

        @Override
        public LocalDateTime start() {
            return dateAt(offset, unit, id);
        }

        @Override
        public LocalDateTime end() {
            return dateAt(offset, unit, id + 1);
        }

        @Override
        public boolean contains(LocalDateTime date) {
            return idAt(offset, unit, date) == id;
        }

        @Override
        public String toString() {
            return TsPeriod.toString(offset, unit, id);
        }

        public String toShortString() {
            return TsPeriod.toShortString(offset, unit, id);
        }
    }
}

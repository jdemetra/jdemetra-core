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

    @lombok.NonNull
    LocalDateTime origin;

    @lombok.NonNull
    TsFrequency freq;

    long offset;

    @Override
    public LocalDateTime start() {
        return dateAt(origin, freq, offset);
    }

    @Override
    public LocalDateTime end() {
        return dateAt(origin, freq, offset + 1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return offsetAt(origin, freq, date) == offset;
    }

    @Override
    public int compareTo(TsPeriod period) {
        checkCompatibility(period);
        return Long.compare(offset, getRebasedOffset(period));
    }

    public boolean isAfter(TsPeriod period) {
        checkCompatibility(period);
        return offset > getRebasedOffset(period);
    }

    public boolean isBefore(TsPeriod period) {
        checkCompatibility(period);
        return offset < getRebasedOffset(period);
    }

    public TsPeriod next() {
        return plus(1);
    }

    public TsPeriod plus(long count) {
        return new TsPeriod(origin, freq, offset + count);
    }

    public TsPeriod withOrigin(LocalDate newOrigin) {
        return withOrigin(newOrigin.atStartOfDay());
    }

    public TsPeriod withOrigin(LocalDateTime newOrigin) {
        return make(newOrigin, freq, start());
    }

    public TsPeriod withFreq(TsFrequency newFreq) {
        return make(origin, newFreq, start());
    }

    public TsPeriod withDate(LocalDate date) {
        return withDate(date.atStartOfDay());
    }

    public TsPeriod withDate(LocalDateTime date) {
        return make(origin, freq, date);
    }

    public TsPeriod withOffset(long offset) {
        return new TsPeriod(origin, freq, offset);
    }

    public int until(TsPeriod endExclusive) {
        return (int) (offset - getRebasedOffset(endExclusive));
    }

    @Override
    public String toString() {
        return start().toLocalDate().toString();
    }

    public String toShortString() {
        return toShortString(origin, freq, offset);
    }

    public long offsetAt(LocalDate date) {
        return offsetAt(date.atStartOfDay());
    }

    public long offsetAt(LocalDateTime date) {
        return offsetAt(origin, freq, date);
    }

    public LocalDateTime dateAt(long offset) {
        return dateAt(origin, freq, offset);
    }

    private boolean hasSameOrigin(TsPeriod period) {
        return getOrigin().equals(period.getOrigin());
    }

    long getRebasedOffset(TsPeriod period) {
        return hasSameOrigin(period)
                ? period.getOffset()
                : offsetAt(period.start());
    }

    void checkCompatibility(TsPeriod period) throws IllegalArgumentException {
        if (!getFreq().equals(period.getFreq())) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final LocalDateTime DEFAULT_ORIGIN = LocalDate.of(1970, 1, 1).atStartOfDay();

    public static TsPeriod of(TsFrequency freq, LocalDateTime date) {
        return make(DEFAULT_ORIGIN, freq, date);
    }

    public static TsPeriod of(TsFrequency freq, LocalDate date) {
        return make(DEFAULT_ORIGIN, freq, date);
    }

    public static TsPeriod of(TsFrequency freq, long offset) {
        return make(DEFAULT_ORIGIN, freq, offset);
    }

    public static TsPeriod yearly(int year) {
        return make(DEFAULT_ORIGIN, TsFrequency.YEARLY, LocalDate.of(year, 1, 1));
    }

    public static TsPeriod quarterly(int year, int quarter) {
        return make(DEFAULT_ORIGIN, TsFrequency.QUARTERLY, LocalDate.of(year, ((quarter - 1) * 3) + 1, 1));
    }

    public static TsPeriod monthly(int year, int month) {
        return make(DEFAULT_ORIGIN, TsFrequency.MONTHLY, LocalDate.of(year, month, 1));
    }

    public static TsPeriod daily(int year, int month, int dayOfMonth) {
        return make(DEFAULT_ORIGIN, TsFrequency.DAILY, LocalDate.of(year, month, dayOfMonth));
    }

    public static TsPeriod hourly(int year, int month, int dayOfMonth, int hour) {
        return make(DEFAULT_ORIGIN, TsFrequency.HOURLY, LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    }

    public static TsPeriod minutely(int year, int month, int dayOfMonth, int hour, int minute) {
        return make(DEFAULT_ORIGIN, TsFrequency.MINUTELY, LocalDateTime.of(year, month, dayOfMonth, hour, minute));
    }

    public static TsPeriod parse(CharSequence text) throws DateTimeParseException {
        String value = text.toString();

        int offsetIdx = value.indexOf('#');
        if (offsetIdx == -1) {
            throw new DateTimeParseException("Text cannot be parsed to a period", text, 0);
        }

        TsFrequency freq = TsFrequency.parse(value.substring(0, offsetIdx));

        int originIdx = value.indexOf('@', offsetIdx);
        if (originIdx == -1) {
            long offset = parseOffset(value.substring(offsetIdx + 1));
            return new TsPeriod(DEFAULT_ORIGIN, freq, offset);
        }

        long offset = parseOffset(value.substring(offsetIdx + 1, originIdx));
        LocalDateTime origin = LocalDateTime.parse(value.substring(originIdx + 1));
        return new TsPeriod(origin, freq, offset);
    }

    private static long parseOffset(String o) {
        try {
            return Long.parseLong(o);
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to an offset", o, 0, ex);
        }
    }

    private static TsPeriod make(LocalDateTime origin, TsFrequency freq, LocalDate date) {
        return new TsPeriod(origin, freq, offsetAt(origin, freq, date.atStartOfDay()));
    }

    private static TsPeriod make(LocalDateTime origin, TsFrequency freq, LocalDateTime date) {
        return new TsPeriod(origin, freq, offsetAt(origin, freq, date));
    }

    private static TsPeriod make(LocalDateTime origin, TsFrequency freq, long offset) {
        return new TsPeriod(origin, freq, offset);
    }

    private static long offsetAt(LocalDateTime origin, TsFrequency freq, LocalDateTime date) {
        return freq.getUnit().between(origin, date) / freq.getAmount();
    }

    private static LocalDateTime dateAt(LocalDateTime origin, TsFrequency freq, long offset) {
        return origin.plus(freq.getAmount() * offset, freq.getUnit());
    }

    private static String toShortDateString(LocalDateTime date) {
        return date.toString();
        // FIXME: this is a problem when parsing later on -> need to investigate
//        return LocalTime.MIDNIGHT.equals(date.toLocalTime())
//                ? date.toLocalDate().toString()
//                : date.toString();
    }

    private static String toString(LocalDateTime origin, TsFrequency freq, long offset) {
        return DEFAULT_ORIGIN.equals(origin)
                ? String.format("TsPeriod(freq=%s, offset=%s)", freq, offset)
                : String.format("TsPeriod(origin=%s, freq=%s, offset=%s)", toShortDateString(origin), freq, offset);
    }

    private static String toShortString(LocalDateTime origin, TsFrequency freq, long offset) {
        return DEFAULT_ORIGIN.equals(origin)
                ? String.format("%s#%s", freq, offset)
                : String.format("%s#%s@%s", freq, offset, toShortDateString(origin));
    }

    public static final class Builder implements Range<LocalDateTime> {

        private LocalDateTime origin = DEFAULT_ORIGIN;
        private TsFrequency freq = TsFrequency.MONTHLY;

        private void refreshOffset(LocalDateTime oldOrigin, TsFrequency oldFreq, LocalDateTime newOrigin, TsFrequency newFreq) {
            offset = offsetAt(newOrigin, newFreq, dateAt(oldOrigin, oldFreq, offset));
        }

        public Builder origin(LocalDate origin) {
            return origin(origin.atStartOfDay());
        }

        public Builder origin(LocalDateTime origin) {
            refreshOffset(this.origin, this.freq, this.origin = origin, this.freq);
            return this;
        }

        public Builder freq(TsFrequency freq) {
            refreshOffset(this.origin, this.freq, this.origin, this.freq = freq);
            return this;
        }

        public Builder date(LocalDate date) {
            return date(date.atStartOfDay());
        }

        public Builder date(LocalDateTime date) {
            this.offset = offsetAt(origin, freq, date);
            return this;
        }

        public Builder plus(int count) {
            this.offset += count;
            return this;
        }

        @Override
        public LocalDateTime start() {
            return dateAt(origin, freq, offset);
        }

        @Override
        public LocalDateTime end() {
            return dateAt(origin, freq, offset + 1);
        }

        @Override
        public boolean contains(LocalDateTime date) {
            return offsetAt(origin, freq, date) == offset;
        }

        @Override
        public String toString() {
            return start().toLocalDate().toString();
        }

        public String toShortString() {
            return TsPeriod.toShortString(origin, freq, offset);
        }
    }
}

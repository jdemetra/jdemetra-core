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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TsPeriod implements Range<LocalDateTime>, Comparable<TsPeriod> {

    @lombok.NonNull
    LocalDateTime reference; // NOT NECESSARY EPOCH

    @lombok.NonNull
    TsUnit unit;

    long id;

    @Override
    public LocalDateTime start() {
        return dateAt(reference, unit, id);
    }

    @Override
    public LocalDateTime end() {
        return dateAt(reference, unit, id + 1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return idAt(reference, unit, date) == id;
    }

    @Override
    public int compareTo(TsPeriod period) {
        checkCompatibility(period);
        return Long.compare(id, getRebasedId(period));
    }

    public int year() {
        return start().getYear();
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
        if (count == 0) {
            return this;
        }
        return new TsPeriod(reference, unit, id + count);
    }

    public TsPeriod withReference(LocalDateTime reference) {
        if (reference.equals(this.reference)) {
            return this;
        }
        return make(reference.equals(EPOCH) ? EPOCH : reference, unit, start());
    }

    public TsPeriod withUnit(TsUnit newUnit) {
        if (unit.equals(newUnit)) {
            return this;
        }
        return make(reference, newUnit, start());
    }

    public TsPeriod withDate(LocalDate date) {
        return withDate(date.atStartOfDay());
    }

    public TsPeriod withDate(LocalDateTime date) {
        return make(reference, unit, date);
    }

    public TsPeriod withId(long id) {
        if (this.id == id) {
            return this;
        }
        return new TsPeriod(reference, unit, id);
    }

    /**
     * Distance between this period and the given period (exclusive)
     *
     * @param endExclusive The given period
     * @return The result is 0 when the two periods are equal, positive if the given
     * period is after this period or negative otherwise.
     */
    public int until(TsPeriod endExclusive) {
        checkCompatibility(endExclusive);
        return (int) (getRebasedId(endExclusive) - id);
    }

//    /**
//     * 
//     * @param low
//     * @return 
//     */
//    public int getPosition(TsUnit low) {
//        return getPosition(reference, this.unit, id, low);
//    }
//
    @Override
    public String toString() {
        return toString(reference, unit, id);
    }

    public String toShortString() {
        return toShortString(reference, unit, id);
    }

    public long idAt(LocalDate date) {
        return idAt(date.atStartOfDay());
    }

    public long idAt(LocalDateTime date) {
        return idAt(reference, unit, date);
    }

    public LocalDateTime dateAt(long id) {
        return dateAt(reference, unit, id);
    }

    private boolean hasSameReference(TsPeriod period) {
        return reference.equals(period.reference);
    }

    long getRebasedId(TsPeriod period) {
        return hasSameReference(period)
                ? period.id
                : idAt(period.start());
    }

    void checkCompatibility(TsPeriod period) throws IllegalArgumentException {
        if (unit != period.unit && !unit.equals(period.unit)) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final LocalDateTime EPOCH = LocalDate.of(1970, 1, 1).atStartOfDay();

    public static TsPeriod of(TsUnit unit, LocalDateTime date) {
        return make(EPOCH, unit, date);
    }

    public static TsPeriod of(TsUnit unit, LocalDate date) {
        return make(EPOCH, unit, date);
    }

    public static TsPeriod of(TsUnit unit, long id) {
        return make(EPOCH, unit, id);
    }

    public static TsPeriod yearly(int year) {
        return make(EPOCH, TsUnit.YEAR, LocalDate.of(year, 1, 1));
    }

    public static TsPeriod quarterly(int year, int quarter) {
        return make(EPOCH, TsUnit.QUARTER, LocalDate.of(year, ((quarter - 1) * 3) + 1, 1));
    }

    public static TsPeriod monthly(int year, int month) {
        return make(EPOCH, TsUnit.MONTH, LocalDate.of(year, month, 1));
    }

    public static TsPeriod daily(int year, int month, int dayOfMonth) {
        return make(EPOCH, TsUnit.DAY, LocalDate.of(year, month, dayOfMonth));
    }

    public static TsPeriod hourly(int year, int month, int dayOfMonth, int hour) {
        return make(EPOCH, TsUnit.HOUR, LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    }

    public static TsPeriod minutely(int year, int month, int dayOfMonth, int hour, int minute) {
        return make(EPOCH, TsUnit.MINUTE, LocalDateTime.of(year, month, dayOfMonth, hour, minute));
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
            return new TsPeriod(EPOCH, unit, id);
        }

        long id = parseId(value.substring(idIdx + 1, offsetIdx));
        LocalDateTime reference = parseReference(value.substring(offsetIdx + 1));
        return new TsPeriod(reference, unit, id);
    }

    private static long parseId(String o) {
        try {
            return Long.parseLong(o);
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to an id", o, 0, ex);
        }
    }

    private static LocalDateTime parseReference(String o) {
        return LocalDateTime.parse(o);
    }

    private static TsPeriod make(LocalDateTime reference, TsUnit unit, LocalDate date) {
        return new TsPeriod(reference, unit, idAt(reference, unit, date.atStartOfDay()));
    }

    private static TsPeriod make(LocalDateTime reference, TsUnit unit, LocalDateTime date) {
        return new TsPeriod(reference, unit, idAt(reference, unit, date));
    }

    private static TsPeriod make(LocalDateTime reference, TsUnit unit, long id) {
        return new TsPeriod(reference, unit, id);
    }

    public static long idAt(LocalDateTime reference, TsUnit unit, LocalDateTime date) {
        if (date.compareTo(reference) >= 0) {
            return (unit.getChronoUnit().between(reference, date)) / unit.getAmount();
        } else {
            long result = (unit.getChronoUnit().between(reference, date)) / unit.getAmount();
            return dateAt(reference, unit, result).compareTo(date) <= 0 ? result : result - 1;
        }
    }

    public static LocalDateTime dateAt(LocalDateTime reference, TsUnit unit, long id) {
        return reference.plus(unit.getAmount() * id, unit.getChronoUnit());
    }

//    private static int getPosition(LocalDateTime reference, TsUnit high, long id, TsUnit low) {
//        long id0 = id;
//        long id1 = idAt(reference, low, dateAt(reference, high, id0));
//        long id2 = idAt(reference, high, dateAt(reference, low, id1));
//        return (int) (id0 - id2);
//    }
//
    private static String toString(LocalDateTime reference, TsUnit unit, long id) {
        return EPOCH == reference
                ? String.format("TsPeriod(unit=%s, start=%s)", unit, dateAt(reference, unit, id))
                : String.format("TsPeriod(reference=%s, unit=%s, start=%s)", reference, unit, dateAt(reference, unit, id));
    }

    private static String toShortString(LocalDateTime reference, TsUnit unit, long id) {
        if (EPOCH.equals(reference)) {
            return String.format("%s#%s", unit, id);
        }
        String sref = reference.format(DateTimeFormatter.ISO_DATE);
        return String.format("%s#%s@%s", unit, id, sref);
    }

    public String display() {
        if (unit.getChronoUnit().getDuration().compareTo(ChronoUnit.DAYS.getDuration()) >= 0) {
            return start().toLocalDate().toString();
        } else {
            return start().toString();
        }
    }

    public static final class Builder implements Range<LocalDateTime> {

        private LocalDateTime reference = EPOCH;
        private TsUnit unit = TsUnit.MONTH;
        private long id;

        private void refreshId(LocalDateTime oldref, TsUnit oldUnit, LocalDateTime newref, TsUnit newUnit) {
            this.id = TsPeriod.idAt(newref, newUnit, dateAt(oldref, oldUnit, id));
        }

        public Builder reference(LocalDateTime reference) {
            refreshId(this.reference, this.unit, this.reference = reference, this.unit);
            return this;
        }

        public Builder unit(TsUnit unit) {
            refreshId(this.reference, this.unit, this.reference, this.unit = unit);
            return this;
        }

        public Builder date(LocalDate date) {
            return date(date.atStartOfDay());
        }

        public Builder date(LocalDateTime date) {
            this.id = TsPeriod.idAt(reference, unit, date);
            return this;
        }

        public Builder plus(int count) {
            this.id += count;
            return this;
        }

//        public int getPosition(TsUnit low) {
//            return TsPeriod.getPosition(reference, this.unit, id, low);
//        }
//
        @Override
        public LocalDateTime start() {
            return TsPeriod.dateAt(reference, unit, id);
        }

        @Override
        public LocalDateTime end() {
            return TsPeriod.dateAt(reference, unit, id + 1);
        }

        @Override
        public boolean contains(LocalDateTime date) {
            return TsPeriod.idAt(reference, unit, date) == id;
        }

        @Override
        public String toString() {
            return TsPeriod.toString(reference, unit, id);
        }

        public String toShortString() {
            return TsPeriod.toShortString(reference, unit, id);
        }
    }
}

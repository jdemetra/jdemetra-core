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

import demetra.time.IsoIntervalConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.time.IsoConverter;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TsPeriod implements TimeSeriesInterval<TsUnit>, Comparable<TsPeriod> {

    @lombok.NonNull
    LocalDateTime epoch;

    @lombok.NonNull
    TsUnit unit;

    long id;

    @Override
    public LocalDateTime start() {
        return dateAt(epoch, unit, id);
    }

    @Override
    public LocalDateTime end() {
        return dateAt(epoch, unit, id + 1);
    }

    @Override
    public boolean contains(LocalDateTime date) {
        return idAt(epoch, unit, date) == id;
    }

    @Override
    public TsUnit getDuration() {
        return unit;
    }

    @Override
    public int compareTo(TsPeriod period) {
        checkCompatibility(period);
        return Long.compare(id, getRebasedId(period));
    }

    /**
     * Year of the start of this period
     *
     * @return
     */
    public int year() {
        return start().getYear();
    }

    /**
     * 0-based position of this period in the year
     *
     * @return
     */
    public int annualPosition() {
        TsPeriod p = withUnit(TsUnit.YEAR);
        return TsDomain.splitOf(p, unit, true).indexOf(this);
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

    public TsPeriod previous() {
        return plus(-1);
    }

    public TsPeriod plus(long count) {
        if (count == 0) {
            return this;
        }
        return new TsPeriod(epoch, unit, id + count);
    }

    public TsPeriod withEpoch(LocalDateTime epoch) {
        if (epoch.equals(this.epoch)) {
            return this;
        }
        return make(epoch.equals(DEFAULT_EPOCH) ? DEFAULT_EPOCH : epoch, unit, start());
    }

    public TsPeriod withUnit(TsUnit newUnit) {
        if (unit.equals(newUnit)) {
            return this;
        }
        return make(epoch, newUnit, start());
    }

    public TsPeriod withDate(LocalDateTime date) {
        return make(epoch, unit, date);
    }

    public TsPeriod withId(long id) {
        if (this.id == id) {
            return this;
        }
        return new TsPeriod(epoch, unit, id);
    }

    /**
     * Distance between this period and the given period (exclusive)
     *
     * @param endExclusive The given period
     * @return The result is 0 when the two periods are equal, positive if the
     * given period is after this period or negative otherwise.
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
//        return getPosition(epoch, this.unit, id, low);
//    }
//
    @Override
    public String toString() {
        return toISO8601();
    }

    @Override
    public String toISO8601() {
        return CONVERTER.format(this).toString();
    }

    public long idAt(LocalDateTime date) {
        return idAt(epoch, unit, date);
    }

    public LocalDateTime dateAt(long id) {
        return dateAt(epoch, unit, id);
    }

    public boolean hasDefaultEpoch() {
        return epoch.equals(DEFAULT_EPOCH);
    }

    private boolean hasSameEpoch(TsPeriod period) {
        return epoch.equals(period.epoch);
    }

    long getRebasedId(TsPeriod period) {
        return hasSameEpoch(period)
                ? period.id
                : idAt(period.start());
    }

    void checkCompatibility(TsPeriod period) throws IllegalArgumentException {
        if (unit != period.unit && !unit.equals(period.unit)) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final LocalDateTime DEFAULT_EPOCH = LocalDate.ofEpochDay(0).atStartOfDay();

    public static TsPeriod of(TsUnit unit, LocalDateTime date) {
        return make(DEFAULT_EPOCH, unit, date);
    }

    public static TsPeriod of(TsUnit unit, LocalDate date) {
        return make(DEFAULT_EPOCH, unit, date);
    }

    public static TsPeriod of(TsUnit unit, long id) {
        return make(DEFAULT_EPOCH, unit, id);
    }

    public static TsPeriod yearly(int year) {
        return make(DEFAULT_EPOCH, TsUnit.YEAR, LocalDate.of(year, 1, 1));
    }

    /**
     * Creates a quarterly period
     *
     * @param year Year of the period
     * @param quarter Quarter of the period (in 1-4)
     * @return
     */
    public static TsPeriod quarterly(int year, int quarter) {
        return make(DEFAULT_EPOCH, TsUnit.QUARTER, LocalDate.of(year, ((quarter - 1) * 3) + 1, 1));
    }

    /**
     * Creates a monthly period
     *
     * @param year Year of the period
     * @param month Month of the period (in 1-12)
     * @return
     */
    public static TsPeriod monthly(int year, int month) {
        return make(DEFAULT_EPOCH, TsUnit.MONTH, LocalDate.of(year, month, 1));
    }

    /**
     * Creates a period of one day
     *
     * @param year Year of the day
     * @param month Month of the day (in 1-12)
     * @param dayOfMonth Day of month of the day (1-31)
     * @return
     */
    public static TsPeriod daily(int year, int month, int dayOfMonth) {
        return make(DEFAULT_EPOCH, TsUnit.DAY, LocalDate.of(year, month, dayOfMonth));
    }

    /**
     * Creates a period of seven days
     *
     * @param year Year of the first day
     * @param month Month of the first day (in 1-12)
     * @param dayOfMonth Day of month of the first day (1-31)
     * @return
     */
    public static TsPeriod weekly(int year, int month, int dayOfMonth) {
        LocalDate start = LocalDate.of(year, month, dayOfMonth);
        int dw_start = start.getDayOfWeek().getValue();
        int dw_epoch = DEFAULT_EPOCH.getDayOfWeek().getValue();
        return make(DEFAULT_EPOCH.plusDays(dw_start - dw_epoch), TsUnit.WEEK, start);
    }

    public static TsPeriod hourly(int year, int month, int dayOfMonth, int hour) {
        return make(DEFAULT_EPOCH, TsUnit.HOUR, LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    }

    public static TsPeriod minutely(int year, int month, int dayOfMonth, int hour, int minute) {
        return make(DEFAULT_EPOCH, TsUnit.MINUTE, LocalDateTime.of(year, month, dayOfMonth, hour, minute));
    }

    @NonNull
    public static TsPeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        return CONVERTER.parse(text);
    }

    private static TsPeriod make(LocalDateTime start, TsUnit duration) {
        return TsPeriod.of(duration, start);
    }

    static final IsoIntervalConverter<TsPeriod> CONVERTER
            = new IsoIntervalConverter.StartDuration<>(IsoConverter.LOCAL_DATE_TIME, TsUnit.CONVERTER, TsPeriod::make);

    private static TsPeriod make(LocalDateTime epoch, TsUnit unit, LocalDate date) {
        return new TsPeriod(epoch, unit, idAt(epoch, unit, date.atStartOfDay()));
    }

    private static TsPeriod make(LocalDateTime epoch, TsUnit unit, LocalDateTime date) {
        return new TsPeriod(epoch, unit, idAt(epoch, unit, date));
    }

    private static TsPeriod make(LocalDateTime epoch, TsUnit unit, long id) {
        return new TsPeriod(epoch, unit, id);
    }

    public static long idAt(LocalDateTime epoch, TsUnit unit, LocalDateTime date) {
        if (date.compareTo(epoch) >= 0) {
            return (unit.getChronoUnit().between(epoch, date)) / unit.getAmount();
        } else {
            long result = (unit.getChronoUnit().between(epoch, date)) / unit.getAmount();
            return dateAt(epoch, unit, result).compareTo(date) <= 0 ? result : result - 1;
        }
    }

    public static LocalDateTime dateAt(LocalDateTime epoch, TsUnit unit, long id) {
        return epoch.plus(unit.getAmount() * id, unit.getChronoUnit());
    }

//    private static int getPosition(LocalDateTime epoch, TsUnit high, long id, TsUnit low) {
//        long id0 = id;
//        long id1 = idAt(epoch, low, dateAt(epoch, high, id0));
//        long id2 = idAt(epoch, high, dateAt(epoch, low, id1));
//        return (int) (id0 - id2);
//    }
//
    public String display() {
        if (unit.getChronoUnit().getDuration().compareTo(ChronoUnit.DAYS.getDuration()) >= 0) {
            return start().toLocalDate().toString();
        } else {
            return start().toString();
        }
    }

    public static final class Builder implements TimeSeriesInterval<TsUnit> {

        private LocalDateTime epoch = DEFAULT_EPOCH;
        private TsUnit unit = TsUnit.MONTH;
        private long id;

        private void refreshId(LocalDateTime oldref, TsUnit oldUnit, LocalDateTime newref, TsUnit newUnit) {
            this.id = TsPeriod.idAt(newref, newUnit, dateAt(oldref, oldUnit, id));
        }

        public Builder epoch(LocalDateTime epoch) {
            refreshId(this.epoch, this.unit, this.epoch = epoch, this.unit);
            return this;
        }

        public Builder unit(TsUnit unit) {
            refreshId(this.epoch, this.unit, this.epoch, this.unit = unit);
            return this;
        }

        public Builder date(LocalDate date) {
            return date(date.atStartOfDay());
        }

        public Builder date(LocalDateTime date) {
            this.id = TsPeriod.idAt(epoch, unit, date);
            return this;
        }

        public Builder plus(int count) {
            this.id += count;
            return this;
        }

//        public int getPosition(TsUnit low) {
//            return TsPeriod.getPosition(epoch, this.unit, id, low);
//        }
//
        @Override
        public LocalDateTime start() {
            return TsPeriod.dateAt(epoch, unit, id);
        }

        @Override
        public LocalDateTime end() {
            return TsPeriod.dateAt(epoch, unit, id + 1);
        }

        @Override
        public TsUnit getDuration() {
            return unit;
        }

        @Override
        public boolean contains(LocalDateTime date) {
            return TsPeriod.idAt(epoch, unit, date) == id;
        }

        @Override
        public String toString() {
            return toISO8601();
        }

        @Override
        public String toISO8601() {
            return converter.format(this).toString();
        }

        private Builder apply(LocalDateTime start, TsUnit duration) {
            return this;
        }

        private final IsoIntervalConverter<Builder> converter
                = new IsoIntervalConverter.StartDuration<>(IsoConverter.LOCAL_DATE_TIME, TsUnit.CONVERTER, this::apply);
    }
}

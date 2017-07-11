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
package ec.tss.tsproviders.utils;

import com.google.common.base.Preconditions;
import ec.tstoolkit.design.IBuilder;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.ObjLongToIntFunction;
import ec.tstoolkit.timeseries.simplets.ObsList.LongObsList;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import ec.tstoolkit.timeseries.simplets.ObsList;
import java.util.EnumSet;
import java.util.Set;

/**
 * An immutable object that may contain some time series data. Each instance of
 * this type either contains a non-null data, or contains a message explaining
 * why the data is "absent"; it is never said to "contain {@code null}".
 *
 * <p>
 * A non-null {@link OptionalTsData} reference can be used as a replacement for
 * a nullable {@link TsData} reference. It allows you to represent "a
 * {@code TsData} that must be present" and a "{@link TsData} that might be
 * absent" as two distinct types in your program, which can aid clarity.
 *
 * <p>
 * Note that, since {@link TsData} is mutable, this class create a new TsData at
 * every call of {@link  #get} in order to guarantee its immutability.
 *
 * @author Philippe Charles
 * @see Optional
 */
@Immutable
public abstract class OptionalTsData {

    //<editor-fold defaultstate="collapsed" desc="Factories">
    /**
     * Creates an OptionalTsData that contains times series data.
     *
     * @param data
     * @return non-null OptionalTsData
     * @since 2.2.0
     */
    @Nonnull
    public static OptionalTsData present(@Nonnull TsData data) {
        return new Present(Objects.requireNonNull(data));
    }

    @Deprecated
    @Nonnull
    public static OptionalTsData present(@Nonnegative int nbrRows, @Nonnegative int nbrUselessRows, @Nonnull TsData data) {
        Preconditions.checkArgument(nbrRows >= nbrUselessRows && nbrUselessRows >= 0);
        return present(data);
    }

    /**
     * Creates an empty OptionalTsData.
     *
     * @param cause
     * @return non-null OptionalTsData
     * @since 2.2.0
     */
    @Nonnull
    public static OptionalTsData absent(@Nonnull String cause) {
        return new Absent(Objects.requireNonNull(cause));
    }

    @Deprecated
    @Nonnull
    public static OptionalTsData absent(@Nonnegative int nbrRows, @Nonnegative int nbrUselessRows, @Nonnull String cause) {
        Preconditions.checkArgument(nbrRows >= nbrUselessRows && nbrUselessRows >= 0);
        return absent(cause);
    }

    /**
     * Creates an OptionalTsData builder that collects {@link Date} values.
     *
     * @param resource non-null resource used to handle dates quirks such as
     * time zones
     * @param gathering non-null observation collection parameters
     * @param characteristics non-null observations characteristics
     * @return non-null builder
     * @since 2.2.0
     */
    @Nonnull
    public static Builder2<Date> builderByDate(@Nonnull Calendar resource, @Nonnull ObsGathering gathering, @Nonnull ObsCharacteristics... characteristics) {
        return builder(gathering, toEnumSet(characteristics), Date::getTime, (f, p) -> getIdFromTimeInMillis(resource, f, p));
    }

    /**
     * Creates an OptionalTsData builder that collects {@link LocalDate} values.
     *
     * @param gathering non-null observation collection parameters
     * @param characteristics non-null observations characteristics
     * @return non-null builder
     * @since 2.2.0
     */
    @Nonnull
    public static Builder2<LocalDate> builderByLocalDate(@Nonnull ObsGathering gathering, @Nonnull ObsCharacteristics... characteristics) {
        return builder(gathering, toEnumSet(characteristics), OptionalTsData::getYearMonthDay, OptionalTsData::getIdFromYearMonthDay);
    }
    //</editor-fold>

    /**
     * Returns the number of rows that were read while creating this data.
     *
     * @return a non-negative number of rows
     */
    @Deprecated
    @Nonnegative
    public int getNbrRows() {
        return 0;
    }

    /**
     * Returns the number of rows that were read and were useless while creating
     * this data.
     *
     * @return a non-negative number of rows
     */
    @Deprecated
    @Nonnegative
    public int getNbrUselessRows() {
        return 0;
    }

    /**
     * Returns {@code true} if this holder contains some time series data.
     *
     * @return true if not empty, false otherwise
     */
    abstract public boolean isPresent();

    /**
     * Returns the time series data, which must be present. If the data might be
     * absent, use {@link #or(TsData)} or {@link #orNull} instead.
     *
     * @return a new non-null TsData
     * @throws IllegalStateException if the time series data is absent
     * ({@link #isPresent} returns {@code false})
     */
    @Nonnull
    @NewObject
    abstract public TsData get() throws IllegalStateException;

    /**
     * Returns the time series data if it is present; {@code defaultValue}
     * otherwise. If no default value should be required because the data is
     * known to be present, use {@link #get()} instead. For a default value of
     * {@code null}, use {@link #orNull}.
     *
     * @param defaultValue a non-null default value if the data is absent
     * @return a non-null TsData
     */
    @Nonnull
    public TsData or(@Nonnull TsData defaultValue) {
        Objects.requireNonNull(defaultValue, "use orNull() instead of or(null)");
        return isPresent() ? get() : defaultValue;
    }

    /**
     * Returns the time series data if it is present; {@code null} otherwise. If
     * the data is known to be present, use {@link #get()} instead.
     *
     * @return a TsData if present, null otherwise
     */
    @Nullable
    abstract public TsData orNull();

    /**
     * Returns a message explaining why the time series data is "absent".
     *
     * @return non-null message
     * @throws IllegalStateException if the data is present
     */
    @Nonnull
    abstract public String getCause() throws IllegalStateException;

    /**
     * Builder that collects observations in order to create an OptionalTsData.
     *
     * @param <T>
     * @since 2.2.0
     */
    @NotThreadSafe
    public interface Builder2<T> extends IBuilder<OptionalTsData> {

        /**
         * Removes all observations.
         *
         * @return this builder
         */
        @Nonnull
        Builder2<T> clear();

        /**
         * Adds an observation by using a date. This date belongs to the
         * considered period (it is not retained in the final time series).
         * <p>
         * An observation with <code>null</code> date is ignored and
         * <code>null</code> value is considered as missing.
         *
         * @param date an optional date
         * @param value an optional value
         * @return this builder
         */
        @Nonnull
        Builder2<T> add(@Nullable T date, @Nullable Number value);

        /**
         * Adds an observation by using a date.
         *
         * @param <X> the observation type
         * @param obs the non-null observation to add
         * @param dateFunc a non-null function that retrieves a date from an
         * observation
         * @param valueFunc a non-null function that retrieves a value from an
         * observation
         * @return this builder
         */
        @Nonnull
        default <X> Builder2<T> add(X obs, Function<? super X, ? extends T> dateFunc, Function<? super X, ? extends Number> valueFunc) {
            T date = dateFunc.apply(obs);
            return Builder2.this.add(date, date != null ? valueFunc.apply(obs) : null);
        }

        /**
         * Adds a stream of observations by using a date.
         *
         * @param <X> the observation type
         * @param stream the non-null stream to add
         * @param dateFunc a non-null function that retrieves a date from an
         * observation
         * @param valueFunc a non-null function that retrieves a value from an
         * observation
         * @return this builder
         */
        @Nonnull
        default <X> Builder2<T> addAll(Stream<X> stream, Function<? super X, ? extends T> dateFunc, Function<? super X, ? extends Number> valueFunc) {
            stream.forEach(o -> add(o, dateFunc, valueFunc));
            return this;
        }

        /**
         * Creates an OptionalTsData from the collected observations.
         *
         * @return a non-null OptionalTsData
         */
        @Nonnull
        @Override
        OptionalTsData build();
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class Present extends OptionalTsData {

        // Converter retreived only once at loading time of this class to avoid: 
        // - incompatible formats if changed later on
        // - overhead of synchronous code
        private static final ByteArrayConverter CONVERTER = ByteArrayConverter.getInstance();

        private final int freq;
        private final int year;
        private final int position;
        private final byte[] data;

        private Present(TsData data) {
            TsPeriod start = data.getStart();
            this.freq = start.getFrequency().intValue();
            this.year = start.getYear();
            this.position = start.getPosition();
            this.data = CONVERTER.fromDoubleArray(data.internalStorage());
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public TsData get() {
            return new TsData(TsFrequency.valueOf(freq), year, position, CONVERTER.toDoubleArray(data), false);
        }

        @Override
        public TsData orNull() {
            return get();
        }

        @Override
        public String getCause() throws IllegalStateException {
            throw new IllegalStateException("TsData is present");
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof Present && equals((Present) obj));
        }

        private boolean equals(Present that) {
            return this.freq == that.freq && this.year == that.year && this.position == that.position
                    && Arrays.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(freq, year, position, data);
        }

        @Override
        public String toString() {
            return "Present: " + freq + "@" + year + "/" + position + " " + Arrays.toString(data);
        }
    }

    private static final class Absent extends OptionalTsData {

        private final String cause;

        private Absent(String cause) {
            this.cause = cause;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public TsData get() throws IllegalStateException {
            throw new IllegalStateException(cause);
        }

        @Override
        public TsData orNull() {
            return null;
        }

        @Override
        public String getCause() {
            return cause;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof Absent && equals((Absent) obj));
        }

        private boolean equals(Absent that) {
            return this.cause.equals(that.cause);
        }

        @Override
        public int hashCode() {
            return cause.hashCode();
        }

        @Override
        public String toString() {
            return "Absent: " + cause;
        }
    }

    @VisibleForTesting
    static final OptionalTsData NO_DATA = new Absent("No data available");
    @VisibleForTesting
    static final OptionalTsData INVALID_AGGREGATION = new Absent("Invalid aggregation mode");
    @VisibleForTesting
    static final OptionalTsData GUESS_SINGLE = new Absent("Cannot guess frequency with a single observation");
    @VisibleForTesting
    static final OptionalTsData GUESS_DUPLICATION = new Absent("Cannot guess frequency with duplicated periods");
    @VisibleForTesting
    static final OptionalTsData DUPLICATION_WITHOUT_AGGREGATION = new Absent("Duplicated observations without aggregation");
    @VisibleForTesting
    static final OptionalTsData UNKNOWN = new Absent("Unexpected error");

    private static final class UndefinedWithAggregation<T> implements Builder2<T> {

        @Override
        public Builder2<T> clear() {
            return this;
        }

        @Override
        public Builder2<T> add(T date, Number value) {
            return this;
        }

        @Override
        public OptionalTsData build() {
            return INVALID_AGGREGATION;
        }
    }

    private static final class BuilderSupport<T> implements Builder2<T> {

        private final LongObsList obs;
        private final ToLongFunction<T> periodFunc;
        private final boolean skipMissingValues;
        private final Function<ObsList, OptionalTsData> maker;

        BuilderSupport(
                LongObsList obs,
                ToLongFunction<T> periodFunc,
                boolean skipMissingValues,
                Function<ObsList, OptionalTsData> maker) {
            this.obs = obs;
            this.periodFunc = periodFunc;
            this.skipMissingValues = skipMissingValues;
            this.maker = maker;
        }

        @Override
        public Builder2<T> clear() {
            obs.clear();
            return this;
        }

        @Override
        public Builder2<T> add(T date, Number value) {
            if (date != null) {
                if (value != null) {
                    obs.add(periodFunc.applyAsLong(date), value.doubleValue());
                } else if (!skipMissingValues) {
                    obs.add(periodFunc.applyAsLong(date), Double.NaN);
                }
            }
            return this;
        }

        @Override
        public OptionalTsData build() {
            return maker.apply(obs);
        }
    }

    private static <T> Builder2<T> builder(
            ObsGathering gathering, @Nonnull Set<ObsCharacteristics> characteristics,
            ToLongFunction<T> periodFunc, ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc) {
        boolean ordered = characteristics.contains(ObsCharacteristics.ORDERED);
        if (gathering.getFrequency() == TsFrequency.Undefined) {
            if (gathering.getAggregationType() != TsAggregationType.None) {
                return new UndefinedWithAggregation<>();
            }
            return new BuilderSupport<>(
                    ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                    periodFunc,
                    gathering.isSkipMissingValues(),
                    o -> makeFromUnknownFrequency(o));
        }
        if (gathering.getAggregationType() != TsAggregationType.None) {
            return new BuilderSupport<>(
                    ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                    periodFunc,
                    gathering.isSkipMissingValues(),
                    o -> makeWithAggregation(o, gathering.getFrequency(), gathering.getAggregationType()));
        }
        return new BuilderSupport<>(
                ObsList.newLongObsList(ordered, tsPeriodIdFunc),
                periodFunc,
                gathering.isSkipMissingValues(),
                o -> makeWithoutAggregation(o, gathering.getFrequency()));
    }

    private static OptionalTsData makeFromUnknownFrequency(ObsList obs) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            case 1:
                return GUESS_SINGLE;
            default:
                TsData result = TsDataCollector.makeFromUnknownFrequency(obs);
                return result != null ? present(result) : GUESS_DUPLICATION;
        }
    }

    private static OptionalTsData makeWithoutAggregation(ObsList obs, TsFrequency freq) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeWithoutAggregation(obs, freq);
                return result != null ? present(result) : DUPLICATION_WITHOUT_AGGREGATION;
        }
    }

    private static OptionalTsData makeWithAggregation(ObsList obs, TsFrequency freq, TsAggregationType convMode) {
        switch (obs.size()) {
            case 0:
                return NO_DATA;
            default:
                TsData result = TsDataCollector.makeFromUnknownFrequency(obs);
                if (result != null && (result.getFrequency().intValue() % freq.intValue() == 0)) {
                    // should succeed
                    result = result.changeFrequency(freq, convMode, true);
                } else {
                    result = TsDataCollector.makeWithAggregation(obs, freq, convMode);
                }
                return result != null ? present(result) : UNKNOWN;
        }
    }

    private static int getIdFromTimeInMillis(Calendar cal, TsFrequency freq, long period) {
        cal.setTimeInMillis(period);
        return calcTsPeriodId(freq.intValue(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    private static long getYearMonthDay(LocalDate date) {
        return (long) (date.getYear() * 100 + date.getMonthValue()) * 100 + date.getDayOfMonth();
    }

    private static int getIdFromYearMonthDay(TsFrequency freq, long period) {
        period = period / 100;
        return calcTsPeriodId(freq.intValue(), (int) (period / 100), (int) (period % 100 - 1));
    }

    private static int calcTsPeriodId(int freq, int year, int month) {
        return (year - 1970) * freq + month / (12 / freq);
    }

    private static EnumSet<ObsCharacteristics> toEnumSet(ObsCharacteristics[] items) {
        switch (items.length) {
            case 0:
                return EnumSet.noneOf(ObsCharacteristics.class);
            case 1:
                return EnumSet.of(items[0]);
            default:
                return EnumSet.copyOf(Arrays.asList(items));
        }
    }
    //</editor-fold>

    @Deprecated
    public static final class Builder implements IBuilder<OptionalTsData> {

        @Nonnull
        public static String toString(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            return "(" + freq + "/" + aggregation + ")";
        }

        private final Builder2<Date> delegate;

        public Builder(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            this(freq, aggregation, false);
        }

        public Builder(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation, boolean skipMissingValues) {
            ObsGathering gathering = skipMissingValues
                    ? ObsGathering.excludingMissingValues(freq, aggregation)
                    : ObsGathering.includingMissingValues(freq, aggregation);
            this.delegate = builderByDate(new GregorianCalendar(), gathering);
        }

        @Nonnull
        public Builder clear() {
            delegate.clear();
            return this;
        }

        @Nonnull
        public Builder add(@Nullable Date period, @Nullable Number value) {
            delegate.add(period, value);
            return this;
        }

        @Nonnull
        @Override
        public OptionalTsData build() {
            return delegate.build();
        }
    }
}

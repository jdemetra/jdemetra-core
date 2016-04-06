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

import com.google.common.base.Optional;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    /**
     * Creates an OptionalTsData that contains times series data.
     *
     * @param nbrRows
     * @param nbrUselessRows
     * @param data
     * @return non-null OptionalTsData
     */
    @Nonnull
    public static OptionalTsData present(@Nonnegative int nbrRows, @Nonnegative int nbrUselessRows, @Nonnull TsData data) {
        Objects.requireNonNull(data);
        Preconditions.checkArgument(nbrRows >= nbrUselessRows && nbrUselessRows >= 0);
        return new Present(nbrRows, nbrUselessRows, data);
    }

    /**
     * Creates an empty OptionalTsData.
     *
     * @param nbrRows
     * @param nbrUselessRows
     * @param cause
     * @return non-null OptionalTsData
     */
    @Nonnull
    public static OptionalTsData absent(@Nonnegative int nbrRows, @Nonnegative int nbrUselessRows, @Nonnull String cause) {
        Objects.requireNonNull(cause);
        Preconditions.checkArgument(nbrRows >= nbrUselessRows && nbrUselessRows >= 0);
        return new Absent(nbrRows, nbrUselessRows, cause);
    }

    private final int nbrRows;
    private final int nbrUselessRows;

    private OptionalTsData(int nbrRows, int nbrUselessRows) {
        this.nbrRows = nbrRows;
        this.nbrUselessRows = nbrUselessRows;
    }

    /**
     * Returns the number of rows that were read while creating this data.
     *
     * @return a non-negative number of rows
     */
    @Nonnegative
    public int getNbrRows() {
        return nbrRows;
    }

    /**
     * Returns the number of rows that were read and were useless while creating
     * this data.
     *
     * @return a non-negative number of rows
     */
    @Nonnegative
    public int getNbrUselessRows() {
        return nbrUselessRows;
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

        private Present(int nbrRows, int nbrUselessRows, TsData data) {
            super(nbrRows, nbrUselessRows);
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
    }

    private static final class Absent extends OptionalTsData {

        private final String cause;

        private Absent(int nbrRows, int nbrUselessRows, String cause) {
            super(nbrRows, nbrUselessRows);
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
    }

    private static final class BuilderAbsent extends OptionalTsData {

        private final BuilderCause cause;

        private BuilderAbsent(int nbrRows, int nbrUselessRows, BuilderCause cause) {
            super(nbrRows, nbrUselessRows);
            this.cause = cause;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public TsData get() throws IllegalStateException {
            throw new IllegalStateException(cause.getMessage());
        }

        @Override
        public TsData orNull() {
            return null;
        }

        @Override
        public String getCause() {
            return cause.getMessage();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof BuilderAbsent && equals((BuilderAbsent) obj));
        }

        private boolean equals(BuilderAbsent that) {
            return this.cause == that.cause;
        }

        @Override
        public int hashCode() {
            return cause.hashCode();
        }
    }

    @VisibleForTesting
    enum BuilderCause {

        NO_DATA, INVALID_AGGREGATION, GUESS_SINGLE, GUESS_DUPLICATION, DUPLICATION_WITHOUT_AGGREGATION, UNKNOWN;

        @Nonnull
        public String getMessage() {
            switch (this) {
                case NO_DATA:
                    return "No data available";
                case INVALID_AGGREGATION:
                    return "Invalid aggregation mode";
                case GUESS_SINGLE:
                    return "Cannot guess frequency with a single observation";
                case GUESS_DUPLICATION:
                    return "Cannot guess frequency with duplicated periods";
                case DUPLICATION_WITHOUT_AGGREGATION:
                    return "Duplicated observations without aggregation";
                case UNKNOWN:
                    return "Unexpected error";
                default:
                    throw new RuntimeException();
            }
        }
    }
    //</editor-fold>

    public static final class Builder implements IBuilder<OptionalTsData> {

        @Nonnull
        public static String toString(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            return "(" + freq + "/" + aggregation + ")";
        }

        private final TsDataCollector dc;
        private final TsFrequency freq;
        private final TsAggregationType aggregation;
        private final boolean skipMissingValues;
        private int nbrUselessRows;

        public Builder(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            this(freq, aggregation, false);
        }

        public Builder(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation, boolean skipMissingValues) {
            this.dc = new TsDataCollector();
            this.freq = Objects.requireNonNull(freq);
            this.aggregation = Objects.requireNonNull(aggregation);
            this.skipMissingValues = skipMissingValues;
            this.nbrUselessRows = 0;
        }

        @Nonnull
        public Builder clear() {
            dc.clear();
            nbrUselessRows = 0;
            return this;
        }

        @Nonnull
        public Builder add(@Nullable Date period, @Nullable Number value) {
            if (period != null) {
                if (value != null) {
                    dc.addObservation(period, value.doubleValue());
                } else if (!skipMissingValues) {
                    dc.addMissingValue(period);
                }
            } else {
                nbrUselessRows++;
            }
            return this;
        }

        @Nonnull
        @Override
        public OptionalTsData build() {
            if (dc.getCount() == 0) {
                return onFailure(BuilderCause.NO_DATA);
            }
            if (!isValidAggregation(freq, aggregation)) {
                return onFailure(BuilderCause.INVALID_AGGREGATION);
            }

            TsData result;
            if (aggregation == TsAggregationType.None) {
                result = dc.make(freq, TsAggregationType.None);
            } else {
                result = dc.make(TsFrequency.Undefined, TsAggregationType.None);
                if (result != null && (result.getFrequency().intValue() % freq.intValue() == 0)) {
                    // should succeed
                    result = result.changeFrequency(freq, aggregation, true);
                } else {
                    result = dc.make(freq, aggregation);
                }
            }

            if (result == null) {
                switch (freq) {
                    case Undefined:
                        return dc.getCount() == 1
                                ? onFailure(BuilderCause.GUESS_SINGLE)
                                : onFailure(BuilderCause.GUESS_DUPLICATION);
                    default:
                        return aggregation == TsAggregationType.None
                                ? onFailure(BuilderCause.DUPLICATION_WITHOUT_AGGREGATION)
                                : onFailure(BuilderCause.UNKNOWN);
                }
            }
            return onSuccess(result);
        }

        private OptionalTsData onSuccess(@Nonnull TsData tsData) {
            return new Present(dc.getCount(), nbrUselessRows, tsData);
        }

        private OptionalTsData onFailure(@Nonnull BuilderCause cause) {
            return new BuilderAbsent(dc.getCount(), nbrUselessRows, cause);
        }

        private static boolean isValidAggregation(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            return freq != TsFrequency.Undefined || aggregation == TsAggregationType.None;
        }
    }
}

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
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable object that may contain a non-null reference to a TsData object.
 * Each instance of this type either contains a non-null reference, or contains
 * a message explaining why the reference is "absent"; it is never said to
 * "contain {@code null}".
 *
 * <p>
 * A non-null {@code OptionalTsData} reference can be used as a replacement for
 * a nullable {@code TsData} reference. It allows you to represent "a
 * {@code TsData} that must be present" and a "{@code TsData} that might be
 * absent" as two distinct types in your program, which can aid clarity.
 *
 * @author Philippe Charles
 * @see Optional
 */
@Immutable
public abstract class OptionalTsData {

    @Nonnull
    public static OptionalTsData present(int nbrRows, int nbrUselessRows, @Nonnull TsData data) {
        Objects.requireNonNull(data);
        Preconditions.checkArgument(nbrRows >= nbrUselessRows && nbrUselessRows >= 0);
        return new Present(nbrRows, nbrUselessRows, data);
    }

    @Nonnull
    public static OptionalTsData absent(int nbrRows, int nbrUselessRows, @Nonnull String cause) {
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
     * Returns {@code true} if this holder contains a (non-null) instance of
     * TsData.
     *
     * @return
     */
    abstract public boolean isPresent();

    /**
     * Returns the contained instance, which must be present. If the instance
     * might be absent, use {@link #or(TsData)} or {@link #orNull} instead.
     *
     * @return a non-null TsData
     * @throws IllegalStateException if the instance is absent
     * ({@link #isPresent} returns {@code false})
     */
    @Nonnull
    abstract public TsData get() throws IllegalStateException;

    /**
     * Returns the contained instance if it is present; {@code defaultValue}
     * otherwise. If no default value should be required because the instance is
     * known to be present, use {@link #get()} instead. For a default value of
     * {@code null}, use {@link #orNull}.
     *
     * @param defaultValue a non-null default value if the instance is absent
     * @return a non-null TsData
     */
    @Nonnull
    public TsData or(@Nonnull TsData defaultValue) {
        Objects.requireNonNull(defaultValue, "use orNull() instead of or(null)");
        return isPresent() ? get() : defaultValue;
    }

    /**
     * Returns the contained instance if it is present; {@code null} otherwise.
     * If the instance is known to be present, use {@link #get()} instead.
     *
     * @return a TsData if present, null otherwise
     */
    @Nullable
    abstract public TsData orNull();

    /**
     * Returns a message explaining why the reference is "absent".
     *
     * @return
     * @throws IllegalStateException if the instance id present
     */
    @Nonnull
    abstract public String getCause() throws IllegalStateException;

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class Present extends OptionalTsData {

        private final TsData data;

        private Present(int nbrRows, int nbrUselessRows, @Nonnull TsData data) {
            super(nbrRows, nbrUselessRows);
            this.data = data;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public TsData get() {
            return data;
        }

        @Override
        public TsData orNull() {
            return data;
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
            return this.data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }

    private static final class Absent extends OptionalTsData {

        private final String cause;

        private Absent(int nbrRows, int nbrUselessRows, @Nonnull String cause) {
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
    //</editor-fold>

    public static final class Builder implements IBuilder<OptionalTsData> {

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
                return onFailure("No data available");
            }
            if (freq == TsFrequency.Undefined && aggregation != TsAggregationType.None) {
                return onFailure("Invalid aggregation mode");
            }
            TsData result;
            if (aggregation == TsAggregationType.None) {
                result = dc.make(freq, aggregation);
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
                                ? onFailure("Cannot guess frequency with a single observation")
                                : onFailure("Cannot guess frequency with duplicated periods");
                    default:
                    // TODO: if TsAggregationType.None
                }
                return onFailure("Unexpected error");
            }
            return onSuccess(result);
        }

        private OptionalTsData onSuccess(TsData tsData) {
            return new Present(dc.getCount(), nbrUselessRows, tsData);
        }

        private OptionalTsData onFailure(String cause) {
            return new Absent(dc.getCount(), nbrUselessRows, cause);
        }

        @Nonnull
        public static String toString(@Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation) {
            return "(" + freq + "/" + aggregation + ")";
        }
    }
}

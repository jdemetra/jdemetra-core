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
package demetra.tsprovider;

import demetra.design.Immutable;
import demetra.design.NewObject;
import demetra.timeseries.simplets.TsData;
import java.util.Objects;
import java.util.Optional;
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
 every call of {@link  #get} in order to guarantee its immutability.
 *
 * @author Philippe Charles
 * @see Optional
 */
@Immutable
public abstract class OptionalTsData {

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

        private final TsData data;

        private Present(TsData data) {
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
            return this.data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public String toString() {
            return "Present: " + data;
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
    //</editor-fold>
}

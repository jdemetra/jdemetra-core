/*
 * Copyright 2015 National Bank of Belgium
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
package ec.tss.tsproviders.cursor;

import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.MetaData;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Defines a low-level time series cursor that may retrieve data and metadata.
 *
 * @author Philippe Charles
 * @param <ID> the type of the cursor identifiers
 * @since 2.2.0
 */
@NotThreadSafe
public interface TsCursor<ID> extends Closeable {

    /**
     * Moves to the next series.
     *
     * @return true if there is a next series, false otherwise
     * @throws IOException if an internal exception prevented data retrieval.
     */
    boolean nextSeries() throws IOException;

    /**
     * Gets the current time series identifier.
     *
     * @return a non-null identifier
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @Nonnull
    ID getId() throws IOException;

    /**
     * Gets the current time series metadata.
     *
     * @return non-null optional metadata
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @Nonnull
    default Optional<MetaData> getMetaData() throws IOException {
        return Optional.empty();
    }

    /**
     * Gets the current time series data.
     *
     * @return a non-null optional data
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @Nonnull
    default OptionalTsData getData() throws IOException {
        return TsCursors.NOT_REQUESTED;
    }

    @Override
    default void close() throws IOException {
        // do nothing by default
    }

    /**
     * Returns a cursor consisting of the results of applying the given function
     * to the identifiers of this cursor.
     *
     * @param <Z> the identifier type of the new cursor
     * @param function a non-null function to apply to each identifier
     * @return a new cursor
     */
    @Nonnull
    default <Z> TsCursor<Z> transform(@Nonnull Function<? super ID, ? extends Z> function) {
        return new TsCursors.TransformingCursor<>(this, function);
    }

    /**
     * Returns a cursor consisting of the elements of this cursor that match the
     * given predicate.
     *
     * @param predicate a non-null predicate to apply to each identifier
     * @return a new cursor
     */
    @Nonnull
    default TsCursor<ID> filter(@Nonnull Predicate<? super ID> predicate) {
        return new TsCursors.FilteringCursor<>(this, predicate);
    }

    /**
     * Creates an empty cursor.
     *
     * @param <T> the type of the cursor identifiers
     * @return a new cursor
     */
    @Nonnull
    static <T> TsCursor<T> noOp() {
        return TsCursors.NoOpCursor.INSTANCE;
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <T> the type of the cursor identifiers
     * @param id the identifier
     * @param data the optional data
     * @param metaData the optional metadata
     * @return a new cursor
     */
    @Nonnull
    static <T> TsCursor<T> singleton(
            @Nonnull T id,
            @Nonnull OptionalTsData data,
            @Nonnull Optional<MetaData> metaData) {
        return new TsCursors.SingletonCursor<>(id, data, metaData);
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <T> the type of the cursor identifiers
     * @param id the identifier
     * @param data the optional data
     * @return a new cursor
     */
    @Nonnull
    static <T> TsCursor<T> singleton(
            @Nonnull T id,
            @Nonnull OptionalTsData data) {
        return new TsCursors.SingletonCursor<>(id, data, Optional.empty());
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <T> the type of the cursor identifiers
     * @param id the identifier
     * @return a new cursor
     */
    @Nonnull
    static <T> TsCursor<T> singleton(
            @Nonnull T id) {
        return new TsCursors.SingletonCursor<>(id, TsCursors.NOT_REQUESTED, Optional.empty());
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <X> the type of the iterator elements
     * @param <Y> the type of the cursor identifiers
     * @param iterator the iterator
     * @param toId a non-null function to get identifier from a element from the
     * iterator
     * @param toData a non-null function to get optional data from a element
     * from the iterator
     * @param toMeta a non-null function to get optional metadata from a element
     * from the iterator
     * @return a new cursor
     */
    @Nonnull
    static <X, Y> TsCursor<Y> from(
            @Nonnull Iterator<X> iterator,
            @Nonnull Function<? super X, ? extends Y> toId,
            @Nonnull Function<? super X, OptionalTsData> toData,
            @Nonnull Function<? super X, Optional<MetaData>> toMeta) {
        return new TsCursors.IteratingCursor<>(iterator, toId, toData, toMeta);
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <X> the type of the iterator elements
     * @param <Y> the type of the cursor identifiers
     * @param iterator the iterator
     * @param toId a non-null function to get identifier from a element from the
     * iterator
     * @param toData a non-null function to get optional data from a element
     * from the iterator
     * @return a new cursor
     */
    @Nonnull
    static <X, Y> TsCursor<Y> from(
            @Nonnull Iterator<X> iterator,
            @Nonnull Function<? super X, ? extends Y> toId,
            @Nonnull Function<? super X, OptionalTsData> toData) {
        return new TsCursors.IteratingCursor<>(iterator, toId, toData, TsCursors.NO_META);
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <X> the type of the iterator elements
     * @param <Y> the type of the cursor identifiers
     * @param iterator the iterator
     * @param toId a non-null function to get identifier from a element from the
     * iterator
     * @return a new cursor
     */
    @Nonnull
    static <X, Y> TsCursor<Y> from(
            @Nonnull Iterator<X> iterator,
            @Nonnull Function<? super X, ? extends Y> toId) {
        return new TsCursors.IteratingCursor<>(iterator, toId, TsCursors.NO_DATA, TsCursors.NO_META);
    }
}

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

import ec.tss.tsproviders.utils.FunctionWithIO;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
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
     * Retrieves whether this cursor has been closed. A cursor is closed if the
     * method close has been called on it, or if it is automatically closed.
     *
     * @return true if this cursor is closed; false if it is still open
     * @throws IOException if an internal exception prevented state retrieval.
     */
    boolean isClosed() throws IOException;

    /**
     * Gets the current metadata of the time series collection.
     *
     * @return a non-null metadata
     * @throws IOException if an internal exception prevented data retrieval.
     * @throws IllegalStateException if this cursor is closed
     */
    @Nonnull
    Map<String, String> getMetaData() throws IOException, IllegalStateException;

    /**
     * Moves to the next series.
     *
     * @return true if there is a next series, false otherwise
     * @throws IOException if an internal exception prevented data retrieval.
     * @throws IllegalStateException if this cursor is closed
     */
    boolean nextSeries() throws IOException, IllegalStateException;

    /**
     * Gets the current time series identifier.
     *
     * @return a non-null identifier
     * @throws IOException if an internal exception prevented data retrieval.
     * @throws IllegalStateException if {@link #nextSeries()} returns false or
     * if this cursor is closed
     */
    @Nonnull
    ID getSeriesId() throws IOException, IllegalStateException;

    /**
     * Gets the current time series metadata.
     *
     * @return a non-null metadata
     * @throws IOException if an internal exception prevented data retrieval.
     * @throws IllegalStateException if {@link #nextSeries()} returns false or
     * if this cursor is closed
     */
    @Nonnull
    Map<String, String> getSeriesMetaData() throws IOException, IllegalStateException;

    /**
     * Gets the current time series data.
     *
     * @return a non-null optional data
     * @throws IOException if an internal exception prevented data retrieval.
     * @throws IllegalStateException if {@link #nextSeries()} returns false or
     * if this cursor is closed
     */
    @Nonnull
    OptionalTsData getSeriesData() throws IOException, IllegalStateException;

    /**
     * Returns a cursor consisting of the results of applying the given function
     * to the identifiers of this cursor.
     *
     * @param <Z> the identifier type of the new cursor
     * @param function a non-null function to apply to each identifier
     * @return a non-null cursor
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
     * @return a non-null cursor
     */
    @Nonnull
    default TsCursor<ID> filter(@Nonnull Predicate<? super ID> predicate) {
        return new TsCursors.FilteringCursor<>(this, predicate);
    }

    /**
     * Returns a cursor that uses the specified collection meta data.
     *
     * @param meta a non-null meta data
     * @return a non-null cursor
     */
    @Nonnull
    default TsCursor<ID> withMetaData(@Nonnull Map<String, String> meta) {
        return new TsCursors.WithMetaDataCursor<>(this, meta);
    }

    /**
     * Returns an cursor with an additional close handler.
     *
     * @param closeHandler a non-null task to execute if the stream is closed
     * @return a non-null cursor
     */
    @Nonnull
    default TsCursor<ID> onClose(@Nonnull Closeable closeHandler) {
        return new TsCursors.OnCloseCursor<>(this, closeHandler);
    }

    /**
     * Creates an empty cursor.
     *
     * @param <ID> the type of the cursor identifiers
     * @return a new cursor
     */
    @Nonnull
    static <ID> TsCursor<ID> empty() {
        return new TsCursors.EmptyCursor<>();
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <ID> the type of the cursor identifiers
     * @param id the identifier
     * @param data the optional data
     * @param meta the metadata
     * @return a new cursor
     */
    @Nonnull
    static <ID> TsCursor<ID> singleton(@Nonnull ID id, @Nonnull OptionalTsData data, @Nonnull Map<String, String> meta) {
        return new TsCursors.SingletonCursor<>(id, data, meta);
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <ID> the type of the cursor identifiers
     * @param id the identifier
     * @param data the optional data
     * @return a new cursor
     */
    @Nonnull
    static <ID> TsCursor<ID> singleton(@Nonnull ID id, @Nonnull OptionalTsData data) {
        return new TsCursors.SingletonCursor<>(id, data, Collections.emptyMap());
    }

    /**
     * Creates a cursor from a single item.
     *
     * @param <ID> the type of the cursor identifiers
     * @param id the identifier
     * @return a new cursor
     */
    @Nonnull
    static <ID> TsCursor<ID> singleton(@Nonnull ID id) {
        return new TsCursors.SingletonCursor<>(id, TsCursors.NOT_REQUESTED, Collections.emptyMap());
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <E> the type of the iterator elements
     * @param iterator the iterator
     * @param toData a non-null function to get optional data from a element
     * from the iterator
     * @param toMeta a non-null function to get optional metadata from a element
     * from the iterator
     * @return a new cursor
     */
    @Nonnull
    static <E> TsCursor<E> from(
            @Nonnull Iterator<E> iterator,
            @Nonnull Function<? super E, OptionalTsData> toData,
            @Nonnull Function<? super E, Map<String, String>> toMeta) {
        return new TsCursors.IteratingCursor<>(iterator, Function.identity(), toData, toMeta);
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <E> the type of the iterator elements
     * @param iterator the iterator
     * @param toData a non-null function to get optional data from a element
     * from the iterator
     * @return a new cursor
     */
    @Nonnull
    static <E> TsCursor<E> from(
            @Nonnull Iterator<E> iterator,
            @Nonnull Function<? super E, OptionalTsData> toData) {
        return new TsCursors.IteratingCursor<>(iterator, Function.identity(), toData, TsCursors.NO_META);
    }

    /**
     * Creates a cursor from an iterator.
     *
     * @param <E> the type of the iterator elements
     * @param iterator the iterator
     * @return a new cursor
     */
    @Nonnull
    static <E> TsCursor<E> from(@Nonnull Iterator<E> iterator) {
        return new TsCursors.IteratingCursor<>(iterator, Function.identity(), TsCursors.NO_DATA, TsCursors.NO_META);
    }

    @Nonnull
    static <KEY, ID> TsCursor<ID> withCache(
            @Nonnull ConcurrentMap<KEY, Object> cache,
            @Nonnull KEY key,
            @Nonnull FunctionWithIO<? super KEY, ? extends TsCursor<ID>> loader) throws IOException {
        return TsCursors.getOrLoad(cache, key, loader);
    }
}

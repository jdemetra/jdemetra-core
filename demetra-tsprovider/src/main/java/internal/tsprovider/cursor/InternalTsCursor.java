/*
 * Copyright 2016 National Bank of Belgium
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
package internal.tsprovider.cursor;

import demetra.tsprovider.OptionalTsData;
import demetra.io.Closeables;
import demetra.tsprovider.cursor.TsCursor;
import internal.util.AbstractIterator;
import ioutil.IO;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.Immutable;
import static java.util.Objects.requireNonNull;

/**
 * Package-private supporting class for {@link TsCursor}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class InternalTsCursor {

    public static final OptionalTsData NOT_REQUESTED = OptionalTsData.absent("Not requested");
    public static final Function<Object, Map<String, String>> NO_META = o -> Collections.emptyMap();
    public static final Function<Object, OptionalTsData> NO_DATA = o -> NOT_REQUESTED;

    static final String CLOSE_ISE = "This cursor is closed";
    static final String NEXT_ISE = "This cursor has no more series or has not been started";

    static final String ID_NPE = "id";
    static final String DATA_NPE = "data";
    static final String META_DATA_NPE = "meta data";
    static final String LABEL_NPE = "label";
    static final String DELEGATE_NPE = "delegate";
    static final String ID_FILTER_NPE = "id filter";
    static final String ID_TRANSFORMER_NPE = "id transformer";
    static final String CLOSE_HANDLER_NPE = "close handler";

    private static <X, Y> Y applyNotNull(String funcName, Function<X, Y> func, X input) throws RuntimeException {
        Y result = func.apply(input);
        if (result != null) {
            return result;
        }
        throw new RuntimeException("Invalid function '" + funcName + "': expected non-null result with parameter + '" + input + "'");
    }

    private static <X, Y> Y applyNotNullWithIO(String funcName, IO.Function<X, Y> func, X input) throws IOException, RuntimeException {
        Y result = func.applyWithIO(input);
        if (result != null) {
            return result;
        }
        throw new RuntimeException("Invalid function '" + funcName + "': expected non-null result with parameter + '" + input + "'");
    }

    @Immutable
    @lombok.AllArgsConstructor
    static final class CachedCollection<ID> {

        private final Map<String, String> meta;
        private final List<CachedSeries<ID>> items;

        public TsCursor<ID> toCursor() {
            return new IteratingCursor<>(items.iterator(), o -> o.id, o -> o.data, o -> o.meta, o -> o.label)
                    .withMetaData(meta);
        }
    }

    @Immutable
    @lombok.AllArgsConstructor
    private static final class CachedSeries<ID> {

        private final ID id;
        private final String label;
        private final Map<String, String> meta;
        private final OptionalTsData data;
    }

    @Nonnull
    public static <KEY, ID> TsCursor<ID> getOrLoad(
            @Nonnull ConcurrentMap<KEY, Object> cache,
            @Nonnull KEY key,
            @Nonnull IO.Function<? super KEY, ? extends TsCursor<ID>> loader) throws IOException {

        requireNonNull(cache, "cache");
        requireNonNull(key, "key");
        requireNonNull(loader, "loader");

        Object result = cache.get(key);
        return result instanceof CachedCollection
                ? ((CachedCollection<ID>) result).toCursor()
                : new CachingCursor<>(loader.applyWithIO(key), key, cache);
    }

    //<editor-fold defaultstate="collapsed" desc="Forwarding cursors">
    private static class ForwardingCursor<ID> implements TsCursor<ID> {

        protected final TsCursor<ID> delegate;

        private ForwardingCursor(TsCursor<ID> delegate) {
            this.delegate = requireNonNull(delegate, DELEGATE_NPE);
        }

        @Override
        public boolean isClosed() throws IOException {
            return delegate.isClosed();
        }

        @Override
        public Map<String, String> getMetaData() throws IOException {
            return delegate.getMetaData();
        }

        @Override
        public boolean nextSeries() throws IOException {
            return delegate.nextSeries();
        }

        @Override
        public ID getSeriesId() throws IOException {
            return delegate.getSeriesId();
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            return delegate.getSeriesLabel();
        }

        @Override
        public Map<String, String> getSeriesMetaData() throws IOException {
            return delegate.getSeriesMetaData();
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            return delegate.getSeriesData();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    public static final class MappingCursor<ID, Z> extends ForwardingCursor<Z> {

        private final IO.Function<? super ID, ? extends Z> function;

        public MappingCursor(
                @Nonnull TsCursor<ID> delegate,
                @Nonnull IO.Function<? super ID, ? extends Z> function) {
            super((TsCursor<Z>) delegate);
            this.function = requireNonNull(function, ID_TRANSFORMER_NPE);
        }

        @Override
        public Z getSeriesId() throws IOException {
            ID id = ((TsCursor<ID>) delegate).getSeriesId();
            return applyNotNullWithIO("id", function, id);
        }
    }

    public static final class FilteringCursor<ID> extends ForwardingCursor<ID> {

        private final IO.Predicate<? super ID> filter;

        public FilteringCursor(
                @Nonnull TsCursor<ID> delegate,
                @Nonnull IO.Predicate<? super ID> filter) {
            super(delegate);
            this.filter = requireNonNull(filter, ID_FILTER_NPE);
        }

        @Override
        public boolean nextSeries() throws IOException {
            while (delegate.nextSeries()) {
                if (filter.testWithIO(delegate.getSeriesId())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class WithMetaDataCursor<ID> extends ForwardingCursor<ID> {

        private final Map<String, String> meta;

        public WithMetaDataCursor(@Nonnull TsCursor<ID> delegate, @Nonnull Map<String, String> meta) {
            super(delegate);
            this.meta = requireNonNull(meta, META_DATA_NPE);
        }

        @Override
        public Map<String, String> getMetaData() throws IOException {
            return meta;
        }
    }

    public static final class OnCloseCursor<ID> extends ForwardingCursor<ID> {

        private final Closeable closeHandler;

        public OnCloseCursor(@Nonnull TsCursor<ID> delegate, @Nonnull Closeable closeHandler) {
            super(delegate);
            this.closeHandler = requireNonNull(closeHandler, CLOSE_HANDLER_NPE);
        }

        @Override
        public void close() throws IOException {
            Closeables.closeBoth(delegate, closeHandler);
        }
    }

    public static final class CachingCursor<KEY, ID> extends ForwardingCursor<ID> {

        private final KEY key;
        private final ConcurrentMap<KEY, Object> cache;
        private final List<CachedSeries<ID>> items;
        private CachedSeries<ID> current;

        public CachingCursor(TsCursor<ID> delegate, KEY key, ConcurrentMap<KEY, Object> cache) {
            super(delegate);
            this.key = key;
            this.cache = cache;
            this.items = new ArrayList<>();
            this.current = null;
        }

        private void checkState() throws IllegalStateException, IOException {
            if (isClosed()) {
                throw new IllegalStateException(CLOSE_ISE);
            }
            if (current == null) {
                throw new IllegalStateException(NEXT_ISE);
            }
        }

        @Override
        public Map<String, String> getMetaData() throws IOException {
            return delegate.getMetaData();
        }

        @Override
        public boolean nextSeries() throws IOException {
            if (delegate.nextSeries()) {
                current = fetchCurrentSeries();
                return true;
            }
            current = null;
            return false;
        }

        @Override
        public ID getSeriesId() throws IOException {
            checkState();
            return current.id;
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            checkState();
            return current.label;
        }

        @Override
        public Map<String, String> getSeriesMetaData() throws IOException {
            checkState();
            return current.meta;
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            checkState();
            return current.data;
        }

        @Override
        public void close() throws IOException {
            Closeables.closeBoth(this::flushToCache, delegate::close);
        }

        private void flushToCache() throws IOException {
            if (!isClosed()) {
                while (delegate.nextSeries()) {
                    fetchCurrentSeries();
                }
                cache.put(key, new CachedCollection<>(delegate.getMetaData(), items));
            }
        }

        private CachedSeries<ID> fetchCurrentSeries() throws IOException {
            CachedSeries<ID> item = new CachedSeries<>(delegate.getSeriesId(), delegate.getSeriesLabel(), delegate.getSeriesMetaData(), delegate.getSeriesData());
            items.add(item);
            return item;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="In-memory cursors">
    static abstract class InMemoryCursor<ID> implements TsCursor<ID> {

        private boolean closed = false;
        private Map<String, String> meta = Collections.emptyMap();
        private Closeable closeable = null;

        protected void checkClosedState() throws IllegalStateException {
            if (closed) {
                throw new IllegalStateException(CLOSE_ISE);
            }
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        final public Map<String, String> getMetaData() {
            checkClosedState();
            return meta;
        }

        @Override
        abstract public boolean nextSeries();

        @Override
        public abstract String getSeriesLabel();

        @Override
        abstract public Map<String, String> getSeriesMetaData();

        @Override
        abstract public OptionalTsData getSeriesData();

        @Override
        final public void close() throws IOException {
            closed = true;
            if (closeable != null) {
                closeable.close();
            }
        }

        @Override
        final public InMemoryCursor<ID> withMetaData(Map<String, String> meta) {
            requireNonNull(meta, META_DATA_NPE);
            this.meta = meta;
            return this;
        }

        @Override
        final public InMemoryCursor<ID> onClose(Closeable closeHandler) {
            requireNonNull(closeHandler, CLOSE_HANDLER_NPE);
            this.closeable = this.closeable == null ? closeHandler : compose(closeHandler);
            return this;
        }

        private Closeable compose(Closeable closeHandler) {
            Closeable first = this.closeable;
            return () -> Closeables.closeBoth(first, closeHandler);
        }
    }

    public static final class EmptyCursor<ID> extends InMemoryCursor<ID> {

        @Override
        public boolean nextSeries() {
            checkClosedState();
            return false;
        }

        @Override
        public ID getSeriesId() {
            checkClosedState();
            throw new IllegalStateException(NEXT_ISE);
        }

        @Override
        public String getSeriesLabel() {
            checkClosedState();
            throw new IllegalStateException(NEXT_ISE);
        }

        @Override
        public Map<String, String> getSeriesMetaData() {
            checkClosedState();
            throw new IllegalStateException(NEXT_ISE);
        }

        @Override
        public OptionalTsData getSeriesData() {
            checkClosedState();
            throw new IllegalStateException(NEXT_ISE);
        }

        @Override
        public EmptyCursor<ID> filter(IO.Predicate<? super ID> predicate) {
            requireNonNull(predicate, ID_FILTER_NPE);
            return this;
        }

        @Override
        public <Z> EmptyCursor<Z> map(IO.Function<? super ID, ? extends Z> function) {
            requireNonNull(function, ID_TRANSFORMER_NPE);
            return (EmptyCursor<Z>) this;
        }
    }

    public static final class SingletonCursor<ID> extends IteratingCursor<ID, ID> {

        public SingletonCursor(
                @Nonnull ID id,
                @Nonnull OptionalTsData data,
                @Nonnull Map<String, String> meta,
                @Nonnull String label) {
            super(Collections.singleton(id).iterator(), IO.Function.identity(), o -> data, o -> meta, o -> label);
            Objects.requireNonNull(data);
            Objects.requireNonNull(meta);
            Objects.requireNonNull(label);
        }
    }

    public static class IteratingCursor<E, ID> extends InMemoryCursor<ID> {

        private Iterator<E> iterator;
        private IO.Function<? super E, ? extends ID> toId;
        private final Function<? super E, OptionalTsData> toData;
        private final Function<? super E, Map<String, String>> toMeta;
        private final Function<? super E, String> toLabel;
        private E current;

        public IteratingCursor(
                @Nonnull Iterator<E> iterator,
                @Nonnull IO.Function<? super E, ? extends ID> toId,
                @Nonnull Function<? super E, OptionalTsData> toData,
                @Nonnull Function<? super E, Map<String, String>> toMeta,
                @Nonnull Function<? super E, String> toLabel) {
            this.iterator = requireNonNull(iterator, "iterator");
            this.toId = requireNonNull(toId, "id extractor");
            this.toData = requireNonNull(toData, "data extractor");
            this.toMeta = requireNonNull(toMeta, "meta extractor");
            this.toLabel = requireNonNull(toLabel, "label extractor");
        }

        private void checkSeriesState() throws IllegalStateException {
            if (current == null) {
                throw new IllegalStateException(NEXT_ISE);
            }
        }

        @Override
        public boolean nextSeries() {
            checkClosedState();
            current = iterator.hasNext() ? iterator.next() : null;
            return current != null;
        }

        @Override
        public ID getSeriesId() throws IOException {
            checkClosedState();
            checkSeriesState();
            return applyNotNullWithIO("id", toId, current);
        }

        @Override
        public String getSeriesLabel() {
            checkClosedState();
            checkSeriesState();
            return applyNotNull("label", toLabel, current);
        }

        @Override
        public Map<String, String> getSeriesMetaData() {
            checkClosedState();
            checkSeriesState();
            return applyNotNull("meta", toMeta, current);
        }

        @Override
        public OptionalTsData getSeriesData() {
            checkClosedState();
            checkSeriesState();
            return applyNotNull("data", toData, current);
        }

        @Override
        public IteratingCursor<E, ID> filter(IO.Predicate<? super ID> predicate) {
            iterator = compose(iterator, toId, requireNonNull(predicate, ID_FILTER_NPE));
            return this;
        }

        @Override
        public <Z> IteratingCursor<E, Z> map(IO.Function<? super ID, ? extends Z> function) {
            IteratingCursor<E, Z> result = (IteratingCursor<E, Z>) this;
            result.toId = toId.andThen(requireNonNull(function, ID_TRANSFORMER_NPE));
            return result;
        }
    }

    private static <E, ID> Iterator<E> compose(Iterator<E> iterator, IO.Function<? super E, ? extends ID> toId, IO.Predicate<? super ID> predicate) {
        return filter(iterator, IO.Predicate.unchecked(o -> predicate.testWithIO(applyNotNullWithIO("id", toId, o))));
    }

    private static <E> Iterator<E> filter(Iterator<E> iterator, Predicate<? super E> predicate) {
        return new AbstractIterator<E>() {
            private E current = null;

            @Override
            protected E get() {
                return current;
            }

            @Override
            protected boolean moveNext() {
                while (iterator.hasNext()) {
                    current = iterator.next();
                    if (predicate.test(current)) {
                        return true;
                    }
                }
                current = null;
                return false;
            }
        };
    }
    //</editor-fold>
}

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
package ec.tss.tsproviders.cursor;

import com.google.common.collect.Iterators;
import ec.tss.tsproviders.utils.FunctionWithIO;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.utilities.Closeables;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.Immutable;

/**
 * Package-private supporting class for {@link TsCursor}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class TsCursors {

    private TsCursors() {
        // static class
    }

    static final OptionalTsData NOT_REQUESTED = OptionalTsData.absent("Not requested");
    static final Function<Object, Map<String, String>> NO_META = o -> Collections.emptyMap();
    static final Function<Object, OptionalTsData> NO_DATA = o -> NOT_REQUESTED;

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

    @Immutable
    static final class XCollection<ID> implements Iterable<XSeries<ID>> {

        private final Map<String, String> meta;
        private final List<XSeries<ID>> items;

        private XCollection(Map<String, String> meta, List<XSeries<ID>> items) {
            this.meta = Collections.unmodifiableMap(meta);
            this.items = Collections.unmodifiableList(items);
        }

        public Map<String, String> getMetaData() {
            return meta;
        }

        @Override
        public Iterator<XSeries<ID>> iterator() {
            return items.iterator();
        }

        public TsCursor<ID> toCursor() {
            return new IteratingCursor<>(iterator(), XSeries<ID>::getId, XSeries::getData, XSeries::getMetadata, XSeries::getLabel)
                    .withMetaData(meta);
        }
    }

    @Immutable
    static final class XSeries<ID> {

        private final ID id;
        private final String label;
        private final Map<String, String> metadata;
        private final OptionalTsData data;

        private XSeries(ID id, String label, Map<String, String> metadata, OptionalTsData data) {
            this.id = id;
            this.label = label;
            this.metadata = metadata;
            this.data = data;
        }

        public ID getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public OptionalTsData getData() {
            return data;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }

    @Nonnull
    static <KEY, ID> TsCursor<ID> getOrLoad(
            @Nonnull ConcurrentMap<KEY, Object> cache,
            @Nonnull KEY key,
            @Nonnull FunctionWithIO<? super KEY, ? extends TsCursor<ID>> loader) throws IOException {

        requireNonNull(cache, "cache");
        requireNonNull(key, "key");
        requireNonNull(loader, "loader");

        Object result = cache.get(key);
        return result instanceof XCollection
                ? ((XCollection<ID>) result).toCursor()
                : new CachingCursor<>(loader.apply(key), key, cache);
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

    static final class TransformingCursor<ID, Z> extends ForwardingCursor<Z> {

        private final Function<? super ID, ? extends Z> function;

        TransformingCursor(
                @Nonnull TsCursor<ID> delegate,
                @Nonnull Function<? super ID, ? extends Z> function) {
            super((TsCursor<Z>) delegate);
            this.function = requireNonNull(function, ID_TRANSFORMER_NPE);
        }

        @Override
        public Z getSeriesId() throws IOException {
            ID id = ((TsCursor<ID>) delegate).getSeriesId();
            return applyNotNull("id", function, id);
        }
    }

    static final class FilteringCursor<ID> extends ForwardingCursor<ID> {

        private final Predicate<? super ID> filter;

        FilteringCursor(
                @Nonnull TsCursor<ID> delegate,
                @Nonnull Predicate<? super ID> filter) {
            super(delegate);
            this.filter = requireNonNull(filter, ID_FILTER_NPE);
        }

        @Override
        public boolean nextSeries() throws IOException {
            while (delegate.nextSeries()) {
                if (filter.test(delegate.getSeriesId())) {
                    return true;
                }
            }
            return false;
        }
    }

    static final class WithMetaDataCursor<ID> extends ForwardingCursor<ID> {

        private final Map<String, String> meta;

        WithMetaDataCursor(@Nonnull TsCursor<ID> delegate, @Nonnull Map<String, String> meta) {
            super(delegate);
            this.meta = requireNonNull(meta, META_DATA_NPE);
        }

        @Override
        public Map<String, String> getMetaData() throws IOException {
            return meta;
        }
    }

    static final class OnCloseCursor<ID> extends ForwardingCursor<ID> {

        private final Closeable closeHandler;

        OnCloseCursor(@Nonnull TsCursor<ID> delegate, @Nonnull Closeable closeHandler) {
            super(delegate);
            this.closeHandler = requireNonNull(closeHandler, CLOSE_HANDLER_NPE);
        }

        @Override
        public void close() throws IOException {
            Closeables.closeBoth(delegate, closeHandler);
        }
    }

    static final class CachingCursor<KEY, ID> extends ForwardingCursor<ID> {

        private final KEY key;
        private final ConcurrentMap<KEY, Object> cache;
        private final List<XSeries<ID>> items;
        private XSeries<ID> currentItem;

        CachingCursor(TsCursor<ID> delegate, KEY key, ConcurrentMap<KEY, Object> cache) {
            super(delegate);
            this.key = key;
            this.cache = cache;
            this.items = new ArrayList<>();
            this.currentItem = null;
        }

        private void checkState() throws IllegalStateException, IOException {
            if (isClosed()) {
                throw new IllegalStateException(CLOSE_ISE);
            }
            if (currentItem == null) {
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
                currentItem = fetchCurrentSeries();
                return true;
            }
            currentItem = null;
            return false;
        }

        @Override
        public ID getSeriesId() throws IOException {
            checkState();
            return currentItem.getId();
        }

        @Override
        public String getSeriesLabel() throws IOException, IllegalStateException {
            checkState();
            return currentItem.getLabel();
        }

        @Override
        public Map<String, String> getSeriesMetaData() throws IOException {
            checkState();
            return currentItem.getMetadata();
        }

        @Override
        public OptionalTsData getSeriesData() throws IOException {
            checkState();
            return currentItem.getData();
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
                cache.put(key, new XCollection<>(delegate.getMetaData(), items));
            }
        }

        private XSeries<ID> fetchCurrentSeries() throws IOException {
            XSeries<ID> item = new XSeries<>(delegate.getSeriesId(), delegate.getSeriesLabel(), delegate.getSeriesMetaData(), delegate.getSeriesData());
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
        abstract public ID getSeriesId();

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

    static final class EmptyCursor<ID> extends InMemoryCursor<ID> {

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
        public EmptyCursor<ID> filter(Predicate<? super ID> predicate) {
            requireNonNull(predicate, ID_FILTER_NPE);
            return this;
        }

        @Override
        public <Z> EmptyCursor<Z> transform(Function<? super ID, ? extends Z> function) {
            requireNonNull(function, ID_TRANSFORMER_NPE);
            return (EmptyCursor<Z>) this;
        }
    }

    static final class SingletonCursor<ID> extends InMemoryCursor<ID> {

        private ID id;
        private final OptionalTsData data;
        private final Map<String, String> meta;
        private final String label;
        private Boolean first;

        SingletonCursor(
                @Nonnull ID id,
                @Nonnull OptionalTsData data,
                @Nonnull Map<String, String> meta,
                @Nonnull String label) {
            this.id = requireNonNull(id, ID_NPE);
            this.data = requireNonNull(data, DATA_NPE);
            this.meta = requireNonNull(meta, META_DATA_NPE);
            this.label = requireNonNull(label, LABEL_NPE);
            this.first = true;
        }

        private void checkSeriesState() throws IllegalStateException {
            if (first == null || first == true) {
                throw new IllegalStateException(NEXT_ISE);
            }
        }

        @Override
        public boolean nextSeries() {
            checkClosedState();
            if (Boolean.TRUE.equals(first)) {
                first = false;
                return true;
            } else if (Boolean.FALSE.equals(first)) {
                first = null;
                return false;
            }
            return false;
        }

        @Override
        public ID getSeriesId() {
            checkClosedState();
            checkSeriesState();
            return id;
        }

        @Override
        public String getSeriesLabel() {
            checkClosedState();
            checkSeriesState();
            return label;
        }

        @Override
        public Map<String, String> getSeriesMetaData() {
            checkClosedState();
            checkSeriesState();
            return meta;
        }

        @Override
        public OptionalTsData getSeriesData() {
            checkClosedState();
            checkSeriesState();
            return data;
        }

        @Override
        public SingletonCursor<ID> filter(Predicate<? super ID> predicate) {
            requireNonNull(predicate, ID_FILTER_NPE);
            first = first != null && first && predicate.test(id);
            return this;
        }

        @Override
        public <Z> SingletonCursor<Z> transform(Function<? super ID, ? extends Z> function) {
            requireNonNull(function, ID_TRANSFORMER_NPE);
            SingletonCursor<Z> result = (SingletonCursor<Z>) this;
            result.id = applyNotNull("id", function, id);
            return result;
        }
    }

    static final class IteratingCursor<E, ID> extends InMemoryCursor<ID> {

        private Iterator<E> iterator;
        private Function<? super E, ? extends ID> toId;
        private final Function<? super E, OptionalTsData> toData;
        private final Function<? super E, Map<String, String>> toMeta;
        private final Function<? super E, String> toLabel;
        private E current;

        IteratingCursor(
                @Nonnull Iterator<E> iterator,
                @Nonnull Function<? super E, ? extends ID> toId,
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
        public ID getSeriesId() {
            checkClosedState();
            checkSeriesState();
            return applyNotNull("id", toId, current);
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
        public IteratingCursor<E, ID> filter(Predicate<? super ID> predicate) {
            iterator = compose(iterator, toId, requireNonNull(predicate, ID_FILTER_NPE));
            return this;
        }

        @Override
        public <Z> IteratingCursor<E, Z> transform(Function<? super ID, ? extends Z> function) {
            IteratingCursor<E, Z> result = (IteratingCursor<E, Z>) this;
            result.toId = toId.andThen(requireNonNull(function, ID_TRANSFORMER_NPE));
            return result;
        }
    }

    private static <E, ID> Iterator<E> compose(Iterator<E> iterator, Function<? super E, ? extends ID> toId, Predicate<? super ID> predicate) {
        return Iterators.filter(iterator, o -> predicate.test(applyNotNull("id", toId, o)));
    }
    //</editor-fold>
}

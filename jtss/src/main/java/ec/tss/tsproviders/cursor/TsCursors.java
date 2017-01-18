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
import ec.tss.tsproviders.utils.OptionalTsData;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import static java.util.Objects.requireNonNull;

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

    private static <X, Y> Y applyNotNull(String funcName, Function<X, Y> func, X input) throws RuntimeException {
        Y result = func.apply(input);
        if (result != null) {
            return result;
        }
        throw new RuntimeException("Invalid function '" + funcName + "': expected non-null result with parameter + '" + input + "'");
    }

    //<editor-fold defaultstate="collapsed" desc="Forwarding cursors">
    private abstract static class ForwardingCursor<ID> implements TsCursor<ID> {

        protected final TsCursor<ID> delegate;

        private ForwardingCursor(TsCursor<ID> delegate) {
            this.delegate = requireNonNull(delegate);
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
            this.function = requireNonNull(function);
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
            this.filter = requireNonNull(filter);
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
            this.meta = requireNonNull(meta);
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
            this.closeHandler = requireNonNull(closeHandler);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } catch (IOException first) {
                try {
                    closeHandler.close();
                } catch (IOException second) {
                    first.addSuppressed(second);
                }
                throw (first);
            }
            closeHandler.close();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="In-memory cursors">
    static abstract class InMemoryCursor<ID> implements TsCursor<ID> {

        @Override
        final public Map<String, String> getMetaData() {
            return Collections.emptyMap();
        }

        @Override
        abstract public boolean nextSeries();

        @Override
        abstract public ID getSeriesId();

        @Override
        abstract public Map<String, String> getSeriesMetaData();

        @Override
        abstract public OptionalTsData getSeriesData();

        @Override
        final public void close() {
            // do nothing
        }

        @Override
        final public TsCursor<ID> withMetaData(Map<String, String> meta) {
            return meta.isEmpty() ? this : TsCursor.super.withMetaData(meta);
        }
    }

    static final class EmptyCursor extends InMemoryCursor {

        static final EmptyCursor INSTANCE = new EmptyCursor();

        @Override
        public boolean nextSeries() {
            return false;
        }

        @Override
        public Object getSeriesId() {
            throw new IllegalStateException();
        }

        @Override
        public Map getSeriesMetaData() {
            throw new IllegalStateException();
        }

        @Override
        public OptionalTsData getSeriesData() {
            throw new IllegalStateException();
        }

        @Override
        public EmptyCursor filter(Predicate predicate) {
            requireNonNull(predicate);
            return this;
        }

        @Override
        public EmptyCursor transform(Function function) {
            requireNonNull(function);
            return this;
        }
    }

    static final class SingletonCursor<ID> extends InMemoryCursor<ID> {

        private ID id;
        private final OptionalTsData data;
        private final Map<String, String> meta;
        private boolean first;

        SingletonCursor(
                @Nonnull ID id,
                @Nonnull OptionalTsData data,
                @Nonnull Map<String, String> meta) {
            this.id = requireNonNull(id);
            this.data = requireNonNull(data);
            this.meta = requireNonNull(meta);
            this.first = true;
        }

        @Override
        public boolean nextSeries() {
            if (first) {
                first = false;
                return true;
            }
            return false;
        }

        @Override
        public ID getSeriesId() {
            return id;
        }

        @Override
        public Map<String, String> getSeriesMetaData() {
            return meta;
        }

        @Override
        public OptionalTsData getSeriesData() {
            return data;
        }

        @Override
        public SingletonCursor<ID> filter(Predicate<? super ID> predicate) {
            requireNonNull(predicate);
            first = first && predicate.test(id);
            return this;
        }

        @Override
        public <Z> SingletonCursor<Z> transform(Function<? super ID, ? extends Z> function) {
            requireNonNull(function);
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
        private E current;

        IteratingCursor(
                @Nonnull Iterator<E> iterator,
                @Nonnull Function<? super E, ? extends ID> toId,
                @Nonnull Function<? super E, OptionalTsData> toData,
                @Nonnull Function<? super E, Map<String, String>> toMeta) {
            this.iterator = requireNonNull(iterator);
            this.toId = requireNonNull(toId);
            this.toData = requireNonNull(toData);
            this.toMeta = requireNonNull(toMeta);
        }

        @Override
        public boolean nextSeries() {
            current = iterator.hasNext() ? iterator.next() : null;
            return current != null;
        }

        @Override
        public ID getSeriesId() {
            return applyNotNull("id", toId, current);
        }

        @Override
        public Map<String, String> getSeriesMetaData() {
            return applyNotNull("meta", toMeta, current);
        }

        @Override
        public OptionalTsData getSeriesData() {
            return applyNotNull("data", toData, current);
        }

        @Override
        public IteratingCursor<E, ID> filter(Predicate<? super ID> predicate) {
            iterator = compose(iterator, toId, requireNonNull(predicate));
            return this;
        }

        @Override
        public <Z> IteratingCursor<E, Z> transform(Function<? super ID, ? extends Z> function) {
            IteratingCursor<E, Z> result = (IteratingCursor<E, Z>) this;
            result.toId = toId.andThen(requireNonNull(function));
            return result;
        }
    }

    private static <E, ID> Iterator<E> compose(Iterator<E> iterator, Function<? super E, ? extends ID> toId, Predicate<? super ID> predicate) {
        return Iterators.filter(iterator, o -> predicate.test(applyNotNull("id", toId, o)));
    }
    //</editor-fold>
}

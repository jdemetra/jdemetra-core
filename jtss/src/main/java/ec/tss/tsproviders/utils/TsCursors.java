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
package ec.tss.tsproviders.utils;

import ec.tstoolkit.MetaData;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
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
    static final Function<Object, Optional<MetaData>> NO_META = o -> Optional.empty();
    static final Function<Object, OptionalTsData> NO_DATA = o -> NOT_REQUESTED;

    static final class NoOpCursor implements TsCursor {

        static final NoOpCursor INSTANCE = new NoOpCursor();

        @Override
        public boolean nextSeries() throws IOException {
            return false;
        }

        @Override
        public Object getId() throws IOException {
            throw new IllegalStateException();
        }
    }

    static final class SingletonCursor<T> implements TsCursor<T> {

        private final T id;
        private final OptionalTsData data;
        private final Optional<MetaData> metaData;
        private boolean first;

        SingletonCursor(
                @Nonnull T id,
                @Nonnull OptionalTsData data,
                @Nonnull Optional<MetaData> metaData) {
            this.id = requireNonNull(id);
            this.data = requireNonNull(data);
            this.metaData = requireNonNull(metaData);
            this.first = true;
        }

        @Override
        public boolean nextSeries() throws IOException {
            if (first) {
                first = false;
                return true;
            }
            return false;
        }

        @Override
        public T getId() throws IOException {
            return id;
        }

        @Override
        public Optional<MetaData> getMetaData() throws IOException {
            return metaData;
        }

        @Override
        public OptionalTsData getData() throws IOException {
            return data;
        }
    }

    static final class TransformingCursor<X, Y> implements TsCursor<Y> {

        private final TsCursor<X> delegate;
        private final Function<? super X, ? extends Y> toId;

        TransformingCursor(
                @Nonnull TsCursor<X> delegate,
                @Nonnull Function<? super X, ? extends Y> toId) {
            this.delegate = requireNonNull(delegate);
            this.toId = requireNonNull(toId);
        }

        @Override
        public boolean nextSeries() throws IOException {
            return delegate.nextSeries();
        }

        @Override
        public Y getId() throws IOException {
            return toId.apply(delegate.getId());
        }

        @Override
        public Optional<MetaData> getMetaData() throws IOException {
            return delegate.getMetaData();
        }

        @Override
        public OptionalTsData getData() throws IOException {
            return delegate.getData();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    static final class FilteringCursor<T> implements TsCursor<T> {

        private final TsCursor<T> delegate;
        private final Predicate<? super T> filter;

        FilteringCursor(
                @Nonnull TsCursor<T> delegate,
                @Nonnull Predicate<? super T> filter) {
            this.delegate = requireNonNull(delegate);
            this.filter = requireNonNull(filter);
        }

        @Override
        public boolean nextSeries() throws IOException {
            while (delegate.nextSeries()) {
                if (filter.test(delegate.getId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public T getId() throws IOException {
            return delegate.getId();
        }

        @Override
        public Optional<MetaData> getMetaData() throws IOException {
            return delegate.getMetaData();
        }

        @Override
        public OptionalTsData getData() throws IOException {
            return delegate.getData();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    static final class IteratingCursor<X, Y> implements TsCursor<Y> {

        private final Iterator<X> delegate;
        private final Function<? super X, ? extends Y> toId;
        private final Function<? super X, OptionalTsData> toData;
        private final Function<? super X, Optional<MetaData>> toMeta;
        private X current;

        IteratingCursor(
                @Nonnull Iterator<X> delegate,
                @Nonnull Function<? super X, ? extends Y> toId,
                @Nonnull Function<? super X, OptionalTsData> toData,
                @Nonnull Function<? super X, Optional<MetaData>> toMeta) {
            this.delegate = requireNonNull(delegate);
            this.toId = requireNonNull(toId);
            this.toData = requireNonNull(toData);
            this.toMeta = requireNonNull(toMeta);
        }

        @Override
        public boolean nextSeries() {
            current = delegate.hasNext() ? delegate.next() : null;
            return current != null;
        }

        @Override
        public Y getId() {
            return toId.apply(current);
        }

        @Override
        public Optional<MetaData> getMetaData() {
            return toMeta.apply(current);
        }

        @Override
        public OptionalTsData getData() {
            return toData.apply(current);
        }
    }
}

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
package ec.tss.tsproviders.cube;

import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.cursor.TsCursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Package private supporting class for {@link CubeAccessor}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class CubeAccessors {

    private CubeAccessors() {
        // static class
    }

    private static class ForwardingAccessor implements CubeAccessor {

        private final CubeAccessor delegate;

        ForwardingAccessor(CubeAccessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public IOException testConnection() {
            return delegate.testConnection();
        }

        @Override
        public CubeId getRoot() {
            return delegate.getRoot();
        }

        @Override
        public TsCursor<CubeId> getAllSeries(CubeId id) throws IOException {
            return delegate.getAllSeries(id);
        }

        @Override
        public TsCursor<CubeId> getAllSeriesWithData(CubeId id) throws IOException {
            return delegate.getAllSeriesWithData(id);
        }

        @Override
        public TsCursor<CubeId> getSeriesWithData(CubeId id) throws IOException {
            return delegate.getSeriesWithData(id);
        }

        @Override
        public TsCursor<CubeId> getChildren(CubeId id) throws IOException {
            return delegate.getChildren(id);
        }

        @Override
        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public String getDisplayName(CubeId id) {
            return delegate.getDisplayName(id);
        }

        @Override
        public String getDisplayNodeName(CubeId id) {
            return delegate.getDisplayNodeName(id);
        }
    }

    @ThreadSafe
    static final class BulkCubeAccessor extends ForwardingAccessor {

        private final ConcurrentMap<CubeId, Object> cache;
        private final int cacheLevel;
        private final int depth;

        BulkCubeAccessor(@Nonnull CubeAccessor delegate, @Nonnegative int depth, @Nonnull ConcurrentMap<CubeId, Object> cache) {
            super(delegate);
            this.cacheLevel = Math.max(0, delegate.getRoot().getMaxLevel() - depth);
            this.cache = cache;
            this.depth = depth;
        }

        private boolean isCacheEnabled() {
            return depth > 0;
        }

        @Nullable
        private CubeId getAncestorForCache(@Nonnull CubeId ref) {
            if (cacheLevel < ref.getLevel()) {
                String[] tmp = new String[cacheLevel];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = ref.getDimensionValue(i);
                }
                return getRoot().child(tmp);
            }
            return null;
        }

        @Override
        public TsCursor<CubeId> getAllSeriesWithData(CubeId ref) throws IOException {
            if (isCacheEnabled() && !ref.isSeries()) {
                if (ref.getLevel() == cacheLevel) {
                    List<TsItem> value = (List<TsItem>) cache.get(ref);
                    return value != null
                            ? TsCursor.from(value.iterator(), TsItem::getData, TsItem::getMetadata).transform(TsItem::getId)
                            : new CacheLoadingCursor(super.getAllSeriesWithData(ref), ref, cache);
                } else {
                    CubeId ancestor = getAncestorForCache(ref);
                    if (ancestor != null) {
                        return getAllSeriesWithData(ancestor).filter(ref::isAncestorOf);
                    }
                }
            }
            return super.getAllSeriesWithData(ref);
        }

        @Override
        public TsCursor<CubeId> getSeriesWithData(CubeId ref) throws IOException {
            if (isCacheEnabled() && ref.isSeries()) {
                CubeId ancestor = getAncestorForCache(ref);
                if (ancestor != null) {
                    return getAllSeriesWithData(ancestor).filter(ref::equals);
                }
            }
            return super.getSeriesWithData(ref);
        }

        //<editor-fold defaultstate="collapsed" desc="Implementation details">
        private static final class CacheLoadingCursor implements TsCursor<CubeId> {

            private final TsCursor<CubeId> delegate;
            private final CubeId ref;
            private final ConcurrentMap<CubeId, Object> cache;
            private final List<TsItem> items;
            private TsItem currentItem;

            private CacheLoadingCursor(TsCursor<CubeId> delegate, CubeId ref, ConcurrentMap<CubeId, Object> cache) {
                this.delegate = delegate;
                this.ref = ref;
                this.cache = cache;
                this.items = new ArrayList<>();
                this.currentItem = null;
            }

            @Override
            public Map<String, String> getMetaData() throws IOException {
                // FIXME: add metadata in cache
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
            public CubeId getSeriesId() throws IOException {
                return currentItem.getId();
            }

            @Override
            public Map<String, String> getSeriesMetaData() throws IOException {
                return currentItem.getMetadata();
            }

            @Override
            public OptionalTsData getSeriesData() throws IOException {
                return currentItem.getData();
            }

            @Override
            public void close() throws IOException {
                while (delegate.nextSeries()) {
                    fetchCurrentSeries();
                }
                cache.put(ref, items);
            }

            private TsItem fetchCurrentSeries() throws IOException {
                TsItem item = new TsItem(delegate.getSeriesId(), delegate.getSeriesMetaData(), delegate.getSeriesData());
                items.add(item);
                return item;
            }
        }

        @Immutable
        private static final class TsItem {

            private final CubeId id;
            private final Map<String, String> metadata;
            private final OptionalTsData data;

            public TsItem(CubeId id, Map<String, String> metadata, OptionalTsData data) {
                this.id = id;
                this.metadata = metadata;
                this.data = data;
            }

            public CubeId getId() {
                return id;
            }

            public OptionalTsData getData() {
                return data;
            }

            public Map<String, String> getMetadata() {
                return metadata;
            }
        }
        //</editor-fold>
    }
}

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

import ec.tss.tsproviders.cursor.TsCursor;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
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

    static final Collector<? super String, ?, String> LABEL_COLLECTOR = Collectors.joining(", ");

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
        public String getDisplayName() throws IOException {
            return delegate.getDisplayName();
        }

        @Override
        public String getDisplayName(CubeId id) throws IOException {
            return delegate.getDisplayName(id);
        }

        @Override
        public String getDisplayNodeName(CubeId id) throws IOException {
            return delegate.getDisplayNodeName(id);
        }

        @Override
        public CubeAccessor bulk(int depth, ConcurrentMap<CubeId, Object> cache) {
            return delegate.bulk(depth, cache);
        }
    }

    @ThreadSafe
    static final class BulkCubeAccessor extends ForwardingAccessor {

        private final ConcurrentMap<CubeId, Object> cache;
        private final int cacheLevel;
        private final boolean cacheEnabled;

        BulkCubeAccessor(@Nonnull CubeAccessor delegate, @Nonnegative int depth, @Nonnull ConcurrentMap<CubeId, Object> cache) {
            super(delegate);
            this.cacheLevel = Math.max(0, delegate.getRoot().getMaxLevel() - depth);
            this.cache = cache;
            this.cacheEnabled = depth > 0;
        }

        @Override
        public TsCursor<CubeId> getAllSeriesWithData(CubeId ref) throws IOException {
            if (cacheEnabled && !ref.isSeries()) {
                if (ref.getLevel() == cacheLevel) {
                    return TsCursor.withCache(cache, ref, super::getAllSeriesWithData);
                } else {
                    CubeId ancestor = ref.getAncestor(cacheLevel);
                    if (ancestor != null) {
                        return getAllSeriesWithData(ancestor).filter(ref::isAncestorOf);
                    }
                }
            }
            return super.getAllSeriesWithData(ref);
        }

        @Override
        public TsCursor<CubeId> getSeriesWithData(CubeId ref) throws IOException {
            if (cacheEnabled && ref.isSeries()) {
                CubeId ancestor = ref.getAncestor(cacheLevel);
                if (ancestor != null) {
                    return getAllSeriesWithData(ancestor).filter(ref::equals);
                }
            }
            return super.getSeriesWithData(ref);
        }
    }
}

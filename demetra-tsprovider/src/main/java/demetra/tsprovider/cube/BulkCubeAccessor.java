/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.tsprovider.cube;

import demetra.io.IteratorWithIO;
import demetra.tsprovider.cursor.TsCursor;
import demetra.tsprovider.util.CacheProvider;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class BulkCubeAccessor implements CubeAccessor {

    @Nonnull
    public static CubeAccessor of(@Nonnull CubeAccessor delegate, @Nonnull BulkCubeConfig options) {
        return options.isCacheEnabled()
                ? new BulkCubeAccessor(delegate, options.getDepth(), CacheProvider.getDefault().ttlCacheAsMap(options.getTtl()))
                : delegate;
    }

    @lombok.NonNull
    private final CubeAccessor delegate;

    @Nonnegative
    private final int depth;

    @lombok.NonNull
    private final ConcurrentMap<CubeId, Object> cache;

    private int getCacheLevel() throws IOException {
        return Math.max(0, delegate.getRoot().getMaxLevel() - depth);
    }

    @Override
    public TsCursor<CubeId> getAllSeriesWithData(CubeId ref) throws IOException {
        if (!ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            if (ref.getLevel() == cacheLevel) {
                return TsCursor.withCache(cache, ref, delegate::getAllSeriesWithData);
            } else {
                CubeId ancestor = ref.getAncestor(cacheLevel);
                if (ancestor != null) {
                    return getAllSeriesWithData(ancestor).filter(ref::isAncestorOf);
                }
            }
        }
        return delegate.getAllSeriesWithData(ref);
    }

    @Override
    public TsCursor<CubeId> getSeriesWithData(CubeId ref) throws IOException {
        if (ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            CubeId ancestor = ref.getAncestor(cacheLevel);
            if (ancestor != null) {
                return getAllSeriesWithData(ancestor).filter(ref::equals);
            }
        }
        return delegate.getSeriesWithData(ref);
    }

    @Override
    public IOException testConnection() {
        return delegate.testConnection();
    }

    @Override
    public CubeId getRoot() throws IOException {
        return delegate.getRoot();
    }

    @Override
    public TsCursor<CubeId> getAllSeries(CubeId id) throws IOException {
        return delegate.getAllSeries(id);
    }

    @Override
    public IteratorWithIO<CubeId> getChildren(CubeId id) throws IOException {
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
}

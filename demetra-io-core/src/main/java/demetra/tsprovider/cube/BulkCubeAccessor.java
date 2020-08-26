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

import demetra.design.ThreadSafe;
import java.io.IOException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static java.util.Objects.requireNonNull;
import java.util.stream.Stream;
import nbbrd.io.IOIterator;
import nbbrd.io.Resource;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IORunnable;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class BulkCubeAccessor implements CubeAccessor {

    @NonNull
    public static CubeAccessor of(@NonNull CubeAccessor delegate, @NonNull BulkCubeConfig options, BulkCubeCache.@NonNull Factory cacheFactory) {
        return options.isCacheEnabled()
                ? new BulkCubeAccessor(delegate, options.getDepth(), cacheFactory.ofTtl(options.getTtl()))
                : delegate;
    }

    @lombok.NonNull
    private final CubeAccessor delegate;

    @NonNegative
    private final int depth;

    @lombok.NonNull
    private final BulkCubeCache cache;

    private int getCacheLevel() throws IOException {
        return Math.max(0, delegate.getRoot().getMaxLevel() - depth);
    }

    @Override
    public Stream<CubeSeriesWithData> getAllSeriesWithData(CubeId ref) throws IOException {
        if (!ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            if (ref.getLevel() == cacheLevel) {
                return getOrLoad(cache, ref, delegate::getAllSeriesWithData);
            } else {
                CubeId ancestor = ref.getAncestor(cacheLevel);
                if (ancestor != null) {
                    return getAllSeriesWithData(ancestor).filter(ts -> ref.isAncestorOf(ts.getId()));
                }
            }
        }
        return delegate.getAllSeriesWithData(ref);
    }

    @Override
    public CubeSeriesWithData getSeriesWithData(CubeId ref) throws IOException {
        if (ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            CubeId ancestor = ref.getAncestor(cacheLevel);
            if (ancestor != null) {
                try (Stream<CubeSeriesWithData> stream = getAllSeriesWithData(ancestor)) {
                    return stream.filter(ts -> ref.equals(ts.getId()))
                            .findFirst()
                            .orElse(null);
                }
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
    public Stream<CubeSeries> getAllSeries(CubeId id) throws IOException {
        return delegate.getAllSeries(id);
    }

    @Override
    public CubeSeries getSeries(CubeId id) throws IOException {
        return delegate.getSeries(id);
    }

    @Override
    public Stream<CubeId> getChildren(CubeId id) throws IOException {
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
    public void close() throws IOException {
        Resource.closeBoth(cache, delegate);
    }

    @NonNull
    private static Stream<CubeSeriesWithData> getOrLoad(
            @NonNull BulkCubeCache cache,
            @NonNull CubeId key,
            @NonNull IOFunction<CubeId, Stream<CubeSeriesWithData>> loader) throws IOException {

        requireNonNull(cache, "cache");
        requireNonNull(key, "key");
        requireNonNull(loader, "loader");

        List<CubeSeriesWithData> result = cache.get(key);
        if (result != null) {
            return result.stream();
        }
        Stream<CubeSeriesWithData> delegate = loader.applyWithIO(key);
        IOIterator<CubeSeriesWithData> iterator = IOIterator.checked(delegate.iterator());
        return new CachingIterator(key, cache, iterator, delegate::close).asStream();
    }

    @lombok.RequiredArgsConstructor
    private static final class CachingIterator implements IOIterator<CubeSeriesWithData>, Closeable {

        private final CubeId key;
        private final BulkCubeCache cache;
        private final IOIterator<CubeSeriesWithData> delegate;
        private final Closeable closeable;

        private final List<CubeSeriesWithData> items = new ArrayList<>();

        @Override
        public boolean hasNextWithIO() throws IOException {
            return delegate.hasNextWithIO();
        }

        @Override
        public CubeSeriesWithData nextWithIO() throws IOException, NoSuchElementException {
            CubeSeriesWithData result = delegate.nextWithIO();
            items.add(result);
            return result;
        }

        @Override
        public Stream<CubeSeriesWithData> asStream() {
            return IOIterator.super.asStream().onClose(IORunnable.unchecked(this::close));
        }

        @Override
        public void close() throws IOException {
            Resource.closeBoth(this::flushToCache, closeable::close);
        }

        private void flushToCache() throws IOException {
            while (hasNextWithIO()) {
                nextWithIO();
            }
            cache.put(key, items);
        }
    }
}

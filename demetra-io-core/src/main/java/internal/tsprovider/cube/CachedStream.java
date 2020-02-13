/*
 * Copyright 2020 National Bank of Belgium
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
package internal.tsprovider.cube;

import demetra.design.Immutable;
import demetra.tsprovider.cube.CubeSeriesWithData;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static java.util.Objects.requireNonNull;
import java.util.stream.Stream;
import javax.cache.Cache;
import nbbrd.io.IOIterator;
import nbbrd.io.Resource;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IORunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public class CachedStream {

    @NonNull
    public static <KEY> Stream<CubeSeriesWithData> getOrLoad(
            @NonNull Cache<KEY, Object> cache,
            @NonNull KEY key,
            @NonNull IOFunction<? super KEY, ? extends Stream<CubeSeriesWithData>> loader) throws IOException {

        requireNonNull(cache, "cache");
        requireNonNull(key, "key");
        requireNonNull(loader, "loader");

        Object result = cache.get(key);
        if (result instanceof CachedCollection) {
            return ((CachedCollection) result).stream();
        }
        Stream<CubeSeriesWithData> delegate = loader.applyWithIO(key);
        IOIterator<CubeSeriesWithData> iterator = IOIterator.checked(delegate.iterator());
        return new CachingIterator<>(key, cache, iterator, delegate::close).asStream();
    }

    @Immutable
    @lombok.AllArgsConstructor
    static final class CachedCollection {

        private final List<CubeSeriesWithData> items;

        public Stream<CubeSeriesWithData> stream() {
            return items.stream();
        }
    }

    @lombok.RequiredArgsConstructor
    public static final class CachingIterator<KEY> implements IOIterator<CubeSeriesWithData>, Closeable {

        private final KEY key;
        private final Cache<KEY, Object> cache;
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
            cache.put(key, new CachedCollection(items));
        }
    }
}

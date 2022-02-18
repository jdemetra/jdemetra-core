/*
 * Copyright 2017 National Bank of Belgium
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

import _util.tsproviders.XCubeConnection;
import demetra.io.ResourceWatcher;
import demetra.tsprovider.util.IOCache;
import demetra.tsprovider.util.IOCacheFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;

import static demetra.tsprovider.cube.CubeIdTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class BulkCubeConnectionTest {

    private static CubeConnection newSample() {
        return new XCubeConnection(SECTOR_REGION, new ResourceWatcher());
    }

    @Test
    public void testBulkApi() throws IOException {
        CubeConnection accessor = BulkCubeConnection.of(newSample(), BulkCube.NONE, new FakeCacheFactory());
        assertThatThrownBy(() -> accessor.getAllSeriesWithData(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> accessor.getSeriesWithData(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testBulkDepth() throws IOException {
        ConcurrentMap x = new ConcurrentHashMap<>();
        try (IOCache cache = new FakeCache(x)) {
            IntFunction<BulkCubeConnection> factory = o -> {
                x.clear();
                return new BulkCubeConnection(newSample(), o, cache);
            };

            factory.apply(0).getSeriesWithData(INDUSTRY_BE);
            assertThat(x).isEmpty();

            factory.apply(0).getAllSeriesWithData(INDUSTRY).close();
            assertThat(x).isEmpty();

            factory.apply(0).getAllSeriesWithData(SECTOR_REGION).close();
            assertThat(x).isEmpty();

            factory.apply(1).getSeriesWithData(INDUSTRY_BE);
            assertThat(x).isNotEmpty();

            factory.apply(1).getAllSeriesWithData(INDUSTRY).close();
            assertThat(x).isNotEmpty();

            factory.apply(1).getAllSeriesWithData(SECTOR_REGION).close();
            assertThat(x).isEmpty();

            factory.apply(2).getSeriesWithData(INDUSTRY_BE);
            assertThat(x).isNotEmpty();

            factory.apply(2).getAllSeriesWithData(INDUSTRY).close();
            assertThat(x).isNotEmpty();

            factory.apply(2).getAllSeriesWithData(SECTOR_REGION).close();
            assertThat(x).isNotEmpty();
        }
    }

    @Test
    public void testResourceLeak() throws IOException {
        ResourceWatcher watcher = new ResourceWatcher();
        ConcurrentMap x = new ConcurrentHashMap<>();
        try (IOCache cache = new FakeCache(x)) {
            BulkCubeConnection accessor = new BulkCubeConnection(new XCubeConnection(SECTOR_REGION, watcher), 1, cache);
            accessor.getSeriesWithData(INDUSTRY_BE);
            assertThat(x).isNotEmpty();
            assertThat(watcher.isLeaking()).isFalse();
        }
    }

    private static final class FakeCacheFactory implements IOCacheFactory {

        @Override
        public @NonNull <K, V> IOCache<K, V> ofTtl(@NonNull Duration ttl) {
            return new FakeCache<>(new ConcurrentHashMap<>());
        }

        @Override
        public @NonNull <K, V> IOCache<K, V> ofFile(@NonNull File file) {
            return new FakeCache<>(new ConcurrentHashMap<>());
        }
    }

    @lombok.AllArgsConstructor
    private static final class FakeCache<K, V> implements IOCache<K, V> {

        @lombok.NonNull
        private final ConcurrentMap<K, V> delegate;

        @Override
        public void put(K key, V value) {
            delegate.put(key, value);
        }

        @Override
        public V get(K key) {
            return delegate.get(key);
        }

        @Override
        public void close() throws IOException {
        }
    }
}

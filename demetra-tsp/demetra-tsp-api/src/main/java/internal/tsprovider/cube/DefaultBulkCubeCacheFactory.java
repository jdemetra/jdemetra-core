package internal.tsprovider.cube;

import demetra.tsprovider.cube.BulkCubeCache;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSeriesWithData;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

@ServiceProvider
public final class DefaultBulkCubeCacheFactory implements BulkCubeCache.Factory {

    @Override
    public @NonNull BulkCubeCache ofTtl(@NonNull Duration ttl) {
        return new DefaultBulkCubeCache(new ConcurrentHashMap<>(), ttl, Clock.systemDefaultZone());
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class DefaultBulkCubeCache implements BulkCubeCache {

        @lombok.NonNull
        private final ConcurrentMap<CubeId, CubeSeriesWithDataAndTtl> cache;

        @lombok.NonNull
        private final Duration ttl;

        @lombok.NonNull
        private final Clock clock;

        @Override
        public void put(@NonNull CubeId key, @NonNull List<CubeSeriesWithData> value) {
            store(cache, key, new CubeSeriesWithDataAndTtl(value, clock.instant().plus(ttl)));
        }

        @Override
        public @Nullable List<CubeSeriesWithData> get(@NonNull CubeId key) {
            CubeSeriesWithDataAndTtl result = load(value -> !value.isExpired(clock), cache, key);
            return result != null ? result.getData() : null;
        }

        @Override
        public void close() throws IOException {
            cache.clear();
        }
    }

    @Nullable
    private static <K, V> V load(@NonNull Predicate<V> validator, @NonNull ConcurrentMap<K, V> map, @NonNull K key) {
        V result = map.get(key);
        if (result == null) {
            return null;
        }
        if (!validator.test(result)) {
            map.remove(key);
            return null;
        }
        return result;
    }

    private static <K, V> void store(@NonNull ConcurrentMap<K, V> map, @NonNull K key, @NonNull V value) {
        Objects.requireNonNull(value);
        map.put(key, value);
    }

    @VisibleForTesting
    @lombok.Value
    static class CubeSeriesWithDataAndTtl {

        @lombok.NonNull
        List<CubeSeriesWithData> data;

        @lombok.NonNull
        Instant expirationTime;

        public boolean isExpired(@NonNull Clock clock) {
            return !clock.instant().isBefore(expirationTime);
        }
    }
}

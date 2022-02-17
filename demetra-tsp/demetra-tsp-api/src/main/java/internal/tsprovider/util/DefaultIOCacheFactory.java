package internal.tsprovider.util;

import demetra.tsprovider.util.IOCache;
import demetra.tsprovider.util.IOCacheFactory;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;

@ServiceProvider
public final class DefaultIOCacheFactory implements IOCacheFactory {

    @Override
    public <K, V> @NonNull IOCache<K, V> ofTtl(@NonNull Duration ttl) {
        return new DefaultIOCache<>(new ConcurrentHashMap<>(), Clock.systemDefaultZone(), ttlValidator(ttl));
    }

    @Override
    public @NonNull <K, V> IOCache<K, V> ofFile(@NonNull File file) {
        return new DefaultIOCache<>(new ConcurrentHashMap<>(), Clock.systemDefaultZone(), fileValidator(file));
    }

    @VisibleForTesting
    @FunctionalInterface
    interface Validator<V> extends BiPredicate<Clock, ValueHolder<V>> {
    }

    @VisibleForTesting
    static <V> Validator<V> ttlValidator(Duration ttl) {
        Objects.requireNonNull(ttl);
        return (clock, holder) -> clock.instant().isBefore(holder.getCreationTime().plus(ttl));
    }

    @VisibleForTesting
    static <V> Validator<V> fileValidator(File file) {
        Objects.requireNonNull(file);
        return (clock, holder) -> file.exists() && file.lastModified() <= holder.getCreationTime().toEpochMilli();
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class DefaultIOCache<K, V> implements IOCache<K, V> {

        @lombok.NonNull
        private final ConcurrentMap<K, ValueHolder<V>> cache;

        @lombok.NonNull
        private final Clock clock;

        @lombok.NonNull
        private final Validator<V> validator;

        @Override
        public void put(@NonNull K key, @NonNull V value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            cache.put(key, new ValueHolder<>(value, clock.instant()));
        }

        @Override
        public @Nullable V get(@NonNull K key) {
            Objects.requireNonNull(key);
            ValueHolder<V> result = cache.get(key);
            if (result == null) {
                return null;
            }
            if (!validator.test(clock, result)) {
                cache.remove(key);
                return null;
            }
            return result.getValue();
        }

        @Override
        public void close() throws IOException {
            cache.clear();
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class ValueHolder<V> {

        @lombok.NonNull
        V value;

        @lombok.NonNull
        Instant creationTime;
    }
}

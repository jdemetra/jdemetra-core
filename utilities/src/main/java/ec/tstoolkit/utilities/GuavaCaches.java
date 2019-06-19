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
package ec.tstoolkit.utilities;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class GuavaCaches {

    private GuavaCaches() {
        // static class
    }

    @NonNull
    public static <K, V> Cache<K, V> ttlCache(@NonNull Duration duration) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(duration.toNanos(), TimeUnit.NANOSECONDS)
                .build();
    }

    @NonNull
    public static <K, V> ConcurrentMap<K, V> ttlCacheAsMap(@NonNull Duration duration) {
        return GuavaCaches.<K, V>ttlCache(duration).asMap();
    }

    @NonNull
    public static <K, V> Cache<K, V> softValuesCache() {
        return CacheBuilder.newBuilder().softValues().build();
    }

    @NonNull
    public static <K, V> ConcurrentMap<K, V> softValuesCacheAsMap() {
        return GuavaCaches.<K, V>softValuesCache().asMap();
    }

    @NonNull
    public static <K, V> V getOrThrowIOException(@NonNull LoadingCache<K, V> cache, @NonNull K key) throws IOException {
        try {
            return cache.get(key);
        } catch (ExecutionException ex) {
            throw unboxToIOException(ex);
        } catch (UncheckedExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    @NonNull
    public static <K, V> V getOrThrowIOException(@NonNull Cache<K, V> cache, @NonNull K key, @NonNull Callable<V> loader) throws IOException {
        try {
            return cache.get(key, loader);
        } catch (ExecutionException ex) {
            throw unboxToIOException(ex);
        } catch (UncheckedExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    @NonNull
    private static IOException unboxToIOException(@NonNull ExecutionException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof IOException) {
            return (IOException) cause;
        }
        if (cause != null) {
            return new IOException(cause);
        }
        return new IOException(ex);
    }
}

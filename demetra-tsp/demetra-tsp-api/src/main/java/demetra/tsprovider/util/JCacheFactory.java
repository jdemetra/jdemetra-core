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
package demetra.tsprovider.util;

import demetra.tsprovider.cube.BulkCubeCache;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSeriesWithData;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.spi.CachingProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class JCacheFactory {

    public <K, V> Cache<K, V> getTtlCacheByRef(Duration ttl) {
        return getTtlCacheByRef(() -> UUID.randomUUID().toString(), ttl);
    }

    public <K, V> Cache<K, V> getTtlCacheByRef(Supplier<String> name, Duration ttl) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<K, V> config = new MutableConfiguration<>();
        config.setStoreByValue(false);
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(toCacheDuration(ttl)));

        return cacheManager.createCache(name.get(), config);
    }

    private javax.cache.expiry.Duration toCacheDuration(Duration o) {
        return new javax.cache.expiry.Duration(TimeUnit.MILLISECONDS, o.toMillis());
    }

    public BulkCubeCache.Factory bulkCubeCacheOf(Supplier<String> name) {
        return ttl -> new CacheAdapter(getTtlCacheByRef(name, ttl));
    }

    @lombok.AllArgsConstructor
    private static final class CacheAdapter implements BulkCubeCache {

        @lombok.NonNull
        private final Cache<CubeId, List<CubeSeriesWithData>> delegate;

        @Override
        public void put(CubeId key, List<CubeSeriesWithData> value) {
            delegate.put(key, value);
        }

        @Override
        public List<CubeSeriesWithData> get(CubeId key) {
            return delegate.get(key);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}

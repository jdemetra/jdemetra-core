/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
import com.google.common.cache.ForwardingCache;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public abstract class LastModifiedFileCache<K, V> extends ForwardingCache<K, V> {

    @NonNull
    public static <K, V> LastModifiedFileCache<K, V> from(@NonNull File file, @NonNull final Cache<K, V> cache) {
        return new LastModifiedFileCache<K, V>(file) {
            @Override
            protected Cache<K, V> delegate() {
                return cache;
            }
        };
    }
    //
    protected final File file;
    protected long lastModifiedFile;

    protected LastModifiedFileCache(@NonNull File file) {
        this.file = file;
        this.lastModifiedFile = file.lastModified();
    }

    protected void cleanIfFileModified() {
        long current = file.lastModified();
        if (current != lastModifiedFile) {
            lastModifiedFile = current;
            invalidateAll();
        }
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        cleanIfFileModified();
        return super.asMap();
    }

    @Override
    public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
        cleanIfFileModified();
        return super.get(key, valueLoader);
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        cleanIfFileModified();
        return super.getAllPresent(keys);
    }

    @Override
    public V getIfPresent(Object key) {
        cleanIfFileModified();
        return super.getIfPresent(key);
    }
}

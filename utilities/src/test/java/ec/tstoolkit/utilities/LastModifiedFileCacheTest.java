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
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LastModifiedFileCacheTest {

    @Test
    public void test() throws IOException {
        Path path = Files.createTempFile("cache", "");
        Cache<String, String> original = CacheBuilder.newBuilder().build();
        Cache<String, String> cache = LastModifiedFileCache.from(path.toFile(), original);
        cache.put("hello", "world");
        Assert.assertEquals("world", cache.getIfPresent("hello"));
        Files.write(path, Collections.singleton("1234"), StandardCharsets.UTF_8, StandardOpenOption.DELETE_ON_CLOSE);
        Assert.assertNull(cache.getIfPresent("hello"));
    }
}

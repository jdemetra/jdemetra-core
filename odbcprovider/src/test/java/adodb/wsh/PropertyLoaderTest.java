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
package adodb.wsh;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class PropertyLoaderTest {

    private static final class CountingLoader implements PropertyLoader {

        private int nbrCalls = 0;

        @Override
        public Map<String, String> loadAll() throws IOException {
            return ImmutableMap.of("nbrCalls", Integer.toString(++nbrCalls));
        }
    }

    @Test
    public void testMemoize() throws IOException {
        PropertyLoader loader = new CountingLoader().memoize();
        assertThat(loader.load("nbrCalls")).isEqualTo("1");
        assertThat(loader.load("nbrCalls")).isEqualTo("1");
    }

    @Test
    public void testMemoizeWithExpiration() throws IOException {
        AtomicLong clock = new AtomicLong(0);
        PropertyLoader loader = new CountingLoader().memoizeWithExpiration(7, TimeUnit.NANOSECONDS, clock::get);

        clock.set(System.nanoTime());
        assertThat(loader.load("nbrCalls")).isEqualTo("1");
        assertThat(loader.load("nbrCalls")).isEqualTo("1");

        clock.addAndGet(6);
        assertThat(loader.load("nbrCalls")).isEqualTo("1");
        assertThat(loader.load("nbrCalls")).isEqualTo("1");

        clock.addAndGet(1);
        assertThat(loader.load("nbrCalls")).isEqualTo("2");
        assertThat(loader.load("nbrCalls")).isEqualTo("2");
    }
}

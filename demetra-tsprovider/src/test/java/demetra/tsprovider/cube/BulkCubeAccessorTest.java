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

import demetra.io.ResourceWatcher;
import _util.tsproviders.XCubeAccessor;
import static demetra.tsprovider.cube.CubeIdTest.INDUSTRY;
import static demetra.tsprovider.cube.CubeIdTest.INDUSTRY_BE;
import static demetra.tsprovider.cube.CubeIdTest.SECTOR_REGION;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class BulkCubeAccessorTest {

    private static CubeAccessor newSample() {
        return new XCubeAccessor(SECTOR_REGION, new ResourceWatcher());
    }

    @Test
    public void testBulkApi() throws IOException {
        CubeAccessor accessor = BulkCubeAccessor.of(newSample(), BulkCubeConfig.of(Duration.ZERO, 0));
        assertThatThrownBy(() -> accessor.getAllSeriesWithData(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> accessor.getSeriesWithData(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testBulkDepth() throws IOException {
        ConcurrentMap<CubeId, Object> cache = new ConcurrentHashMap<>();
        IntFunction<BulkCubeAccessor> factory = o -> {
            cache.clear();
            return new BulkCubeAccessor(newSample(), o, cache);
        };

        factory.apply(0).getSeriesWithData(INDUSTRY_BE).close();
        assertThat(cache).isEmpty();

        factory.apply(0).getAllSeriesWithData(INDUSTRY).close();
        assertThat(cache).isEmpty();

        factory.apply(0).getAllSeriesWithData(SECTOR_REGION).close();
        assertThat(cache).isEmpty();

        factory.apply(1).getSeriesWithData(INDUSTRY_BE).close();
        assertThat(cache).isNotEmpty();

        factory.apply(1).getAllSeriesWithData(INDUSTRY).close();
        assertThat(cache).isNotEmpty();

        factory.apply(1).getAllSeriesWithData(SECTOR_REGION).close();
        assertThat(cache).isEmpty();

        factory.apply(2).getSeriesWithData(INDUSTRY_BE).close();
        assertThat(cache).isNotEmpty();

        factory.apply(2).getAllSeriesWithData(INDUSTRY).close();
        assertThat(cache).isNotEmpty();

        factory.apply(2).getAllSeriesWithData(SECTOR_REGION).close();
        assertThat(cache).isNotEmpty();
    }

    @Test
    public void testResourceLeak() throws IOException {
        ResourceWatcher watcher = new ResourceWatcher();
        ConcurrentMap<CubeId, Object> cache = new ConcurrentHashMap<>();
        BulkCubeAccessor accessor = new BulkCubeAccessor(new XCubeAccessor(SECTOR_REGION, watcher), 1, cache);
        accessor.getSeriesWithData(INDUSTRY_BE).close();
        assertThat(cache).isNotEmpty();
        assertThat(watcher.isLeaking()).isFalse();
    }
}

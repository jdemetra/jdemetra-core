package internal.tsprovider.cube;

import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSeriesWithData;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@SuppressWarnings("ConstantConditions")
public class DefaultBulkCubeCacheFactoryTest {

    @Test
    public void testOfTtl() {
        assertThatNullPointerException().isThrownBy(() -> new DefaultBulkCubeCacheFactory().ofTtl(null));
    }

    @Test
    public void testDefaultBulkCubeCache() {
        ConcurrentMap<CubeId, DefaultBulkCubeCacheFactory.CubeSeriesWithDataAndTtl> map = new ConcurrentHashMap<>();
        Duration ttl = Duration.ofMillis(100);
        FakeClock clock = new FakeClock();

        DefaultBulkCubeCacheFactory.DefaultBulkCubeCache cache = new DefaultBulkCubeCacheFactory.DefaultBulkCubeCache(map, ttl, clock);

        CubeId key = CubeId.root("a");
        ArrayList<CubeSeriesWithData> value = new ArrayList<>();

        cache.put(key, value);
        assertThat(map).containsKey(key);
        assertThat(cache.get(key)).isEqualTo(value);

        clock.plus(ttl.toMillis());
        assertThat(map).containsKey(key);
        assertThat(cache.get(key)).isNull();
        assertThat(map).isEmpty();
    }

    static final class FakeClock extends Clock {

        private Instant current = Instant.now();

        public FakeClock set(Instant current) {
            this.current = current;
            return this;
        }

        public FakeClock set(long epochMilli) {
            return set(Instant.ofEpochMilli(epochMilli));
        }

        public FakeClock plus(long durationInMillis) {
            return set(current.plus(durationInMillis, ChronoUnit.MILLIS));
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }
    }
}

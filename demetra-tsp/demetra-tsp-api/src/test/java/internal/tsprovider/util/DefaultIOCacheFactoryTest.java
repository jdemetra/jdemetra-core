package internal.tsprovider.util;

import com.google.common.io.Files;
import nbbrd.design.MightBePromoted;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@SuppressWarnings("ConstantConditions")
public class DefaultIOCacheFactoryTest {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testOfTtl() {
        assertThatNullPointerException().isThrownBy(() -> new DefaultIOCacheFactory().ofTtl(null));
    }

    @Test
    public void testOfFile() {
        assertThatNullPointerException().isThrownBy(() -> new DefaultIOCacheFactory().ofFile(null));
    }

    @Test
    public void testTtlValidator() {
        FakeClock clock = new FakeClock();

        Duration ttl = Duration.ofMillis(100);
        DefaultIOCacheFactory.Validator<Object> validator = DefaultIOCacheFactory.ttlValidator(ttl);

        assertThat(validator.test(clock, new DefaultIOCacheFactory.ValueHolder<>("", clock.instant())))
                .isTrue();

        assertThat(validator.test(clock, new DefaultIOCacheFactory.ValueHolder<>("", clock.instant().minus(ttl))))
                .isFalse();
    }

    @Test
    public void testFileValidator() throws IOException {
        File file = temp.newFile();
        assertThat(file).exists();

        FakeClock clock = new FakeClock().set(file.lastModified());

        DefaultIOCacheFactory.Validator<String> validator = DefaultIOCacheFactory.fileValidator(file);
        DefaultIOCacheFactory.ValueHolder<String> valueHolder = new DefaultIOCacheFactory.ValueHolder<>("", clock.instant());

        assertThat(validator.test(clock, valueHolder)).isTrue();

        Files.touch(file);
        assertThat(validator.test(clock, valueHolder)).isFalse();

        assertThat(file.delete()).isTrue();
        assertThat(validator.test(clock, valueHolder)).isFalse();
    }

    @Test
    public void testDefaultIOCache() {
        ConcurrentMap<String, DefaultIOCacheFactory.ValueHolder<Integer>> map = new ConcurrentHashMap<>();
        FakeClock clock = new FakeClock();
        AtomicBoolean valid = new AtomicBoolean(true);

        DefaultIOCacheFactory.DefaultIOCache<String, Integer> cache = new DefaultIOCacheFactory.DefaultIOCache<>(map, clock, (x, y) -> valid.get());

        String key = "key1";
        Integer value = 1;

        cache.put(key, value);
        assertThat(map).containsKey(key);
        assertThat(cache.get(key)).isEqualTo(value);

        valid.set(false);
        assertThat(map).containsKey(key);
        assertThat(cache.get(key)).isNull();
        assertThat(map).isEmpty();
    }

    @MightBePromoted
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

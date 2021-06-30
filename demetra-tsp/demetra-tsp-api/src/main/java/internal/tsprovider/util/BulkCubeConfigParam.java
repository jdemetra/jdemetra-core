package internal.tsprovider.util;

import demetra.tsprovider.cube.BulkCubeConfig;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class BulkCubeConfigParam<S extends IConfig> implements Param<S, BulkCubeConfig> {

    private final BulkCubeConfig defaultValue;
    private final Param<S, Long> cacheTtl;
    private final Param<S, Integer> cacheDepth;

    public BulkCubeConfigParam(
            @NonNull BulkCubeConfig defaultValue,
            @NonNull String ttlKey,
            @NonNull String depthKey) {
        this.defaultValue = defaultValue;
        this.cacheTtl = Param.onLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), ttlKey);
        this.cacheDepth = Param.onInteger(1, depthKey);
    }

    @Override
    public BulkCubeConfig defaultValue() {
        return defaultValue;
    }

    @Override
    public BulkCubeConfig get(S config) {
        return BulkCubeConfig.of(Duration.ofMillis(cacheTtl.get(config)), cacheDepth.get(config));
    }

    @Override
    public void set(IConfig.Builder<?, S> builder, BulkCubeConfig value) {
        Objects.requireNonNull(builder);
        cacheTtl.set(builder, value.getTtl().toMillis());
        cacheDepth.set(builder, value.getDepth());
    }
}

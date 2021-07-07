package internal.tsprovider.util;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.BulkCubeConfig;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class BulkCubeConfigParam implements DataSource.Converter<BulkCubeConfig> {

    private final BulkCubeConfig defaultValue;
    private final Property<Long> cacheTtl;
    private final Property<Integer> cacheDepth;

    public BulkCubeConfigParam(
            @NonNull BulkCubeConfig defaultValue,
            @NonNull String ttlKey,
            @NonNull String depthKey) {
        this.defaultValue = defaultValue;
        this.cacheTtl = Property.of(ttlKey, TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), Parser.onLong(), Formatter.onLong());
        this.cacheDepth = Property.of(depthKey, 1, Parser.onInteger(), Formatter.onInteger());
    }

    @Override
    public BulkCubeConfig getDefaultValue() {
        return defaultValue;
    }

    @Override
    public BulkCubeConfig get(DataSource config) {
        return BulkCubeConfig.of(Duration.ofMillis(cacheTtl.get(config::getParameter)), cacheDepth.get(config::getParameter));
    }

    @Override
    public void set(DataSource.Builder builder, BulkCubeConfig value) {
        Objects.requireNonNull(builder);
        cacheTtl.set(builder::parameter, value.getTtl().toMillis());
        cacheDepth.set(builder::parameter, value.getDepth());
    }
}

package internal.tsprovider.util;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.BulkCube;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class BulkCubeParam implements DataSource.Converter<BulkCube> {

    private final BulkCube defaultValue;
    private final Property<Long> cacheTtl;
    private final Property<Integer> cacheDepth;

    public BulkCubeParam(
            @NonNull BulkCube defaultValue,
            @NonNull String ttlKey,
            @NonNull String depthKey) {
        this.defaultValue = defaultValue;
        this.cacheTtl = Property.of(ttlKey, TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), Parser.onLong(), Formatter.onLong());
        this.cacheDepth = Property.of(depthKey, 1, Parser.onInteger(), Formatter.onInteger());
    }

    @Override
    public BulkCube getDefaultValue() {
        return defaultValue;
    }

    @Override
    public BulkCube get(DataSource config) {
        return BulkCube
                .builder()
                .ttl(Duration.ofMillis(cacheTtl.get(config::getParameter)))
                .depth(cacheDepth.get(config::getParameter))
                .build();
    }

    @Override
    public void set(DataSource.Builder builder, BulkCube value) {
        Objects.requireNonNull(builder);
        cacheTtl.set(builder::parameter, value.getTtl().toMillis());
        cacheDepth.set(builder::parameter, value.getDepth());
    }
}

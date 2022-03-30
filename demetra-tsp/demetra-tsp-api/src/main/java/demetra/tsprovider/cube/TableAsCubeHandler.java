package demetra.tsprovider.cube;

import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class TableAsCubeHandler implements PropertyHandler<TableAsCube> {

    @lombok.NonNull
    private final PropertyHandler<List<String>> dimensions;

    @lombok.NonNull
    private final PropertyHandler<String> timeDimension;

    @lombok.NonNull
    private final PropertyHandler<String> measure;

    @lombok.NonNull
    private final PropertyHandler<String> version;

    @lombok.NonNull
    private final PropertyHandler<String> label;

    @lombok.NonNull
    private final PropertyHandler<ObsFormat> format;

    @lombok.NonNull
    private final PropertyHandler<ObsGathering> gathering;

    @Override
    public @NonNull TableAsCube get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return TableAsCube
                .builder()
                .dimensions(dimensions.get(properties))
                .timeDimension(timeDimension.get(properties))
                .measure(measure.get(properties))
                .version(version.get(properties))
                .label(label.get(properties))
                .format(format.get(properties))
                .gathering(gathering.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable TableAsCube value) {
        if (value != null) {
            dimensions.set(properties, value.getDimensions());
            timeDimension.set(properties, value.getTimeDimension());
            measure.set(properties, value.getMeasure());
            version.set(properties, value.getVersion());
            label.set(properties, value.getLabel());
            format.set(properties, value.getFormat());
            gathering.set(properties, value.getGathering());
        }
    }
}

package demetra.tsprovider.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class ObsGatheringHandler implements PropertyHandler<ObsGathering> {

    @lombok.NonNull
    private final PropertyHandler<TsUnit> unit;

    @lombok.NonNull
    private final PropertyHandler<AggregationType> aggregationType;

    @lombok.NonNull
    private final PropertyHandler<Boolean> allowPartialAggregation;

    @lombok.NonNull
    private final PropertyHandler<Boolean> includeMissingValues;

    @Override
    public @NonNull ObsGathering get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return ObsGathering
                .builder()
                .unit(unit.get(properties))
                .aggregationType(aggregationType.get(properties))
                .allowPartialAggregation(allowPartialAggregation.get(properties))
                .includeMissingValues(includeMissingValues.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable ObsGathering value) {
        if (value != null) {
            unit.set(properties, value.getUnit());
            aggregationType.set(properties, value.getAggregationType());
            allowPartialAggregation.set(properties, value.isAllowPartialAggregation());
            includeMissingValues.set(properties, value.isIncludeMissingValues());
        }
    }
}

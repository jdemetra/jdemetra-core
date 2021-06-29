package internal.tsprovider.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ObsGatheringParam<S extends IConfig> implements Param<S, ObsGathering> {

    private final ObsGathering defaultValue;
    private final Param<S, String> unit;
    private final Param<S, AggregationType> aggregationType;
    private final Param<S, Boolean> skipMissingValues;

    public ObsGatheringParam(
            @NonNull ObsGathering defaultValue,
            @NonNull String frequencyKey,
            @NonNull String aggregationKey,
            @NonNull String skipKey) {
        this.defaultValue = defaultValue;
        this.unit = Param.onString(defaultValue.getUnit().toISO8601(), frequencyKey);
        this.aggregationType = Param.onEnum(defaultValue.getAggregationType(), aggregationKey);
        this.skipMissingValues = Param.onBoolean(!defaultValue.isIncludeMissingValues(), skipKey);
    }

    @Override
    public ObsGathering defaultValue() {
        return defaultValue;
    }

    @Override
    public ObsGathering get(S config) {
        return ObsGathering.builder()
                .unit(getUnit(config))
                .aggregationType(aggregationType.get(config))
                .includeMissingValues(!skipMissingValues.get(config))
                .build();
    }

    @Override
    public void set(IConfig.Builder<?, S> builder, ObsGathering value) {
        Objects.requireNonNull(builder);
        skipMissingValues.set(builder, !value.isIncludeMissingValues());
        setFreq(builder, value.getUnit());
        aggregationType.set(builder, value.getAggregationType());
    }

    private TsUnit getUnit(S config) {
        String text = unit.get(config);
        TsUnit value = freqToUnit(text);
        if (value != null) {
            return value;
        }
        try {
            return TsUnit.parse(text);
        } catch (DateTimeParseException ex) {
            return TsUnit.parse(unit.defaultValue());
        }
    }

    private void setFreq(IConfig.Builder<?, S> builder, TsUnit value) {
        String freq = unitToFreq(value);
        unit.set(builder, freq != null ? freq : value.toISO8601());
    }

    private static TsUnit freqToUnit(String freq) {
        switch (freq) {
            case "Yearly":
                return TsUnit.YEAR;
            case "HalfYearly":
                return TsUnit.HALF_YEAR;
            case "QuadriMonthly":
                return TsUnit.of(4, ChronoUnit.MONTHS);
            case "Quarterly":
                return TsUnit.QUARTER;
            case "BiMonthly":
                return TsUnit.of(2, ChronoUnit.MONTHS);
            case "Monthly":
                return TsUnit.MONTH;
        }
        return null;
    }

    private static String unitToFreq(TsUnit unit) {
        switch (unit.getChronoUnit()) {
            case YEARS:
                if (unit.getAmount() == 1) {
                    return "Yearly";
                }
                break;
            case MONTHS:
                if (unit.getAmount() == 6) {
                    return "HalfYearly";
                }
                if (unit.getAmount() == 4) {
                    return "QuadriMonthly";
                }
                if (unit.getAmount() == 3) {
                    return "Quarterly";
                }
                if (unit.getAmount() == 2) {
                    return "BiMonthly";
                }
                if (unit.getAmount() == 1) {
                    return "Monthly";
                }
                break;
        }
        return null;
    }
}

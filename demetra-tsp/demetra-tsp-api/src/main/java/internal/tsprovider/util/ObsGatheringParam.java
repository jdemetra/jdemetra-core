package internal.tsprovider.util;

import demetra.data.AggregationType;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.DataSource;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ObsGatheringParam implements DataSource.Converter<ObsGathering> {

    private final ObsGathering defaultValue;
    private final Property<String> unit;
    private final Property<AggregationType> aggregationType;
    private final Property<Boolean> skipMissingValues;

    public ObsGatheringParam(
            @NonNull ObsGathering defaultValue,
            @NonNull String frequencyKey,
            @NonNull String aggregationKey,
            @NonNull String skipKey) {
        this.defaultValue = defaultValue;
        this.unit = Property.of(frequencyKey, defaultValue.getUnit().toISO8601(), Parser.onString(), Formatter.onString());
        this.aggregationType = Property.of(aggregationKey, defaultValue.getAggregationType(), Parser.onEnum(AggregationType.class), Formatter.onEnum());
        this.skipMissingValues = Property.of(skipKey, !defaultValue.isIncludeMissingValues(), Parser.onBoolean(), Formatter.onBoolean());
    }

    @Override
    public ObsGathering getDefaultValue() {
        return defaultValue;
    }

    @Override
    public ObsGathering get(DataSource config) {
        return ObsGathering.builder()
                .unit(getUnit(config))
                .aggregationType(aggregationType.get(config::getParameter))
                .includeMissingValues(!skipMissingValues.get(config::getParameter))
                .build();
    }

    @Override
    public void set(DataSource.Builder builder, ObsGathering value) {
        Objects.requireNonNull(builder);
        skipMissingValues.set(builder::parameter, !value.isIncludeMissingValues());
        setFreq(builder, value.getUnit());
        aggregationType.set(builder::parameter, value.getAggregationType());
    }

    private TsUnit getUnit(DataSource config) {
        String text = unit.get(config::getParameter);
        TsUnit value = freqToUnit(text);
        if (value != null) {
            return value;
        }
        try {
            return TsUnit.parse(text);
        } catch (DateTimeParseException ex) {
            return TsUnit.parse(unit.getDefaultValue());
        }
    }

    private void setFreq(DataSource.Builder builder, TsUnit value) {
        String freq = unitToFreq(value);
        unit.set(builder::parameter, freq != null ? freq : value.toISO8601());
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

package demetra.tsprovider.legacy;

import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.RegularFrequency;
import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.ObsFormatHandler;
import demetra.tsprovider.util.ObsGatheringHandler;
import demetra.tsprovider.util.PropertyHandler;
import internal.util.Strings;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.experimental.UtilityClass
public class LegacyHandler {

    @Deprecated
    public static @NonNull PropertyHandler<ObsFormat> onObsFormat(@NonNull String localeKey, @NonNull String datePatternKey, @NonNull String numberPatternKey, @NonNull ObsFormat defaultValue) {
        return ObsFormatHandler.builder()
                .locale(PropertyHandler.of(Property.of(localeKey, defaultValue.getLocale(), LegacyHandler::parseLocale, LegacyHandler::formatLocale)))
                .dateTimePattern(PropertyHandler.of(Property.of(datePatternKey, defaultValue.getDateTimePattern(), Parser.onString().andThen(Strings::emptyToNull), Formatter.onString().compose(Strings::nullToEmpty))))
                .numberPattern(PropertyHandler.of(Property.of(numberPatternKey, defaultValue.getNumberPattern(), Parser.onString().andThen(Strings::emptyToNull), Formatter.onString().compose(Strings::nullToEmpty))))
                .ignoreNumberGrouping(PropertyHandler.onBoolean("ignoreNumberGrouping", defaultValue.isIgnoreNumberGrouping()))
                .build();
    }

    @Deprecated
    public static @NonNull PropertyHandler<ObsGathering> onObsGathering(@NonNull String frequencyKey, @NonNull String aggregationKey, @NonNull String skipKey, @NonNull ObsGathering defaultValue) {
        return ObsGatheringHandler
                .builder()
                .unit(PropertyHandler.of(Property.of(frequencyKey, defaultValue.getUnit(),
                        Parser.onEnum(RegularFrequency.class).andThen(RegularFrequency::toTsUnit).orElse(Parser.of(TsUnit::parse)),
                        Formatter.<RegularFrequency>onEnum().compose(RegularFrequency::parseTsUnit))))//.orElse(Formatter.of(TsUnit::toString)));
                .aggregationType(PropertyHandler.onEnum(aggregationKey, defaultValue.getAggregationType()))
                .allowPartialAggregation(PropertyHandler.onBoolean("allowPartialAggregation", defaultValue.isAllowPartialAggregation()))
                .includeMissingValues(not(PropertyHandler.onBoolean(skipKey, !defaultValue.isIncludeMissingValues())))
                .build();
    }

    @Deprecated
    private static @NonNull PropertyHandler<Boolean> not(PropertyHandler<Boolean> delegate) {
        return new PropertyHandler<Boolean>() {
            @Override
            public @NonNull Boolean getDefaultValue() {
                return !delegate.getDefaultValue();
            }

            @Override
            public @NonNull Boolean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
                return !delegate.get(properties);
            }

            @Override
            public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable Boolean value) {
                delegate.set(properties, value != null ? !value : null);
            }
        };
    }

    @Nullable
    private static Locale parseLocale(@lombok.NonNull CharSequence locale) {
        // Fix behavior change in Parser#onLocale()
        Locale result = Parser.onLocale().parse(locale);
        return Locale.ROOT.equals(result) && locale.length() == 0 ? null : result;
    }

    private static CharSequence formatLocale(Locale locale) {
        return locale != null ? locale.toString() : "";
    }
}

package demetra.tsprovider.util;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class ObsFormatHandler implements PropertyHandler<ObsFormat> {

    @lombok.NonNull
    private final PropertyHandler<Locale> locale;

    @lombok.NonNull
    private final PropertyHandler<String> dateTimePattern;

    @lombok.NonNull
    private final PropertyHandler<String> numberPattern;

    @lombok.NonNull
    private final PropertyHandler<Boolean> ignoreNumberGrouping;

    @Override
    public @NonNull ObsFormat get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return ObsFormat
                .builder()
                .locale(locale.get(properties))
                .dateTimePattern(dateTimePattern.get(properties))
                .numberPattern(numberPattern.get(properties))
                .ignoreNumberGrouping(ignoreNumberGrouping.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable ObsFormat value) {
        if (value != null) {
            locale.set(properties, value.getLocale());
            dateTimePattern.set(properties, value.getDateTimePattern());
            numberPattern.set(properties, value.getNumberPattern());
            ignoreNumberGrouping.set(properties, value.isIgnoreNumberGrouping());
        }
    }
}

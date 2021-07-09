package internal.tsprovider.util;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.ObsFormat;
import internal.util.Strings;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.Objects;

@lombok.AllArgsConstructor
public final class ObsFormatParam implements DataSource.Converter<ObsFormat> {

    @lombok.NonNull
    private final ObsFormat defaultValue;

    @lombok.NonNull
    private final String localeKey;

    @lombok.NonNull
    private final String datePatternKey;

    @lombok.NonNull
    private final String numberPatternKey;

    private boolean isValid(String locale, String datePattern) {
        return locale != null && datePattern != null;
    }

    @Nullable
    private Locale parseLocale(@NonNull String locale) {
        // Fix behavior change in Parser#onLocale()
        Locale result = Parser.onLocale().parse(locale);
        return Locale.ROOT.equals(result) && locale.isEmpty() ? null : result;
    }

    @Override
    public ObsFormat getDefaultValue() {
        return defaultValue;
    }

    @Override
    public ObsFormat get(DataSource config) {
        String locale = config.getParameter(localeKey);
        String datePattern = config.getParameter(datePatternKey);
        String numberPattern = config.getParameter(numberPatternKey);
        return isValid(locale, datePattern)
                ? ObsFormat.of(parseLocale(locale), datePattern, numberPattern)
                : defaultValue;
    }

    @Override
    public void set(DataSource.Builder builder, ObsFormat value) {
        Objects.requireNonNull(builder);
        if (!defaultValue.equals(value)) {
            builder.parameter(localeKey, getLocaleValue(value.getLocale()));
            builder.parameter(datePatternKey, getDateTimePatternValue(value.getDateTimePattern()));
            builder.parameter(numberPatternKey, getNumberPatternValue(value.getNumberPattern()));
        }
    }

    private static String getLocaleValue(Locale locale) {
        return locale != null ? locale.toString() : "";
    }

    private static String getDateTimePatternValue(String dateTimePattern) {
        return Strings.nullToEmpty(dateTimePattern);
    }

    private static String getNumberPatternValue(String numberPattern) {
        return Strings.nullToEmpty(numberPattern);
    }
}

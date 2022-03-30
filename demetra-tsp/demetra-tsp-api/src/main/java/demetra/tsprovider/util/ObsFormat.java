/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package demetra.tsprovider.util;

import nbbrd.design.LombokWorkaround;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Locale;

/**
 * A special object that contains all information needed to format and parse
 * observations.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class ObsFormat {

    @VisibleForTesting
    static final Locale NULL_AS_SYSTEM_DEFAULT = null;

    public static final ObsFormat DEFAULT = new ObsFormat(Locale.ROOT, "", "", false);

    public static @NonNull ObsFormat getSystemDefault() {
        return new ObsFormat(NULL_AS_SYSTEM_DEFAULT, "", "", false);
    }

    @Nullable
    Locale locale;

    @lombok.NonNull
    String dateTimePattern;

    @lombok.NonNull
    String numberPattern;

    boolean ignoreNumberGrouping;

    @LombokWorkaround
    public static ObsFormat.Builder builder() {
        return DEFAULT.toBuilder();
    }

    @NonNull
    public Formatter<LocalDateTime> dateTimeFormatter() {
        try {
            return Formatter.onDateTimeFormatter(newDateTimeFormatter());
        } catch (IllegalArgumentException ex) {
            return Formatter.onNull();
        }
    }

    @NonNull
    public Parser<LocalDateTime> dateTimeParser() {
        try {
            return Parser.onDateTimeFormatter(newDateTimeFormatter(), TEMPORAL_QUERIES);
        } catch (IllegalArgumentException ex) {
            return Parser.onNull();
        }
    }

    /**
     * Returns a date formatter from the current locale and date pattern. Note
     * that an invalid pattern returns a do-nothing formatter.
     *
     * @return a non-null formatter
     */
    @NonNull
    public Formatter<Date> calendarFormatter() {
        try {
            return Formatter.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return Formatter.onNull();
        }
    }

    /**
     * Returns a date parser from the current locale and date pattern. Note that
     * an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    @NonNull
    public Parser<Date> calendarParser() {
        try {
            return Parser.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return Parser.onNull();
        }
    }

    /**
     * Returns a number formatter from the current locale and number pattern.
     * Note that an invalid pattern returns a do-nothing formatter.
     *
     * @return a non-null formatter
     */
    @NonNull
    public Formatter<Number> numberFormatter() {
        try {
            return Formatter.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return Formatter.onNull();
        }
    }

    /**
     * Returns a number parser from the current locale and number pattern.Note
     * that an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    @NonNull
    public Parser<Number> numberParser() {
        try {
            return Parser.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return Parser.onNull();
        }
    }

    private static final String SEP = " ~ ";

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (locale != NULL_AS_SYSTEM_DEFAULT) {
            builder.append(locale);
        }
        builder.append(SEP);
        if (!dateTimePattern.isEmpty()) {
            builder.append(dateTimePattern);
        }
        builder.append(SEP);
        if (!numberPattern.isEmpty()) {
            builder.append(numberPattern);
        }
        if (ignoreNumberGrouping) {
            builder.append(SEP);
            builder.append("ignore-grouping");
        }
        return builder.toString();
    }

    @VisibleForTesting
    DateTimeFormatter newDateTimeFormatter() throws IllegalArgumentException {
        Locale nonNullLocale = getLocaleOrSystemDefault();

        DateTimeFormatterBuilder result = new DateTimeFormatterBuilder();

        // 1. pattern
        if (!dateTimePattern.isEmpty()) {
            result.appendPattern(dateTimePattern);
        } else {
            if (Locale.ROOT.equals(nonNullLocale)) {
                result.appendPattern("yyyy-MM-dd['T'HH:mm:ss]");
            } else {
                result
                        .append(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        .optionalStart()
                        .appendLiteral(' ')
                        .append(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
                        .optionalEnd();
            }
        }

        // 2. default values
        result
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1);

        // 3. locale
        return result.toFormatter(nonNullLocale);
    }

    @VisibleForTesting
    DateFormat newDateFormat() throws IllegalArgumentException {
        Locale nonNullLocale = getLocaleOrSystemDefault();

        DateFormat result = !dateTimePattern.isEmpty()
                ? new SimpleDateFormat(dateTimePattern, nonNullLocale)
                : Locale.ROOT.equals(nonNullLocale)
                ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", nonNullLocale)
                : SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, nonNullLocale);

        result.setLenient(dateTimePattern.isEmpty() && locale == NULL_AS_SYSTEM_DEFAULT);

        return result;
    }

    @VisibleForTesting
    NumberFormat newNumberFormat() throws IllegalArgumentException {
        Locale nonNullLocale = getLocaleOrSystemDefault();

        NumberFormat result = !numberPattern.isEmpty()
                ? new DecimalFormat(numberPattern, DecimalFormatSymbols.getInstance(nonNullLocale))
                : Locale.ROOT.equals(nonNullLocale)
                ? new DecimalFormat("#.################", DecimalFormatSymbols.getInstance(nonNullLocale))
                : NumberFormat.getInstance(nonNullLocale);

        if (ignoreNumberGrouping) {
            result.setGroupingUsed(false);
        }

        return result;
    }

    private Locale getLocaleOrSystemDefault() {
        return locale != NULL_AS_SYSTEM_DEFAULT ? locale : Locale.getDefault(Locale.Category.FORMAT);
    }

    private static final TemporalQuery[] TEMPORAL_QUERIES = {LocalDateTime::from, o -> LocalDate.from(o).atStartOfDay()};
}

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

import demetra.design.Immutable;
import demetra.design.VisibleForTesting;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import demetra.util.Parser;
import demetra.util.Formatter;
import internal.util.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQuery;
import lombok.AccessLevel;

/**
 * A special object that contains all information needed to format and parse
 * observations.
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
@lombok.Getter
public final class ObsFormat {

    public static final ObsFormat DEFAULT = new ObsFormat(null, "", "");

    /**
     * Creates a DataFormat from an optional locale, an optional date pattern
     * and an optional number pattern.
     *
     * @param locale an optional locale
     * @param datePattern an optional date pattern
     * @param numberPattern an optional number pattern
     * @return
     * @see Locale
     * @see SimpleDateFormat
     * @see DecimalFormat
     */
    @NonNull
    public static ObsFormat of(@Nullable Locale locale, @Nullable String datePattern, @Nullable String numberPattern) {
        return new ObsFormat(locale, Strings.nullToEmpty(datePattern), Strings.nullToEmpty(numberPattern));
    }

    private final Locale locale;

    @lombok.NonNull
    private final String datePattern;

    @lombok.NonNull
    private final String numberPattern;

    @NonNull
    public String getLocaleString() {
        return locale != null ? locale.toString() : "";
    }

    @Override
    public String toString() {
        return getLocaleString() + " ~ " + datePattern + " ~ " + numberPattern;
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

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @VisibleForTesting
    DateTimeFormatter newDateTimeFormatter() throws IllegalArgumentException {
        DateTimeFormatterBuilder result = new DateTimeFormatterBuilder();

        // 1. pattern
        if (!datePattern.isEmpty()) {
            result.appendPattern(datePattern);
        } else {
            result
                    .append(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    .optionalStart()
                    .appendLiteral(' ')
                    .append(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
                    .optionalEnd();
        }

        // 2. default values
        result
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1);

        // 3. locale
        return locale != null
                ? result.toFormatter(locale)
                : result.toFormatter();
    }

    @VisibleForTesting
    DateFormat newDateFormat() throws IllegalArgumentException {
        DateFormat result;
        if (!datePattern.isEmpty()) {
            if (locale != null) {
                result = new SimpleDateFormat(datePattern, locale);
                result.setLenient(false);
            } else {
                result = new SimpleDateFormat(datePattern);
                result.setLenient(false);
            }
        } else if (locale != null) {
            result = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            result.setLenient(false);
        } else {
            result = SimpleDateFormat.getDateInstance();
            result.setLenient(true);
        }
        return result;
    }

    @VisibleForTesting
    NumberFormat newNumberFormat() throws IllegalArgumentException {
        if (!numberPattern.isEmpty()) {
            if (locale != null) {
                return new DecimalFormat(numberPattern, new DecimalFormatSymbols(locale));
            } else {
                return new DecimalFormat(numberPattern);
            }
        } else if (locale != null) {
            return NumberFormat.getInstance(locale);
        } else {
            return NumberFormat.getInstance();
        }
    }

    private static final TemporalQuery[] TEMPORAL_QUERIES = {LocalDateTime::from, o -> LocalDate.from(o).atStartOfDay()};
    //</editor-fold>
}

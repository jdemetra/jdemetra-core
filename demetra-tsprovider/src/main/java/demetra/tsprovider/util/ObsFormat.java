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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import demetra.util.Parser;
import demetra.util.Formatter;
import internal.util.Strings;

/**
 * A special object that contains all information needed to format and parse
 * observations.
 *
 * @author Philippe Charles
 */
@Immutable
public final class ObsFormat {

    public static final ObsFormat DEFAULT = new ObsFormat(null, null, null);
    public static final String SEP = " ~ ";
    // PROPERTIES
    private final Locale locale;
    private final String datePattern;
    private final String numberPattern;

    /**
     * Creates a DataFormat from an optional locale, an optional date pattern
     * and an optional number pattern.
     *
     * @param locale an optional locale
     * @param datePattern an optional date pattern
     * @param numberPattern an optional number pattern
     * @see Locale
     * @see SimpleDateFormat
     * @see DecimalFormat
     */
    public ObsFormat(@Nullable Locale locale, @Nullable String datePattern, @Nullable String numberPattern) {
        this.locale = locale;
        this.datePattern = Strings.nullToEmpty(datePattern);
        this.numberPattern = Strings.nullToEmpty(numberPattern);
    }

    @Nonnull
    public String getNumberPattern() {
        return numberPattern;
    }

    @Nonnull
    public String getDatePattern() {
        return datePattern;
    }

    @Nullable
    public Locale getLocale() {
        return locale;
    }

    @Nonnull
    public String getLocaleString() {
        return locale != null ? locale.toString() : "";
    }

    @Override
    public String toString() {
        return getLocaleString() + SEP + datePattern + SEP + numberPattern;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ObsFormat && equals((ObsFormat) obj));
    }

    private boolean equals(ObsFormat that) {
        return Objects.equals(this.locale, that.locale)
                && this.datePattern.equals(that.datePattern)
                && this.numberPattern.equals(that.numberPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, datePattern, numberPattern);
    }

    //<editor-fold defaultstate="collapsed" desc="Parsers/Formatters">
    /**
     * Returns a date formatter from the current locale and date pattern. Note
     * that an invalid pattern returns a do-nothing formatter.
     *
     * @return a non-null formatter
     */
    @Nonnull
    public Formatter<Date> dateFormatter() {
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
    @Nonnull
    public Parser<Date> dateParser() {
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
    @Nonnull
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
    @Nonnull
    public Parser<Number> numberParser() {
        try {
            return Parser.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return Parser.onNull();
        }
    }

    /**
     * Creates a new {@link DateFormat} based on the current locale and date
     * pattern.
     *
     * @return a non-null {@link DateFormat}
     * @throws IllegalArgumentException if the date pattern is invalid
     * @see SimpleDateFormat
     */
    @Nonnull
    public DateFormat newDateFormat() throws IllegalArgumentException {
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

    /**
     * Creates a new {@link NumberFormat} based on the current locale and number
     * pattern.
     *
     * @return a non-null {@link NumberFormat}
     * @throws IllegalArgumentException if the number pattern is invalid
     * @see DecimalFormat
     * @see DecimalFormatSymbols
     */
    @Nonnull
    public NumberFormat newNumberFormat() throws IllegalArgumentException {
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
    //</editor-fold>

    /**
     * Creates a new DataFormat from a locale name, a date pattern and a number
     * pattern.
     *
     * @param locale an optional locale name
     * @param datePattern an optional date pattern
     * @param numberPattern an optional number pattern
     * @return a non-null DataFormat
     */
    @Nonnull
    public static ObsFormat create(@Nullable String locale, @Nullable String datePattern, @Nullable String numberPattern) {
        return new ObsFormat(locale != null ? Parser.onLocale().parse(locale) : null, datePattern, numberPattern);
    }
}

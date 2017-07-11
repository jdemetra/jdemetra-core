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
package ec.tss.tsproviders.utils;

import com.google.common.base.Strings;
import ec.tss.tsproviders.utils.Parsers.FailSafeParser;
import ec.tss.tsproviders.utils.Parsers.Parser;
import ec.tstoolkit.design.Immutable;
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

/**
 * A special object that contains all information needed to format and parse
 * observations.
 *
 * @author Philippe Charles
 */
@Immutable
public final class DataFormat {

    public static final DataFormat DEFAULT = new DataFormat(null, null, null);
    public static final String SEP = " ~ ";
    // PROPERTIES
    private final Locale locale;
    private final String datePattern;
    private final String numberPattern;

    /**
     *
     * @param locale
     * @param datePattern
     * @deprecated use
     * {@link #DataFormat(java.util.Locale, java.lang.String, java.lang.String)}
     * instead
     */
    @Deprecated
    public DataFormat(@Nullable Locale locale, @Nullable String datePattern) {
        this(locale, datePattern, null);
    }

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
    public DataFormat(@Nullable Locale locale, @Nullable String datePattern, @Nullable String numberPattern) {
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
        return this == obj || (obj instanceof DataFormat && equals((DataFormat) obj));
    }

    private boolean equals(DataFormat that) {
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
    public Formatters.Formatter<Date> dateFormatter() {
        try {
            return Formatters.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return FALLBACK_DATE_FORMATTER;
        }
    }

    /**
     * Returns a date parser from the current locale and date pattern. Note that
     * an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    @Nonnull
    public Parsers.Parser<Date> dateParser() {
        try {
            return Parsers.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return FALLBACK_DATE_PARSER;
        }
    }

    /**
     * Returns a number formatter from the current locale and number pattern.
     * Note that an invalid pattern returns a do-nothing formatter.
     *
     * @return a non-null formatter
     */
    @Nonnull
    public Formatters.Formatter<Number> numberFormatter() {
        try {
            return Formatters.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return FALLBACK_NUMBER_FORMATTER;
        }
    }

    /**
     * Returns a number parser from the current locale and number pattern.Note
     * that an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    @Nonnull
    public Parsers.Parser<Number> numberParser() {
        try {
            return Parsers.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return FALLBACK_NUMBER_PARSER;
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
     * Creates a new DataFormat from a locale name and a date pattern.
     *
     * @param locale an optional locale name
     * @param datePattern an optional date pattern
     * @return a non-null DataFormat
     * @deprecated use
     * {@link #create(java.lang.String, java.lang.String, java.lang.String)}
     * instead
     */
    @Deprecated
    @Nonnull
    public static DataFormat create(@Nullable String locale, @Nullable String datePattern) {
        return create(locale, datePattern, null);
    }

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
    public static DataFormat create(@Nullable String locale, @Nullable String datePattern, @Nullable String numberPattern) {
        return new DataFormat(locale != null ? LOCALE_PARSER.parseValue(locale).orElse(null) : null, datePattern, numberPattern);
    }

    @Nonnull
    public static Parser<Locale> localeParser() {
        return LOCALE_PARSER;
    }

    /**
     * <p>
     * Converts a String to a Locale.</p>
     *
     * <p>
     * This method takes the string format of a locale and creates the locale
     * object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>
     * (#) The behaviour of the JDK variant constructor changed between JDK1.3
     * and JDK1.4. In JDK1.3, the constructor upper cases the variant, in
     * JDK1.4, it doesn't. Thus, the result from getVariant() may vary depending
     * on your JDK.</p>
     *
     * <p>
     * This method validates the input strictly. The language code must be
     * lowercase. The country code must be uppercase. The separator must be an
     * underscore. The length must be correct. </p>
     *
     * @param str the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     * @see
     * http://www.java2s.com/Code/Java/Data-Type/ConvertsaStringtoaLocale.htm
     */
    @Nonnull
    public static Locale toLocale(@Nonnull String str) throws IllegalArgumentException {
        Objects.requireNonNull(str);
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (len == 2) {
            return new Locale(str, "");
        } else {
            if (str.charAt(2) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            char ch3 = str.charAt(3);
            if (ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if (str.charAt(5) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }

    @Deprecated
    @Nullable
    public String previewDate() {
        try {
            return newDateFormat().format(new Date());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     *
     * @param datePattern
     * @param locale
     * @return
     * @throws IllegalArgumentException
     * @deprecated use {@link #newDateFormat()} instead
     */
    @Deprecated
    @Nonnull
    public static DateFormat newDateFormat(@Nullable String datePattern, @Nullable Locale locale) throws IllegalArgumentException {
        return new DataFormat(locale, datePattern, null).newDateFormat();
    }

    /**
     *
     * @param locale
     * @return
     * @deprecated use {@link #newNumberFormat()} instead
     */
    @Deprecated
    @Nonnull
    public static NumberFormat newNumberFormat(@Nullable Locale locale) {
        return new DataFormat(locale, null, null).newNumberFormat();
    }
    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final Parsers.Parser<Locale> LOCALE_PARSER = new FailSafeParser<Locale>() {
        @Override
        protected Locale doParse(CharSequence input) throws Exception {
            return toLocale(input.toString());
        }
    };
    private static final Parsers.Parser<Date> FALLBACK_DATE_PARSER = Parsers.ofInstance(null);
    private static final Formatters.Formatter<Date> FALLBACK_DATE_FORMATTER = Formatters.ofInstance(null);
    private static final Parsers.Parser<Number> FALLBACK_NUMBER_PARSER = Parsers.ofInstance(null);
    private static final Formatters.Formatter<Number> FALLBACK_NUMBER_FORMATTER = Formatters.ofInstance(null);
    //</editor-fold>
}

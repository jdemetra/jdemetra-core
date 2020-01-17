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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A special object that contains all information needed to format and parse
 * observations.
 *
 * @author Philippe Charles
 */
@Immutable
public final class DataFormat {

    public static final DataFormat DEFAULT = of(null, null, null);
    public static final DataFormat ROOT = of(Locale.ROOT, null, null);

    /**
     * Creates an DataFormat from an optional locale, an optional date pattern
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
    public static DataFormat of(@Nullable Locale locale, @Nullable String datePattern, @Nullable String numberPattern) {
        return new DataFormat(locale, Strings.nullToEmpty(datePattern), Strings.nullToEmpty(numberPattern), null);
    }

    public static final String SEP = " ~ ";

    @Nullable
    private final Locale locale;

    @NonNull
    private final String datePattern;

    @NonNull
    private final String numberPattern;

    private DataFormat(Locale locale, String datePattern, String numberPattern, Void fake) {
        this.locale = locale;
        this.datePattern = datePattern;
        this.numberPattern = numberPattern;
    }

    @NonNull
    public String getNumberPattern() {
        return numberPattern;
    }

    @NonNull
    public String getDatePattern() {
        return datePattern;
    }

    @Nullable
    public Locale getLocale() {
        return locale;
    }

    @NonNull
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
    public Formatters.@NonNull Formatter<Date> dateFormatter() {
        try {
            return Formatters.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return Formatters.onNull();
        }
    }

    /**
     * Returns a date parser from the current locale and date pattern. Note that
     * an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    public Parsers.@NonNull Parser<Date> dateParser() {
        try {
            return Parsers.onDateFormat(newDateFormat());
        } catch (IllegalArgumentException ex) {
            return Parsers.onNull();
        }
    }

    /**
     * Returns a number formatter from the current locale and number pattern.
     * Note that an invalid pattern returns a do-nothing formatter.
     *
     * @return a non-null formatter
     */
    public Formatters.@NonNull Formatter<Number> numberFormatter() {
        try {
            return Formatters.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return Formatters.onNull();
        }
    }

    /**
     * Returns a number parser from the current locale and number pattern.Note
     * that an invalid pattern returns a do-nothing parser.
     *
     * @return a non-null parser
     */
    public Parsers.@NonNull Parser<Number> numberParser() {
        try {
            return Parsers.onNumberFormat(newNumberFormat());
        } catch (IllegalArgumentException ex) {
            return Parsers.onNull();
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
    @NonNull
    public DateFormat newDateFormat() throws IllegalArgumentException {
        DateFormat result = !datePattern.isEmpty()
                ? new SimpleDateFormat(datePattern, getLocaleOrDefault())
                : SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, getLocaleOrDefault());
        result.setLenient(datePattern.isEmpty() && locale == null);
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
    @NonNull
    public NumberFormat newNumberFormat() throws IllegalArgumentException {
        NumberFormat result = !numberPattern.isEmpty()
                ? new DecimalFormat(numberPattern, DecimalFormatSymbols.getInstance(getLocaleOrDefault()))
                : NumberFormat.getInstance(getLocaleOrDefault());
        return result;
    }
    //</editor-fold>

    private Locale getLocaleOrDefault() {
        return locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT);
    }

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
        this(locale, datePattern, "", null);
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
     * @deprecated use
     * {@link #of(java.util.Locale, java.lang.String, java.lang.String)} instead
     */
    @Deprecated
    public DataFormat(@Nullable Locale locale, @Nullable String datePattern, @Nullable String numberPattern) {
        this(locale, Strings.nullToEmpty(datePattern), Strings.nullToEmpty(numberPattern), null);
    }

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
    @NonNull
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
     * @deprecated use {@link #of(Locale, java.lang.String, java.lang.String)}
     * instead
     */
    @Deprecated
    @NonNull
    public static DataFormat create(@Nullable String locale, @Nullable String datePattern, @Nullable String numberPattern) {
        return of(locale != null ? Parsers.localeParser().parseValue(locale).orElse(null) : null, datePattern, numberPattern);
    }

    @Deprecated
    @NonNull
    public static Parser<Locale> localeParser() {
        return Parsers.localeParser();
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
     * @deprecated use {@link Parsers#localeParser()} instead
     */
    @Deprecated
    @NonNull
    public static Locale toLocale(@NonNull String str) throws IllegalArgumentException {
        Objects.requireNonNull(str);
        Locale result = Parsers.localeParser().parse(str);
        if (result == null) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        return result;
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
    @NonNull
    public static DateFormat newDateFormat(@Nullable String datePattern, @Nullable Locale locale) throws IllegalArgumentException {
        return of(locale, datePattern, null).newDateFormat();
    }

    /**
     *
     * @param locale
     * @return
     * @deprecated use {@link #newNumberFormat()} instead
     */
    @Deprecated
    @NonNull
    public static NumberFormat newNumberFormat(@Nullable Locale locale) {
        return of(locale, null, null).newNumberFormat();
    }
}

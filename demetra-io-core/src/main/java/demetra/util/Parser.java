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
package demetra.util;

import internal.util.InternalParser;
import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a class that creates an object from a {@link CharSequence}.<br> For
 * example, you could use it to parse a String into a Date. Note that it can
 * also be used to convert a String to a new one.<br> The parser must not throw
 * Exceptions; it must swallow it and return {@code null}. This means that
 * {@code null} is not considered has a value (same as Collection API). To
 * create a "null value" from a parser, you should use the NullObject pattern.
 *
 * @author Philippe Charles
 * @param <T> The type of the object to be created
 * @see IFormatter
 * @since 1.0.0
 */
@FunctionalInterface
public interface Parser<T> {

    /**
     * Parse a CharSequence to create an object.
     *
     * @param input the input used to create the object
     * @return a new object if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     */
    @Nullable
    T parse(@Nonnull CharSequence input);

    /**
     * Returns an {@link Optional} containing the object that has bean created
     * by the parsing if this parsing was possible.<p>
     * Use this instead of {@link #parse(java.lang.CharSequence)} to increase
     * readability and prevent NullPointerExceptions.
     *
     * @param input the input used to create the object
     * @return a never-null {@link Optional}
     * @throws NullPointerException if input is null
     * @since 2.2.0
     */
    @Nonnull
    default Optional<T> parseValue(@Nonnull CharSequence input) {
        return Optional.ofNullable(parse(input));
    }

    /**
     *
     * @param other
     * @return
     * @since 2.2.0
     */
    @Nonnull
    @SuppressWarnings("null")
    default Parser<T> orElse(@Nonnull Parser<T> other) {
        Objects.requireNonNull(other);
        return o -> {
            T result = parse(o);
            return result != null ? result : other.parse(o);
        };
    }

    @Nonnull
    @SuppressWarnings("null")
    default <X> Parser<X> andThen(@Nonnull Function<? super T, ? extends X> after) {
        Objects.requireNonNull(after);
        return o -> {
            T tmp = parse(o);
            return tmp != null ? after.apply(tmp) : null;
        };
    }

    @Nonnull
    static <T> Parser<T> onDateTimeFormatter(@Nonnull DateTimeFormatter formatter, TemporalQuery<T>... queries) {
        Objects.requireNonNull(formatter);
        Objects.requireNonNull(queries);
        return o -> InternalParser.parseTemporalAccessor(formatter, queries, o);
    }

    @Nonnull
    static Parser<Date> onStrictDatePattern(@Nonnull String datePattern, @Nonnull Locale locale) {
        return new InternalParser.StrictDatePatternParser(datePattern, locale);
    }

    @Nonnull
    static Parser<Date> onDateFormat(@Nonnull DateFormat dateFormat) {
        return o -> InternalParser.parseDate(dateFormat, o);
    }

    @Nonnull
    static Parser<Number> onNumberFormat(@Nonnull NumberFormat numberFormat) {
        return o -> InternalParser.parseNumber(numberFormat, o);
    }

    @Nonnull
    static <T> Parser<T> onConstant(@Nullable T instance) {
        return o -> InternalParser.parseConstant(instance, o);
    }

    @Nonnull
    static <T> Parser<T> onNull() {
        return InternalParser::parseNull;
    }

    @Nonnull
    static Parser<File> onFile() {
        return InternalParser::parseFile;
    }

    /**
     * Create a {@link Parser} that delegates its parsing to
     * {@link Integer#valueOf(java.lang.String)}.
     *
     * @return a non-null parser
     */
    @Nonnull
    static Parser<Integer> onInteger() {
        return InternalParser::parseInteger;
    }

    @Nonnull
    static Parser<Long> onLong() {
        return InternalParser::parseLong;
    }

    @Nonnull
    static Parser<Boolean> onBoolean() {
        return InternalParser::parseBoolean;
    }

    @Nonnull
    static Parser<Character> onCharacter() {
        return InternalParser::parseCharacter;
    }

    /**
     * Create a {@link Parser} that delegates its parsing to
     * {@link Double#valueOf(java.lang.String)}.
     *
     * @return a non-null parser
     */
    @Nonnull
    static Parser<Double> onDouble() {
        return InternalParser::parseDouble;
    }

    @Nonnull
    static Parser<Charset> onCharset() {
        return InternalParser::parseCharset;
    }

    @Nonnull
    static <T extends Enum<T>> Parser<T> onEnum(@Nonnull Class<T> enumClass) {
        return o -> InternalParser.parseEnum(enumClass, o);
    }

    @Nonnull
    static Parser<String> onString() {
        return Object::toString;
    }

    @Nonnull
    static Parser<double[]> onDoubleArray() {
        return InternalParser::parseDoubleArray;
    }

    @Nonnull
    static Parser<String[]> onStringArray() {
        return InternalParser::parseStringArray;
    }

    @Nonnull
    static Parser<List<String>> onStringList(@Nonnull Function<CharSequence, Stream<String>> splitter) {
        return o -> InternalParser.parseStringList(splitter, o);
    }

    @Nonnull
    static Parser<Locale> onLocale() {
        return InternalParser::parseLocale;
    }
}

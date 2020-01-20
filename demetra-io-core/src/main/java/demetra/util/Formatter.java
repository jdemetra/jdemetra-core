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

import internal.util.InternalFormatter;
import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Defines a class that creates a {@link CharSequence} from an object.<br> For
 * example, you could use it to format a Date into a String. Note that it can
 * also be used to convert a String to a new one.<br> The formatter must not
 * throw Exceptions; it must swallow it and return {@code null}. This means that
 * {@code null} is not considered has a value (same as Collection API). To
 * create a "null value" from a formatter, you should use the NullObject
 * pattern.
 *
 * @author Philippe Charles
 * @param <T> The type of the object to be formatted
 * @see Parser
 */
@FunctionalInterface
public interface Formatter<T> {

    /**
     * Format an object into a CharSequence.
     *
     * @param value the input used to create the CharSequence
     * @return a new CharSequence if possible, {@code null} otherwise
     */
    @Nullable
    CharSequence format(@Nullable T value);

    /**
     * Format an object into a String.
     *
     * @param value the input used to create the String
     * @return a new String if possible, {@code null} otherwise
     */
    @Nullable
    default String formatAsString(@Nullable T value) {
        CharSequence result = format(value);
        return result != null ? result.toString() : null;
    }

    /**
     * Returns an {@link Optional} containing the CharSequence that has bean
     * created by the formatting if this formatting was possible.<p>
     * Use this instead of {@link #format(java.lang.Object)} to increase
     * readability and prevent NullPointerExceptions.
     *
     * @param value the input used to create the CharSequence
     * @return a never-null {@link Optional}
     */
    @NonNull
    default Optional<CharSequence> formatValue(@Nullable T value) {
        return Optional.ofNullable(format(value));
    }

    /**
     * Returns an {@link Optional} containing the String that has bean created
     * by the formatting if this formatting was possible.<p>
     * Use this instead of {@link #format(java.lang.Object)} to increase
     * readability and prevent NullPointerExceptions.
     *
     * @param value the input used to create the String
     * @return a never-null {@link Optional}
     */
    @NonNull
    default Optional<String> formatValueAsString(@Nullable T value) {
        return Optional.ofNullable(formatAsString(value));
    }

    /**
     * Returns a formatter that applies a function on the input value before
     * formatting its result.
     *
     * @param <Y>
     * @param before
     * @return a never-null formatter
     */
    @NonNull
    default <Y> Formatter<Y> compose(@NonNull Function<? super Y, ? extends T> before) {
        Objects.requireNonNull(before);
        return o -> format(before.apply(o));
    }

    @NonNull
    static <T extends TemporalAccessor> Formatter<T> onDateTimeFormatter(@NonNull DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter);
        return o -> InternalFormatter.formatTemporalAccessor(formatter, o);
    }

    @NonNull
    static Formatter<Date> onDateFormat(@NonNull DateFormat dateFormat) {
        Objects.requireNonNull(dateFormat);
        return o -> InternalFormatter.formatDate(dateFormat, o);
    }

    @NonNull
    static Formatter<Number> onNumberFormat(@NonNull NumberFormat numberFormat) {
        Objects.requireNonNull(numberFormat);
        return o -> InternalFormatter.formatNumber(numberFormat, o);
    }

    @NonNull
    static <T> Formatter<T> onConstant(@Nullable CharSequence instance) {
        return o -> InternalFormatter.formatConstant(instance, o);
    }

    @NonNull
    static <T> Formatter<T> onNull() {
        return InternalFormatter::formatNull;
    }

    @NonNull
    static Formatter<File> onFile() {
        return InternalFormatter::formatFile;
    }

    @NonNull
    static Formatter<Integer> onInteger() {
        return InternalFormatter::formatInteger;
    }

    @NonNull
    static Formatter<Long> onLong() {
        return InternalFormatter::formatLong;
    }

    @NonNull
    static Formatter<Double> onDouble() {
        return InternalFormatter::formatDouble;
    }

    @NonNull
    static Formatter<Boolean> onBoolean() {
        return InternalFormatter::formatBoolean;
    }

    @NonNull
    static Formatter<Character> onCharacter() {
        return InternalFormatter::formatCharacter;
    }

    @NonNull
    static Formatter<Charset> onCharset() {
        return InternalFormatter::formatCharset;
    }

    @NonNull
    static <T extends Enum<T>> Formatter<T> onEnum() {
        return InternalFormatter::formatEnum;
    }

    @NonNull
    static Formatter<String> onString() {
        return InternalFormatter::formatString;
    }

    @NonNull
    static Formatter<Object> onObjectToString() {
        return InternalFormatter::formatObjectToString;
    }

    @NonNull
    static Formatter<double[]> onDoubleArray() {
        return InternalFormatter::formatDoubleArray;
    }

    @NonNull
    static Formatter<String[]> onStringArray() {
        return InternalFormatter::formatStringArray;
    }

    @NonNull
    static Formatter<List<String>> onStringList(@NonNull Function<Stream<CharSequence>, String> joiner) {
        Objects.requireNonNull(joiner);
        return o -> InternalFormatter.formatStringList(joiner, o);
    }
}

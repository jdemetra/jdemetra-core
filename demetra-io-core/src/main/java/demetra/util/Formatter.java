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
 * example, you could use it to format a into a String. Note that it can also be
 * used to convert a String to a new one.<br> The formatter must not throw
 * Exceptions; it must swallow it and return {@code null}. This means that
 * {@code null} is not considered has a value (same as Collection API). To
 * create a "null value" from a formatter, you should use the NullObject
 * pattern.
 *
 * @author Philippe Charles
 * @param <T> The type of the object to be formatted
 * @see IParser
 * @since 1.0.0
 */
@FunctionalInterface
public interface Formatter<T> {

    /**
     * Format an object into a CharSequence.
     *
     * @param value the input used to create the CharSequence
     * @return a new CharSequence if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     */
    @Nullable
    CharSequence format(@NonNull T value);

    /**
     * Format an object into a String.
     *
     * @param value the non-null input used to create the String
     * @return a new String if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     * @since 2.2.0
     */
    @Nullable
    default String formatAsString(@NonNull T value) {
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
     * @throws NullPointerException if input is null
     * @since 2.2.0
     */
    @NonNull
    default Optional<CharSequence> formatValue(@NonNull T value) {
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
     * @throws NullPointerException if input is null
     * @since 2.2.0
     */
    @NonNull
    default Optional<String> formatValueAsString(@NonNull T value) {
        return Optional.ofNullable(formatAsString(value));
    }

    /**
     * Returns a formatter that applies a function on the input value before
     * formatting its result.
     *
     * @param <Y>
     * @param before
     * @return a never-null formatter
     * @since 2.2.0
     */
    @NonNull
    @SuppressWarnings("null")
    default <Y> Formatter<Y> compose(@NonNull Function<? super Y, ? extends T> before) {
        return o -> {
            T tmp = before.apply(o);
            return tmp != null ? format(tmp) : null;
        };
    }

    @NonNull
    static <T extends TemporalAccessor> Formatter<T> onDateTimeFormatter(@NonNull DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter);
        return o -> InternalFormatter.formatTemporalAccessor(formatter, o);
    }

    @NonNull
    static Formatter<Date> onDateFormat(@NonNull DateFormat dateFormat) {
        return dateFormat::format;
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
        return o -> InternalFormatter.formatNull(o);
    }

    @NonNull
    static Formatter<File> onFile() {
        return File::getPath;
    }

    @NonNull
    static Formatter<Integer> onInteger() {
        return Object::toString;
    }

    @NonNull
    static Formatter<Long> onLong() {
        return Object::toString;
    }

    @NonNull
    static Formatter<Double> onDouble() {
        return Object::toString;
    }

    @NonNull
    static Formatter<Boolean> onBoolean() {
        return Object::toString;
    }

    @NonNull
    static Formatter<Character> onCharacter() {
        return Object::toString;
    }

    @NonNull
    static Formatter<Charset> onCharset() {
        return Charset::name;
    }

    @NonNull
    static <T extends Enum<T>> Formatter<T> onEnum() {
        return Enum::name;
    }

    @NonNull
    static Formatter<String> onString() {
        return Object::toString;
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
    static Formatter<Object> onObjectToString() {
        return Object::toString;
    }

    @NonNull
    static Formatter<List<String>> onStringList(@NonNull Function<Stream<CharSequence>, String> joiner) {
        return o -> InternalFormatter.formatStringList(joiner, o);
    }
}

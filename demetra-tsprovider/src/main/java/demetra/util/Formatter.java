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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
    CharSequence format(@Nonnull T value);

    /**
     * Format an object into a String.
     *
     * @param value the non-null input used to create the String
     * @return a new String if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     * @since 2.2.0
     */
    @Nullable
    default String formatAsString(@Nonnull T value) {
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
    @Nonnull
    default Optional<CharSequence> formatValue(@Nonnull T value) {
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
    @Nonnull
    default Optional<String> formatValueAsString(@Nonnull T value) {
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
    @Nonnull
    @SuppressWarnings("null")
    default <Y> Formatter<Y> compose(@Nonnull Function<? super Y, ? extends T> before) {
        return o -> {
            T tmp = before.apply(o);
            return tmp != null ? format(tmp) : null;
        };
    }

    /**
     * Creates a new formatter using {@link JAXBContext#newInstance(java.lang.Class[])
     * }.
     * <p>
     * Note that "<i>{@link JAXBContext} is thread-safe and should only be
     * created once and reused to avoid the cost of initializing the metadata
     * multiple times. {@link Marshaller} and {@link Unmarshaller} are not
     * thread-safe, but are lightweight to create and could be created per
     * operation (<a
     * href="http://stackoverflow.com/a/7400735">http://stackoverflow.com/a/7400735</a>)".</i>
     *
     * @param <T>
     * @param classToBeFormatted
     * @param formattedOutput
     * @return
     */
    @Nonnull
    static <T> Formatter<T> onJAXB(@Nonnull Class<T> classToBeFormatted, boolean formattedOutput) {
        return onJAXB(InternalFormatter.newMarshaller(classToBeFormatted, formattedOutput));
    }

    @Nonnull
    static <T> Formatter<T> onJAXB(@Nonnull JAXBContext context, boolean formattedOutput) {
        return onJAXB(InternalFormatter.newMarshaller(context, formattedOutput));
    }

    @Nonnull
    static <T> Formatter<T> onJAXB(@Nonnull Marshaller marshaller) {
        return o -> InternalFormatter.marshal(marshaller, o);
    }

    @Nonnull
    static <T extends TemporalAccessor> Formatter<T> onDateTimeFormatter(@Nonnull DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter);
        return o -> InternalFormatter.formatTemporalAccessor(formatter, o);
    }

    @Nonnull
    static Formatter<Date> onDateFormat(@Nonnull DateFormat dateFormat) {
        return dateFormat::format;
    }

    @Nonnull
    static Formatter<Number> onNumberFormat(@Nonnull NumberFormat numberFormat) {
        Objects.requireNonNull(numberFormat);
        return o -> InternalFormatter.formatNumber(numberFormat, o);
    }

    @Nonnull
    static <T> Formatter<T> onConstant(@Nullable CharSequence instance) {
        return o -> InternalFormatter.formatConstant(instance, o);
    }

    @Nonnull
    static <T> Formatter<T> onNull() {
        return o -> InternalFormatter.formatNull(o);
    }

    @Nonnull
    static Formatter<File> onFile() {
        return File::getPath;
    }

    @Nonnull
    static Formatter<Integer> onInteger() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<Long> onLong() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<Double> onDouble() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<Boolean> onBoolean() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<Character> onCharacter() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<Charset> onCharset() {
        return Charset::name;
    }

    @Nonnull
    static <T extends Enum<T>> Formatter<T> onEnum() {
        return Enum::name;
    }

    @Nonnull
    static Formatter<String> onString() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<double[]> onDoubleArray() {
        return InternalFormatter::formatDoubleArray;
    }

    @Nonnull
    static Formatter<String[]> onStringArray() {
        return InternalFormatter::formatStringArray;
    }

    @Nonnull
    static Formatter<Object> onObjectToString() {
        return Object::toString;
    }

    @Nonnull
    static Formatter<List<String>> onStringList(@Nonnull Function<Stream<CharSequence>, String> joiner) {
        return o -> InternalFormatter.formatStringList(joiner, o);
    }
}

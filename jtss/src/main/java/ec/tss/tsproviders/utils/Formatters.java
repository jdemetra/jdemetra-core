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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import ec.tstoolkit.design.UtilityClass;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Provides utility methods for the {@link IFormatter} class.
 *
 * @author Philippe Charles
 */
@UtilityClass(IFormatter.class)
public final class Formatters {

    private Formatters() {
        // static class
    }

    @Nullable
    public static <T> CharSequence formatFirstNotNull(@Nonnull T value, @Nonnull Iterable<? extends IFormatter<T>> formatters) {
        Objects.requireNonNull(value); // if formatters is empty
        for (IFormatter<T> o : formatters) {
            CharSequence result = o.format(value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Nonnull
    public static <T> Formatter<T> firstNotNull(@Nonnull IFormatter<T>... formatters) {
        return firstNotNull(ImmutableList.copyOf(formatters));
    }

    @Nonnull
    public static <T> Formatter<T> firstNotNull(@Nonnull ImmutableList<? extends IFormatter<T>> formatters) {
        return new Wrapper<>(o -> formatFirstNotNull(o, formatters));
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
    public static <T> Formatter<T> onJAXB(@Nonnull Class<T> classToBeFormatted, boolean formattedOutput) {
        try {
            return onJAXB(JAXBContext.newInstance(classToBeFormatted), formattedOutput);
        } catch (JAXBException ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Nonnull
    public static <T> Formatter<T> onJAXB(@Nonnull JAXBContext context, boolean formattedOutput) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
            return onJAXB(marshaller);
        } catch (JAXBException ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Nonnull
    public static <T> Formatter<T> onJAXB(@Nonnull Marshaller marshaller) {
        return new FailSafeFormatter<T>() {
            @Override
            protected CharSequence doFormat(T value) throws Exception {
                StringWriter result = new StringWriter();
                marshaller.marshal(value, result);
                return result.toString();
            }
        };
    }

    @Nonnull
    public static Formatter<Date> onDateFormat(@Nonnull DateFormat dateFormat) {
        return new Wrapper<>(dateFormat::format);
    }

    @Nonnull
    public static Formatter<Number> onNumberFormat(@Nonnull NumberFormat numberFormat) {
        return new Wrapper<>(numberFormat::format);
    }

    @Nonnull
    @SuppressWarnings("null")
    public static <T> Formatter<T> ofInstance(@Nullable CharSequence instance) {
        return new Wrapper<>(o -> {
            Objects.requireNonNull(o);
            return instance;
        });
    }

    @Nonnull
    public static Formatter<File> fileFormatter() {
        return FILE_FORMATTER;
    }

    @Nonnull
    public static Formatter<Integer> intFormatter() {
        return (Formatter<Integer>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<Long> longFormatter() {
        return (Formatter<Long>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<Double> doubleFormatter() {
        return (Formatter<Double>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<Boolean> boolFormatter() {
        return (Formatter<Boolean>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<Character> charFormatter() {
        return (Formatter<Character>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<Charset> charsetFormatter() {
        return CHARSET_FORMATTER;
    }

    @Nonnull
    public static <T extends Enum<T>> Formatter<T> enumFormatter() {
        return (Formatter<T>) ENUM_NAME_FORMATTER;
    }

    @Nonnull
    public static Formatter<String> stringFormatter() {
        return (Formatter<String>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<double[]> doubleArrayFormatter() {
        return DOUBLE_ARRAY_FORMATTER;
    }

    @Nonnull
    public static Formatter<String[]> stringArrayFormatter() {
        return STRING_ARRAY_FORMATTER;
    }

    @Nonnull
    @SuppressWarnings("null")
    public static <X, Y> Formatter<Y> compose(@Nonnull IFormatter<X> formatter, @Nonnull Function<? super Y, ? extends X> before) {
        return new Wrapper<>(o -> {
            X tmp = before.apply(o);
            return tmp != null ? formatter.format(tmp) : null;
        });
    }

    @Nonnull
    public static Formatter<Object> usingToString() {
        return (Formatter<Object>) OBJECT_TO_STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<List<String>> onJoiner(@Nonnull Joiner joiner) {
        return new FailSafeFormatter<List<String>>() {
            @Override
            protected CharSequence doFormat(List<String> value) throws Exception {
                return joiner.join(value);
            }
        };
    }

    /**
     *
     * @param <T>
     * @param formatter
     * @return
     * @since 2.2.0
     */
    @Nonnull
    public static <T> Formatter<T> wrap(@Nonnull IFormatter<T> formatter) {
        return formatter instanceof Formatter ? (Formatter<T>) formatter : new Wrapper(Objects.requireNonNull(formatter));
    }

    /**
     * An abstract formatter that contains convenient methods.
     *
     * @param <T>
     */
    public static abstract class Formatter<T> implements IFormatter<T> {

        /**
         * Returns an {@link Optional} containing the CharSequence that has bean
         * created by the formatting if this formatting was possible.<p>
         * Use this instead of {@link #format(java.lang.Object)} to increase
         * readability and prevent NullPointerExceptions.
         *
         * @param value the input used to create the CharSequence
         * @return a never-null {@link Optional}
         * @throws NullPointerException if input is null
         * @deprecated use {@link #formatValue(java.lang.Object)} instead
         */
        @Deprecated
        @Nonnull
        public Optional<CharSequence> tryFormat(@Nonnull T value) throws NullPointerException {
            return Optional.fromNullable(format(value));
        }

        /**
         * Returns a formatter that applies a function on the input value before
         * formatting its result.
         *
         * @param <Y>
         * @param before
         * @return a never-null formatter instead
         */
        @Override
        @Nonnull
        public <Y> Formatter<Y> compose(@Nonnull Function<? super Y, ? extends T> before) {
            return Formatters.<T, Y>compose(this, before);
        }

        /**
         * Returns an {@link Optional} containing the String that has bean
         * created by the formatting if this formatting was possible.<p>
         * Use this instead of {@link #format(java.lang.Object)} to increase
         * readability and prevent NullPointerExceptions.
         *
         * @param value the input used to create the String
         * @return a never-null {@link Optional}
         * @throws NullPointerException if input is null
         * @deprecated use {@link #formatValueAsString(java.lang.Object)}
         * instead
         */
        @Deprecated
        @Nonnull
        public Optional<String> tryFormatAsString(@Nonnull T value) throws NullPointerException {
            return Optional.fromNullable(formatAsString(value));
        }
    }

    /**
     * An abstract formatter that swallows any exception thrown and returns
     * <code>null</code> instead.
     *
     * @param <T>
     */
    public static abstract class FailSafeFormatter<T> extends Formatter<T> {

        @Override
        public CharSequence format(T value) throws NullPointerException {
            Objects.requireNonNull(value);
            try {
                return doFormat(value);
            } catch (Exception ex) {
                return null;
            }
        }

        @Nullable
        abstract protected CharSequence doFormat(@Nonnull T value) throws Exception;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class Wrapper<T> extends Formatter<T> {

        private final IFormatter<T> formatter;

        private Wrapper(IFormatter<T> formatter) {
            this.formatter = formatter;
        }

        @Override
        public CharSequence format(T value) {
            return formatter.format(value);
        }

        @Override
        public String formatAsString(T value) {
            return formatter.formatAsString(value);
        }

        @Override
        public java.util.Optional<CharSequence> formatValue(T value) {
            return formatter.formatValue(value);
        }

        @Override
        public java.util.Optional<String> formatValueAsString(T value) {
            return formatter.formatValueAsString(value);
        }
    }

    private static final Formatter<File> FILE_FORMATTER = new Wrapper<>(File::getPath);
    private static final Formatter<Charset> CHARSET_FORMATTER = new Wrapper<>(Charset::name);
    private static final Formatter<double[]> DOUBLE_ARRAY_FORMATTER = new Wrapper<>(o -> Arrays.toString(Objects.requireNonNull(o)));
    private static final Formatter<?> OBJECT_TO_STRING_FORMATTER = new Wrapper<>(Object::toString);
    private static final Formatter<? extends Enum<?>> ENUM_NAME_FORMATTER = new Wrapper<>(Enum::name);
    private static final Formatter<String[]> STRING_ARRAY_FORMATTER = new Wrapper<>(o -> Arrays.toString(Objects.requireNonNull(o)));

    //</editor-fold>
}

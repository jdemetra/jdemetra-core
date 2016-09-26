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

import com.google.common.base.Function;
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
    public static <T> CharSequence formatFirstNotNull(@Nonnull T value, @Nonnull Iterable<? extends IFormatter<T>> formatters) throws NullPointerException {
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
    public static <T> Formatter<T> firstNotNull(@Nonnull final ImmutableList<? extends IFormatter<T>> formatters) {
        return new Formatter<T>() {
            @Override
            public CharSequence format(T value) throws NullPointerException {
                return formatFirstNotNull(value, formatters);
            }
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
    public static <T> Formatter<T> onJAXB(@Nonnull final Marshaller marshaller) {
        return new FailSafeFormatter<T>() {
            @Override
            protected CharSequence doFormat(T value) throws Exception {
                StringWriter writer = new StringWriter();
                marshaller.marshal(value, writer);
                return writer.toString();
            }
        };
    }

    @Nonnull
    public static Formatter<Date> onDateFormat(@Nonnull final DateFormat dateFormat) {
        return new Formatter<Date>() {
            @Override
            public CharSequence format(Date value) throws NullPointerException {
                return dateFormat.format(value);
            }
        };
    }

    @Nonnull
    public static Formatter<Number> onNumberFormat(@Nonnull final NumberFormat numberFormat) {
        return new Formatter<Number>() {
            @Override
            public CharSequence format(Number value) throws NullPointerException {
                return numberFormat.format(value);
            }
        };
    }

    @Nonnull
    public static <T> Formatter<T> ofInstance(@Nullable final CharSequence instance) {
        return new Formatter<T>() {
            @Override
            public CharSequence format(T value) throws NullPointerException {
                Objects.requireNonNull(value);
                return instance;
            }
        };
    }

    @Nonnull
    public static Formatter<File> fileFormatter() {
        return FILE_FORMATTER;
    }

    @Nonnull
    public static Formatter<Integer> intFormatter() {
        return INT_FORMATTER;
    }

    @Nonnull
    public static Formatter<Long> longFormatter() {
        return LONG_FORMATTER;
    }

    @Nonnull
    public static Formatter<Double> doubleFormatter() {
        return DOUBLE_FORMATTER;
    }

    @Nonnull
    public static Formatter<Boolean> boolFormatter() {
        return BOOL_FORMATTER;
    }

    @Nonnull
    public static Formatter<Charset> charsetFormatter() {
        return CHARSET_FORMATTER;
    }

    @Nonnull
    public static <T extends Enum<T>> Formatter<T> enumFormatter() {
        return new Formatter<T>() {
            @Override
            public CharSequence format(T value) throws NullPointerException {
                return value.name();
            }
        };
    }

    @Nonnull
    public static Formatter<String> stringFormatter() {
        return STRING_FORMATTER;
    }

    @Nonnull
    public static Formatter<double[]> doubleArrayFormatter() {
        return DOUBLE_ARRAY_FORMATTER;
    }

    @Nonnull
    public static <X, Y> Formatter<Y> compose(@Nonnull final IFormatter<X> formatter, @Nonnull final Function<Y, X> func) {
        return new Formatter<Y>() {
            @Override
            public CharSequence format(Y value) throws NullPointerException {
                X tmp = func.apply(value);
                return tmp != null ? formatter.format(tmp) : null;
            }
        };
    }

    @Nonnull
    public static Formatter<Object> usingToString() {
        return TO_STRING_FORMATTER;
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
         */
        @Nonnull
        public Optional<CharSequence> tryFormat(@Nonnull T value) throws NullPointerException {
            return Optional.fromNullable(format(value));
        }

        /**
         * Returns a formatter that applies a function on the input value before
         * formatting its result.
         *
         * @param <Y>
         * @param func
         * @return a never-null formatter
         */
        @Nonnull
        public <Y> Formatter<Y> compose(@Nonnull Function<Y, T> func) {
            return Formatters.<T, Y>compose(this, func);
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
         */
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
    private static final Formatter<File> FILE_FORMATTER = new Formatter<File>() {
        @Override
        public CharSequence format(File value) throws NullPointerException {
            return value.getPath();
        }
    };
    private static final Formatter<Integer> INT_FORMATTER = new Formatter<Integer>() {
        @Override
        public CharSequence format(Integer value) throws NullPointerException {
            return value.toString();
        }
    };
    private static final Formatter<Long> LONG_FORMATTER = new Formatter<Long>() {
        @Override
        public CharSequence format(Long value) throws NullPointerException {
            return value.toString();
        }
    };
    private static final Formatter<Double> DOUBLE_FORMATTER = new Formatter<Double>() {
        @Override
        public CharSequence format(Double value) throws NullPointerException {
            return value.toString();
        }
    };
    private static final Formatter<Boolean> BOOL_FORMATTER = new Formatter<Boolean>() {
        @Override
        public CharSequence format(Boolean value) throws NullPointerException {
            return value.toString();
        }
    };
    private static final Formatter<Charset> CHARSET_FORMATTER = new Formatter<Charset>() {
        @Override
        public CharSequence format(Charset value) throws NullPointerException {
            return value.name();
        }
    };
    private static final Formatter<String> STRING_FORMATTER = new Formatter<String>() {
        @Override
        public CharSequence format(String value) throws NullPointerException {
            return Objects.requireNonNull(value);
        }
    };
    private static final Formatter<double[]> DOUBLE_ARRAY_FORMATTER = new Formatter<double[]>() {
        @Override
        public CharSequence format(double[] value) throws NullPointerException {
            return Arrays.toString(Objects.requireNonNull(value));
        }
    };
    private static final Formatter<Object> TO_STRING_FORMATTER = new Formatter<Object>() {
        @Override
        public CharSequence format(Object value) throws NullPointerException {
            return value.toString();
        }
    };
    //</editor-fold>
}

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

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import ec.tstoolkit.design.UtilityClass;
import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass(IParser.class)
public final class Parsers {

    private Parsers() {
        // static class
    }

    @Nullable
    public static <T> T parseFirstNotNull(@NonNull CharSequence input, @NonNull Iterable<? extends IParser<T>> parsers) {
        Objects.requireNonNull(input); // if parsers is empty
        for (IParser<T> o : parsers) {
            T result = o.parse(input);
            if (result != null) {
                return result;
            }
        }
        return null;

    }

    /**
     *
     * @param <T>
     * @param parsers
     * @return
     * @see Parsers#using(java.lang.Iterable)
     */
    @NonNull
    public static <T> Parser<T> firstNotNull(IParser<T>... parsers) {
        return firstNotNull(ImmutableList.copyOf(parsers));
    }

    @NonNull
    public static <T> Parser<T> firstNotNull(@NonNull ImmutableList<? extends IParser<T>> parsers) {
        return new Parser<T>() {
            @Override
            public T parse(CharSequence input) throws NullPointerException {
                return parseFirstNotNull(input, parsers);
            }
        };
    }

    /**
     * Creates a new parser using
     * {@link JAXBContext#newInstance(java.lang.Class[])}.
     * <p>
     * Note that "<i>{@link JAXBContext} is thread-safe and should only be
     * created once and reused to avoid the cost of initializing the metadata
     * multiple times. {@link Marshaller} and {@link Unmarshaller} are not
     * thread-safe, but are lightweight to create and could be created per
     * operation (<a
     * href="http://stackoverflow.com/a/7400735">http://stackoverflow.com/a/7400735</a>)".</i>
     *
     * @param <T>
     * @param classToBeParsed
     * @return
     */
    @NonNull
    public static <T> Parser<T> onJAXB(@NonNull Class<T> classToBeParsed) {
        try {
            return onJAXB(JAXBContext.newInstance(classToBeParsed));
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    public static <T> Parser<T> onJAXB(@NonNull JAXBContext context) {
        try {
            return onJAXB(context.createUnmarshaller());
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    public static <T> Parser<T> onJAXB(@NonNull Unmarshaller unmarshaller) {
        Jaxb.Parser<T> p = Jaxb.Parser.<T>builder().factory(() -> unmarshaller).build();
        return new FailSafeParser<T>() {
            @Override
            protected T doParse(CharSequence input) throws Exception {
                return p.parseChars(input);
            }
        };
    }

    @Deprecated
    public static Parsers.@NonNull Parser<Date> onStrictDatePattern(@NonNull String datePattern, @NonNull Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(datePattern, locale);
        dateFormat.setLenient(false);
        return onDateFormat(dateFormat);
    }

    @NonNull
    public static Parser<Date> onDateFormat(@NonNull DateFormat dateFormat) {
        return new Adapter<>(nbbrd.io.text.Parser.onDateFormat(dateFormat));
    }

    @NonNull
    public static Parser<Number> onNumberFormat(@NonNull NumberFormat numberFormat) {
        return new Adapter<>(nbbrd.io.text.Parser.onNumberFormat(numberFormat));
    }

    @NonNull
    public static <T> Parser<T> onNull() {
        return new Adapter<>(nbbrd.io.text.Parser.onNull());
    }

    @NonNull
    public static <T> Parser<T> ofInstance(@Nullable T instance) {
        return new Adapter<>(nbbrd.io.text.Parser.onConstant(instance));
    }

    @NonNull
    public static Parser<File> fileParser() {
        return FILE_PARSER;
    }

    /**
     * Create a {@link Parser} that delegates its parsing to
     * {@link Integer#valueOf(java.lang.String)}.
     *
     * @return a non-null parser
     */
    @NonNull
    public static Parser<Integer> intParser() {
        return INT_PARSER;
    }

    @NonNull
    public static Parser<Long> longParser() {
        return LONG_PARSER;
    }

    @NonNull
    public static Parser<Boolean> boolParser() {
        return BOOL_PARSER;
    }

    @NonNull
    public static Parser<Character> charParser() {
        return CHAR_PARSER;
    }

    /**
     * Create a {@link Parser} that delegates its parsing to
     * {@link Double#valueOf(java.lang.String)}.
     *
     * @return a non-null parser
     */
    @NonNull
    public static Parser<Double> doubleParser() {
        return DOUBLE_PARSER;
    }

    @NonNull
    public static Parser<Charset> charsetParser() {
        return CHARSET_PARSER;
    }

    @NonNull
    public static <T extends Enum<T>> Parser<T> enumParser(@NonNull Class<T> enumClass) {
        return new Adapter<>(nbbrd.io.text.Parser.onEnum(enumClass));
    }

    @NonNull
    public static Parser<String> stringParser() {
        return STRING_PARSER;
    }

    @NonNull
    public static Parser<double[]> doubleArrayParser() {
        return DOUBLE_ARRAY_PARSER;
    }

    @NonNull
    public static Parser<String[]> stringArrayParser() {
        return STRING_ARRAY_PARSER;
    }

    @NonNull
    public static Parser<Locale> localeParser() {
        return LOCALE_PARSER;
    }

    @NonNull
    public static <X, Y> Parser<Y> compose(@NonNull IParser<X> parser, @NonNull Function<X, Y> after) {
        return new Parser<Y>() {
            @Override
            public Y parse(CharSequence input) throws NullPointerException {
                X tmp = parser.parse(input);
                return tmp != null ? after.apply(tmp) : null;
            }
        };
    }

    @NonNull
    public static Parser<List<String>> onSplitter(@NonNull Splitter splitter) {
        Objects.requireNonNull(splitter);
        return new Adapter<>(nbbrd.io.text.Parser.onStringList(chars -> StreamSupport.stream(splitter.split(chars).spliterator(), false)));
    }

    /**
     *
     * @param <T>
     * @param parser
     * @return
     * @since 2.2.0
     */
    @NonNull
    public static <T> Parser<T> wrap(@NonNull IParser<T> parser) {
        return parser instanceof Parser ? (Parser<T>) parser : new Wrapper<>(parser);
    }

    public static abstract class Parser<T> implements IParser<T> {

        /**
         * Returns an {@link Optional} containing the object that has bean
         * created by the parsing if this parsing was possible.<p>
         * Use this instead of {@link #parse(java.lang.CharSequence)} to
         * increase readability and prevent NullPointerExceptions.
         *
         * @param input the input used to create the object
         * @return a never-null {@link Optional}
         * @throws NullPointerException if input is null
         * @deprecated use {@link #parseValue(java.lang.CharSequence)} instead
         */
        @Deprecated
        @NonNull
        public Optional<T> tryParse(@NonNull CharSequence input) {
            return Optional.fromNullable(parse(input));
        }

        @Deprecated
        @NonNull
        public <X> Parser<X> compose(@NonNull Function<T, X> after) {
            return Parsers.<T, X>compose(this, after);
        }

        @Deprecated
        @NonNull
        public Parser<T> or(IParser<T>... parsers) {
            switch (parsers.length) {
                case 0:
                    return this;
                case 1:
                    return firstNotNull(ImmutableList.of(this, parsers[0]));
                default:
                    return firstNotNull(ImmutableList.<IParser<T>>builder().add(this).add(parsers).build());
            }
        }
    }

    public static abstract class FailSafeParser<T> extends Parser<T> {

        @Override
        public T parse(CharSequence input) throws NullPointerException {
            Objects.requireNonNull(input);
            try {
                return doParse(input);
            } catch (Exception ex) {
                return null;
            }
        }

        @Nullable
        abstract protected T doParse(@NonNull CharSequence input) throws Exception;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @lombok.RequiredArgsConstructor
    private static final class Adapter<T> extends Parser<T> {

        private final nbbrd.io.text.Parser<T> parser;

        @Override
        public T parse(CharSequence input) {
            Objects.requireNonNull(input);
            return parser.parse(input);
        }

        @Override
        public java.util.Optional<T> parseValue(CharSequence input) {
            Objects.requireNonNull(input);
            return parser.parseValue(input);
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class Wrapper<T> extends Parser<T> {

        private final IParser<T> parser;

        @Override
        public T parse(CharSequence input) {
            return parser.parse(input);
        }

        @Override
        public java.util.Optional<T> parseValue(CharSequence input) {
            return parser.parseValue(input);
        }

        @Override
        public IParser<T> orElse(IParser<T> other) {
            return parser.orElse(other);
        }

        @Override
        public <X> IParser<X> andThen(java.util.function.Function<? super T, ? extends X> after) {
            return parser.andThen(after);
        }
    }

    private static final Parser<File> FILE_PARSER = new Adapter<>(nbbrd.io.text.Parser.onFile());
    private static final Parser<Integer> INT_PARSER = new Adapter<>(nbbrd.io.text.Parser.onInteger());
    private static final Parser<Long> LONG_PARSER = new Adapter<>(nbbrd.io.text.Parser.onLong());
    private static final Parser<Double> DOUBLE_PARSER = new Adapter<>(nbbrd.io.text.Parser.onDouble());
    private static final Parser<Boolean> BOOL_PARSER = new Adapter<>(nbbrd.io.text.Parser.onBoolean());
    private static final Parser<Character> CHAR_PARSER = new Adapter<>(nbbrd.io.text.Parser.onCharacter());
    private static final Parser<Charset> CHARSET_PARSER = new Adapter<>(nbbrd.io.text.Parser.onCharset());
    private static final Parser<String> STRING_PARSER = new Adapter<>(nbbrd.io.text.Parser.onString());
    private static final Parser<double[]> DOUBLE_ARRAY_PARSER = new Adapter<>(nbbrd.io.text.Parser.onDoubleArray());
    private static final Parser<String[]> STRING_ARRAY_PARSER = new Adapter<>(nbbrd.io.text.Parser.onStringArray());
    private static final Parser<Locale> LOCALE_PARSER = new Adapter<>(nbbrd.io.text.Parser.onLocale());
    //</editor-fold>
}

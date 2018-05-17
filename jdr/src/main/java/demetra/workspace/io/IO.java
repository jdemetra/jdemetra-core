/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.workspace.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Set of utilities related to IO.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class IO {

    /**
     * Represents a function without argument and result.
     */
    @FunctionalInterface
    public interface Runnable {

        /**
         * Run this function.
         *
         * @throws IOException
         */
        @JdkWithIO
        void runWithIO() throws IOException;

        @Nonnull
        default Closeable asCloseable() {
            return this::runWithIO;
        }

        @Nonnull
        default java.lang.Runnable asUnchecked() {
            return () -> {
                try {
                    runWithIO();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static java.lang.Runnable unchecked(@Nonnull Runnable o) {
            return o.asUnchecked();
        }

        @Nonnull
        static Runnable checked(@Nonnull java.lang.Runnable o) {
            return () -> {
                try {
                    o.run();
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            };
        }

        @Nonnull
        static Runnable throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return () -> {
                throw ex.get();
            };
        }

        @Nonnull
        static Runnable noOp() {
            return () -> {
            };
        }
    }

    /**
     * Represents a supplier of results.
     *
     * @param <T> the type of results supplied by this supplier
     */
    @FunctionalInterface
    public interface Supplier<T> {

        /**
         * Gets a result.
         *
         * @return a result
         * @throws IOException
         */
        @JdkWithIO
        T getWithIO() throws IOException;

        @Nonnull
        default java.util.function.Supplier<T> asUnchecked() {
            return () -> {
                try {
                    return getWithIO();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> java.util.function.Supplier<T> unchecked(@Nonnull Supplier<T> o) {
            return o.asUnchecked();
        }

        @Nonnull
        static <T> Supplier<T> checked(@Nonnull java.util.function.Supplier<T> o) {
            Objects.requireNonNull(o);
            return () -> {
                try {
                    return o.get();
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            };
        }

        @Nonnull
        static <T> Supplier<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return () -> {
                throw ex.get();
            };
        }

        @Nonnull
        @SuppressWarnings("null")
        static <T> Supplier<T> of(@Nullable T t) {
            return () -> t;
        }
    }

    /**
     * Represents a function that accepts one argument and produces a result.
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     */
    @FunctionalInterface
    public interface Function<T, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         * @throws java.io.IOException
         */
        @JdkWithIO
        R applyWithIO(T t) throws IOException;

        /**
         * Returns a composed function that first applies the {@code before}
         * function to its input, and then applies this function to the result.
         * If evaluation of either function throws an exception, it is relayed
         * to the caller of the composed function.
         *
         * @param <V> the type of input to the {@code before} function, and to
         * the composed function
         * @param before the function to apply before this function is applied
         * @return a composed function that first applies the {@code before}
         * function and then applies this function
         * @throws NullPointerException if before is null
         *
         * @see #andThen(FunctionWithIO)
         */
        @JdkWithIO
        @Nonnull
        default <V> Function<V, R> compose(@Nonnull Function<? super V, ? extends T> before) {
            Objects.requireNonNull(before);
            return (V v) -> applyWithIO(before.applyWithIO(v));
        }

        /**
         * Returns a composed function that first applies this function to its
         * input, and then applies the {@code after} function to the result. If
         * evaluation of either function throws an exception, it is relayed to
         * the caller of the composed function.
         *
         * @param <V> the type of output of the {@code after} function, and of
         * the composed function
         * @param after the function to apply after this function is applied
         * @return a composed function that first applies this function and then
         * applies the {@code after} function
         * @throws NullPointerException if after is null
         *
         * @see #compose(FunctionWithIO)
         */
        @JdkWithIO
        @Nonnull
        default <V> Function<T, V> andThen(@Nonnull Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t) -> after.applyWithIO(applyWithIO(t));
        }

        @Nonnull
        default java.util.function.Function<T, R> asUnchecked() {
            return (T t) -> {
                try {
                    return applyWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T, R> java.util.function.Function<T, R> unchecked(@Nonnull Function<T, R> o) {
            return o.asUnchecked();
        }

        @Nonnull
        static <T, R> Function<T, R> checked(@Nonnull java.util.function.Function<T, R> func) {
            Objects.requireNonNull(func);
            return o -> {
                try {
                    return func.apply(o);
                } catch (UncheckedIOException ex) {
                    throw ex.getCause();
                }
            };
        }

        @Nonnull
        static <T, R> Function<T, R> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        /**
         * Returns a function that always returns its input argument.
         *
         * @param <T> the type of the input and output objects to the function
         * @return a function that always returns its input argument
         */
        @JdkWithIO
        @Nonnull
        static <T> Function<T, T> identity() {
            return t -> t;
        }

        @Nonnull
        @SuppressWarnings("null")
        static <T, R> Function<T, R> of(@Nullable R r) {
            return o -> r;
        }
    }

    /**
     * Represents a predicate (boolean-valued function) of one argument.
     *
     * @param <T> the type of the input to the predicate
     */
    @FunctionalInterface
    public interface Predicate<T> {

        /**
         * Evaluates this predicate on the given argument.
         *
         * @param t the input argument
         * @return {@code true} if the input argument matches the predicate,
         * otherwise {@code false}
         * @throws java.io.IOException
         */
        @JdkWithIO
        boolean testWithIO(T t) throws IOException;

        /**
         * Returns a composed predicate that represents a short-circuiting
         * logical AND of this predicate and another. When evaluating the
         * composed predicate, if this predicate is {@code false}, then the
         * {@code other} predicate is not evaluated.
         *
         * <p>
         * Any exceptions thrown during evaluation of either predicate are
         * relayed to the caller; if evaluation of this predicate throws an
         * exception, the {@code other} predicate will not be evaluated.
         *
         * @param other a predicate that will be logically-ANDed with this
         * predicate
         * @return a composed predicate that represents the short-circuiting
         * logical AND of this predicate and the {@code other} predicate
         * @throws NullPointerException if other is null
         */
        @JdkWithIO
        @Nonnull
        default Predicate<T> and(@Nonnull Predicate<? super T> other) {
            Objects.requireNonNull(other);
            return (t) -> testWithIO(t) && other.testWithIO(t);
        }

        /**
         * Returns a predicate that represents the logical negation of this
         * predicate.
         *
         * @return a predicate that represents the logical negation of this
         * predicate
         */
        @JdkWithIO
        @Nonnull
        default Predicate<T> negate() {
            return (t) -> !testWithIO(t);
        }

        /**
         * Returns a composed predicate that represents a short-circuiting
         * logical OR of this predicate and another. When evaluating the
         * composed predicate, if this predicate is {@code true}, then the
         * {@code other} predicate is not evaluated.
         *
         * <p>
         * Any exceptions thrown during evaluation of either predicate are
         * relayed to the caller; if evaluation of this predicate throws an
         * exception, the {@code other} predicate will not be evaluated.
         *
         * @param other a predicate that will be logically-ORed with this
         * predicate
         * @return a composed predicate that represents the short-circuiting
         * logical OR of this predicate and the {@code other} predicate
         * @throws NullPointerException if other is null
         */
        @JdkWithIO
        @Nonnull
        default Predicate<T> or(@Nonnull Predicate<? super T> other) {
            Objects.requireNonNull(other);
            return (t) -> testWithIO(t) || other.testWithIO(t);
        }

        @Nonnull
        default java.util.function.Predicate<T> asUnchecked() {
            return (T t) -> {
                try {
                    return testWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> java.util.function.Predicate<T> unchecked(@Nonnull Predicate<T> o) {
            return o.asUnchecked();
        }

        @Nonnull
        static <T> Predicate<T> checked(@Nonnull java.util.function.Predicate<T> predicate) {
            Objects.requireNonNull(predicate);
            return o -> {
                try {
                    return predicate.test(o);
                } catch (UncheckedIOException ex) {
                    throw ex.getCause();
                }
            };
        }

        @Nonnull
        static <T> Predicate<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        /**
         * Returns a predicate that tests if two arguments are equal according
         * to {@link Objects#equals(Object, Object)}.
         *
         * @param <T> the type of arguments to the predicate
         * @param targetRef the object reference with which to compare for
         * equality, which may be {@code null}
         * @return a predicate that tests if two arguments are equal according
         * to {@link Objects#equals(Object, Object)}
         */
        @JdkWithIO
        @Nonnull
        static <T> Predicate<T> isEqual(Object targetRef) {
            return (null == targetRef)
                    ? Objects::isNull
                    : object -> targetRef.equals(object);
        }

        @Nonnull
        static <T> Predicate<T> of(boolean r) {
            return o -> r;
        }
    }

    /**
     * Represents an operation that accepts a single input argument and returns
     * no result. Unlike most other functional interfaces, {@code Consumer} is
     * expected to operate via side-effects.
     *
     * @param <T> the type of the input to the operation
     */
    @FunctionalInterface
    public interface Consumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws java.io.IOException
         */
        @JdkWithIO
        void acceptWithIO(T t) throws IOException;

        /**
         * Returns a composed {@code Consumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing
         * either operation throws an exception, it is relayed to the caller of
         * the composed operation. If performing this operation throws an
         * exception, the {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code Consumer} that performs in sequence this
         * operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         */
        @JdkWithIO
        @Nonnull
        default Consumer<T> andThen(@Nonnull Consumer<? super T> after) {
            Objects.requireNonNull(after);
            return (T t) -> {
                acceptWithIO(t);
                after.acceptWithIO(t);
            };
        }

        @Nonnull
        default java.util.function.Consumer<T> asUnchecked() {
            return (T t) -> {
                try {
                    acceptWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> java.util.function.Consumer<T> unchecked(@Nonnull Consumer<T> o) {
            return o.asUnchecked();
        }

        @Nonnull
        static <T> Consumer<T> checked(@Nonnull java.util.function.Consumer<T> consumer) {
            return o -> {
                try {
                    consumer.accept(o);
                } catch (UncheckedIOException ex) {
                    throw ex.getCause();
                }
            };
        }

        @Nonnull
        static <T> Consumer<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        @Nonnull
        static <T> Consumer<T> noOp() {
            return o -> {
            };
        }
    }

    public interface ResourceLoader<K> extends Closeable {

        @Nonnull
        InputStream load(@Nonnull K key) throws IOException, IllegalStateException;

        @Nonnull
        static <K> ResourceLoader<K> of(@Nonnull Function<? super K, ? extends InputStream> loader) {
            return of(loader, Runnable.noOp().asCloseable());
        }

        @Nonnull
        static <K> ResourceLoader<K> of(@Nonnull Function<? super K, ? extends InputStream> loader, @Nonnull Closeable closer) {
            Objects.requireNonNull(loader);
            Objects.requireNonNull(closer);
            return new ResourceLoader<K>() {
                boolean closed = false;

                @Override
                public InputStream load(K key) throws IOException {
                    Objects.requireNonNull(key);
                    if (closed) {
                        throw new IllegalStateException("Closed");
                    }
                    InputStream result = loader.applyWithIO(key);
                    if (result == null) {
                        throw new IOException("Null stream");
                    }
                    return result;
                }

                @Override
                public void close() throws IOException {
                    closed = true;
                    closer.close();
                }
            };
        }
    }

    public interface ResourceStorer<K> extends Closeable {

        void store(@Nonnull K key, @Nonnull OutputStream output) throws IOException, IllegalStateException;
    }

    public interface Resource<K> extends ResourceLoader<K>, ResourceStorer<K> {

    }

    @lombok.experimental.UtilityClass
    public static final class Stream {

        @Nonnull
        public <T extends Closeable, R> java.util.stream.Stream<R> open(@Nonnull Supplier<T> source, @Nonnull Function<? super T, java.util.stream.Stream<R>> streamer) throws IOException {
            return asParser(streamer).applyWithIO(source);
        }

        @Nonnull
        public <T> java.util.stream.Stream<T> generateUntilNull(@Nonnull Supplier<T> generator) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(asIterator(generator), Spliterator.ORDERED | Spliterator.NONNULL), false);
        }

        private <C extends Closeable, R> Function<Supplier<C>, java.util.stream.Stream<R>> asParser(Function<? super C, java.util.stream.Stream<R>> streamer) {
            return flowOf(
                    source -> source.getWithIO(),
                    resource -> streamer.applyWithIO(resource).onClose(Runnable.unchecked(resource::close)),
                    Closeable::close
            );
        }

        @Nonnull
        private <T> Iterator<T> asIterator(@Nonnull Supplier<T> nextSupplier) {
            Objects.requireNonNull(nextSupplier);
            return new Iterator<T>() {
                T nextElement = null;

                @Override
                public boolean hasNext() {
                    if (nextElement != null) {
                        return true;
                    } else {
                        try {
                            nextElement = nextSupplier.getWithIO();
                            return (nextElement != null);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }

                @Override
                public T next() {
                    if (nextElement != null || hasNext()) {
                        T line = nextElement;
                        nextElement = null;
                        return line;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    /**
     * Returns a {@link File} object representing this path. Where this {@code
     * Path} is associated with the default provider, then this method is
     * equivalent to returning a {@code File} object constructed with the
     * {@code String} representation of this path.
     *
     * <p>
     * If this path was created by invoking the {@code File} {@link
     * File#toPath toPath} method then there is no guarantee that the {@code
     * File} object returned by this method is {@link #equals equal} to the
     * original {@code File}.
     *
     * @param path
     * @return an optional {@code File} object representing this path if
     * associated with the default provider
     */
    @Nonnull
    public Optional<File> getFile(@Nonnull Path path) {
        try {
            return Optional.of(path.toFile());
        } catch (UnsupportedOperationException ex) {
            return Optional.empty();
        }
    }

    @Nonnull
    public Optional<InputStream> getResourceAsStream(@Nonnull Class<?> type, @Nonnull String name) {
        return Optional.ofNullable(type.getResourceAsStream(name));
    }

    @Nonnull
    @SuppressWarnings("ThrowableResultIgnored")
    public <X extends Throwable> void ensureClosed(@Nonnull X exception, @Nonnull Closeable closeable) {
        Objects.requireNonNull(exception);
        try {
            closeable.close();
        } catch (IOException suppressed) {
            try {
                exception.addSuppressed(suppressed);
            } catch (Throwable ignore) {
            }
        }
    }

    @Nonnull
    static <S, R, VALUE> Function<S, VALUE> valueOf(
            @Nonnull Function<? super S, ? extends R> opener,
            @Nonnull Function<? super R, ? extends VALUE> reader,
            @Nonnull Consumer<? super R> closer) {
        Objects.requireNonNull(opener);
        Objects.requireNonNull(reader);
        Objects.requireNonNull(closer);
        return source -> {
            R resource = opener.applyWithIO(source);
            try (Closeable c = () -> closer.acceptWithIO(resource)) {
                return reader.applyWithIO(resource);
            }
        };
    }

    @Nonnull
    static <S, R, FLOW extends AutoCloseable> Function<S, FLOW> flowOf(
            @Nonnull Function<? super S, ? extends R> opener,
            @Nonnull Function<? super R, ? extends FLOW> reader,
            @Nonnull Consumer<? super R> closer) {
        Objects.requireNonNull(opener);
        Objects.requireNonNull(reader);
        Objects.requireNonNull(closer);
        return source -> {
            R resource = opener.applyWithIO(source);
            try {
                return reader.applyWithIO(resource);
            } catch (Error | RuntimeException | IOException e) {
                ensureClosed(e, () -> closer.acceptWithIO(resource));
                throw e;
            }
        };
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    private @interface JdkWithIO {

        String value() default "";
    }
}

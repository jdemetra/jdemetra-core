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
package demetra.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * @author Philippe Charles
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @since 2.2.0
 */
@FunctionalInterface
public interface FunctionWithIO<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws java.io.IOException
     */
    R apply(T t) throws IOException;

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the
     * composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(FunctionWithIO)
     */
    default <V> FunctionWithIO<V, R> compose(FunctionWithIO<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies this function to its
     * input, and then applies the {@code after} function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     * composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     * @see #compose(FunctionWithIO)
     */
    default <V> FunctionWithIO<T, V> andThen(FunctionWithIO<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> FunctionWithIO<T, T> identity() {
        return t -> t;
    }

    default Function<T, R> unchecked() {
        return unchecked(this);
    }

    static <T, R> Function<T, R> unchecked(FunctionWithIO<T, R> func) {
        Objects.requireNonNull(func);
        return o -> {
            try {
                return func.apply(o);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };
    }

    static <T, R> FunctionWithIO<T, R> checked(Function<T, R> func) {
        Objects.requireNonNull(func);
        return o -> {
            try {
                return func.apply(o);
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        };
    }
}

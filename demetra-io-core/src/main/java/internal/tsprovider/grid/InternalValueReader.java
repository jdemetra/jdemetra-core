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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;
import java.time.LocalDateTime;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@FunctionalInterface
public interface InternalValueReader<T> {

    @Nullable
    T read(@Nonnull GridInput grid, int row, int column);

    @Nonnull
    default InternalValueReader<T> or(@Nonnull InternalValueReader<T> fallback) {
        return compose(this, fallback);
    }

    @Nonnull
    static <X> InternalValueReader<X> onStringParser(@Nonnull Function<String, X> parser) {
        return FuncReader.onStringParser(parser);
    }

    @Nonnull
    static InternalValueReader<LocalDateTime> onDateTime() {
        return FuncReader.DATETIME;
    }

    @Nonnull
    static InternalValueReader<Number> onNumber() {
        return FuncReader.NUMBER;
    }

    @Nonnull
    static InternalValueReader<String> onString() {
        return FuncReader.STRING;
    }

    @Nonnull
    static <T> InternalValueReader<T> onNull() {
        return NullReader.INSTANCE;
    }

    static <T> InternalValueReader<T> compose(InternalValueReader<T> main, InternalValueReader<T> fallback) {
        return (g, r, c) -> {
            T result = main.read(g, r, c);
            return result != null ? result : fallback.read(g, r, c);
        };
    }

    enum NullReader implements InternalValueReader {
        INSTANCE;

        @Override
        public Object read(GridInput grid, int row, int column) {
            return null;
        }

        @Override
        public InternalValueReader or(InternalValueReader fallback) {
            return fallback;
        }
    }

    interface FuncReader<T> extends InternalValueReader<T>, Function<Object, T> {

        @Override
        default public T read(GridInput grid, int row, int column) {
            return apply(grid.getValue(row, column));
        }

        @Override
        default public InternalValueReader<T> or(InternalValueReader<T> fallback) {
            if (fallback == NullReader.INSTANCE) {
                return this;
            }
            if (fallback instanceof FuncReader) {
                return compose(this, (FuncReader<T>) fallback);
            }
            return InternalValueReader.super.or(fallback);
        }

        static final FuncReader<LocalDateTime> DATETIME = o -> o instanceof LocalDateTime ? (LocalDateTime) o : null;
        static final FuncReader<Number> NUMBER = o -> o instanceof Number ? (Number) o : null;
        static final FuncReader<String> STRING = o -> o instanceof String ? (String) o : null;

        static <T> FuncReader<T> onStringParser(Function<String, T> parser) {
            return o -> o instanceof String ? parser.apply((String) o) : null;
        }

        static <T> FuncReader<T> compose(FuncReader<T> main, FuncReader<T> fallback) {
            return o -> {
                T result = main.apply(o);
                return result != null ? result : fallback.apply(o);
            };
        }
    }
}

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
import demetra.util.Parser;
import java.time.LocalDateTime;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface ValueReader<T> {

    @Nullable
    T read(@Nonnull GridInput grid, int row, int column);

    @Nonnull
    default ValueReader<T> or(@Nonnull ValueReader<T> fallback) {
        return compose(this, fallback);
    }

    @Nonnull
    static <X> ValueReader<X> onStringParser(@Nonnull Parser<X> parser) {
        return FuncReader.onStringParser(parser);
    }

    @Nonnull
    static ValueReader<LocalDateTime> onDateTime() {
        return FuncReader.DATETIME;
    }

    @Nonnull
    static ValueReader<Number> onNumber() {
        return FuncReader.NUMBER;
    }

    @Nonnull
    static ValueReader<String> onString() {
        return FuncReader.STRING;
    }

    @Nonnull
    static <T> ValueReader<T> onNull() {
        return NullReader.INSTANCE;
    }

    static <T> ValueReader<T> compose(ValueReader<T> main, ValueReader<T> fallback) {
        return (g, r, c) -> {
            T result = main.read(g, r, c);
            return result != null ? result : fallback.read(g, r, c);
        };
    }

    enum NullReader implements ValueReader {
        INSTANCE;

        @Override
        public Object read(GridInput grid, int row, int column) {
            return null;
        }

        @Override
        public ValueReader or(ValueReader fallback) {
            return fallback;
        }
    }

    interface FuncReader<T> extends ValueReader<T>, Function<Object, T> {

        @Override
        default public T read(GridInput grid, int row, int column) {
            return apply(grid.getValue(row, column));
        }

        @Override
        default public ValueReader<T> or(ValueReader<T> fallback) {
            if (fallback == NullReader.INSTANCE) {
                return this;
            }
            if (fallback instanceof FuncReader) {
                return compose(this, (FuncReader<T>) fallback);
            }
            return ValueReader.super.or(fallback);
        }

        static final FuncReader<LocalDateTime> DATETIME = o -> o instanceof LocalDateTime ? (LocalDateTime) o : null;
        static final FuncReader<Number> NUMBER = o -> o instanceof Number ? (Number) o : null;
        static final FuncReader<String> STRING = o -> o instanceof String ? (String) o : null;

        static <T> FuncReader<T> onStringParser(Parser<T> parser) {
            return o -> o instanceof String ? parser.parse((String) o) : null;
        }

        static <T> FuncReader<T> compose(FuncReader<T> main, FuncReader<T> fallback) {
            return o -> {
                T result = main.apply(o);
                return result != null ? result : fallback.apply(o);
            };
        }
    }
}

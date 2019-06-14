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

import demetra.tsprovider.grid.GridOutput;
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
public interface InternalValueWriter<T> {

    void write(@Nonnull GridOutput grid, int row, int column, @Nullable T value);

    @Nonnull
    static <X> InternalValueWriter<X> onStringFormatter(@Nonnull Function<X, String> formatter) {
        return FuncWriter.onStringFormatter(formatter);
    }

    @Nonnull
    static InternalValueWriter<LocalDateTime> onDateTime() {
        return FuncWriter.DATETIME;
    }

    @Nonnull
    static InternalValueWriter<Number> onNumber() {
        return FuncWriter.NUMBER;
    }

    @Nonnull
    static InternalValueWriter<String> onString() {
        return FuncWriter.STRING;
    }

    @Nonnull
    static <T> InternalValueWriter<T> onNull() {
        return NullWriter.INSTANCE;
    }

    enum NullWriter implements InternalValueWriter {
        INSTANCE;

        @Override
        public void write(GridOutput grid, int row, int column, Object value) {
            grid.setValue(row, column, null);
        }
    }

    interface FuncWriter<T> extends InternalValueWriter<T>, Function<T, Object> {

        @Override
        default public void write(GridOutput grid, int row, int column, T value) {
            grid.setValue(row, column, apply(value));
        }

        static final FuncWriter<LocalDateTime> DATETIME = o -> o;
        static final FuncWriter<Number> NUMBER = o -> o;
        static final FuncWriter<String> STRING = o -> o;

        static <T> FuncWriter<T> onStringFormatter(Function<T, String> parser) {
            return o -> o != null ? parser.apply(o) : null;
        }
    }
}

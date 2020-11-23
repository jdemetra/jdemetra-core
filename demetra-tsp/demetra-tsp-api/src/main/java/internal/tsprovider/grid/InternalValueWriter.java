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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@FunctionalInterface
public interface InternalValueWriter<T> {

    void write(GridOutput.@NonNull Stream stream, @Nullable T value) throws IOException;

    @NonNull
    static <X> InternalValueWriter<X> onStringFormatter(@NonNull Function<X, String> formatter) {
        return FuncWriter.onStringFormatter(formatter);
    }

    @NonNull
    static InternalValueWriter<LocalDateTime> onDateTime() {
        return FuncWriter.DATETIME;
    }

    @NonNull
    static InternalValueWriter<Double> onDouble() {
        return FuncWriter.DOUBLE;
    }

    @NonNull
    static InternalValueWriter<String> onString() {
        return FuncWriter.STRING;
    }

    @NonNull
    static <T> InternalValueWriter<T> onNull() {
        return NullWriter.INSTANCE;
    }

    enum NullWriter implements InternalValueWriter {
        INSTANCE;

        @Override
        public void write(GridOutput.Stream stream, Object value) throws IOException {
            stream.writeCell(null);
        }
    }

    interface FuncWriter<T> extends InternalValueWriter<T>, Function<T, Object> {

        @Override
        default public void write(GridOutput.Stream stream, T value) throws IOException {
            stream.writeCell(apply(value));
        }

        static final FuncWriter<LocalDateTime> DATETIME = o -> o;
        static final FuncWriter<Double> DOUBLE = o -> o;
        static final FuncWriter<String> STRING = o -> o;

        static <T> FuncWriter<T> onStringFormatter(Function<T, String> parser) {
            return o -> o != null ? parser.apply(o) : null;
        }
    }
}

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
public interface InternalValueReader<T> {

    @Nullable
    T read(@Nullable Object obj);

    @NonNull
    default InternalValueReader<T> or(@NonNull InternalValueReader<T> fallback) {
        return (obj) -> {
            T result = this.read(obj);
            return result != null ? result : fallback.read(obj);
        };
    }

    @NonNull
    static <X> InternalValueReader<X> onStringParser(@NonNull Function<String, X> parser) {
        return obj -> obj instanceof String ? parser.apply((String) obj) : null;
    }

    @NonNull
    static InternalValueReader<LocalDateTime> onDateTime() {
        return CastReader.DATETIME;
    }

    @NonNull
    static InternalValueReader<Number> onNumber() {
        return CastReader.NUMBER;
    }

    @NonNull
    static InternalValueReader<String> onString() {
        return CastReader.STRING;
    }

    @NonNull
    static <T> InternalValueReader<T> onNull() {
        return NullReader.INSTANCE;
    }

    enum NullReader implements InternalValueReader {
        INSTANCE;

        @Override
        public Object read(Object obj) {
            return null;
        }

        @Override
        public InternalValueReader or(InternalValueReader fallback) {
            return fallback;
        }
    }

    @lombok.AllArgsConstructor
    static final class CastReader<T> implements InternalValueReader<T> {

        private final Class<T> type;

        @Override
        public T read(Object obj) {
            return type.isInstance(obj) ? (T) obj : null;
        }

        @Override
        public InternalValueReader<T> or(InternalValueReader<T> fallback) {
            return fallback == NullReader.INSTANCE ? this : InternalValueReader.super.or(fallback);
        }

        static final CastReader<LocalDateTime> DATETIME = new CastReader(LocalDateTime.class);
        static final CastReader<Number> NUMBER = new CastReader(Number.class);
        static final CastReader<String> STRING = new CastReader(String.class);
    }
}

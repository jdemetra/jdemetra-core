/*
 * Copyright 2020 National Bank of Belgium
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
package internal.time;

import demetra.time.IsoConverter;
import java.util.Objects;
import java.util.function.Function;

/**
 * @param <T>
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class DefaultConverter<T> implements IsoConverter<T> {

    @lombok.NonNull
    private final Function<T, ? extends CharSequence> formatter;

    @lombok.NonNull
    private final Function<? super CharSequence, T> parser;

    @Override
    public CharSequence format(T value) {
        Objects.requireNonNull(value);
        return Objects.requireNonNull(formatter.apply(value));
    }

    @Override
    public T parse(CharSequence text) {
        Objects.requireNonNull(text);
        return Objects.requireNonNull(parser.apply(text));
    }
}

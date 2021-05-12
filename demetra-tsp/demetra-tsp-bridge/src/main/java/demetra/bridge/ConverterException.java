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
package demetra.bridge;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * @author Philippe Charles
 */
public final class ConverterException extends RuntimeException {

    public static <FROM, TO> @NonNull ConverterException of(@NonNull Class<FROM> from, @NonNull Class<TO> to, @NonNull FROM value) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(value, "value");
        return new ConverterException(String.format("Cannot convert from %s to %s with value '%s'", from, to, value));
    }

    public static @NonNull ConverterException notPossible(Enum<?> value) {
        return new ConverterException("Not possible " + value);
    }

    public ConverterException(String message) {
        super(message);
    }
}

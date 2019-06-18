/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.util;

import demetra.design.MightBePromoted;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * General-purpose utility to perform validation.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Validations {

    @NonNull
    public <T extends CharSequence> T notBlank(@NonNull T actual, @NonNull String message) throws IllegalArgumentException {
        return notBlank(actual, o -> message);
    }

    @NonNull
    public <T extends CharSequence> T notBlank(@NonNull T actual, @NonNull Function<? super T, String> message) throws IllegalArgumentException {
        if (isBlank(actual)) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }

    @MightBePromoted
    private boolean isBlank(@NonNull CharSequence text) {
        return text.length() == 0 || text.chars().anyMatch(Character::isWhitespace);
    }

    public int min(int actual, int expected, @NonNull String message) throws IllegalArgumentException {
        return min(actual, expected, o -> message);
    }

    public int min(int actual, int expected, @NonNull IntFunction<String> message) throws IllegalArgumentException {
        if (expected > actual) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }

    @NonNull
    public <T> Collection<T> atLeast(@NonNull Collection<T> actual, int expected, @NonNull String message) throws IllegalArgumentException {
        return atLeast(actual, expected, o -> message);
    }

    @NonNull
    public <T> Collection<T> atLeast(@NonNull Collection<T> actual, int expected, @NonNull IntFunction<String> message) throws IllegalArgumentException {
        if (expected > actual.size()) {
            throw new IllegalArgumentException(message.apply(actual.size()));
        }
        return actual;
    }

    public <T> T on(T actual, @NonNull Predicate<T> expected, @NonNull String message) throws IllegalArgumentException {
        return on(actual, expected, o -> message);
    }

    public <T> T on(T actual, @NonNull Predicate<T> expected, @NonNull Function<? super T, String> message) throws IllegalArgumentException {
        if (!expected.test(actual)) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }
}

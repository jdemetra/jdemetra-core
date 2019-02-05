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
import javax.annotation.Nonnull;

/**
 * General-purpose utility to perform validation.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Validations {

    @Nonnull
    public <T extends CharSequence> T notBlank(@Nonnull T actual, @Nonnull String message) throws IllegalArgumentException {
        return notBlank(actual, o -> message);
    }

    @Nonnull
    public <T extends CharSequence> T notBlank(@Nonnull T actual, @Nonnull Function<? super T, String> message) throws IllegalArgumentException {
        if (isBlank(actual)) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }

    @MightBePromoted
    private boolean isBlank(@Nonnull CharSequence text) {
        return text.length() == 0 || text.chars().anyMatch(Character::isWhitespace);
    }

    public int min(int actual, int expected, @Nonnull String message) throws IllegalArgumentException {
        return min(actual, expected, o -> message);
    }

    public int min(int actual, int expected, @Nonnull IntFunction<String> message) throws IllegalArgumentException {
        if (expected > actual) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }

    @Nonnull
    public <T> Collection<T> atLeast(@Nonnull Collection<T> actual, int expected, @Nonnull String message) throws IllegalArgumentException {
        return atLeast(actual, expected, o -> message);
    }

    @Nonnull
    public <T> Collection<T> atLeast(@Nonnull Collection<T> actual, int expected, @Nonnull IntFunction<String> message) throws IllegalArgumentException {
        if (expected > actual.size()) {
            throw new IllegalArgumentException(message.apply(actual.size()));
        }
        return actual;
    }

    public <T> T on(T actual, @Nonnull Predicate<T> expected, @Nonnull String message) throws IllegalArgumentException {
        return on(actual, expected, o -> message);
    }

    public <T> T on(T actual, @Nonnull Predicate<T> expected, @Nonnull Function<? super T, String> message) throws IllegalArgumentException {
        if (!expected.test(actual)) {
            throw new IllegalArgumentException(message.apply(actual));
        }
        return actual;
    }
}

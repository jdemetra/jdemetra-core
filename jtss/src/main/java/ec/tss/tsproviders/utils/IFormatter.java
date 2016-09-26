/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a class that creates a {@link CharSequence} from an object.<br> For
 * example, you could use it to format a into a String. Note that it can also be
 * used to convert a String to a new one.<br> The formatter must not throw
 * Exceptions; it must swallow it and return {@code null}. This means that
 * {@code null} is not considered has a value (same as Collection API). To
 * create a "null value" from a formatter, you should use the NullObject
 * pattern.
 *
 * @author Philippe Charles
 * @param <T> The type of the object to be formatted
 * @see IParser
 */
@FunctionalInterface
public interface IFormatter<T> {

    /**
     * Format an object into a CharSequence.
     *
     * @param value the input used to create the CharSequence
     * @return a new CharSequence if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     */
    @Nullable
    CharSequence format(@Nonnull T value);

    /**
     * Format an object into a String.
     *
     * @param value the non-null input used to create the String
     * @return a new String if possible, {@code null} otherwise
     * @throws NullPointerException if input is null
     */
    @Nullable
    default String formatAsString(@Nonnull T value) {
        CharSequence result = format(value);
        return result != null ? result.toString() : null;
    }
}

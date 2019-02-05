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

import javax.annotation.Nonnull;

/**
 * Defines the ability of an object to be self-validated.
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface Validable<T> {

    /**
     * Check if the current object is valid.
     *
     * @return itself
     * @throws IllegalArgumentException if the object is invalid
     */
    @Nonnull
    T validate() throws IllegalArgumentException;

    /**
     * Defines a builder associated to a validable object.
     *
     * @param <T>
     */
    interface Builder<T extends Validable<T>> {

        /**
         * Builds a new object with validation.
         *
         * @return a non-null object
         * @throws IllegalArgumentException
         */
        @Nonnull
        default T build() throws IllegalArgumentException {
            return buildWithoutValidation().validate();
        }

        /**
         * Builds a new object without validation.
         *
         * @return a non-null object
         */
        @Nonnull
        T buildWithoutValidation();
    }
}

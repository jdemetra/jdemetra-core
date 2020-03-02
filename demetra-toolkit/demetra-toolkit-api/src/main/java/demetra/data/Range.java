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
package demetra.data;

import demetra.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
@Development(status = Development.Status.Release)
public interface Range<E extends Comparable<? super E>> {

    /**
     * Gets the start of the range (included)
     *
     * @return The start of the range.
     */
    @NonNull
    E start();

    /**
     * Gets the end of the range (excluded)
     *
     * @return The end of the range (excluded)
     */
    @NonNull
    E end();

    boolean contains(@NonNull E element);

    public static <E extends Comparable<? super E>> Range<E> of(final E start, final E end) {
        return new Range<E>() {
            @Override
            public E start() {
                return start;
            }

            @Override
            public E end() {
                return end;
            }

            @Override
            public boolean contains(E element) {
                return start.compareTo(element) <= 0 && element.compareTo(end) < 0;
            }
        };
    }
}

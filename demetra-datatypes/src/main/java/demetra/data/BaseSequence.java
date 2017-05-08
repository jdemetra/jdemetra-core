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
package demetra.data;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface BaseSequence<E> {

    /**
     * Returns the length of this sequence.
     *
     * @return the number of <code>values</code>s in this sequence
     */
    @Nonnegative
    int length();

    default boolean isEmpty() {
        return length() == 0;
    }

    @Nonnull
    Iterator<E> iterator();

    @Nonnull
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), length(), 0);
    }
}

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

import internal.data.InternalSeqCursor;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnegative;

/**
 * Describes a writable sequence of elements.
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface Vector<E> extends Seq<E> {

    /**
     * Sets value at the specified index.
     *
     *
     * @param index the index of the value to be modified
     * @param value the specified value
     */
    void set(@Nonnegative int index, E value) throws IndexOutOfBoundsException;

    default void apply(@Nonnegative int index, UnaryOperator<E> fn) throws IndexOutOfBoundsException {
        set(index, fn.apply(get(index)));
    }

    @Override
    default VectorCursor<E> cursor() {
        return new InternalSeqCursor.DefaultVectorCursor(this);
    }
}

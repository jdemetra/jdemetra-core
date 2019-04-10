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

import internal.data.InternalSeq;
import internal.data.InternalSeqCursor;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Describes a generic sequence of elements.
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface Seq<E> extends BaseSeq, Iterable<E> {

    /**
     * Returns the value at the specified index. An index ranges from zero to
     * <tt>length() - 1</tt>. The first value of the sequence is at index zero,
     * the next at index one, and so on, as for array indexing.
     *
     * @param index the index of the value to be returned
     * @return the specified value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
     * negative or not less than <tt>length()</tt>
     */
    E get(@Nonnegative int index) throws IndexOutOfBoundsException;

    @Override
    default SeqCursor<E> cursor() {
        return new InternalSeqCursor.DefaultSeqCursor(this);
    }

    @Override
    default Iterator<E> iterator() {
        return new InternalSeq.SequenceIterator(this);
    }

    @Override
    default void forEach(Consumer<? super E> action) {
        InternalSeq.forEach(this, action);
    }

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), length(), 0);
    }

    @Nonnull
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Nonnull
    default E[] toArray(@Nonnull IntFunction<E[]> generator) {
        return InternalSeq.toArray(this, generator);
    }
}

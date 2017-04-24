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

import internal.Tripwire;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <E>
 */
public interface Sequence<E> extends Iterable<E> {

    /**
     * Returns the length of this sequence.
     *
     * @return the number of <code>values</code>s in this sequence
     */
    @Nonnegative
    int length();

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

    default boolean isEmpty() {
        return length() == 0;
    }

    @Override
    default Iterator<E> iterator() {
        return new Sequences.SequenceIterator(this);
    }

    @Override
    default void forEach(Consumer<? super E> action) {
        Sequences.forEach(this, action);
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
        return Sequences.toArray(this, generator);
    }

    interface OfDouble extends Sequence<Double> {

        /**
         * Returns the <code>double</code> value at the specified index. An
         * index ranges from zero to <tt>length() - 1</tt>. The first
         * <code>double</code> value of the sequence is at index zero, the next
         * at index one, and so on, as for array indexing.
         *
         * @param index the index of the <code>double</code> value to be
         * returned
         * @return the specified <code>double</code> value
         * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
         * negative or not less than <tt>length()</tt>
         */
        double getDouble(@Nonnegative int index) throws IndexOutOfBoundsException;

        @Override
        default Double get(int index) throws IndexOutOfBoundsException {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Sequence.OfDouble.get()");
            }
            return getDouble(index);
        }

        @Nonnull
        @Override
        default PrimitiveIterator.OfDouble iterator() {
            return new Sequences.DoubleIterator(this);
        }

        default void forEach(@Nonnull DoubleConsumer action) {
            Sequences.forEach(this, action);
        }

        @Nonnull
        @Override
        default Spliterator.OfDouble spliterator() {
            return Sequences.spliterator(this);
        }

        /**
         * Returns a stream of {@code double} zero-extending the {@code double}
         * values from this sequence.
         *
         * @return an IntStream of double values from this sequence
         */
        @Nonnull
        default DoubleStream doubleStream() {
            return Sequences.stream(this);
        }

        /**
         * Copies the data into a given buffer
         *
         * @param buffer The buffer that will receive the data.
         * @param offset The start position in the buffer for the copy. The data
         * will be copied in the buffer at the indexes [start,
         * start+getLength()[. The length of the buffer is not checked (it could
         * be larger than this array.
         */
        default void copyTo(@Nonnull double[] buffer, @Nonnegative int offset) {
            Sequences.copyTo(this, buffer, offset);
        }

        /**
         * @return @see DoubleStream#toArray()
         */
        @Nonnull
        default double[] toArray() {
            return Sequences.toArray(this);
        }

        @Override
        default Double[] toArray(IntFunction<Double[]> generator) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Sequence.OfDouble.toArray()");
            }
            return Sequence.super.toArray(generator);
        }

        /**
         * @param pred
         * @return
         * @see DoubleStream#allMatch(java.util.function.DoublePredicate))
         */
        default boolean allMatch(@Nonnull DoublePredicate pred) {
            return Sequences.allMatch(this, pred);
        }

        /**
         *
         * @param initial
         * @param fn
         * @return
         * @see DoubleStream#reduce(double,
         * java.util.function.DoubleBinaryOperator)
         */
        default double reduce(double initial, @Nonnull DoubleBinaryOperator fn) {
            return Sequences.reduce(this, initial, fn);
        }

        default int indexOf(@Nonnull DoublePredicate pred) {
            return Sequences.firstIndexOf(this, pred);
        }

        default int lastIndexOf(@Nonnull DoublePredicate pred) {
            return Sequences.lastIndexOf(this, pred);
        }
    }
}

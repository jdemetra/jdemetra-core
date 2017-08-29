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

import static demetra.data.DoubleArray.EMPTY;
import demetra.design.Internal;
import demetra.utilities.functions.DoubleBiPredicate;
import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface DoubleSequence extends BaseSequence<Double> {

    public static final DoubleSequence EMPTY = new DoubleArray(new double[0]);
    /**
     * Creates a new value using an array of doubles. Internal use only since it
     * can break immutability.
     *
     * @param data
     * @return
     */
    @Internal
    @Nonnull
    public static DoubleSequence ofInternal(@Nonnull double... data) {
        return data.length > 0 ? new DoubleArray(data) : EMPTY;
    }

    @Internal
    @Nonnull
    public static DoubleSequence ofInternal(@Nonnull double[] data, int start, int end) {
        return end <= start ? EMPTY : new PartialDoubleArray(data, start, end);
    }

    @Nonnull
    public static DoubleSequence of(@Nonnull double... data) {
        return ofInternal(data.clone());
    }

    @Nonnull
    public static DoubleSequence of(@Nonnull DoubleStream stream) {
        return ofInternal(stream.toArray());
    }

    @Nonnull
    public static DoubleSequence of(@Nonnull DoubleSequence seq) {
        return seq instanceof DoubleArray ? (DoubleArray) seq : ofInternal(seq.toArray());
    }

    @Nonnull
    public static DoubleSequence of(int length, IntToDoubleFunction fn) {
        return new IntToDoubleSequence(length, fn);
    }

    @Nonnull
    public static DoubleSequence transformation(DoubleSequence source, DoubleUnaryOperator fn) {
        return new IntToDoubleSequence(source.length(), i->fn.applyAsDouble(source.get(i)));
    }
    /**
     * Returns the <code>double</code> value at the specified index. An index
     * ranges from zero to <tt>length() - 1</tt>. The first <code>double</code>
     * value of the sequence is at index zero, the next at index one, and so on,
     * as for array indexing.
     *
     * @param index the index of the <code>double</code> value to be returned
     * @return the specified <code>double</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
     * negative or not less than <tt>length()</tt>
     */
    double get(@Nonnegative int index) throws IndexOutOfBoundsException;

    /**
     * The cell reader at the beginning of this object. The first data will be
     * retrieved by "next".
     *
     * @return
     */
    default DoubleReader reader(){
        return DoubleReader.defaultReaderOf(this);
    }

    /**
     * Makes an extract of this data block.
     *
     * @param start The position of the first extracted item.
     * @param length The number of extracted items. The size of the result could
     * be smaller than length, if the data block doesn't contain enough items.
     * Cannot be null.
     * @return A new (read only) toArray block. Cannot be null (but the length
     * of the result could be 0.
     */
    default DoubleSequence extract(@Nonnegative final int start, @Nonnegative final int length){
        return of(length, i->get(start+i));
    }

    /**
     * Copies the data into a given buffer
     *
     * @param buffer The buffer that will receive the data.
     * @param offset The start position in the buffer for the copy. The data
     * will be copied in the buffer at the indexes [start, start+getLength()[.
     * The length of the buffer is not checked (it could be larger than this
     * array.
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

    /**
     * Returns a stream of {@code double} zero-extending the {@code double}
     * values from this sequence.
     *
     * @return an IntStream of double values from this sequence
     */
    @Nonnull
    default DoubleStream stream() {
        return Sequences.stream(this);
    }

    default void forEach(@Nonnull DoubleConsumer action) {
        Sequences.forEach(this, action);
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
     * @param seq
     * @param pred
     * @return
     */
    default boolean allMatch(@Nonnull DoubleSequence seq, @Nonnull DoubleBiPredicate pred) {
        return Sequences.allMatch(this, seq, pred);
    }
   /**
     * @param pred
     * @return
     * @see DoubleStream#anyMatch(java.util.function.DoublePredicate))
     */
    default boolean anyMatch(@Nonnull DoublePredicate pred) {
        return Sequences.anyMatch(this, pred);
    }

    /**
     *
     * @param initial
     * @param fn
     * @return
     * @see DoubleStream#reduce(double, java.util.function.DoubleBinaryOperator)
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

    default int count(DoublePredicate pred) {
        return Sequences.count(this, pred);
    }

    /**
     * Drops some items at the beginning and/or at the end of the array
     *
     * @param beg The number of items dropped at the beginning
     * @param end The number of items dropped at the end
     * @return The shortened array
     */
    default DoubleSequence drop(int beg, int end) {
        return extract(beg, length() - beg - end);
    }

    /**
     * Returns a new array of doubles in reverse order
     *
     * @return
     */
    default DoubleSequence reverse() {
        final int n = length();
        return new IntToDoubleSequence(n, i -> get(n - 1 - i));
    }

    public static boolean equals(double a, double b, double epsilon) {
        return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
    }

    public static String toString(DoubleSequence rd) {
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            builder.append(rd.get(0));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(rd.get(i));
            }
        }
        return builder.toString();
    }

    public static String toString(DoubleSequence rd, String fmt) {
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            builder.append(new DecimalFormat(fmt).format(rd.get(0)));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(new DecimalFormat(fmt).format(rd.get(i)));
            }
        }
        return builder.toString();
    }

        /**
     * Transforms this object into a function: 0, length()[ -> R.
     *
     * @return
     */
    default IntToDoubleFunction asFunction() {
        return i -> get(i);
    }

    public static double round(double r, final int ndec) {
        if (ndec < 0) {
            throw new IllegalArgumentException("Negative rounding parameter");
        }
        double f = 1;
        for (int i = 0; i < ndec; ++i) {
            f *= 10;
        }
        if (Double.isFinite(r)) {
            double v = r;
            if (ndec > 0) {
                r = Math.round(v * f) / f;
            } else {
                r = Math.round(v);
            }
        }
        return r;
    }

}

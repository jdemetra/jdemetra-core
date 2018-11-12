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

import demetra.design.Internal;
import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import demetra.util.function.BiDoublePredicate;
import internal.data.InternalDoubleSeq;

/**
 *
 * @author Philippe Charles
 */
public interface DoubleSequence extends BaseSequence<Double> {

    static final double[] EMPTYARRAY = new double[0];

    /**
     * Creates a new value using an array of doubles. Internal use only since it
     * can break immutability.
     *
     * @param data
     * @return
     */
    @Internal
    @Nonnull
    static DoubleSequence ofInternal(@Nonnull double[] data) {
        return new InternalDoubleSeq.DoubleSeqN(data);
    }

    /**
     *
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     * @return
     */
    @Internal
    @Nonnull
    static DoubleSequence ofInternal(@Nonnull double[] data, @Nonnegative int start, @Nonnegative int len) {
        return new InternalDoubleSeq.PartialDoubleArray(data, start, len);
    }

    /**
     * Makes a sequence of regularly spaced doubles
     *
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     * @param inc Increment in the underlying storage of two succesive items
     * @return
     */
    @Internal
    @Nonnull
    static DoubleSequence ofInternal(@Nonnull double[] data, @Nonnegative int start, @Nonnegative int len, int inc) {
        return new InternalDoubleSeq.RegularlySpacedDoubles(data, start, len, inc);
    }

    @Nonnull
    static DoubleSequence empty() {
        return InternalDoubleSeq.DoubleSeq0.INSTANCE;
    }

    @Nonnull
    static DoubleSequence of(double value) {
        return new InternalDoubleSeq.DoubleSeq1(value);
    }

    @Nonnull
    static DoubleSequence of(@Nonnull double... data) {
        switch (data.length) {
            case 0:
                return empty();
            case 1:
                return of(data[0]);
            default:
                return ofInternal(data.clone());
        }
    }

    @Nonnull
    static DoubleSequence of(@Nonnull DoubleStream stream) {
        return ofInternal(stream.toArray());
    }

    @Nonnull
    static DoubleSequence of(@Nonnull DoubleSequence seq) {
        return seq instanceof InternalDoubleSeq.DoubleSeqN
                ? (InternalDoubleSeq.DoubleSeqN) seq
                : ofInternal(seq.toArray());
    }

    @Nonnull
    static DoubleSequence onMapping(@Nonnegative int length, @Nonnull IntToDoubleFunction fn) {
        return new InternalDoubleSeq.IntToDoubleSequence(length, fn);
    }

    @Nonnull
    static DoubleSequence onMapping(@Nonnull DoubleSequence source, @Nonnull DoubleUnaryOperator fn) {
        return new InternalDoubleSeq.IntToDoubleSequence(source.length(), i -> fn.applyAsDouble(source.get(i)));
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
    @Nonnull
    default DoubleReader reader() {
        return DoubleReader.of(this);
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
    @Nonnull
    default DoubleSequence extract(@Nonnegative final int start, @Nonnegative final int length) {
        return DoubleSequence.onMapping(length, i -> get(start + i));
    }

    @Nonnull
    default DoubleSequence extract(@Nonnegative final int start, @Nonnegative final int length, final int increment) {
        return DoubleSequence.onMapping(length, i -> get(start + i * increment));
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
        InternalDoubleSeq.copyTo(this, buffer, offset);
    }

    /**
     * @return @see DoubleStream#toArray()
     */
    @Nonnull
    default double[] toArray() {
        return InternalDoubleSeq.toArray(this);
    }

    /**
     * Returns a stream of {@code double} zero-extending the {@code double}
     * values from this sequence.
     *
     * @return an IntStream of double values from this sequence
     */
    @Nonnull
    default DoubleStream stream() {
        return InternalDoubleSeq.stream(this);
    }

    default void forEach(@Nonnull DoubleConsumer action) {
        InternalDoubleSeq.forEach(this, action);
    }

    /**
     * @param pred
     * @return
     * @see DoubleStream#allMatch(java.util.function.DoublePredicate))
     */
    default boolean allMatch(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.allMatch(this, pred);
    }

    /**
     * @param seq
     * @param pred
     * @return
     */
    default boolean allMatch(@Nonnull DoubleSequence seq, @Nonnull BiDoublePredicate pred) {
        return InternalDoubleSeq.allMatch(this, seq, pred);
    }

    /**
     * @param pred
     * @return
     * @see DoubleStream#anyMatch(java.util.function.DoublePredicate))
     */
    default boolean anyMatch(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.anyMatch(this, pred);
    }

    /**
     *
     * @param initial
     * @param fn
     * @return
     * @see DoubleStream#reduce(double, java.util.function.DoubleBinaryOperator)
     */
    default double reduce(double initial, @Nonnull DoubleBinaryOperator fn) {
        return InternalDoubleSeq.reduce(this, initial, fn);
    }

    default int indexOf(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.firstIndexOf(this, pred);
    }

    default int lastIndexOf(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.lastIndexOf(this, pred);
    }

    default int count(DoublePredicate pred) {
        return InternalDoubleSeq.count(this, pred);
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
     * Range of a sequence. Other way for extracting information
     *
     * @param beg The first item
     * @param end The last item
     * @return
     */
    default DoubleSequence range(int beg, int end) {
        return end <= beg ? DoubleSequence.empty() : extract(beg, end - beg);
    }

    /**
     * Returns a new array of doubles in reverse order
     *
     * @return
     */
    default DoubleSequence reverse() {
        final int n = length();
        return new InternalDoubleSeq.IntToDoubleSequence(n, i -> get(n - 1 - i));
    }

    static boolean equals(double a, double b, double epsilon) {
        return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
    }

    static String format(DoubleSequence rd) {
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

    static String format(DoubleSequence rd, String fmt) {
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

    static double round(double r, final int ndec) {
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

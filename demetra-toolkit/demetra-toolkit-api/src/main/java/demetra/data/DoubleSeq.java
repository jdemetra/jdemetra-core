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

import demetra.design.Development;
import demetra.design.ReturnNew;
import demetra.util.IntList;
import demetra.util.function.BiDoublePredicate;
import internal.data.InternalDoubleSeq;
import internal.data.InternalDoubleSeqCursor;
import internal.data.InternalDoubleSeqMath;
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
 * Describes a sequence of doubles.
 *
 * @author Philippe Charles
 */
@Development(status = Development.Status.Release)
public interface DoubleSeq extends BaseSeq {

    /**
     * Returns the <code>double</code> value at the specified index. An index
     * ranges from zero to <tt>length() - 1</tt>. The first <code>double</code>
     * value of the sequence is at index zero, the next at index one, and so on,
     * as for array indexing.
     *
     * @param index the index of the <code>double</code> value to be returned
     *
     * @return the specified <code>double</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is
     * negative or not less than <tt>length()</tt>
     */
    double get(@Nonnegative int index) throws IndexOutOfBoundsException;

    /**
     * Creates a new cursor at the beginning of this object. The first data will
     * be retrieved by "next".
     *
     * @return
     */
    @Nonnull
    default DoubleSeqCursor cursor() {
        return new InternalDoubleSeqCursor.DefaultDoubleSeqCursor(this);
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
        InternalDoubleSeq.copyToByCursor(this, buffer, offset);
    }

    /**
     * @return @see DoubleStream#toArray()
     */
    @ReturnNew
    @Nonnull
    default double[] toArray() {
        return InternalDoubleSeq.toArrayByCursor(this);
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
     *
     * @return
     * @see DoubleStream#allMatch(java.util.function.DoublePredicate))
     */
    default boolean allMatch(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.allMatchByCursor(this, pred);
    }

    /**
     * @param seq
     * @param pred
     *
     * @return
     */
    default boolean allMatch(@Nonnull DoubleSeq seq, @Nonnull BiDoublePredicate pred) {
        return InternalDoubleSeq.allMatchByCursor(this, seq, pred);
    }

    /**
     * @param pred
     *
     * @return
     * @see DoubleStream#anyMatch(java.util.function.DoublePredicate))
     */
    default boolean anyMatch(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.anyMatchByCursor(this, pred);
    }

    /**
     *
     * @param initial
     * @param fn
     *
     * @return
     * @see DoubleStream#reduce(double, java.util.function.DoubleBinaryOperator)
     */
    default double reduce(double initial, @Nonnull DoubleBinaryOperator fn) {
        return InternalDoubleSeq.reduceByCursor(this, initial, fn);
    }

    default int indexOf(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.firstIndexOfByCursor(this, pred);
    }

    default int lastIndexOf(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.lastIndexOf(this, pred);
    }

    default int count(DoublePredicate pred) {
        return InternalDoubleSeq.countByCursor(this, pred);
    }

    @Nonnull
    default DoubleSeq map(@Nonnull DoubleUnaryOperator fn) {
        return new InternalDoubleSeq.IntToDoubleSequence(length(), i -> fn.applyAsDouble(get(i)));
    }

    /**
     * Makes an extract of this data block.
     *
     * @param start The position of the first extracted item.
     * @param length The number of extracted items. The size of the result could
     * be smaller than length, if the data block doesn't contain enough items.
     * Cannot be null.
     *
     * @return A new (read only) toArray block. Cannot be null (but the length
     * of the result could be 0.
     */
    @Nonnull
    default DoubleSeq extract(@Nonnegative final int start, @Nonnegative final int length) {
        return DoubleSeq.onMapping(length, i -> get(start + i));
    }

    @Nonnull
    default DoubleSeq extract(@Nonnegative final int start, @Nonnegative final int length, final int increment) {
        return DoubleSeq.onMapping(length, i -> get(start + i * increment));
    }

    /**
     * Drops some items at the beginning and/or at the end of the array
     *
     * @param beg The number of items dropped at the beginning
     * @param end The number of items dropped at the end
     *
     * @return The shortened array
     */
    default DoubleSeq drop(int beg, int end) {
        return extract(beg, length() - beg - end);
    }

    /**
     * Range of a sequence. Other way for extracting information
     *
     * @param beg The first item
     * @param end The last item
     *
     * @return
     */
    default DoubleSeq range(int beg, int end) {
        return end <= beg ? DoubleSeq.empty() : extract(beg, end - beg);
    }

    /**
     * Returns a new array of doubles in reverse order
     *
     * @return
     */
    default DoubleSeq reverse() {
        final int n = length();
        return new InternalDoubleSeq.IntToDoubleSequence(n, i -> get(n - 1 - i));
    }

    default int[] search(final DoublePredicate pred) {
        IntList list = new IntList();
        int n = length();
        DoubleSeqCursor cell = cursor();
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.getAndNext())) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    default int search(final DoublePredicate pred, final int[] first) {
        int n = length();
        DoubleSeqCursor cell = cursor();
        int cur = 0;
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.getAndNext())) {
                first[cur++] = j;
                if (cur == first.length) {
                    return cur;
                }
            }
        }
        return cur;
    }

    default DoubleSeq fn(DoubleUnaryOperator fn) {
        double[] data = toArray();
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(data[i]);
        }
        return DoubleSeq.of(data);
    }

    default DoubleSeq fn(int lag, DoubleBinaryOperator fn) {
        int n = length() - lag;
        if (n <= 0) {
            return null;
        }
        double[] result = new double[n];
        for (int j = 0; j < lag; ++j) {
            double prev = get(j);
            for (int i = j; i < n; i += lag) {
                double next = get(i + lag);
                result[i] = fn.applyAsDouble(prev, next);
                prev = next;
            }
        }
        return DoubleSeq.of(result);
    }

    default DoubleSeq extend(@Nonnegative int nbeg, @Nonnegative int nend) {
        int n = length() + nbeg + nend;
        double[] result = new double[n];
        for (int i = 0; i < nbeg; ++i) {
            result[i] = Double.NaN;
        }
        copyTo(result, nbeg);
        for (int i = n - nend; i < n; ++i) {
            result[i] = Double.NaN;
        }
        return DoubleSeq.of(result);
    }

    default DoubleSeq select(DoublePredicate pred) {
        double[] x = toArray();
        int cur = 0;
        for (int i = 0; i < x.length; ++i) {
            if (pred.test(x[i])) {
                if (cur < i) {
                    x[cur] = x[i];
                }
                ++cur;
            }
        }
        if (cur == x.length) {
            return DoubleSeq.of(x);
        } else {
            double[] xc = new double[cur];
            System.arraycopy(x, 0, xc, 0, cur);
            return DoubleSeq.of(xc);
        }
    }

    default DoubleSeq op(DoubleSeq b, DoubleBinaryOperator op) {
        double[] data = toArray();
        DoubleSeqCursor cursor = b.cursor();
        for (int i = 0; i < data.length; ++i) {
            data[i] = op.applyAsDouble(data[i], cursor.getAndNext());
        }
        return DoubleSeq.of(data);
    }

    default DoubleSeq fastOp(DoubleSeq b, DoubleBinaryOperator op) {
        int n = length();
        return DoubleSeq.onMapping(n, i -> get(i) + b.get(i));
    }

    default DoubleSeq commit() {
        return DoubleSeq.of(toArray());
    }

    default double sum() {
        return InternalDoubleSeqMath.sum(this);
    }

    default double average() {
        return InternalDoubleSeqMath.average(this);
    }

    default double ssq() {
        return InternalDoubleSeqMath.ssq(this);
    }

    default double ssqc(double mean) {
        return InternalDoubleSeqMath.ssqc(this, mean);
    }

    default double sumWithMissing() {
        return InternalDoubleSeqMath.sumWithMissing(this);
    }

    default double ssqWithMissing() {
        return InternalDoubleSeqMath.ssqWithMissing(this);
    }

    default double ssqcWithMissing(final double mean) {
        return InternalDoubleSeqMath.ssqcWithMissing(this, mean);
    }

    default double averageWithMissing() {
        return InternalDoubleSeqMath.averageWithMissing(this);
    }

    public default double norm1() {
        return InternalDoubleSeqMath.norm1(this);
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    default double norm2() {
        return InternalDoubleSeqMath.norm2(this);
    }

    default double fastNorm2() {
        return InternalDoubleSeqMath.fastNorm2(this);
    }

    /**
     * Computes the infinite-norm of this src block
     *
     * @return Returns min{|src(i)|}
     */
    default double normInf() {
        return InternalDoubleSeqMath.normInf(this);
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    default int getRepeatCount() {
        return InternalDoubleSeqMath.getRepeatCount(this);
    }

    default double dot(DoubleSeq data) {
        return InternalDoubleSeqMath.dot(this, data);
    }

    default double jdot(DoubleSeq data, int pos) {
        return InternalDoubleSeqMath.jdot(this, data, pos);
    }

    default double distance(DoubleSeq data) {
        return InternalDoubleSeqMath.distance(this, data);
    }

    default DoubleSeq removeMean() {
        return InternalDoubleSeqMath.removeMean(this);
    }

    default DoubleSeq delta(int lag) {
        return InternalDoubleSeqMath.delta(this, lag);
    }

    default DoubleSeq delta(int lag, int pow) {
        return InternalDoubleSeqMath.delta(this, lag, pow);
    }

    default DoubleSeq log() {
        return InternalDoubleSeqMath.log(this);
    }

    default DoubleSeq exp() {
        return InternalDoubleSeqMath.exp(this);
    }

    static boolean equals(double a, double b, double epsilon) {
        return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
    }

    static String format(DoubleSeq rd) {
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

    static String format(DoubleSeq rd, String fmt) {
        DecimalFormat df = new DecimalFormat(fmt);
        StringBuilder builder = new StringBuilder();
        int n = rd.length();
        if (n > 0) {
            builder.append(df.format(rd.get(0)));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(df.format(rd.get(i)));
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

    //<editor-fold defaultstate="collapsed" desc="Factories">
    static final double[] EMPTYARRAY = new double[0];

    /**
     * Creates a new value using an array of doubles.
     *
     * @param data
     *
     * @return
     */
    @Nonnull
    static DoubleSeq of(@Nonnull double[] data) {
        return new InternalDoubleSeq.MultiDoubleSeq(data);
    }

    /**
     *
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     *
     * @return
     */
    @Nonnull
    static DoubleSeq of(@Nonnull double[] data, @Nonnegative int start, @Nonnegative int len) {
        return new InternalDoubleSeq.SubDoubleSeq(data, start, len);
    }

    /**
     * Makes a sequence of regularly spaced doubles
     *
     * @param data Storage
     * @param start Position of the first item (non negative)
     * @param len Number of items (non negative)
     * @param inc Increment in the underlying storage of two succesive items
     *
     * @return
     */
    @Nonnull
    static DoubleSeq of(@Nonnull double[] data, @Nonnegative int start, @Nonnegative int len, int inc) {
        return new InternalDoubleSeq.RegularlySpacedDoubles(data, start, len, inc);
    }

    @Nonnull
    static DoubleSeq empty() {
        return InternalDoubleSeq.EmptyDoubleSeq.DOUBLE_SEQ;
    }

    @Nonnull
    static DoubleSeq of(double value) {
        return new InternalDoubleSeq.SingleDoubleSeq(value);
    }

    @Nonnull
    static DoubleSeq copyOf(@Nonnull double... data) {
        switch (data.length) {
            case 0:
                return empty();
            case 1:
                return DoubleSeq.of(data[0]);
            default:
                return of(data.clone());
        }
    }

    @Nonnull
    static DoubleSeq copyOf(@Nonnull DoubleStream stream) {
        return of(stream.toArray());
    }

    @Nonnull
    static DoubleSeq onMapping(@Nonnegative int length, @Nonnull IntToDoubleFunction fn) {
        return new InternalDoubleSeq.IntToDoubleSequence(length, fn);
    }
    //</editor-fold>
}

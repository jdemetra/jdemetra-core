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
import demetra.design.Internal;
import demetra.design.ReturnNew;
import demetra.util.IntList;
import demetra.util.function.BiDoublePredicate;
import internal.data.ArrayBaseSeq;
import internal.data.InternalDefaultCursors;
import internal.data.InternalDoubleSeq;
import internal.data.EmptyBaseSeq;
import internal.data.SingleBaseSeq;
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
        return new InternalDefaultCursors.DefaultDoubleSeqCursor(this);
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
    @ReturnNew
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
     *
     * @return
     * @see DoubleStream#allMatch(java.util.function.DoublePredicate))
     */
    default boolean allMatch(@Nonnull DoublePredicate pred) {
        return InternalDoubleSeq.allMatch(this, pred);
    }

    /**
     * @param seq
     * @param pred
     *
     * @return
     */
    default boolean allMatch(@Nonnull DoubleSeq seq, @Nonnull BiDoublePredicate pred) {
        return InternalDoubleSeq.allMatch(this, seq, pred);
    }

    /**
     * @param pred
     *
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
     *
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

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Descriptive statistics (with default implementations">
    default double sum() {
        return reduce(0, (s, x) -> s + x);
    }

    default double average() {
        return reduce(0, (s, x) -> s + x) / length();
    }

    default double ssq() {
        return reduce(0, (s, x) -> s + x * x);
    }

    default double ssqc(double mean) {
        return reduce(0, (s, x) -> {
            x -= mean;
            return s + x * x;
        });
    }

    default double sumWithMissing() {
        int n = length();
        double s = 0;
        DoubleSeqCursor cell = cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    default double ssqWithMissing() {
        int n = length();
        double s = 0;
        DoubleSeqCursor cell = cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    default double ssqcWithMissing(final double mean) {
        int n = length();
        double s = 0;
        DoubleSeqCursor cell = cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext() - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    default double averageWithMissing() {
        int n = length();
        int m = 0;
        double s = 0;
        DoubleSeqCursor cell = cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                s += cur;
            } else {
                m++;
            }
        }
        return s / (n - m);
    }

    public default double norm1() {
        int n = length();
        double nrm = 0;
        DoubleSeqCursor cur = cursor();
        for (int i = 0; i < n; ++i) {
            nrm += Math.abs(cur.getAndNext());
        }
        return nrm;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    default double norm2() {
        int n = length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(get(0));
            default:
                double scale = 0;
                double ssq = 1;
                DoubleSeqCursor cell = cursor();
                for (int i = 0; i < n; ++i) {
                    double cur = cell.getAndNext();
                    if (cur != 0) {
                        double absxi = Math.abs(cur);
                        if (scale < absxi) {
                            double s = scale / absxi;
                            ssq = 1 + ssq * s * s;
                            scale = absxi;
                        } else {
                            double s = absxi / scale;
                            ssq += s * s;
                        }
                    }
                }
                return scale * Math.sqrt(ssq);
        }
    }

    default double fastNorm2() {
        int n = length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(get(0));
            default:
                DoubleSeqCursor cell = cursor();
                double ssq = 0;
                for (int i = 0; i < n; ++i) {
                    double cur = cell.getAndNext();
                    if (cur != 0) {
                        ssq += cur * cur;
                    }
                }
                return Math.sqrt(ssq);
        }
    }

    /**
     * Computes the infinite-norm of this src block
     *
     * @return Returns min{|src(i)|}
     */
    default double normInf() {
        int n = length();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(get(0));
            DoubleSeqCursor cell = cursor();
            for (int i = 1; i < n; ++i) {
                double tmp = Math.abs(cell.getAndNext());
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    default int getRepeatCount() {
        int i = 0;
        int n = length();
        DoubleSeqCursor cell = cursor();
        double prev = 0;
        while (i++ < n) {
            prev = cell.getAndNext();
            if (Double.isFinite(prev)) {
                break;
            }
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        for (; i < n; ++i) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                if (cur == prev) {
                    ++c;
                } else {
                    prev = cur;
                }
            }
        }
        return c;
    }

    default double dot(DoubleSeq data) {
        int n = length();
        double s = 0;
        DoubleSeqCursor cur = cursor();
        DoubleSeqCursor xcur = data.cursor();
        for (int i = 0; i < n; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    default double jdot(DoubleSeq data, int pos) {
        int n = length();
        double s = 0;
        DoubleSeqCursor cur = cursor();
        DoubleSeqCursor xcur = data.cursor();
        for (int i = 0; i < pos; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        for (int i = pos; i < n; i++) {
            s -= cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    default double distance(DoubleSeq data) {
        double scale = 0;
        double ssq = 1;
        DoubleSeqCursor cur = cursor();
        DoubleSeqCursor xcur = data.cursor();
        int n = length();
        for (int i = 0; i < n; ++i) {
            double x = cur.getAndNext(), y = xcur.getAndNext();
            if (Double.compare(x, y) != 0) {
                double d = x - y;
                if (d != 0) {
                    double absxi = Math.abs(d);
                    if (scale < absxi) {
                        double s = scale / absxi;
                        ssq = 1 + ssq * s * s;
                        scale = absxi;
                    } else {
                        double s = absxi / scale;
                        ssq += s * s;
                    }
                }
            }
        }
        return scale * Math.sqrt(ssq);
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

    default DoubleSeq removeMean() {
        double[] y = toArray();
        double s = 0;
        for (int i = 0; i < y.length; ++i) {
            s += y[i];
        }
        s /= y.length;
        for (int i = 0; i < y.length; ++i) {
            y[i] -= s;
        }
        return DoubleSeq.of(y);
    }

    default DoubleSeq fn(DoubleUnaryOperator fn) {
        double[] data = toArray();
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(data[i]);
        }
        return DoubleSeq.of(data);
    }

    default DoubleSeq fastFn(DoubleUnaryOperator fn) {
        return DoubleSeq.onMapping(length(), i -> fn.applyAsDouble(get(i)));
    }

    default DoubleSeq fn(int lag, DoubleBinaryOperator fn) {
        int n = length() - lag;
        if (n <= 0) {
            return null;
        }
        double[] nvalues = new double[n];
        for (int j = 0; j < lag; ++j) {
            double prev = get(j);
            for (int i = j; i < n; i += lag) {
                double next = get(i + lag);
                nvalues[i] = fn.applyAsDouble(prev, next);
                prev = next;
            }
        }
        return DoubleSeq.of(nvalues);
    }

    default DoubleSeq extend(@Nonnegative int nbeg, @Nonnegative int nend) {
        int n = length() + nbeg + nend;
        double[] nvalues = new double[n];
        for (int i = 0; i < nbeg; ++i) {
            nvalues[i] = Double.NaN;
        }
        copyTo(nvalues, nbeg);
        for (int i = n - nend; i < n; ++i) {
            nvalues[i] = Double.NaN;
        }
        return DoubleSeq.of(nvalues);
    }

    default DoubleSeq delta(int lag) {
        return fn(lag, (x, y) -> y - x);
    }

    default DoubleSeq delta(int lag, int pow) {
        DoubleSeq ns = this;
        for (int i = 0; i < pow; ++i) {
            ns = ns.fn(lag, (x, y) -> y - x);
        }
        return ns;
    }

    default DoubleSeq log() {
        return fn(x -> Math.log(x));
    }

    default DoubleSeq exp() {
        return fn(x -> Math.exp(x));
    }

    default DoubleSeq op(DoubleSeq b, DoubleBinaryOperator op) {
        double[] data = toArray();
        DoubleSeqCursor reader = b.cursor();
        for (int i = 0; i < data.length; ++i) {
            data[i] = op.applyAsDouble(data[i], reader.getAndNext());
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
        return new ArrayBaseSeq.ArrayDoubleSeq(data);
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
        return new InternalDoubleSeq.PartialDoubleArray(data, start, len);
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
        return EmptyBaseSeq.EmptyDoubleSeq.DOUBLE_SEQ;
    }

    @Nonnull
    static DoubleSeq of(double value) {
        return new SingleBaseSeq.SingleDoubleSeq(value);
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

    @Nonnull
    static DoubleSeq onMapping(@Nonnull DoubleSeq source, @Nonnull DoubleUnaryOperator fn) {
        return new InternalDoubleSeq.IntToDoubleSequence(source.length(), i -> fn.applyAsDouble(source.get(i)));
    }
    //</editor-fold>
}

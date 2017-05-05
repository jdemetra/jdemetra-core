/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import demetra.utilities.DoubleList;
import java.util.function.Supplier;

/**
 *
 * @author Jean Palate
 */
public final class DataBlock implements Doubles {

    @FunctionalInterface
    public static interface DataBlockFunction {

        /**
         * Real function on an array of doubles. This interface is mainly used
         * with the corresponding "apply" method. For example: Matrix M=...
         * DataBlock rsum=... rsum.apply(M.columnsIterator(), col->col.sum());
         *
         * @param data The toArray
         * @return the function result
         */
        double apply(DataBlock data);
    }

    public static final DataBlock EMPTY = new DataBlock(new double[0], 0, 0, 1);

    //<editor-fold defaultstate="collapsed" desc="Static factories">
    /**
     * Creates a toArray block of a given length. The buffer is created
     * internally
     *
     * @param n The number of elements
     * @return
     */
    public static DataBlock make(int n) {
        return n == 0 ? EMPTY : new DataBlock(new double[n], 0, n, 1);
    }

    /**
     * Envelope around an array of doubles.
     *
     * @param data The array of doubles. the toArray are not copied (they might
     * be modified externally).
     * @return
     */
    public static DataBlock of(double[] data) {
        return new DataBlock(data, 0, data.length, 1);
    }

    /**
     * Envelope around a part of an array of doubles.
     *
     * @param data The array of doubles. the toArray are not copied.
     * @param start The starting position (included)
     * @param end The ending position (excluded)
     * @return
     * @throws IllegalArgumentException is thrown if the end position is not
     * after the reader position.
     */
    public static DataBlock of(double[] data, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new DataBlock(data, start, end, 1);
    }

    /**
     * Envelope around a part of an array of doubles.
     *
     * @param data The array of doubles. the toArray are not copied.
     * @param start The starting position (included)
     * @param end The ending position (excluded)
     * @param inc The differences between two successive positions. Might be
     * negative.
     * @return
     * @throws (end-start) must be a positive multiple of inc.
     */
    public static DataBlock of(double[] data, int start, int end, int inc) {
//        if (inc == 1) {
//            return of(toArray, reader, end);
//        }
        if ((end - start) % inc != 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        if ((end - start) / inc < 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new DataBlock(data, start, end, inc);
    }

    /**
     * Envelope around a copy of read only Doubles.
     *
     * @param data The Doubles being copied
     * @return
     */
    public static DataBlock copyOf(Doubles data) {
        double[] x = new double[data.length()];
        data.copyTo(x, 0);
        return new DataBlock(x, 0, x.length, 1);
    }

    /**
     * Envelope around a copy of an array of doubles.
     *
     * @param data The array of doubles. the toArray are copied
     * @return
     */
    public static DataBlock copyOf(double[] data) {
        return new DataBlock(data.clone(), 0, data.length, 1);
    }

    /**
     * Select the toArray that match a given criterion and put them in a new
     * DataBlock.
     *
     * @param data The toArray
     * @param pred The selection criterion
     * @return
     */
    public static DataBlock select(Doubles data, DoublePredicate pred) {
        DoubleList list = new DoubleList();
        int n = data.length();
        for (int i = 0; i < n; ++i) {
            double cur = data.get(i);
            if (pred.test(cur)) {
                list.add(cur);
            }
        }
        return DataBlock.of(list.toArray());
    }

    //</editor-fold>
    final double[] data;
    final int inc;
    int beg, end;

    DataBlock(double[] data, int start, int end, int inc) {
        this.data = data;
        beg = start;
        this.end = end;
        this.inc = inc;
    }

    @Override
    public String toString() {
        return Doubles.toString(this);
    }

    @Override
    public CellReader reader() {
        return CellReader.of(data, beg, inc);
    }

    public Cell cells() {
        return Cell.of(data, beg, inc);
    }

    public void copyFrom(double[] buffer, int start) {
        for (int i = beg, j = start; i != end; i += inc, ++j) {
            data[i] = buffer[j];
        }
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        for (int i = beg, j = start; i != end; i += inc, ++j) {
            buffer[j] = data[i];
        }
    }

    @Override
    public DataBlock extract(int start, int length) {
        return new DataBlock(data, beg + start * inc, beg + (start + length) * inc, inc);
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    @Override
    public double norm2() {
        int n = length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(get(0));
            default:
                double scale = 0;
                double ssq = 1;
                for (int i = beg; i != end; i += inc) {
                    double cur = data[i];
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

    @Override
    public double fastNorm2() {
        int n = length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(get(0));
            default:
                double ssq = 0;
                for (int i = beg; i != end; i += inc) {
                    double cur = data[i];
                    if (cur != 0) {
                        ssq += cur * cur;
                    }
                }
                return Math.sqrt(ssq);
        }
    }

    /**
     * Creates a new src block from the given one. The increment and the
     * starting position are relative to the existing one. More precisely, if
     * the increment in the current src block is inc0 and if its starting
     * position (in the underlying buffer) is start0, The new src block will
     * reader at position start0 + reader*inc0 and its increment will be
     * inc0*inc1.
     *
     * @param start The starting position (in the current src block).
     * @param count The number copyOf items in the selection. If count is -1,
     * the largest extract is returned.
     * @param inc The increment copyOf the selection.
     * @return A new block is returned.
     */
    public DataBlock extract(int start, int count, int inc) {
        int i0 = beg + start * this.inc, i1, ninc;
        ninc = inc * this.inc;
        if (count == -1) {
            // not optimized. We go from i0 to i1 by step copyOf ninc (i1 = i0 + n*ninc)
            // (i1-ninc) must be <= (end-linc) if ninc > 0 or >= end-linc if ninc <0
            // case linc > 0 : n = 1 + (end - linc - i0) / ninc
            int n = 0;
            if ((this.inc > 0 && i0 <= end - this.inc) || (this.inc < 0 && i0 >= end - this.inc)) {
                if (inc > 0) {
                    n = 1 + (end - this.inc - i0) / ninc;
                } else {
                    n = 1 + (beg - i0) / ninc;
                }
            }
            i1 = i0 + n * ninc;
        } else {
            i1 = i0 + ninc * count;
        }
        return new DataBlock(data, i0, i1, ninc);
    }

    public DataWindow window() {
        return new DataWindow(data, beg, end, inc);
    }

    public DataWindow left() {
        return new DataWindow(data, beg, beg, inc);
    }

    public DataBlock range(int beg, int end) {
        return new DataBlock(data, this.beg + beg * inc, this.beg + end * inc, inc);
    }

    public DataBlock drop(int beg, int end) {
        return new DataBlock(data, this.beg + beg * inc, this.end - end * inc, inc);
    }

    public DataBlock extend(int beg, int end) {
        return new DataBlock(data, this.beg - beg * inc, this.end + end * inc, inc);
    }

    public DataWindow window(int beg, int end) {
        return new DataWindow(data, this.beg + beg * inc, this.beg + end * inc, inc);
    }

    public DataBlock reverse() {
        return new DataBlock(data, end - inc, beg - inc, -inc);
    }

    public void bshiftAndNegSum() {
        int imax = end - inc;
        double s = data[beg];
        if (inc == 1) {
            for (int i = beg; i < imax; ++i) {
                double z = data[i + 1];
                data[i] = z;
                s += z;
            }
        } else {
            for (int i = beg; i != imax; i += inc) {
                double z = data[i + inc];
                data[i] = z;
                s += z;
            }
        }
        data[imax] = -s;
    }

    public void bshiftAndSum() {
        int imax = end - inc;
        double s = data[beg];
        if (inc == 1) {
            for (int i = beg; i < imax; ++i) {
                double z = data[i + 1];
                data[i] = z;
                s += z;
            }
        } else {
            for (int i = beg; i != imax; i += inc) {
                double z = data[i + inc];
                data[i] = z;
                s += z;
            }
        }
        data[imax] = s;
    }

    public void bshiftAndZero() {
        int imax = end - inc;
        if (inc == 1) {
            for (int i = beg; i < imax; ++i) {
                double z = data[i + 1];
                data[i] = z;
            }
        } else {
            for (int i = beg; i != imax; i += inc) {
                double z = data[i + inc];
                data[i] = z;
            }
        }
        data[imax] = 0;
    }

    public void brotate() {
        int imax = end - inc;
        double s=data[beg];
        if (inc == 1) {
            for (int i = beg; i < imax; ++i) {
                double z = data[i + 1];
                data[i] = z;
            }
        } else {
            for (int i = beg; i != imax; i += inc) {
                double z = data[i + inc];
                data[i] = z;
            }
        }
        data[imax] = s;
    }

    public void fshift(int n) {
        if (inc == 1) {
            int i0 = end - inc, i1 = beg + n;
            for (int i = i0; i >= i1; --i) {
                data[i] = data[i - n];
            }
        } else {
            int i0 = end - inc, i1 = beg + (n - 1) * inc, ninc = n * inc;
            for (int i = i0; i != i1; i -= inc) {
                data[i] = data[i - ninc];
            }
        }
    }

    public void bshift(int n) {
        if (inc == 1) {
            int i0 = beg, i1 = end - n;
            for (int i = i0; i < i1; ++i) {
                data[i] = data[i + n];
            }
        } else {
            int i0 = beg, ninc = n * inc, i1 = end - ninc;
            for (int i = i0; i != i1; i += inc) {
                data[i] = data[i + ninc];
            }
        }
    }

    public void fshiftAndNegSum() {
        double s = data[beg];
        if (inc == 1) {
            for (int i = end - 1; i > beg; --i) {
                s += data[i];
                data[i] = data[i - i];
            }
        } else {
            for (int i = end - inc; i != beg; i -= inc) {
                s += data[i];
                data[i] = data[i - inc];
            }
        }
        data[beg] = -s;
    }

    public void fshiftAndSum() {
        double s = data[beg];
        if (inc == 1) {
            for (int i = end - 1; i > beg; --i) {
                s += data[i];
                data[i] = data[i - i];
            }
        } else {
            for (int i = end - inc; i != beg; i -= inc) {
                s += data[i];
                data[i] = data[i - inc];
            }
        }
        data[beg] = s;
    }

    public void frotate() {
        int last = end - inc;
        double s = data[last];
        if (inc == 1) {
            for (int i = last; i > beg; --i) {
                data[i] = data[i - i];
            }
        } else {
            for (int i = last; i != beg; i -= inc) {
                s += data[i];
                data[i] = data[i - inc];
            }
        }
        data[beg] = s;
    }

    public void fshiftAndZero() {
        if (inc == 1) {
            for (int i = end - inc; i > beg; --i) {
                data[i] = data[i - i];
            }
        } else {
            for (int i = end - inc; i != beg; i -= inc) {
                data[i] = data[i - inc];
            }
        }
        data[beg] = 0;
    }

    public final void set(int idx, double value) {
        data[beg + idx * inc] = value;
    }

    @Override
    public final double get(int idx) {
        return data[beg + idx * inc];
    }

    @Override
    public final int length() {
        return (end - beg) / inc;
    }

    public final double[] getStorage() {
        return data;
    }

    public final int getStartPosition() {
        return beg;
    }

    public final int getEndPosition() {
        return end;
    }

    public final int getLastPosition() {
        return end - inc;
    }

    public final int getIncrement() {
        return inc;
    }

    public double dot(DataBlock x) {
        double s = 0;
        if (inc == 1) {
            if (x.inc == 1) {
                for (int i = beg, j = x.beg; i != end; ++i, ++j) {
                    s += data[i] * x.data[j];
                }
            } else {
                for (int i = beg, j = x.beg; i != end; ++i, j += x.inc) {
                    s += data[i] * x.data[j];
                }
            }
        } else if (x.inc == 1) {
            for (int i = beg, j = x.beg; i != end; i += inc, ++j) {
                s += data[i] * x.data[j];
            }
        } else {
            for (int i = beg, j = x.beg; i != end; i += inc, j += x.inc) {
                s += data[i] * x.data[j];
            }
        }
        return s;
    }

    public void robustDot(DataBlock x, DoubleAccumulator sum) {
        if (inc == 1) {
            if (x.inc == 1) {
                for (int i = beg, j = x.beg; i != end; ++i, ++j) {
                    sum.add(data[i] * x.data[j]);
                }
            } else {
                for (int i = beg, j = x.beg; i != end; ++i, j += x.inc) {
                    sum.add(data[i] * x.data[j]);
                }
            }
        } else if (x.inc == 1) {
            for (int i = beg, j = x.beg; i != end; i += inc, ++j) {
                sum.add(data[i] * x.data[j]);
            }
        } else {
            for (int i = beg, j = x.beg; i != end; i += inc, j += x.inc) {
                sum.add(data[i] * x.data[j]);
            }
        }
    }

    @Override
    public double sum() {
        double s = 0;
        for (int i = beg; i != end; i += inc) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    @Override
    public double ssq() {
        int n = length();
        double s = 0;
        for (int i = beg; i != end; i += inc) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    @Override
    public double ssqc(double mean) {
        int n = length();
        double s = 0;
        for (int i = beg; i != end; i += inc) {
            double cur = data[i] - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    @Override
    public double average() {
        int n = length();
        int m = 0;
        double s = 0;
        for (int i = beg; i != end; i += inc) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                s += cur;
            } else {
                m++;
            }
        }
        return s / (n - m);
    }

    public final void copy(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = xdata[j];
        }
    }

    public final void copy(Doubles x) {
        CellReader cell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = cell.next();
        }
    }

    public final void swap(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            double tmp = xdata[j];
            xdata[j] = data[i];
            data[i] = tmp;
        }
    }

    /**
     * Computes the product copyOf a vector by a matrix and stores the result in
     * this src block this = row * cols. We must have that 1. the length copyOf
     * this src block = the number copyOf columns 2. the length copyOf the
     * vector = the length copyOf each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns copyOf the matrix
     */
    public void product(DataBlock row, Iterator<DataBlock> cols) {
        int idx = beg;
        while (cols.hasNext()) {
            data[idx] = cols.next().dot(row);
            idx += inc;
        }
    }

    public void product(DataBlock row, Iterator<DataBlock> cols, DoubleAccumulator acc) {
        int idx = beg;
        while (cols.hasNext()) {
            acc.reset();
            cols.next().robustDot(row, acc);
            data[idx] = acc.sum();
            idx += inc;
        }
    }

    public void product(Iterator<DataBlock> rows, DataBlock column) {
        int idx = beg;
        while (rows.hasNext()) {
            data[idx] = column.dot(rows.next());
            idx += inc;
        }
    }

    public void robustProduct(Iterator<DataBlock> rows, DataBlock column, DoubleAccumulator acc) {
        int idx = beg;
        while (rows.hasNext()) {
            acc.reset();
            column.robustDot(rows.next(), acc);
            data[idx] = acc.sum();
            idx += inc;
        }
    }

    /**
     * Adds the product copyOf a vector by a matrix to this src block this +=
     * row * cols. We must have that 1. the length copyOf this src block = the
     * number copyOf columns 2. the length copyOf the vector = the length copyOf
     * each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns copyOf the matrix
     */
    public void addProduct(DataBlock row, Iterator<DataBlock> cols) {
        int idx = beg;
        while (cols.hasNext()) {
            data[idx] += cols.next().dot(row);
            idx += inc;
        }
    }

    public void addProduct(Iterator<DataBlock> rows, DataBlock column) {
        int idx = beg;
        while (rows.hasNext()) {
            data[idx] += column.dot(rows.next());
            idx += inc;
        }
    }

    public final void apply(int pos, DoubleUnaryOperator fn) {
        int idx = beg + pos * inc;
        data[idx] = fn.applyAsDouble(data[idx]);
    }

    public final void add(int pos, double d) {
        data[beg + pos * inc] += d;
    }

    public final void mul(int pos, double d) {
        data[beg + pos * inc] *= d;
    }

    public final void sub(int pos, double d) {
        data[beg + pos * inc] -= d;
    }

    public final void div(int pos, double d) {
        if (d != 1) {
            data[beg + pos * inc] /= d;
        }
    }

    public final void set(Iterator<DataBlock> blocks, DataBlockFunction fn) {
        int pos = beg;
        if (inc == 1) {
            while (blocks.hasNext()) {
                data[pos++] = fn.apply(blocks.next());
            }
        } else {
            while (blocks.hasNext()) {
                data[pos] = fn.apply(blocks.next());
                pos += inc;
            }
        }
    }

    public final void apply(Iterator<DataBlock> blocks, DataBlockFunction fn, DoubleBinaryOperator op) {
        int pos = beg;
        while (blocks.hasNext()) {
            data[pos] = op.applyAsDouble(data[pos], fn.apply(blocks.next()));
            pos += inc;
        }
    }

    public final void apply(DataBlock x, DoubleBinaryOperator fn) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = fn.applyAsDouble(data[i], xdata[j]);
        }
    }

    public final void apply(Doubles x, DoubleBinaryOperator fn) {
        CellReader cell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(data[i], cell.next());
        }
    }

    public final void apply(DoubleUnaryOperator fn) {
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] = fn.applyAsDouble(data[i]);
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] = fn.applyAsDouble(data[i]);
            }
        }
    }

    public final void add(double d) {
        if (d == 0) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] += d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] += d;
            }
        }
    }

    public final void sub(double d) {
        if (d == 0) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] -= d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] -= d;
            }
        }
    }

    public final void mul(double d) {
        if (d == 1) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] *= d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] *= d;
            }
        }
    }

    public final void div(double d) {
        if (d == 1) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] /= d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] /= d;
            }
        }
    }

    public final void set(DataBlock x, DoubleUnaryOperator fn) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = fn.applyAsDouble(xdata[j]);
        }
    }

    public final void set(DataBlock x, DataBlock y, DoubleBinaryOperator fn) {
        int ybeg = y.getStartPosition(), yinc = y.getIncrement();
        double[] ydata = y.getStorage();
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg, k = ybeg; i != end; i += inc, j += xinc, k += yinc) {
            data[i] = fn.applyAsDouble(xdata[j], ydata[k]);
        }
    }

    public final void set(Doubles x, DoubleUnaryOperator fn) {
        CellReader xcell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(xcell.next());
        }
    }

    public final void set(Doubles x, Doubles y, DoubleBinaryOperator fn) {
        CellReader xcell = x.reader(), ycell = y.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(xcell.next(), ycell.next());
        }
    }

    // some default shortcuts
    public final void setAY(final double a, DataBlock y) {
        if (a == 0) {
            set(0);
        } else if (a == 1) {
            copy(y);
        } else if (a == -1) {
            set(y, u -> -u);
        } else if (inc == 1 && y.inc == 1) {
            for (int i = beg, j = y.beg; i != end; ++i, ++j) {
                data[i] = a * y.data[j];
            }
        } else {
            for (int i = beg, j = y.beg; i != end; i += inc, j += y.inc) {
                data[i] = a * y.data[j];
            }
        }
    }

    public final void addAY(double a, DataBlock y) {
        if (a == 0) {
            return;
        } else if (a == 1) {
            add(y);
        } else if (a == -1) {
            sub(y);
        } else if (inc == 1 && y.inc == 1) {
            for (int i = beg, j = y.beg; i < end; ++i, ++j) {
                data[i] += a * y.data[j];
            }
        } else {
            for (int i = beg, j = y.beg; i != end; i += inc, j += y.inc) {
                data[i] += a * y.data[j];
            }
        }
    }

    public final void add(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        if (inc == 1 && xinc == 1) {
            for (int i = beg, j = xbeg; i < end; ++i, ++j) {
                data[i] += xdata[j];
            }
        } else {
            for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
                data[i] += xdata[j];
            }
        }

    }

    public final void sub(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] -= xdata[j];
        }
    }

    public final void mul(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] *= xdata[j];
        }
    }

    public final void div(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] /= xdata[j];
        }
    }

    public final void set(DoubleSupplier fn) {
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.getAsDouble();
        }
    }

    public final void set(IntToDoubleFunction fn) {
        if (inc == 1) {
            for (int i = beg, j = 0; i < end; ++i, ++j) {
                data[i] = fn.applyAsDouble(j);
            }
        } else {
            for (int i = beg, j = 0; i != end; i += inc, ++j) {
                data[i] = fn.applyAsDouble(j);
            }
        }
    }

    public final void set(double val) {
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] = val;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] = val;
            }
        }
    }

    @Override
    public double computeIteratively(final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = beg; i != end; i += inc) {
            cur = fn.applyAsDouble(cur, data[i]);
        }
        return cur;
    }

    public String toString(String fmt) {
        return Doubles.toString(this, fmt);
    }

    void slide(int del) {
        beg += del;
        end += del;
    }

}

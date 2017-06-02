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

import demetra.design.Unsafe;
import java.util.Iterator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public final class DataBlock implements Doubles {

    @FunctionalInterface
    public static interface DataBlockFunction {

        /**
         * Real function on an array of doubles. This interface is
         * mainly used with the corresponding "apply" method. For example:
         * Matrix M=... DataBlock rsum=... rsum.apply(M.columnsIterator(),
         * col->col.sum());
         *
         * @param data The data
         * @return the function result
         */
        double apply(@Nonnull DataBlock data);
    }

    public static final DataBlock EMPTY = new DataBlock(new double[0], 0, 0, 1);

    //<editor-fold defaultstate="collapsed" desc="Static factories">
    /**
     * Creates a data block of a given length. The buffer is created
     * internally
     *
     * @param n The number of elements
     * @return
     */
    public static DataBlock make(@Nonnegative int n) {
        return n == 0 ? EMPTY : new DataBlock(new double[n], 0, n, 1);
    }

    /**
     * Envelope around an array of doubles.
     *
     * @param data The array of doubles. the data are not copied (they might be modified externally).
     * @return
     */
    public static DataBlock ofInternal(@Nonnull double[] data) {
        return new DataBlock(data, 0, data.length, 1);
    }

    /**
     * Envelope around a part of an array of doubles.
     *
     * @param data The array of doubles. the data are not copied.
     * @param start The starting position (included)
     * @param end The ending position (excluded)
     * @return
     * @throws IllegalArgumentException is thrown if the end position is not
     * after the reader position.
     */
    public static DataBlock ofInternal(@Nonnull double[] data, @Nonnegative int start, @Nonnegative int end) {
        if (end < start) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new DataBlock(data, start, end, 1);
    }

    /**
     * Envelope around a part of an array of doubles.
     *
     * @param data The array of doubles. the data are not copied.
     * @param start The starting position (included)
     * @param end The ending position (excluded)
     * @param inc The differences between two successive positions. Might be
     * negative.
     * @return
     * @throws (end-start) must be a positive multiple of inc.
     */
    public static DataBlock ofInternal(@Nonnull double[] data, @Nonnegative int start, int end, int inc) {
        if (inc == 1) {
            return ofInternal(data, start, end);
        }
        if ((end - start) % inc != 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        if ((end - start) / inc < 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new DataBlock(data, start, end, inc);
    }

    /**
     * Envelope around a copy of read-only Doubles.
     *
     * @param data The Doubles being copied
     * @return
     */
    public static DataBlock copyOf(@Nonnull DoubleSequence data) {
        double[] x = new double[data.length()];
        data.copyTo(x, 0);
        return new DataBlock(x, 0, x.length, 1);
    }

    /**
     * Envelope around a copy of an array of doubles.
     *
     * @param data The array of doubles. the data are copied
     * @return
     */
    public static DataBlock copyOf(@Nonnull double[] data) {
        return new DataBlock(data.clone(), 0, data.length, 1);
    }

    /**
     * Select the data that match a given criterion and put them in a new
     * DataBlock.
     *
     * @param data The data
     * @param pred The selection criterion
     * @return
     */
    public static DataBlock select(@Nonnull Doubles data, @Nonnull DoublePredicate pred) {
        DoubleList list = new DoubleList();
        int n = data.length();
        for (int i = 0; i < n; ++i) {
            double cur = data.get(i);
            if (pred.test(cur)) {
                list.add(cur);
            }
        }
        return DataBlock.ofInternal(list.toArray());
    }

    //</editor-fold>
    
    final double[] data;
    final int inc;
    int beg, end;

    DataBlock(final double[] data, final int start, final int end, final int inc) {
        this.data = data;
        beg = start;
        this.end = end;
        this.inc = inc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Doubles.toString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellReader reader() {
        return CellReader.of(data, beg, inc);
    }

    /**
     * 
     * @return 
     */
    public Cell cells() {
        return Cell.of(data, beg, inc);
    }

    /**
     * Copies the data stored in a buffer.
     * The buffer must contain enough data for this object (buffer.length-start greater or equal than this.length())
     * @param buffer The buffer that contains the data being copied
     * @param start The position of the first data that will be copied
     */
    public void copyFrom(@Nonnull double[] buffer, @Nonnegative int start) {
        for (int i = beg, j = start; i != end; i += inc, ++j) {
            data[i] = buffer[j];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTo(@Nonnull double[] buffer, @Nonnegative int start) {
        for (int i = beg, j = start; i != end; i += inc, ++j) {
            buffer[j] = data[i];
        }
    }

    /**
     * Takes and extract of this DataBlock, defined by its first position and its length
     * in the current block
     * @param start The first position of the extract in the current block
     * @param length The length of the extract
     * @return A new DataBlock is returned
     */
    @Override
    public DataBlock extract(@Nonnegative int start, @Nonnegative int length) {
        return new DataBlock(data, beg + start * inc, beg + (start + length) * inc, inc);
    }

    /**
     * Computes the euclidian norm of the src block. Based on the
     * "dnrm2" Lapack function.
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

    /**
     * Computes the euclidian norm of the src block. 
     * This implementation is faster than norm2, but less accurate.
     *
     * @return The euclidian norm (&gt=0).
     */
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
     * @param count The number of items in the selection. If count is -1, the largest extract is returned.
     * @param inc The increment of the selection.
     * @return A new block is returned.
     */
    public DataBlock extract(@Nonnegative int start, @Nonnegative int count, int inc) {
        int i0 = beg + start * this.inc, i1, ninc;
        ninc = inc * this.inc;
        if (count == -1) {
            // not optimized. We go from i0 to i1 by step of ninc (i1 = i0 + n*ninc)
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

    public DataBlock range(int beg, int end) {
        return new DataBlock(data, this.beg + beg * inc, this.beg + end * inc, inc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBlock drop(@Nonnegative int beg, @Nonnegative int end) {
        return new DataBlock(data, this.beg + beg * inc, this.end - end * inc, inc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBlock reverse() {
        return new DataBlock(data, end - inc, beg - inc, -inc);
    }

    /**
     * Extends the current DataBlock. The underlying data buffer should be long enough
     * @param beg The number of cells added at the beginning
     * @param end The number of cells added at the end
     * @return 
     */
    public DataBlock extend(int beg, int end) {
        return new DataBlock(data, this.beg - beg * inc, this.end + end * inc, inc);
    }

    /**
     * Transform this DataBlock in the corresponding window.
     * Contrary to a DataBlock, the bounds of a window can be modified 
     * @return 
     */
    public DataWindow window() {
        return new DataWindow(data, beg, end, inc);
    }

    /**
     * 
     * @param beg Start of the window (included)
     * @param end End of the window (excluded)
     * @return 
     */
    public DataWindow window(@Nonnegative int beg, @Nonnegative int end) {
        return new DataWindow(data, this.beg + beg * inc, this.beg + end * inc, inc);
    }

    /**
     * Returns an empty window on the left of this array.
     * Typically, it should be used with a call to the next(n) method 
     * 
     * DataWindow wnd=this.left();
     * wnd.next(n);
     * @return 
     */
    public DataWindow left() {
        return new DataWindow(data, beg, beg, inc);
    }

    /**
     * Makes a deep clone of this object. 
     * The new object will contain a copy of the data of this object
     * @return 
     */
    public DataBlock deepClone() {
        double[] copy = toArray();
        return new DataBlock(copy, 0, copy.length, 1);
    }

    
     //<editor-fold defaultstate="collapsed" desc="shift operations">

    /**
     * Shift the cells to the left and put in the last item the opposite of the
     * sum of the initial data.
     */
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

    /**
     * Shift the cells to the left and put in the last item the 
     * sum of the initial data.
     */
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

    /**
     * Shift the cells to the left and put 0 in the last item.
     */
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

    /**
     * Rotates the cells to the left.
     */
    public void brotate() {
        int imax = end - inc;
        double s = data[beg];
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

    /**
     * Shift the cells n positions to the left 
     * @param n 
     */
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

    /**
     * Shift the cells n positions to the right 
     * @param n 
     */
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

    /**
     * Shift the cells to the right and put in the first item the opposite of the
     * sum of the initial data.
     */
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

    /**
     * Shift the cells to the right and put in the first item the 
     * sum of the initial data.
     */
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

    /**
     * Rotates the cells to the right
     */
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

    /**
     * Shift the cells to the right and put 0 in the first item
     */
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

    //</editor-fold>
    
    /**
     * Sets the given value in the ith cell
     * @param idx The considered cell
     * @param value The new value
     */
    public void set(@Nonnegative int idx, double value) {
        data[beg + idx * inc] = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double get(int idx) {
        return data[beg + idx * inc];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return (end - beg) / inc;
    }

    /**
     * Gets the underlying storage
     * The cells of this object are defined by
     * cell 0: getStorage()[getStartPosition()]
     * cell i: getStorage()[getStartPosition()+i*getIncrement()]
     * last cell (included): getStorage()[getLastPosition()]
     * @return 
     */
    @Unsafe
    public double[] getStorage() {
        return data;
    }

    /**
     * Gets the starting position in the underlying storage
     * @return 
     */
    @Unsafe
    public int getStartPosition() {
        return beg;
    }

    /**
     * Gets the ending position (excluded) in the underlying storage
     * @return 
     */
    public int getEndPosition() {
        return end;
    }

    /**
     * Gets the last position (included) in the underlying storage
     * @return 
     */
    public int getLastPosition() {
        return end - inc;
    }

    /**
     * Gets the distance between two successive cells.
     * Can be negative.
     * 
     * @return 
     */
    public int getIncrement() {
        return inc;
    }

    /**
     * Scalar product between two DataBlocks 
     * 
     * @param x The other DataBlock. Cannot be smaller than this object.
     * @return 
     */
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

    /**
     * Computes in a robust way the scalar product
     * @param x The other DataBlock. Cannot be smaller than this object.
     * @param sum The robust accumulator. Should be correctly initialized. It will
     * contain the result on exit     * 
     */
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

    public void copy(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = xdata[j];
        }
    }

    public void copy(Doubles x) {
        CellReader cell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = cell.next();
        }
    }

    /**
     * Exchanges the data between this DataBlock and the given DataBlock
     * @param x 
     */
    public void swap(DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            double tmp = xdata[j];
            xdata[j] = data[i];
            data[i] = tmp;
        }
    }

    /**
     * Computes the product of a vector by a matrix and stores the result in
 this src block this = row * cols. We must have that 1. the length of this
 src block = the number of columns 2. the length of the vector = the
 length of each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
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
     * Adds the product of a vector by a matrix to this src block this += row *
 cols. We must have that 1. the length of this src block = the number of
 columns 2. the length of the vector = the length of each column. The
 iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
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

    /**
     * Applies the given operator on the data at the given position.
     * It is equivalent to "setPosition(pos, fn.applyAsDouble(get(pos))" 
     * @param pos The position of the data being modified
     * @param fn The unary operator
     */
    public void apply(final @Nonnegative int pos, final @Nonnull DoubleUnaryOperator fn) {
        int idx = beg + pos * inc;
        data[idx] = fn.applyAsDouble(data[idx]);
    }

    /**
     * this(pos)=this(pos)+d
     * 
     * @param pos The position of the data being modified
     * @param d 
     */
    public void add(final @Nonnegative int pos, final double d) {
        data[beg + pos * inc] += d;
    }

    /**
     * this(pos)=this(pos)*d
     * @param pos
     * @param d 
     */
    public void mul(int pos, double d) {
        data[beg + pos * inc] *= d;
    }

    public void sub(int pos, double d) {
        data[beg + pos * inc] -= d;
    }

    public void div(int pos, double d) {
        if (d != 1) {
            data[beg + pos * inc] /= d;
        }
    }

    public void set(Iterator<DataBlock> blocks, DataBlockFunction fn) {
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

    public void apply(Iterator<DataBlock> blocks, DataBlockFunction fn, DoubleBinaryOperator op) {
        int pos = beg;
        while (blocks.hasNext()) {
            data[pos] = op.applyAsDouble(data[pos], fn.apply(blocks.next()));
            pos += inc;
        }
    }

    /**
     * this(i)=fn(this(i), x(i))
     * Apply a given unary operator to all the data
     * @param x
     * @param fn The operator 
     */
    public void apply(@Nonnull DataBlock x, @Nonnull DoubleBinaryOperator fn) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = fn.applyAsDouble(data[i], xdata[j]);
        }
    }

    /**
     * this(i)=fn(this(i), x(i))
     * Apply a given unary operator to all the data
     * @param x
     * @param fn The operator 
     */
    public void apply(@Nonnull Doubles x, @Nonnull DoubleBinaryOperator fn) {
        CellReader cell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(data[i], cell.next());
        }
    }

    /**
     * this(i)=fn(this(i))
     * Apply a given unary operator to all the data
     * @param fn The operator 
     */
    public void apply(@Nonnull final DoubleUnaryOperator fn) {
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

    /**
     * Add d to all the data
     * @param d 
     */
    public void add(double d) {
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

    /**
     * Changes the sign of the data
     */
    public void chs() {
        if (inc == 1) {
            for (int i = beg; i < end; ++i) {
                data[i] = -data[i];
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                data[i] = -data[i];
            }
        }

    }

    /**
     * Subtracts d from all the data 
     * @param d 
     */
    public void sub(double d) {
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

    /**
     * Multiplies all the data by d
     * @param d The multiplier
     */
    public void mul(double d) {
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

    /**
     * divides all the data by d.
     * @param d The divisor
     */
    public void div(double d) {
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

    /**
     * Sets this(i)=fn(x(i))
     * @param x
     * @param fn 
     */
    public void set(@Nonnull DataBlock x, @Nonnull DoubleUnaryOperator fn) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] = fn.applyAsDouble(xdata[j]);
        }
    }

    /**
     * Sets this(i)=fn(x(i), y(i))
     * @param x
     * @param y
     * @param fn 
     */
    public void set(@Nonnull DataBlock x, @Nonnull DataBlock y, @Nonnull DoubleBinaryOperator fn) {
        int ybeg = y.getStartPosition(), yinc = y.getIncrement();
        double[] ydata = y.getStorage();
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg, k = ybeg; i != end; i += inc, j += xinc, k += yinc) {
            data[i] = fn.applyAsDouble(xdata[j], ydata[k]);
        }
    }

    /**
     * Sets this(i)=fn(x(i))
     * @param x
     * @param fn 
     */
    public void set(@Nonnull Doubles x, @Nonnull DoubleUnaryOperator fn) {
        CellReader xcell = x.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(xcell.next());
        }
    }

    /**
     * Sets this(i)=fn(x(i), y(i))
     * @param x
     * @param y
     * @param fn 
     */
    public void set(@Nonnull Doubles x, @Nonnull Doubles y, @Nonnull DoubleBinaryOperator fn) {
        CellReader xcell = x.reader(), ycell = y.reader();
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.applyAsDouble(xcell.next(), ycell.next());
        }
    }

    /**
     * Sets this(i)=a*y(i)
     * @param a
     * @param y 
     */
    public void setAY(final double a, @Nonnull DataBlock y) {
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

    /**
     * Computes this(i)=this(i)+a*y(i)
     * @param a
     * @param y 
     */
    public void addAY(double a, @Nonnull DataBlock y) {
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

    public void add(@Nonnull DataBlock x) {
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

    public void sub(@Nonnull DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] -= xdata[j];
        }
    }

    public void mul(@Nonnull DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] *= xdata[j];
        }
    }

    public void div(@Nonnull DataBlock x) {
        int xbeg = x.getStartPosition(), xinc = x.getIncrement();
        double[] xdata = x.getStorage();
        for (int i = beg, j = xbeg; i != end; i += inc, j += xinc) {
            data[i] /= xdata[j];
        }
    }

    public void set(@Nonnull DoubleSupplier fn) {
        for (int i = beg; i != end; i += inc) {
            data[i] = fn.getAsDouble();
        }
    }

    public void set(IntToDoubleFunction fn) {
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

    public void set(double val) {
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
    public double computeIteratively(final double initial, @Nonnull DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = beg; i != end; i += inc) {
            cur = fn.applyAsDouble(cur, data[i]);
        }
        return cur;
    }

    /**
     * Computes the minimum of this src block
     *
     * @return Returns min{src(i)}
     */
    public double min() {
        if (beg == end) {
            return 0;
        } else {
            double nrm = data[beg];
            for (int ix = beg + inc; ix != end; ix += inc) {
                double tmp = data[ix];
                if (tmp < nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Computes the maximum of this src block
     *
     * @return Returns max{src(i)}
     */
    public double max() {
        if (beg == end) {
            return 0;
        } else {
            double nrm = data[beg];
            for (int ix = beg + inc; ix != end; ix += inc) {
                double tmp = data[ix];
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    public String toString(String fmt) {
        return Doubles.toString(this, fmt);
    }

    void slide(int del) {
        beg += del;
        end += del;
    }

}

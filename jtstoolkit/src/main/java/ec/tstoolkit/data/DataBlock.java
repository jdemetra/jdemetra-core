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
package ec.tstoolkit.data;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.JdkRNG;
import ec.tstoolkit.utilities.DoubleList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

/**
 * A DataBlock represents an array of equally spaced doubles. The "DataBlock" is
 * the key concept for many vector operations. It is intensively used in many
 * computations (matrix, ssf...) of the library.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public final class DataBlock implements IDataBlock, Cloneable {

    public static final DataBlock EMPTY = new DataBlock(null, 0, 0, 0);

    /**
     * The option used by shift operations
     */
    public static enum ShiftOption {

        /**
         * No transformation. (a b c d) becomes (b c d d) in the case of left
         * shift. (a b c d) becomes (a a b c) in the case of right shift.
         */
        None,
        /**
         * Transforms (a b c d) to (b c d a) in the case of left shift, (a b c
         * d) to ( d a b c) in the case of right shift.
         */
        Rotate,
        /**
         * Transforms (a b c d) to (b c d 0) in the case of left shift.
         */
        Zero,
        /**
         * Transforms (a b c d) to (b c d a+b+c+d) in the case of left shift.
         */
        Sum,
        /**
         * Transforms (a b c d) to (b c d -a-b-c-d) in the case of left shift.
         */
        NegSum
    }
    private static final IRandomNumberGenerator RNG = JdkRNG.newRandom();

    /**
     * Creates a src block with random numbers
     *
     * @param len The length of the src block
     * @return A new src block with len random numbers
     */
    public static DataBlock random(final int len) {
        double[] d = new double[len];
        for (int i = 0; i < d.length; ++i) {
            d[i] = RNG.nextDouble();
        }
        return new DataBlock(d);
    }

    /**
     * Creates a copy of the given read only data.
     *
     * @param data The data being copied. May be null or empty.
     * @return A new DataBlock is always returned. May be EMPTY
     */
    public static DataBlock of(IReadDataBlock data) {
        if (data == null || data.getLength() == 0) {
            return EMPTY;
        } else {
            return new DataBlock(data);
        }
    }

    final double[] src;
    final int inc;
    int beg, end;
    /**
     * 0
     */
    public static final double ZERO = 0.0;
    /**
     * 1
     */
    public static final double ONE = 1.0;
    /**
     * Small value, identifying a negligible quantity. 1e-15.
     */
    public static final double EPSILON = 1e-15;

    /**
     * Creates a new src block with a given array of doubles. The src are not
     * copied; the src block is just a wrapper around the array.
     *
     * @param data The array of src
     */
    public DataBlock(final double[] data) {
        this.src = data;
        beg = 0;
        inc = 1;
        if (data != null) {
            end = data.length;
        } else {
            end = 0;
        }
    }

    /**
     * Creates a new src block with a given array of doubles. The src are not
     * copied. We must have that end = beg + n * linc for some n. That
     * relationship is not verified. If it is not respected, the result of some
     * methods could be unpredictable.
     *
     * @param data The array of src
     * @param beg The position in the original array of the first element
     * @param end The position in the original array of the last element
     * (excluded).
     * @param inc The increment of two successive elements of the src block. Can
     * be negative.
     */
    public DataBlock(final double[] data, final int beg, final int end, final int inc) {
        this.src = data;
        this.beg = beg;
        this.end = end;
        this.inc = inc;
    }

    /**
     * Creates a new src block with n items (equal to 0). The underlying buffer
     * is automatically allocated.
     *
     * @param n The number of elements in the new src block. Should be positive.
     */
    public DataBlock(final int n) {
        src = new double[n];
        beg = 0;
        end = n;
        inc = 1;
    }

    /**
     * Creates a new src block from a read only src block. The src a copied
     *
     * @param data The src being copied
     */
    public DataBlock(final IReadDataBlock data) {
        this.src = new double[data.getLength()];
        data.copyTo(this.src, 0);
        beg = 0;
        end = this.src.length;
        inc = 1;
    }

    /**
     * Creates a datablock in an existing array.
     *
     * @param data The underlying array
     * @param istart The starting position
     * @param len The number of items in the block
     * @param inc The increment between to items
     * @return
     */
    public static DataBlock create(final double[] data, final int istart, final int len, final int inc) {
        return new DataBlock(data, istart, istart + len * inc, inc);
    }

    /**
     * Safe creation of a datablock. Reuse of the EMPTY src block
     *
     * @param n
     * @return
     */
    public static DataBlock create(final int n) {
        return n <= 0 ? DataBlock.EMPTY : new DataBlock(n);
    }

    public static DataBlock select(IReadDataBlock data, boolean[] sel) {
        int n = 0;
        for (int i = 0; i < sel.length; ++i) {
            if (sel[i]) {
                ++n;
            }
        }
        DataBlock d = new DataBlock(n);
        for (int i = 0, j = 0; i < sel.length; ++i) {
            if (sel[i]) {
                d.src[j++] = data.get(i);
            }
        }
        return d;
    }

    public static DataBlock select(IReadDataBlock data, int[] isel) {
        int n = isel.length;
        DataBlock d = new DataBlock(n);
        for (int i = 0; i < n; ++i) {
            d.src[i] = data.get(isel[i]);
        }
        return d;
    }

    /**
     * Creates an adjacent datablock in an existing array.
     *
     * @param data The underlying array
     * @param istart The starting position
     * @param len The number of items in the block
     * @return
     */
    public static DataBlock create(final double[] data, final int istart, final int len) {
        return new DataBlock(data, istart, istart + len, 1);
    }

    /**
     * Adds the src block r to this object. this(i) = this(i) + r(i)
     *
     * @param r The added src block. Its length must be &ge the length of this
     * object.
     */
    public void add(final DataBlock r) {
        if (inc == 1 && r.inc == 1) {
            for (int i = beg, j = r.beg; i != end; ++i, ++j) {
                src[i] += r.src[j];
            }
        } else {
            for (int i = beg, j = r.beg; i != end; i += inc, j += r.inc) {
                src[i] += r.src[j];
            }
        }
    }

    /**
     * Adds d to all the elements of this src block this(i) = this(i) + d
     *
     * @param d
     */
    public void add(final double d) {
        if (d == 0) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] += d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] += d;
            }
        }
    }

    /**
     * Adds d to the item idx. this(idx)=this(idx)+d
     *
     * @param idx 0-based position of the element being modified
     * @param d The added value
     */
    public void add(int idx, double d) {
        src[beg + idx * inc] += d;
    }

    /**
     * Adds a rescaled src block. this(i) = this(i) +a * y(i)
     *
     * @param a The scaling factor
     * @param y The added src block. Its length must be &ge the length of this
     * object.
     */
    public void addAY(double a, DataBlock y) {
        if (a == 0) {
            return;
        }
        if (a == 1) {
            add(y);
        } else if (a == -1) {
            sub(y);
        } else if (inc == 1 && y.inc == 1) {
            for (int i = beg, j = y.beg; i != end; ++i, ++j) {
                src[i] += a * y.src[j];
            }
        } else {
            for (int i = beg, j = y.beg; i != end; i += inc, j += y.inc) {
                src[i] += a * y.src[j];
            }
        }
    }

    /**
     * Adds a rescaled product of src blocks. this(i) = this(i) +a * src(i)*y(i)
     *
     * @param a The scaling factor
     * @param x The left hand of the added product. Its length must be &ge the
     * length of this
     * @param y The right hand of the added product. Its length must be &ge the
     * length of this object.
     */
    public void addAXY(double a, DataBlock x, DataBlock y) {
        if (a == 0) {
            return;
        }
        if (a == 1) {
            for (int i = beg, j = x.beg, k = y.beg; i != end; i += inc, j += x.inc, k += y.inc) {
                this.src[i] += x.src[j] * y.src[k];
            }
        } else if (a == -1) {
            for (int i = beg, j = x.beg, k = y.beg; i != end; i += inc, j += x.inc, k += y.inc) {
                this.src[i] -= x.src[j] * y.src[k];
            }
        } else {
            for (int i = beg, j = x.beg, k = y.beg; i != end; i += inc, j += x.inc, k += y.inc) {
                this.src[i] += a * x.src[j] * y.src[k];
            }
        }
    }

    /**
     * Copies a re-scaled src block. this(i) = a * y(i)
     *
     * @param a The scaling factor
     * @param y The src whose re-scaled version is copied. Unmodified
     */
    public void setAY(double a, DataBlock y) {
        if (a == 0) {
            set(0);
            return;
        }
        if (a == 1) {
            copy(y);
            return;
        }
        if (inc == 1 && y.inc == 1) {
            for (int i = beg, j = y.beg; i < end; ++i, ++j) {
                src[i] = a * y.src[j];
            }
        } else {
            for (int i = beg, j = y.beg; i != end; i += inc, j += y.inc) {
                src[i] = a * y.src[j];
            }
        }
    }

    private void bshift() {
        int imax = end - inc;
        if (inc == 1) {
            for (int i = beg; i < imax; ++i) {
                src[i] = src[i + 1];
            }
        } else {
            for (int i = beg; i != imax; i += inc) {
                src[i] = src[i + inc];
            }
        }
    }

    /**
     * Shifts the elements of this object to the left. this(i) = this(i+1). See
     * the ShiftOption description for further information.
     *
     * @param option Specifies the way the last element is updated
     */
    public void bshift(ShiftOption option) {
        int imax = end - inc;
        switch (option) {
            case Rotate:
                double first = src[beg];
                bshift();
                src[imax] = first;
                break;
            case Zero:
                bshift();
                src[imax] = 0;
                break;
            case Sum:
                src[imax] = sbshift();
                break;
            case NegSum:
                src[imax] = -sbshift();
                break;
            default:
                bshift();
                break;
        }
    }

    /**
     * Changes the sign of this src block this(i) = - this(i)
     */
    public void chs() {
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] = -src[i];
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] = -src[i];
            }
        }
    }

    /**
     * Changes the sign of this src block this(i) = - this(i)
     */
    public void sqrt() {
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                if (src[i] > 0) {
                    src[i] = Math.sqrt(src[i]);
                } else {
                    src[i] = 0;
                }
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                if (src[i] > 0) {
                    src[i] = Math.sqrt(src[i]);
                } else {
                    src[i] = 0;
                }
            }
        }
    }

    public void inv() {
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] = 1 / src[i];
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] = 1 / src[i];
            }
        }
    }

    public void square() {
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] = src[i] * src[i];
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] = src[i] * src[i];
            }
        }
    }

    @Override
    public DataBlock clone() {
        try {
            DataBlock db = (DataBlock) super.clone();
            return db;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     * Copies a given src block
     *
     * @param data The src being copied. Its length must be smaller or equal
     * than the length of this object.
     */
    public void copy(DataBlock data) {
        if (inc == 1 && data.inc == 1) {
            System.arraycopy(data.src, data.beg, this.src, beg, data.getLength());
        } else {
            for (int i = beg, j = data.beg; j != data.end; i += inc, j += data.inc) {
                this.src[i] = data.src[j];
            }
        }
    }

    /**
     * Copies a generic (read only) src block
     *
     * @param data The src being copied. Its length must be smaller or equal
     * than the length of this object.
     */
    public void copy(IReadDataBlock data) {
        int n = data.getLength();
        for (int i = beg, j = 0; j < n; i += inc, ++j) {
            this.src[i] = data.get(j);
        }
    }

    @Override
    public void copyFrom(double[] buffer, int start) {
        if (inc == 1) {
            System.arraycopy(buffer, start, src, beg, end - beg);
        } else {
            for (int t = beg, s = start; t != end; t += inc, ++s) {
                src[t] = buffer[s];
            }
        }
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        if (inc == 1) {
            System.arraycopy(src, beg, buffer, start, end - beg);
        } else {
            for (int s = beg, t = start; s != end; s += inc, ++t) {
                buffer[t] = src[s];
            }
        }
    }

    /**
     * Transforms this object into its cumulative sum: this(t) =
     * sum(this(0...t))
     */
    public void cumul() {
        int cur = beg;
        double s = src[cur];
        cur += inc;
        while (cur != end) {
            s += src[cur];
            src[cur] = s;
            cur += inc;
        }
    }

    /**
     * Transforms this object into its cumulative sum, with an dumping
     * coefficient. If src is the initial src block and if y is the final src
     * block, we have: y(0) = src(0), y(t) = src(t) + c * y(t-1)
     *
     * @param c The dumping factor
     */
    public void cumul(double c) {
        int cur = beg;
        double s = src[cur];
        cur += inc;
        while (cur != end) {
            s = c * s + src[cur];
            src[cur] = s;
            cur += inc;
        }
    }

    /**
     * Transforms this object to its cumulative sum, with an dumping coefficient
     * and a given lag.. If src is the initial src block and if y is the final
     * src block, we have: y(i) = src(i) where i &lt lag, y(t) = src(t) + c *
     * y(t-lag)
     *
     * @param c The dumping factor
     * @param lag The lag
     */
    public void cumul(double c, int lag) {
        int linc = lag * this.inc;
        if (getLength() < lag) {
            return;
        }
        int cur = beg + linc;
        if (c == 1) {
            while (cur != end) {
                src[cur] += src[cur - linc];
                cur += this.inc;
            }
        } else {
            while (cur != end) {
                src[cur] += c * src[cur - linc];
                cur += this.inc;
            }
        }
    }

    /**
     * Creates a deep clone of this object. The underlying src a copied in a new
     * buffer
     *
     * @return The new object.
     */
    public DataBlock deepClone() {
        if (this == EMPTY) {
            return EMPTY;
        }
        DataBlock rc = new DataBlock(getLength());
        copyTo(rc.src, 0);
        return rc;
    }

    /**
     * Differences this object. this(i) = this(i) - this(i-1). The first element
     * is unchanged
     */
    public void difference() {
        if (getLength() <= 1) {
            return;
        }
        int cur = end - inc;
        do {
            cur -= inc;
            src[cur + inc] -= src[cur];
        } while (cur != beg);
    }

    /**
     * Computes the difference of two src blocks. The results are put in this
     * object. this(i) = l(i) - r(i). Length(this) &le min(length(l), length(r))
     * If this object is smaller than the operands, only the first items are
     * considered.
     *
     * @param l Left operand
     * @param r right operand
     */
    public void difference(DataBlock l, DataBlock r) {
        for (int i = beg, j = l.beg, k = r.beg; i != end; i += inc, j += l.inc, k += r.inc) {
            src[i] = l.src[j] - r.src[k];
        }
    }

    /**
     * Differences this object with mulyiplication. this(i) = this(i) -
     * this(i-1)*delta. The first elements are unchanged
     *
     * @param delta 
     */
    public void difference(double delta) {
        if (getLength() <= 1) {
            return;
        }
        int cur = end - inc;
        do {
            cur -= inc;
            src[cur + inc] -= delta * src[cur];
        } while (cur != beg);
    }

    /**
     * Differences this object with a given lag and a dumping factor. It is
     * equivalent to applying a ( 1 - c * B^lag) operator. this(i) = this(i) -
     * c*this(i-lag). The first elements are unchanged
     *
     * @param c The dumping factor
     * @param lag The lag.
     */
    public void difference(double c, int lag) {
        if (getLength() <= lag) {
            return;
        }
        int linc = this.inc * lag;
        int cur = end - linc;
        do {
            cur -= this.inc;
            src[cur + linc] -= c * src[cur];
        } while (cur != beg);
    }

    /**
     * Return the euclidian norm of the differences between two src blocks
     *
     * @param data The comparison src block.
     * @return The euclidian norm
     */
    public double distance(DataBlock data) {
        if (beg == end) {
            return 0;
        } else if (beg + inc == end) {
            return Math.abs(src[beg] - data.src[data.beg]);
        } else {
            double scale = ZERO;
            double ssq = ONE;
            for (int ix = beg, jx = data.beg; ix != end; ix += inc, jx += data.inc) {
                double x = src[ix], y = data.src[jx];
                if (Double.compare(x, y) != 0) {
                    double d = x - y;
                    if (d != ZERO) {
                        double absxi = Math.abs(d);
                        if (scale < absxi) {
                            double s = scale / absxi;
                            ssq = ONE + ssq * s * s;
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
    }

    /**
     * Computes the scalar product of two src blocks. r = this(0)*src(0) + ... +
     * this(n)*src(n)
     *
     * @param data The second src block. src can be smaller than this object. In
     * that case, only the first elements are considered (the size of the buffer
     * (src) is preponderant).
     * @return The scalar product
     */
    public double dot(DataBlock data) {
        double r = 0;
        //
        if (inc == 1 && data.inc == 1) {
            for (int i = beg, j = data.beg; i != end; ++i, ++j) {
                r += this.src[i] * data.src[j];
            }
        } else if (inc == -1 && data.inc == -1) {
            for (int i = beg, j = data.beg; i != end; --i, --j) {
                r += this.src[i] * data.src[j];
            }
        } else {
            for (int i = beg, j = data.beg; i != end; i += inc, j += data.inc) {
                r += this.src[i] * data.src[j];
            }
        }
        return r;
    }

    public double dot(double[] data) {
        double r = 0;
        //
        if (inc == 1 && beg == 0) {
            for (int i = 0; i < data.length; ++i) {
                r += this.src[i] * data[i];
            }
        } else {
            for (int i = beg, j = 0; j < data.length; i += inc, ++j) {
                r += this.src[i] * data[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product of two src blocks using the J-norm. r =
     * this(0)*src(0) + ...this(p-1)*src(p-1) - this(p)*src(p) - ... -
     * this(n)*src(n)
     *
     * @param data The second src block. src can be larger than this object. In
     * that case, only the first elements are considered.
     * @param p The number of (leading) positive terms in the norm
     * @return The scalar product
     */
    public double jdot(int p, DataBlock data) {
        double r = 0;
        int pend = beg + p * inc;
        //
        if (inc == 1 && data.inc == 1) {
            int i = beg, j = data.beg;
            for (; i != pend; ++i, ++j) {
                r += this.src[i] * data.src[j];
            }
            for (; i != end; ++i, ++j) {
                r -= this.src[i] * data.src[j];
            }
        } else if (inc == -1 && data.inc == -1) {
            int i = beg, j = data.beg;
            for (; i != pend; --i, --j) {
                r += this.src[i] * data.src[j];
            }
            for (; i != end; --i, --j) {
                r -= this.src[i] * data.src[j];
            }
        } else {
            int i = beg, j = data.beg;
            for (; i != pend; i += inc, j += data.inc) {
                r += this.src[i] * data.src[j];
            }
            for (; i != end; i += inc, j += data.inc) {
                r += this.src[i] * data.src[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product two src blocks, inverting the order of the
     * second one. r = this(0)*src(n) + ... + this(n)*src(0).
     *
     * @param data The second src block. src can be larger than this object. In
     * that case, only the first elements of this and the last elements of src
     * are considered.
     * @return The scalar product
     */
    public double dotReverse(DataBlock data) {
        double r = 0;
        for (int i = beg, j = data.end - data.inc; i != end; i += inc, j -= data.inc) {
            r += this.src[i] * data.src[j];
        }
        return r;
    }

    /**
     * Computes the scalar product two src blocks, inverting the order of the
     * second one. r = this(0)*src(n) + ... + this(n)*src(0).
     *
     * @param data The second src block. src can be smaller than this object. In
     * that case, only the first elements of this and the last elements of src
     * are considered.
     * @return The scalar product
     */
    public double dotReverse(double[] data) {
        double r = 0;
        if (inc == 1) {
            for (int i = beg, j = data.length - 1; j >= 0; ++i, --j) {
                r += this.src[i] * data[j];
            }
        } else {
            for (int i = beg, j = data.length - 1; j >= 0; i += inc, --j) {
                r += this.src[i] * data[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product two src blocks, inverting the order of the
     * two blocks. r = this(n)*src(m) + ... + this(n-m)*src(0).
     *
     * @param data The second src block. src can be smaller than this object. In
     * that case, only the first elements of this and the last elements of src
     * are considered.
     * @return The scalar product
     */
    public double reverseDot(double[] data) {
        double r = 0;
        int m = data.length;
        if (inc == 1) {
            for (int i = end - 1, j = m - 1; j >= 0; --i, --j) {
                r += this.src[i] * data[j];
            }
        } else {
            for (int i = end - inc, j = m - 1; j >= 0; i -= inc, --j) {
                r += this.src[i] * data[j];
            }
        }
        return r;
    }

    /**
     * Creates an extract of this src block by dropping ending elements. The new
     * src block refers to the same underlying physical src buffer.
     *
     * @param nbeg The number of elements dropped at the beginning of this
     * object. Should be positive.
     * @param nend The number of elements dropped at the end of this object.
     * Should be positive.
     * @return The new src block. Can be empty (but not null)
     */
    public DataBlock drop(int nbeg, int nend) {
//	if (nbeg + nend >= getLength())
//	    return null;
//	else
        return inc == 1 ? new DataBlock(src, beg + nbeg, end - nend, 1)
                : new DataBlock(src, beg + nbeg * inc, end - nend * inc, inc);
    }

    /**
     * Shrinks the src block by 1 src at the beginning of the block
     *
     * @return true if the src block has been correctly shrunk, false otherwise.
     */
    public boolean bshrink() {
        if (beg != end) {
            beg += inc;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shrinks the src block by 1 src at the end of the block
     *
     * @return true if the src block has been correctly shrunk, false otherwise.
     */
    public boolean eshrink() {
        if (beg != end) {
            end -= inc;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shrinks the current src block by removing src at the beginning and at the
     * end of the src block.
     *
     * @param nbeg Number of elements removed at the beginning of this src block
     * @param nend Number of elements removed at the end of this src block
     * @return true if the src block has been correctly shrunk, false otherwise.
     */
    public boolean shrink(int nbeg, int nend) {
        if (nbeg + nend <= getLength()) {
            beg += inc * nbeg;
            end -= inc * nend;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Expands the current src block by adding src at the beginning or at the
     * end of the src block. The buffer must be larger enough.
     *
     * @param nbeg Number of elements added at the beginning of this src block
     * @param nend Number of elements added at the end of this src block
     * @return true if the src block has been correctly expanded, false
     * otherwise.
     */
    public boolean expand(int nbeg, int nend) {
        int xbeg = beg - nbeg * inc;
        int xend = end + nend * inc;

        if (xbeg < 0 || xbeg > src.length) {
            return false;
        } else {
            beg = xbeg;
            end = xend;
            return true;
        }
    }

    /**
     * Creates a new src block that is an extension of this one. The new src
     * block refers to the same underlying physical src buffer. We have that
     * src.clone().expand(n, m) is equivalent to
     *
     * @param nbeg The number of elements added at the beginning of this object.
     * Should be positive.
     * @param nend The number of elements added at the end of this object.
     * Should be positive.
     * @return The new src block. The method doesn't check that the operation is
     * valid (i.e. that the buffer is large enough).
     */
    public DataBlock extend(int nbeg, int nend) {
        return new DataBlock(src, beg - nbeg * inc,
                end + nend * inc, inc);
    }

    @Override
    public DataBlock extract(int start, int count) {
        return extract(start, count, 1);
    }

    /**
     * Creates a new src block from the given one. The increment and the
     * starting position are relative to the existing one. More precisely, if
     * the increment in the current src block is inc0 and if its starting
     * position (in the underlying buffer) is start0, The new src block will
     * start at position start0 + start*inc0 and its increment will be
     * inc0*inc1.
     *
     * @param start The starting position (in the current src block).
     * @param count The number of items in the selection. If count is -1, the
     * largest extract is returned.
     * @param inc The increment of the selection.
     * @return A new src block is returned.
     */
    public DataBlock extract(int start, int count, int inc) {
        int i0, i1, ninc;
        if (this.inc == 1) {
            i0 = beg + start;
            ninc = inc;
        } else {
            i0 = beg + start * this.inc;
            ninc = inc * this.inc;
        }
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
        return new DataBlock(src, i0, i1, ninc);
    }

    public void fshift(int n) {
        int i0=end-inc, i1=beg+(n-1)*inc, ninc=n*inc;
        for (int i = i0; i != i1; i -= inc) {
            src[i] = src[i - ninc];
        }
    }

    public void bshift(int n) {
        int i0=beg, ninc=n*inc, i1=end-ninc;
        for (int i = i0; i != i1; i += inc) {
            src[i] = src[i + ninc];
        }
    }

    private void fshift() {
        if (inc == 1) {
            for (int i = end - 1; i != beg; --i) {
                src[i] = src[i - 1];
            }
        } else {
            for (int i = end - inc; i != beg; i -= inc) {
                src[i] = src[i - inc];
            }
        }
    }

    /**
     * Shifts the elements of this object to the right. this(i) = this(i-1)
     *
     * @param option Specifies the way the first element is updated
     */
    public void fshift(ShiftOption option) {
        switch (option) {
            case Rotate:
                double last = src[end - inc];
                fshift();
                src[beg] = last;
                break;
            case Zero:
                fshift();
                src[beg] = 0;
                break;
            case Sum:
                src[beg] = sfshift();
                break;
            case NegSum:
                src[beg] = -sfshift();
                break;
            default:
                fshift();
                break;
        }
    }

    /**
     * Return the idx-th element of the src block
     *
     * @param idx 0-base position of the element in the src block. Should belong
     * to [0, length[. Not checked
     * @return The required element. Unpredictable result when the index is
     * invalid.
     */
    @Override
    public double get(int idx) {
        return src[beg + inc * idx];
    }

    /**
     * Returns the underlying buffer. This method, which breaks down the src
     * encapsulation principle, is provided for optimisation reason only.
     *
     * @return The underlying buffer.
     */
    public double[] getData() {
        return src;
    }

    /**
     * Return the end position of the src block in the underlying array. The end
     * position doesn't belong to the src block. It should be noted that
     * endPosition can be &lt startPosition (in the case of negative increment)
     *
     * @return The end position.
     */
    public int getEndPosition() {
        return end;
    }

    /**
     * Return the last position of the src block in the underlying array. The
     * last position belongs to the src block.
     *
     * @return The last position.
     */
    public int getLastPosition() {
        return end - inc;
    }

    /**
     * Return the last valid index that can be applied on this src block
     *
     * @return The last position.
     */
    public int getLastIndex() {
        return inc == 1 ? end - beg - 1 : (end - beg) / inc - 1;
    }

    /**
     * Return the first invalid index for this src block
     *
     * @return The last position.
     */
    public int getEndIndex() {
        return inc == 1 ? end - beg : (end - beg) / inc;
    }

    /**
     * Return the interval between two selected elements.
     *
     * @return The increment. Can be &lt 0.
     */
    public int getIncrement() {
        return inc;
    }

    @Override
    public int getLength() {
        if (beg == end) {
            return 0;
        } else if (inc == 1) {
            return end - beg;
        } else {
            return (end - beg) / inc;
        }
    }

    /**
     * Return the start position of the src block in the underlying array.
     *
     * @return The start position.
     */
    public int getStartPosition() {
        return beg;
    }

    /**
     * Checks that all the src of the block are identical
     *
     * @return true if all the src are identical, false otherwise
     */
    public boolean isConstant() {
        double d = src[beg];
        for (int i = beg + inc; i != end; i += inc) {
            if (src[i] != d) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that all the src of the block are equal to a given value
     *
     * @param c The constant value
     * @return true if all the src are equal to c, false otherwise
     */
    public boolean isConstant(double c) {
        for (int i = beg; i != end; i += inc) {
            if (src[i] != c) {
                return false;
            }
        }
        return true;
    }

    public int getMissingCount() {
        int n = 0;
        for (int i = beg; i != end; i += inc) {
            if (!Double.isFinite(src[i])) {
                ++n;
            }
        }
        return n;
    }

    /**
     * Checks that the src block is empty. Occurs when getStartPosition() =
     * getendPosition().
     *
     * @return true if the src block is empty, false otherwise.
     */
    public boolean isEmpty() {
        return beg == end;
    }

    /**
     * Checks that all the src in the block are (nearly) 0. Equivalent to
     * isZero(EPSILON);
     *
     * @return true if all the src in the block are &lt or = EPSILON in absolute
     * value, false otherwise.
     */
    public boolean isZero() {
        return isZero(EPSILON);
    }

    /**
     * Moves the current selection of cells (keeping the current interval length
     * between the cells)
     *
     * @param del The length of the displacement. The beginning and the end of
     * the current selection are moved by del*increment.
     */
    public void move(int del) {
        if (inc != 1) {
            del *= inc;
        }
        beg += del;
        end += del;
    }

    /**
     * Multiplies the elements of this src block by a given value
     * this(i)=this(i)*d
     *
     * @param d The multiplier.
     */
    public void mul(double d) {
        if (d == 1) {
        } else if (d == 0) {
            set(0);
        } else if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] *= d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] *= d;
            }
        }
    }

    /**
     * Multiply the elements of this object by the element of another src block
     * this(i) = this(i) * r(i)
     *
     * @param data The other src block. Its length must be &ge the length of
     * this object.
     */
    public void mul(DataBlock data) {
        if (inc == 1 && data.inc == 1) {
            for (int i = beg, j = data.beg; i != end; ++i, ++j) {
                this.src[i] *= data.src[j];
            }
        } else {
            for (int i = beg, j = data.beg; i != end; i += inc, j += data.inc) {
                this.src[i] *= data.src[j];
            }
        }
    }

    /**
     * Divide the elements of this object by the element of another src block
     * this(i) = this(i) / r(i)
     *
     * @param data The other src block. Its length must be &ge the length of
     * this object.
     */
    public void div(DataBlock data) {
        if (inc == 1 && data.inc == 1) {
            for (int i = beg, j = data.beg; i != end; ++i, ++j) {
                this.src[i] /= data.src[j];
            }
        } else {
            for (int i = beg, j = data.beg; i != end; i += inc, j += data.inc) {
                this.src[i] /= data.src[j];
            }
        }
    }

    public void div(double d) {
        mul(1 / d);
    }

    /**
     * Multiplies the item idx by d. this(idx)=this(idx)*d
     *
     * @param idx 0-based position of the element being modified
     * @param d The multiplier.
     */
    public void mul(int idx, double d) {
        src[beg + idx * inc] *= d;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    public double nrm2() {
        if (beg == end) {
            return 0;
        } else if (beg + inc == end) {
            return Math.abs(src[beg]);
        } else {
            double scale = ZERO;
            double ssq = ONE;
            for (int ix = beg; ix != end; ix += inc) {
                if (src[ix] != ZERO) {
                    double absxi = Math.abs(src[ix]);
                    if (scale < absxi) {
                        double s = scale / absxi;
                        ssq = ONE + ssq * s * s;
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
     * Computes the infinite-norm of this src block
     *
     * @return Returns min{|src(i)|}
     */
    public double nrmInf() {
        if (beg == end) {
            return 0;
        } else {
            double nrm = Math.abs(src[beg]);
            for (int ix = beg + inc; ix != end; ix += inc) {
                double tmp = Math.abs(src[ix]);
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
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
            double nrm = src[beg];
            for (int ix = beg + inc; ix != end; ix += inc) {
                double tmp = src[ix];
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
            double nrm = src[beg];
            for (int ix = beg + inc; ix != end; ix += inc) {
                double tmp = src[ix];
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Computes the product of the element of the src block/
     *
     * @return The product of the elements or 1 if the src block is empty.
     */
    public double product() {
        double s = 1;
        for (int i = beg; i != end; i += inc) {
            s *= src[i];
        }
        return s;
    }

    /**
     * Computes the product of a vector by a matrix and stores the result in
     * this src block this = row * cols. We must have that 1. the length of this
     * src block = the number of columns 2. the length of the vector = the
     * length of each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
     */
    public void product(DataBlock row, DataBlockIterator cols) {
        int idx = beg;
        DataBlock cur = cols.getData();
        do {
            src[idx] = row.dot(cur);
            idx += inc;
        } while (cols.next());
    }

    /**
     * Adds the product of a vector by a matrix to this src block this += row *
     * cols. We must have that 1. the length of this src block = the number of
     * columns 2. the length of the vector = the length of each column. The
     * iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
     */
    public void addProduct(DataBlock row, DataBlockIterator cols) {
        int idx = beg;
        DataBlock cur = cols.getData();
        do {
            src[idx] += row.dot(cur);
            idx += inc;
        } while (cols.next());
    }

    /**
     * this = l * d. Product of a vector by a double. The results is stored in
     * this object
     *
     * @param l The vector (same length as this src block)
     * @param d The double
     */
    public void product(DataBlock l, double d) {
        if (d == 0) {
            set(0);
        } else if (d == 1) {
            copy(l);
        } else if (inc == 1 && l.inc == 1) {
            for (int i = beg, j = l.beg; i != end; ++i, ++j) {
                src[i] = l.src[j] * d;
            }
        } else if (inc == -1 && l.inc == -1) {
            for (int i = beg, j = l.beg; i != end; --i, --j) {
                src[i] = l.src[j] * d;
            }
        } else {
            for (int i = beg, j = l.beg; i != end; i += inc, j += l.inc) {
                src[i] = l.src[j] * d;
            }
        }
    }

    /**
     * Computes the product of a matrix by a vector and stores the result in
     * this src block this = rows * col. We must have that 1. the length of this
     * src block = the number of rows 2. the length of the vector = the length
     * of each row. The iterator is changed !!!
     *
     * @param rows The rows of the matrix.
     * @param col The vector.
     */
    public void product(DataBlockIterator rows, DataBlock col) {
        int idx = beg;
        DataBlock cur = rows.getData();
        do {
            src[idx] = cur.dot(col);
            idx += inc;
        } while (rows.next());
    }

    /**
     * Adds the product of a matrix by a vector to this src block this += rows *
     * col. We must have that 1. the length of this src block = the number of
     * rows 2. the length of the vector = the length of each row. The iterator
     * is changed !!!
     *
     * @param rows The rows of the matrix.
     * @param col The vector.
     */
    public void addProduct(DataBlockIterator rows, DataBlock col) {
        int idx = beg;
        DataBlock cur = rows.getData();
        do {
            src[idx] += cur.dot(col);
            idx += inc;
        } while (rows.next());
    }

    /**
     * Creates a new src block from this one
     *
     * @param c0 The first item of the selection (included)
     * @param c1 The last item of the selection (excluded)
     * @return The new src block
     */
    public DataBlock range(int c0, int c1) {
        if (inc == 1) {
            return new DataBlock(src, beg + c0, beg + c1, 1);
        } else {
            return new DataBlock(src, beg + inc * c0,
                    beg + inc * c1, inc);
        }
    }

    /**
     * Reverses the order of the src block (from the last item to the first
     * item);
     *
     * @return A new src block (the current object is not modified).
     */
    public DataBlock reverse() {
        return new DataBlock(src, end - inc, beg - inc, -inc);
    }

    @Override
    public DataBlock rextract(int start, int count) {
        return extract(start, count, 1);
    }

    private double sbshift() {
        int imax = end - inc;
        double s = src[beg];
        if (inc == 1) {
            for (int i = beg; i != imax; ++i) {
                src[i] = src[i + 1];
                s += src[i];
            }
        } else {
            for (int i = beg; i < imax; i += inc) {
                src[i] = src[i + inc];
                s += src[i];
            }
        }
        return s;
    }

    /**
     * Sets all the src of the block to a given value.
     *
     * @param d The value to be set.
     */
    public void set(double d) {
        if (inc == 1) {
            Arrays.fill(src, beg, end, d);
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] = d;
            }
        }
    }

    /**
     * Sets the src at the idx-th position to a given value.
     *
     * @param idx The position of the src being changed
     * @param value The new value.
     */
    @Override
    public void set(int idx, double value) {
        src[beg + inc * idx] = value;
    }

    private double sfshift() {
        double s = src[beg];
        if (inc == 1) {
            for (int i = end - 1; i != beg; --i) {
                s += src[i];
                src[i] = src[i - 1];
            }
        } else {
            for (int i = end - inc; i != beg; i -= inc) {
                s += src[i];
                src[i] = src[i - inc];
            }
        }
        return s;
    }

    /**
     * Moves the current src block by del cells. If the "move" method takes into
     * account the increment of the src block, this method is actually a move on
     * the physical buffer (without considering the increment)
     *
     * @param del The length of the sliding.
     */
    public void slide(int del) {
        beg += del;
        end += del;
    }

    /**
     * Returns the sum of the squared items.
     *
     * @return The sum of the squares.
     */
    @Override
    public double ssq() {
        if (beg == end) {
            return 0;
        } else if (beg + inc == end) {
            return src[beg] * src[beg];
        } else {
            double ssq = ZERO;
            for (int ix = beg; ix != end; ix += inc) {
                double x = this.src[ix];
                ssq += x * x;
            }
            return ssq;
        }
    }

    /**
     * Computes sum([src(i)-m]^2
     *
     * @param m The correction parameter
     * @return sum([src(i)-m]^2
     */
    public double ssqc(double m) {
        if (beg == end) {
            return 0;
        } else if (beg + inc == end) {
            double xc = src[beg] - m;
            return xc * xc;
        } else {
            double ssq = ZERO;
            for (int ix = beg; ix != end; ix += inc) {
                double x = this.src[ix] - m;
                ssq += x * x;
            }
            return ssq;
        }
    }

    /**
     * Subtracts the src block r to this object. this(i) = this(i) - r(i)
     *
     * @param data The subtracted src block. Its length must be &ge the length
     * of this object.
     */
    public void sub(DataBlock data) {
        if (inc == 1 && data.inc == 1) {
            for (int i = beg, j = data.beg; i != end; ++i, ++j) {
                this.src[i] -= data.src[j];
            }
        } else {
            for (int i = beg, j = data.beg; i != end; i += inc, j += data.inc) {
                this.src[i] -= data.src[j];
            }
        }
    }

    /**
     * Subtracts d to all the elements of this src block this(i) = this(i) - d
     *
     * @param d. The subtracted value.
     */
    public void sub(double d) {
        if (d == 0) {
            return;
        }
        if (inc == 1) {
            for (int i = beg; i != end; ++i) {
                src[i] -= d;
            }
        } else {
            for (int i = beg; i != end; i += inc) {
                src[i] -= d;
            }
        }
    }

    /**
     * Computes the sum of the src in the block.
     *
     * @return The sum of the src block
     */
    @Override
    public double sum() {
        double s = 0;
        for (int i = beg; i != end; i += inc) {
            s += src[i];
        }
        return s;
    }

    /**
     * Computes the sum of two src blocks. The results are stored in this
     * object. this[i] = l[i] + r[i]
     *
     * @param l The left operand.
     * @param r The right operand.
     */
    public void sum(DataBlock l, DataBlock r) {
        for (int i = beg, j = l.beg, k = r.beg; i != end; i += inc, j += l.inc, k += r.inc) {
            src[i] = l.src[j] + r.src[k];
        }
    }

    /**
     * Computes the product of the items of this src block, under a log (and
     * sign) form. Returns
     *
     * @return The log and signed result of the product.
     */
    public LogSign sumLog() {
        LogSign ls = new LogSign();
        ls.value = 0;
        ls.pos = true;
        for (int i = beg; i != end; i += inc) {
            double x = this.src[i];
            if (x < 0) {
                ls.pos = !ls.pos;
                x = -x;
            }
            ls.value += Math.log(x);
        }
        return ls;
    }

    /**
     * Computes the hypothenuse of two numbers (h = sqrt(a*a + b*b) in a
     * numerically stable way
     *
     * @param a The first number
     * @param b The second number
     * @return The hypothenuse. A positive real number.
     */
    public static double hypot(double a, double b) {
        double xa = Math.abs(a), xb = Math.abs(b);
        double w, z;
        if (xa > xb) {
            w = xa;
            z = xb;
        } else {
            w = xb;
            z = xa;
        }
        if (z == ZERO) {
            return w;
        } else {
            double zw = z / w;
            return w * Math.sqrt(ONE + zw * zw);
        }
    }

    public void round(final int ndec) {
        if (ndec < 0) {
            throw new IllegalArgumentException("Negative rounding parameter");
        }
        double f = 1;
        for (int i = 0; i < ndec; ++i) {
            f *= 10;
        }
        for (int i = beg; i != end; i += inc) {
            if (ndec > 0) {
                src[i] = Math.round(src[i] * f) / f;
            } else {
                src[i] = Math.round(src[i]);
            }
        }
    }

    @Override
    public String toString() {
        return ReadDataBlock.toString(this);
    }

    public String toString(String fmt) {
        return ReadDataBlock.toString(this, fmt);
    }

    /**
     * Sets random numbers in this src block
     */
    public void randomize() {
        for (int i = beg; i != end; i += inc) {
            src[i] = RNG.nextDouble() - .5;
        }
    }

    public void randomize(int seed) {
        Random rnd = new Random(seed);
        for (int i = beg; i != end; i += inc) {
            src[i] = rnd.nextDouble() - .5;
        }
    }

    /**
     * Checks that all the src in the block are (nearly) 0.
     *
     * @param zero A given zero
     * @return false if some src in the block are &gt zero in absolute value,
     * true otherwise.
     */
    public boolean isZero(double zero) {
        for (int i = beg; i != end; i += inc) {
            if (Math.abs(src[i]) > zero) {
                return false;
            }
        }
        return true;
    }
//<editor-fold defaultstate="collapsed" desc="iterator method">

    /**
     * The following methods can be used to create fast iterations. They avoid
     * the creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); } /** The
     * following methods can be used to create fast iterations. They avoid the
     * creation of unnecessary objects
     *
     * example:
     *
     * DataBlock data=... DataBlock cur=data.start(); while
     * (cur.getEndPosition() != data.getEndPosition()){ cur.next(z); }
     */
    /**
     * Moves the current DataBlock to the right by a given number of items. The
     * new starting position is the old ending position and the ending position
     * is incremented by the given number.
     *
     * @param nitems The number of items in the block
     */
    public void next(int nitems) {
        beg = end;
        end += inc * nitems;
    }

    /**
     * Moves the current DataBlock to the left by a given number of items. The
     * new ending position is the old starting position and the starting
     * position is decremented by the given number.
     *
     * @param nitems The number of items in the block
     */
    public void previous(int nitems) {
        end = beg;
        beg -= inc * nitems;
    }

    /**
     * Creates an empty DataBlock positioned at the current start. To be used
     * with next/previous
     *
     * @return The new DataBlock
     */
    public DataBlock start() {
        return new DataBlock(src, beg, beg, inc);
    }

    /**
     * Creates an empty DataBlock positioned at the current end. To be used with
     * next/previous
     *
     * @return The new DataBlock
     */
    public DataBlock end() {
        return new DataBlock(src, end, end, inc);
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="functional methods">
    @Override
    public double computeRecursively(final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = beg; i != end; i += inc) {
            cur = fn.applyAsDouble(cur, src[i]);
        }
        return cur;
    }

    @Override
    public void apply(DoubleUnaryOperator fn) {
        for (int i = beg; i != end; i += inc) {
            src[i] = fn.applyAsDouble(src[i]);
        }
    }

    @Override
    public void applyIf(DoublePredicate pred, DoubleUnaryOperator fn) {
        for (int i = beg; i != end; i += inc) {
            double cur = src[i];
            if (pred.test(cur)) {
                src[i] = fn.applyAsDouble(cur);
            }
        }
    }

    @Override
    public void applyRecursively(final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = beg; i != end; i += inc) {
            cur = fn.applyAsDouble(cur, src[i]);
            src[i] = cur;
        }
    }

    @Override
    public boolean check(DoublePredicate pred) {
        for (int i = beg; i != end; i += inc) {
            if (!pred.test(src[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int count(DoublePredicate pred) {
        int n = 0;
        for (int i = beg; i != end; i += inc) {
            if (pred.test(src[i])) {
                n++;
            }
        }
        return n;
    }

    @Override
    public int first(DoublePredicate pred) {
        for (int i = beg; i != end; i += inc) {
            if (pred.test(src[i])) {
                return (i - beg) / inc;
            }
        }
        return getLength();
    }

    @Override
    public int last(DoublePredicate pred) {
        for (int i = end - inc; i != beg - inc; i -= inc) {
            if (pred.test(src[i])) {
                return (i - beg) / inc;
            }
        }
        return -1;
    }

    public void apply(DoubleBinaryOperator fn, DataBlock x) {
        for (int i = beg, j = x.beg; i != end; i += inc, j += inc) {
            src[i] = fn.applyAsDouble(src[i], x.src[j]);
        }
    }

    @Override
    public void apply(IReadDataBlock x, DoubleBinaryOperator fn) {
        for (int i = beg, j = 0; i != end; i += inc, j++) {
            src[i] = fn.applyAsDouble(src[i], x.get(j));
        }
    }

    @Override
    public void set(DoubleSupplier fn) {
        for (int i = beg; i != end; i += inc) {
            src[i] = fn.getAsDouble();
        }
    }

    @Override
    public void set(IntToDoubleFunction fn) {
        for (int i = beg, j = 0; i != end; i += inc) {
            src[i] = fn.applyAsDouble(j++);
        }
    }

    public void set(DoubleUnaryOperator fn, DataBlock x) {
        for (int i = beg, j = x.beg; i != end; i += inc, j += x.inc) {
            src[i] = fn.applyAsDouble(x.src[j]);
        }
    }

    @Override
    public void set(IReadDataBlock x, DoubleUnaryOperator fn) {
        for (int i = beg, j = 0; i != end; i += inc, j++) {
            src[i] = fn.applyAsDouble(x.get(j));
        }
    }

    @Override
    public void setIf(DoublePredicate pred, DoubleSupplier fn) {
        for (int i = beg, j = 0; i != end; i += inc, j++) {
            if (pred.test(src[i])) {
                src[i] = fn.getAsDouble();
            }
        }

    }

    public void set(DataBlock x, DataBlock y, DoubleBinaryOperator fn) {
        for (int i = beg, j = x.beg, k = y.beg; i != end; i += inc, j += x.inc, k += y.inc) {
            src[i] = fn.applyAsDouble(x.src[j], y.src[k]);
        }
    }

    @Override
    public void set(IReadDataBlock x, IReadDataBlock y, DoubleBinaryOperator fn) {
        for (int i = beg, j = 0; i != end; i += inc, j++) {
            src[i] = fn.applyAsDouble(x.get(j), y.get(j));
        }
    }

    public DataBlock select(DoublePredicate pred) {
        DoubleList list = new DoubleList();
        for (int i = beg; i != end; i += inc) {
            double cur = src[i];
            if (pred.test(cur)) {
                list.add(cur);
            }
        }
        return new DataBlock(list.toArray());
    }

    public static DataBlock select(IReadDataBlock data, DoublePredicate pred) {
        DoubleList list = new DoubleList();
        int n = data.getLength();
        for (int i = 0; i < n; ++i) {
            double cur = data.get(i);
            if (pred.test(cur)) {
                list.add(cur);
            }
        }
        return new DataBlock(list.toArray());
    }

//</editor-fold>
}

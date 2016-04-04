/*
 * Copyright 2013 National Bank of Belgium
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
import java.util.Arrays;
import java.util.Random;

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
     * Creates a data block with random numbers
     *
     * @param len The length of the data block
     * @return A new data block with len random numbers
     */
    public static DataBlock random(final int len) {
        double[] d = new double[len];
        for (int i = 0; i < d.length; ++i) {
            d[i] = RNG.nextDouble();
        }
        return new DataBlock(d);
    }
    final double[] x_;
    final int inc_;
    int beg_, end_;
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
     * Creates a new data block with a given array of doubles. The data are not
     * copied; the data block is just a wrapper around the array.
     *
     * @param data The array of data
     */
    public DataBlock(final double[] data) {
        x_ = data;
        beg_ = 0;
        inc_ = 1;
        if (data != null) {
            end_ = data.length;
        } else {
            end_ = 0;
        }
    }

    /**
     * Creates a new data block with a given array of doubles. The data are not
     * copied. We must have that end = beg + n * inc for some n. That
     * relationship is not verified. If it is not respected, the result of some
     * methods could be unpredictable.
     *
     * @param data The array of data
     * @param beg The position in the original array of the first element
     * @param end The position in the original array of the last element
     * (excluded).
     * @param inc The increment of two successive elements of the data block.
     * Can be negative.
     */
    public DataBlock(final double[] data, final int beg, final int end, final int inc) {
        x_ = data;
        beg_ = beg;
        end_ = end;
        inc_ = inc;
    }

    /**
     * Creates a new data block with n items (equal to 0). The underlying buffer
     * is automatically allocated.
     *
     * @param n The number of elements in the new data block. Should be
     * positive.
     */
    public DataBlock(final int n) {
        x_ = new double[n];
        beg_ = 0;
        end_ = n;
        inc_ = 1;
    }

    /**
     * Creates a new data block from a read only data block. The data a copied
     *
     * @param data The data being copied
     */
    public DataBlock(final IReadDataBlock data) {
        x_ = new double[data.getLength()];
        data.copyTo(x_, 0);
        beg_ = 0;
        end_ = x_.length;
        inc_ = 1;
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
     * Safe creation of a datablock. Reuse of the EMPTY data block
     * @param n
     * @return 
     */
    public static DataBlock create(final int n){
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
                d.x_[j++] = data.get(i);
            }
        }
        return d;
    }

    public static DataBlock select(IReadDataBlock data, int[] isel) {
        int n = isel.length;
        DataBlock d = new DataBlock(n);
        for (int i = 0; i < n; ++i) {
            d.x_[i] = data.get(isel[i]);
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
     * Adds the data block r to this object. this(i) = this(i) + r(i)
     *
     * @param r The added data block. Its length must be &ge the length of this
     * object.
     */
    public void add(final DataBlock r) {
        if (inc_ == 1 && r.inc_ == 1) {
            for (int i = beg_, j = r.beg_; i != end_; ++i, ++j) {
                x_[i] += r.x_[j];
            }
        } else {
            for (int i = beg_, j = r.beg_; i != end_; i += inc_, j += r.inc_) {
                x_[i] += r.x_[j];
            }
        }
    }

    /**
     * Adds d to all the elements of this data block this(i) = this(i) + d
     *
     * @param d
     */
    public void add(final double d) {
        if (d == 0) {
            return;
        }
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] += d;
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] += d;
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
        x_[beg_ + idx * inc_] += d;
    }

    /**
     * Adds a rescaled data block. this(i) = this(i) +a * y(i)
     *
     * @param a The scaling factor
     * @param y The added data block. Its length must be &ge the length of this
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
        } else if (inc_ == 1 && y.inc_ == 1) {
            for (int i = beg_, j = y.beg_; i != end_; ++i, ++j) {
                x_[i] += a * y.x_[j];
            }
        } else {
            for (int i = beg_, j = y.beg_; i != end_; i += inc_, j += y.inc_) {
                x_[i] += a * y.x_[j];
            }
        }
    }

    /**
     * Adds a rescaled product of data blocks. this(i) = this(i) +a * x(i)*y(i)
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
            for (int i = beg_, j = x.beg_, k = y.beg_; i != end_; i += inc_, j += x.inc_, k += y.inc_) {
                x_[i] += x.x_[j] * y.x_[k];
            }
        } else if (a == -1) {
            for (int i = beg_, j = x.beg_, k = y.beg_; i != end_; i += inc_, j += x.inc_, k += y.inc_) {
                x_[i] -= x.x_[j] * y.x_[k];
            }
        } else {
            for (int i = beg_, j = x.beg_, k = y.beg_; i != end_; i += inc_, j += x.inc_, k += y.inc_) {
                x_[i] += a * x.x_[j] * y.x_[k];
            }
        }
    }

    /**
     * Copies a re-scaled data block. this(i) = a * y(i)
     *
     * @param a The scaling factor
     * @param y The data whose re-scaled version is copied. Unmodified
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
        if (inc_ == 1 && y.inc_ == 1) {
            for (int i = beg_, j = y.beg_; i < end_; ++i, ++j) {
                x_[i] = a * y.x_[j];
            }
        } else {
            for (int i = beg_, j = y.beg_; i != end_; i += inc_, j += y.inc_) {
                x_[i] = a * y.x_[j];
            }
        }
    }

    private void bshift() {
        int imax = end_ - inc_;
        if (inc_ == 1) {
            for (int i = beg_; i < imax; ++i) {
                x_[i] = x_[i + 1];
            }
        } else {
            for (int i = beg_; i != imax; i += inc_) {
                x_[i] = x_[i + inc_];
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
        int imax = end_ - inc_;
        switch (option) {
            case Rotate:
                double first = x_[beg_];
                bshift();
                x_[imax] = first;
                break;
            case Zero:
                bshift();
                x_[imax] = 0;
                break;
            case Sum:
                x_[imax] = sbshift();
                break;
            case NegSum:
                x_[imax] = -sbshift();
                break;
            default:
                bshift();
                break;
        }
    }

    /**
     * Changes the sign of this data block this(i) = - this(i)
     */
    public void chs() {
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] = -x_[i];
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] = -x_[i];
            }
        }
    }

    /**
     * Changes the sign of this data block this(i) = - this(i)
     */
    public void sqrt() {
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                if (x_[i] > 0) {
                    x_[i] = Math.sqrt(x_[i]);
                } else {
                    x_[i] = 0;
                }
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                if (x_[i] > 0) {
                    x_[i] = Math.sqrt(x_[i]);
                } else {
                    x_[i] = 0;
                }
            }
        }
    }

    public void inv() {
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] = 1 / x_[i];
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] = 1 / x_[i];
            }
        }
    }

    public void square() {
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] = x_[i] * x_[i];
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] = x_[i] * x_[i];
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
     * Copies a given data block
     *
     * @param data The data being copied. Its length must be smaller or equal
     * than the length of this object.
     */
    public void copy(DataBlock data) {
        if (inc_ == 1 && data.inc_ == 1) {
            System.arraycopy(data.x_, data.beg_, x_, beg_, data.getLength());
        } else {
            for (int i = beg_, j = data.beg_; j != data.end_; i += inc_, j += data.inc_) {
                x_[i] = data.x_[j];
            }
        }
    }

    /**
     * Copies a generic (read only) data block
     *
     * @param data The data being copied. Its length must be smaller or equal
     * than the length of this object.
     */
    public void copy(IReadDataBlock data) {
        int n = data.getLength();
        for (int i = beg_, j = 0; j < n; i += inc_, ++j) {
            x_[i] = data.get(j);
        }
    }

    @Override
    public void copyFrom(double[] buffer, int start) {
        if (inc_ == 1) {
            System.arraycopy(buffer, start, x_, beg_, end_ - beg_);
        } else {
            for (int t = beg_, s = start; t != end_; t += inc_, ++s) {
                x_[t] = buffer[s];
            }
        }
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        if (inc_ == 1) {
            System.arraycopy(x_, beg_, buffer, start, end_ - beg_);
        } else {
            for (int s = beg_, t = start; s != end_; s += inc_, ++t) {
                buffer[t] = x_[s];
            }
        }
    }

    /**
     * Transforms this object into its cumulative sum: this(t) =
     * sum(this(0...t))
     */
    public void cumul() {
        int cur = beg_;
        double s = x_[cur];
        cur += inc_;
        while (cur != end_) {
            s += x_[cur];
            x_[cur] = s;
            cur += inc_;
        }
    }

    /**
     * Transforms this object into its cumulative sum, with an dumping
     * coefficient. If x is the initial data block and if y is the final data
     * block, we have: y(0) = x(0), y(t) = x(t) + c * y(t-1)
     *
     * @param c The dumping factor
     */
    public void cumul(double c) {
        int cur = beg_;
        double s = x_[cur];
        cur += inc_;
        while (cur != end_) {
            s = c * s + x_[cur];
            x_[cur] = s;
            cur += inc_;
        }
    }

    /**
     * Transforms this object to its cumulative sum, with an dumping coefficient
     * and a given lag.. If x is the initial data block and if y is the final
     * data block, we have: y(i) = x(i) where i &lt lag, y(t) = x(t) + c *
     * y(t-lag)
     *
     * @param c The dumping factor
     * @param lag The lag
     */
    public void cumul(double c, int lag) {
        int inc = lag * inc_;
        if (getLength() < lag) {
            return;
        }
        int cur = beg_ + inc;
        if (c == 1) {
            while (cur != end_) {
                x_[cur] += x_[cur - inc];
                cur += inc_;
            }
        } else {
            while (cur != end_) {
                x_[cur] += c * x_[cur - inc];
                cur += inc_;
            }
        }
    }

    /**
     * Creates a deep clone of this object. The underlying data a copied in a
     * new buffer
     *
     * @return The new object.
     */
    public DataBlock deepClone() {
        if (this == EMPTY) {
            return EMPTY;
        }
        DataBlock rc = new DataBlock(getLength());
        copyTo(rc.x_, 0);
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
        int cur = end_ - inc_;
        do {
            cur -= inc_;
            x_[cur + inc_] -= x_[cur];
        } while (cur != beg_);
    }

    /**
     * Computes the difference of two data blocks. The results are put in this
     * object. this(i) = l(i) - r(i). Length(this) &le min(length(l), length(r))
     * If this object is smaller than the operands, only the first items are
     * considered.
     *
     * @param l Left operand
     * @param r right operand
     */
    public void difference(DataBlock l, DataBlock r) {
        for (int i = beg_, j = l.beg_, k = r.beg_; i != end_; i += inc_, j += l.inc_, k += r.inc_) {
            x_[i] = l.x_[j] - r.x_[k];
        }
    }

    /**
     * Differences this object with a given lag. this(i) = this(i) -
     * this(i-lag). The first elements are unchanged
     *
     * @param lag The lag.
     */
    public void difference(double lag) {
        if (getLength() <= 1) {
            return;
        }
        int cur = end_ - inc_;
        do {
            cur -= inc_;
            x_[cur + inc_] -= lag * x_[cur];
        } while (cur != beg_);
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
        int inc = inc_ * lag;
        int cur = end_ - inc;
        do {
            cur -= inc_;
            x_[cur + inc] -= c * x_[cur];
        } while (cur != beg_);
    }

    /**
     * Return the euclidian norm of the differences between two data blocks
     *
     * @param data The comparison data block.
     * @return The euclidian norm
     */
    public double distance(DataBlock data) {
        if (beg_ == end_) {
            return 0;
        } else if (beg_ + inc_ == end_) {
            return Math.abs(x_[beg_] - data.x_[data.beg_]);
        } else {
            double scale = ZERO;
            double ssq = ONE;
            for (int ix = beg_, jx = data.beg_; ix != end_; ix += inc_, jx += data.inc_) {
                double d = x_[ix] - data.x_[jx];
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
            return scale * Math.sqrt(ssq);
        }
    }

    /**
     * Computes the scalar product of two data blocks. r = this(0)*data(0) + ...
     * + this(n)*data(n)
     *
     * @param data The second data block. data can be smaller than this object.
     * In that case, only the first elements are considered (the size of the
     * buffer (data) is preponderant).
     * @return The scalar product
     */
    public double dot(DataBlock data) {
        double r = 0;
        //
        if (inc_ == 1 && data.inc_ == 1) {
            for (int i = beg_, j = data.beg_; i != end_; ++i, ++j) {
                r += x_[i] * data.x_[j];
            }
        } else if (inc_ == -1 && data.inc_ == -1) {
            for (int i = beg_, j = data.beg_; i != end_; --i, --j) {
                r += x_[i] * data.x_[j];
            }
        } else {
            for (int i = beg_, j = data.beg_; i != end_; i += inc_, j += data.inc_) {
                r += x_[i] * data.x_[j];
            }
        }
        return r;
    }

    public double dot(double[] data) {
        double r = 0;
        //
        if (inc_ == 1 && beg_ == 0) {
            for (int i = 0; i < data.length; ++i) {
                r += x_[i] * data[i];
            }
        } else {
            for (int i = beg_, j = 0; j < data.length; i += inc_, ++j) {
                r += x_[i] * data[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product of two data blocks using the J-norm. r =
     * this(0)*data(0) + ...this(p-1)*data(p-1) - this(p)*data(p) - ... -
     * this(n)*data(n)
     *
     * @param data The second data block. data can be larger than this object.
     * In that case, only the first elements are considered.
     * @param p The number of (leading) positive terms in the norm
     * @return The scalar product
     */
    public double jdot(int p, DataBlock data) {
        double r = 0;
        int pend = beg_ + p * inc_;
        //
        if (inc_ == 1 && data.inc_ == 1) {
            int i = beg_, j = data.beg_;
            for (; i != pend; ++i, ++j) {
                r += x_[i] * data.x_[j];
            }
            for (; i != end_; ++i, ++j) {
                r -= x_[i] * data.x_[j];
            }
        } else if (inc_ == -1 && data.inc_ == -1) {
            int i = beg_, j = data.beg_;
            for (; i != pend; --i, --j) {
                r += x_[i] * data.x_[j];
            }
            for (; i != end_; --i, --j) {
                r -= x_[i] * data.x_[j];
            }
        } else {
            int i = beg_, j = data.beg_;
            for (; i != pend; i += inc_, j += data.inc_) {
                r += x_[i] * data.x_[j];
            }
            for (; i != end_; i += inc_, j += data.inc_) {
                r += x_[i] * data.x_[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product two data blocks, inverting the order of the
     * second one. r = this(0)*data(n) + ... + this(n)*data(0).
     *
     * @param data The second data block. data can be larger than this object.
     * In that case, only the first elements of this and the last elements of
     * data are considered.
     * @return The scalar product
     */
    public double dotReverse(DataBlock data) {
        double r = 0;
        for (int i = beg_, j = data.end_ - data.inc_; i != end_; i += inc_, j -= data.inc_) {
            r += x_[i] * data.x_[j];
        }
        return r;
    }

    /**
     * Computes the scalar product two data blocks, inverting the order of the
     * second one. r = this(0)*data(n) + ... + this(n)*data(0).
     *
     * @param data The second data block. data can be smaller than this object.
     * In that case, only the first elements of this and the last elements of
     * data are considered.
     * @return The scalar product
     */
    public double dotReverse(double[] data) {
        double r = 0;
        if (inc_ == 1) {
            for (int i = beg_, j = data.length - 1; j >= 0; ++i, --j) {
                r += x_[i] * data[j];
            }
        } else {
            for (int i = beg_, j = data.length - 1; j >= 0; i += inc_, --j) {
                r += x_[i] * data[j];
            }
        }
        return r;
    }

    /**
     * Computes the scalar product two data blocks, inverting the order of the
     * two blocks. r = this(n)*data(m) + ... + this(n-m)*data(0).
     *
     * @param data The second data block. data can be smaller than this object.
     * In that case, only the first elements of this and the last elements of
     * data are considered.
     * @return The scalar product
     */
    public double reverseDot(double[] data) {
        double r = 0;
        int m=data.length;
        if (inc_ == 1) {
            for (int i = end_-1, j = m - 1; j >= 0; --i, --j) {
                r += x_[i] * data[j];
            }
        } else {
            for (int i = end_-inc_, j = m - 1; j >= 0; i -= inc_, --j) {
                r += x_[i] * data[j];
            }
        }
        return r;
    }
    /**
     * Creates an extract of this data block by dropping ending elements. The
     * new data block refers to the same underlying physical data buffer.
     *
     * @param nbeg The number of elements dropped at the beginning of this
     * object. Should be positive.
     * @param nend The number of elements dropped at the end of this object.
     * Should be positive.
     * @return The new data block. Can be empty (but not null)
     */
    public DataBlock drop(int nbeg, int nend) {
//	if (nbeg + nend >= getLength())
//	    return null;
//	else
        return inc_ == 1 ? new DataBlock(x_, beg_ + nbeg, end_ - nend, 1)
                : new DataBlock(x_, beg_ + nbeg * inc_, end_ - nend * inc_, inc_);
    }

    /**
     * Shrinks the data block by 1 data at the beginning of the block
     *
     * @return true if the data block has been correctly shrunk, false
     * otherwise.
     */
    public boolean bshrink() {
        if (beg_ != end_) {
            beg_ += inc_;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shrinks the data block by 1 data at the end of the block
     *
     * @return true if the data block has been correctly shrunk, false
     * otherwise.
     */
    public boolean eshrink() {
        if (beg_ != end_) {
            end_ -= inc_;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shrinks the current data block by removing data at the beginning and at
     * the end of the data block.
     *
     * @param nbeg Number of elements removed at the beginning of this data
     * block
     * @param nend Number of elements removed at the end of this data block
     * @return true if the data block has been correctly shrunk, false
     * otherwise.
     */
    public boolean shrink(int nbeg, int nend) {
        if (nbeg + nend <= getLength()) {
            beg_ += inc_ * nbeg;
            end_ -= inc_ * nend;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Expands the current data block by adding data at the beginning or at the
     * end of the data block. The buffer must be larger enough.
     *
     * @param nbeg Number of elements added at the beginning of this data block
     * @param nend Number of elements added at the end of this data block
     * @return true if the data block has been correctly expanded, false
     * otherwise.
     */
    public boolean expand(int nbeg, int nend) {
        int xbeg = beg_ - nbeg * inc_;
        int xend = end_ + nend * inc_;

        if (xbeg < 0 || xbeg > x_.length) {
            return false;
        } else {
            beg_ = xbeg;
            end_ = xend;
            return true;
        }
    }
    
    /**
     * Creates a new data block that is an extension of this one. The new data
     * block refers to the same underlying physical data buffer. We have that
     * x.clone().expand(n, m) is equivalent to
     *
     * @param nbeg The number of elements added at the beginning of this object.
     * Should be positive.
     * @param nend The number of elements added at the end of this object.
     * Should be positive.
     * @return The new data block. The method doesn't check that the operation
     * is valid (i.e. that the buffer is large enough).
     */
    public DataBlock extend(int nbeg, int nend) {
        return new DataBlock(x_, beg_ - nbeg * inc_,
                end_ + nend * inc_, inc_);
    }

    @Override
    public DataBlock extract(int start, int count) {
        return extract(start, count, 1);
    }

    /**
     * Creates a new data block from the given one. The increment and the
     * starting position are relative to the existing one. More precisely, if
     * the increment in the current data block is inc0 and if its starting
     * position (in the underlying buffer) is start0, The new data block will
     * start at position start0 + start*inc0 and its increment will be
     * inc0*inc1.
     *
     * @param start The starting position (in the current data block).
     * @param count The number of items in the selection. If count is -1, the
     * largest extract is returned.
     * @param inc The increment of the selection.
     * @return A new data block is returned.
     */
    public DataBlock extract(int start, int count, int inc) {
        int i0, i1, ninc;
        if (inc_ == 1) {
            i0 = beg_ + start;
            ninc = inc;
        } else {
            i0 = beg_ + start * inc_;
            ninc = inc * inc_;
        }
        if (count == -1) {
            // not optimized. We go from i0 to i1 by step of ninc (i1 = i0 + n*ninc)
            // (i1-ninc) must be <= (end-inc) if ninc > 0 or >= end-inc if ninc <0
            // case inc > 0 : n = 1 + (end - inc - i0) / ninc
            int n = 0;
            if ((inc_ > 0 && i0 <= end_ - inc_) || (inc_ < 0 && i0 >= end_ - inc_)) {
                if (inc > 0) {
                    n = 1 + (end_ - inc_ - i0) / ninc;
                } else {
                    n = 1 + (beg_ - i0) / ninc;
                }
            }
            i1 = i0 + n * ninc;
        } else {
            i1 = i0 + ninc * count;
        }
        return new DataBlock(x_, i0, i1, ninc);
    }

    private void fshift() {
        if (inc_ == 1) {
            for (int i = end_ - 1; i != beg_; --i) {
                x_[i] = x_[i - 1];
            }
        } else {
            for (int i = end_ - inc_; i != beg_; i -= inc_) {
                x_[i] = x_[i - inc_];
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
                double last = x_[end_ - inc_];
                fshift();
                x_[beg_] = last;
                break;
            case Zero:
                fshift();
                x_[beg_] = 0;
                break;
            case Sum:
                x_[beg_] = sfshift();
                break;
            case NegSum:
                x_[beg_] = -sfshift();
                break;
            default:
                fshift();
                break;
        }
    }

    /**
     * Return the idx-th element of the data block
     *
     * @param idx 0-base position of the element in the data block. Should
     * belong to [0, length[. Not checked
     * @return The required element. Unpredictable result when the index is
     * invalid.
     */
    @Override
    public double get(int idx) {
        return x_[beg_ + inc_ * idx];
    }

    /**
     * Returns the underlying buffer. This method, which breaks down the data
     * encapsulation principle, is provided for optimisation reason only.
     *
     * @return The underlying buffer.
     */
    public double[] getData() {
        return x_;
    }

    /**
     * Return the end position of the data block in the underlying array. The
     * end position doesn't belong to the data block. It should be noted that
     * endPosition can be &lt startPosition (in the case of negative increment)
     *
     * @return The end position.
     */
    public int getEndPosition() {
        return end_;
    }

    /**
     * Return the last position of the data block in the underlying array. The
     * last position belongs to the data block.
     *
     * @return The last position.
     */
    public int getLastPosition() {
        return end_ - inc_;
    }

    /**
     * Return the last valid index that can be applied on this data block
     *
     * @return The last position.
     */
    public int getLastIndex() {
        return inc_ == 1 ? end_ - beg_ - 1 : (end_ - beg_) / inc_ - 1;
    }

    /**
     * Return the first invalid index for this data block
     *
     * @return The last position.
     */
    public int getEndIndex() {
        return inc_ == 1 ? end_ - beg_ : (end_ - beg_) / inc_;
    }

    /**
     * Return the interval between two selected elements.
     *
     * @return The increment. Can be &lt 0.
     */
    public int getIncrement() {
        return inc_;
    }

    @Override
    public int getLength() {
        if (beg_ == end_) {
            return 0;
        } else if (inc_ == 1) {
            return end_ - beg_;
        } else {
            return (end_ - beg_) / inc_;
        }
    }

    /**
     * Return the start position of the data block in the underlying array.
     *
     * @return The start position.
     */
    public int getStartPosition() {
        return beg_;
    }

    /**
     * Checks that all the data of the block are identical
     *
     * @return true if all the data are identical, false otherwise
     */
    public boolean isConstant() {
        double d = x_[beg_];
        for (int i = beg_ + inc_; i != end_; i += inc_) {
            if (x_[i] != d) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that all the data of the block are equal to a given value
     *
     * @param c The constant value
     * @return true if all the data are equal to c, false otherwise
     */
    public boolean isConstant(double c) {
        for (int i = beg_; i != end_; i += inc_) {
            if (x_[i] != c) {
                return false;
            }
        }
        return true;
    }

    public int getMissingCount() {
        int n = 0;
        for (int i = beg_; i != end_; i += inc_) {
            if (!DescriptiveStatistics.isFinite(x_[i])) {
                ++n;
            }
        }
        return n;
    }

    /**
     * Checks that the data block is empty. Occurs when getStartPosition() =
     * getendPosition().
     *
     * @return true if the data block is empty, false otherwise.
     */
    public boolean isEmpty() {
        return beg_ == end_;
    }

    /**
     * Checks that all the data in the block are (nearly) 0. Equivalent to
     * isZero(EPSILON);
     *
     * @return true if all the data in the block are &lt or = EPSILON in
     * absolute value, false otherwise.
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
        if (inc_ != 1) {
            del *= inc_;
        }
        beg_ += del;
        end_ += del;
    }

    /**
     * Multiplies the elements of this data block by a given value
     * this(i)=this(i)*d
     *
     * @param d The multiplier.
     */
    public void mul(double d) {
        if (d == 1) {
        } else if (d == 0) {
            set(0);
        } else if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] *= d;
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] *= d;
            }
        }
    }

    /**
     * Multiply the elements of this object by the element of another data block
     * this(i) = this(i) * r(i)
     *
     * @param data The other data block. Its length must be &ge the length of
     * this object.
     */
    public void mul(DataBlock data) {
        if (inc_ == 1 && data.inc_ == 1) {
            for (int i = beg_, j = data.beg_; i != end_; ++i, ++j) {
                x_[i] *= data.x_[j];
            }
        } else {
            for (int i = beg_, j = data.beg_; i != end_; i += inc_, j += data.inc_) {
                x_[i] *= data.x_[j];
            }
        }
    }

    /**
     * Divide the elements of this object by the element of another data block
     * this(i) = this(i) / r(i)
     *
     * @param data The other data block. Its length must be &ge the length of
     * this object.
     */
    public void div(DataBlock data) {
        if (inc_ == 1 && data.inc_ == 1) {
            for (int i = beg_, j = data.beg_; i != end_; ++i, ++j) {
                x_[i] /= data.x_[j];
            }
        } else {
            for (int i = beg_, j = data.beg_; i != end_; i += inc_, j += data.inc_) {
                x_[i] /= data.x_[j];
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
        x_[beg_ + idx * inc_] *= d;
    }

    /**
     * Computes the euclidian norm of the data block. Based on the "dnrm2"
     * Lapack function.
     *
     * @return The euclidian norm (&gt=0).
     */
    public double nrm2() {
        if (beg_ == end_) {
            return 0;
        } else if (beg_ + inc_ == end_) {
            return Math.abs(x_[beg_]);
        } else {
            double scale = ZERO;
            double ssq = ONE;
            for (int ix = beg_; ix != end_; ix += inc_) {
                if (x_[ix] != ZERO) {
                    double absxi = Math.abs(x_[ix]);
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
     * Computes the infinite-norm of this data block
     *
     * @return Returns min{|x(i)|}
     */
    public double nrmInf() {
        if (beg_ == end_) {
            return 0;
        } else {
            double nrm = Math.abs(x_[beg_]);
            for (int ix = beg_ + inc_; ix != end_; ix += inc_) {
                double tmp = Math.abs(x_[ix]);
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Computes the minimum of this data block
     *
     * @return Returns min{x(i)}
     */
    public double min() {
        if (beg_ == end_) {
            return 0;
        } else {
            double nrm = x_[beg_];
            for (int ix = beg_ + inc_; ix != end_; ix += inc_) {
                double tmp = x_[ix];
                if (tmp < nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Computes the maximum of this data block
     *
     * @return Returns max{x(i)}
     */
    public double max() {
        if (beg_ == end_) {
            return 0;
        } else {
            double nrm = x_[beg_];
            for (int ix = beg_ + inc_; ix != end_; ix += inc_) {
                double tmp = x_[ix];
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    /**
     * Computes the product of the element of the data block/
     *
     * @return The product of the elements or 1 if the data block is empty.
     */
    public double product() {
        double s = 1;
        for (int i = beg_; i != end_; i += inc_) {
            s *= x_[i];
        }
        return s;
    }

    /**
     * Computes the product of a vector by a matrix and stores the result in
     * this data block this = row * cols. We must have that 1. the length of
     * this data block = the number of columns 2. the length of the vector = the
     * length of each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
     */
    public void product(DataBlock row, DataBlockIterator cols) {
        int idx = beg_;
        DataBlock cur = cols.getData();
        do {
            x_[idx] = row.dot(cur);
            idx += inc_;
        } while (cols.next());
    }

    /**
     * Adds the product of a vector by a matrix to this data block 
     * this += row * cols. We must have that 1. the length of
     * this data block = the number of columns 2. the length of the vector = the
     * length of each column. The iterator is changed !!!
     *
     * @param row The vector array
     * @param cols The columns of the matrix
     */
    public void addProduct(DataBlock row, DataBlockIterator cols) {
        int idx = beg_;
        DataBlock cur = cols.getData();
        do {
            x_[idx] += row.dot(cur);
            idx += inc_;
        } while (cols.next());
    }
    /**
     * this = l * d. Product of a vector by a double. The results is stored in
     * this object
     *
     * @param l The vector (same length as this data block)
     * @param d The double
     */
    public void product(DataBlock l, double d) {
        if (d == 0) {
            set(0);
        } else if (d == 1) {
            copy(l);
        } else if (inc_ == 1 && l.inc_ == 1) {
            for (int i = beg_, j = l.beg_; i != end_; ++i, ++j) {
                x_[i] = l.x_[j] * d;
            }
        } else if (inc_ == -1 && l.inc_ == -1) {
            for (int i = beg_, j = l.beg_; i != end_; --i, --j) {
                x_[i] = l.x_[j] * d;
            }
        } else {
            for (int i = beg_, j = l.beg_; i != end_; i += inc_, j += l.inc_) {
                x_[i] = l.x_[j] * d;
            }
        }
    }

    /**
     * Computes the product of a matrix by a vector and stores the result in
     * this data block this = rows * col. We must have that 1. the length of
     * this data block = the number of rows 2. the length of the vector = the
     * length of each row. The iterator is changed !!!
     *
     * @param rows The rows of the matrix.
     * @param col The vector.
     */
    public void product(DataBlockIterator rows, DataBlock col) {
        int idx = beg_;
        DataBlock cur = rows.getData();
        do {
            x_[idx] = cur.dot(col);
            idx += inc_;
        } while (rows.next());
    }

    /**
     * Adds the product of a matrix by a vector to this data block 
     * this += rows * col. We must have that 1. the length of
     * this data block = the number of rows 2. the length of the vector = the
     * length of each row. The iterator is changed !!!
     *
     * @param rows The rows of the matrix.
     * @param col The vector.
     */
    public void addProduct(DataBlockIterator rows, DataBlock col) {
        int idx = beg_;
        DataBlock cur = rows.getData();
        do {
            x_[idx] += cur.dot(col);
            idx += inc_;
        } while (rows.next());
    }
    /**
     * Creates a new data block from this one
     *
     * @param c0 The first item of the selection (included)
     * @param c1 The last item of the selection (excluded)
     * @return The new data block
     */
    public DataBlock range(int c0, int c1) {
        if (inc_ == 1) {
            return new DataBlock(x_, beg_ + c0, beg_ + c1, 1);
        } else {
            return new DataBlock(x_, beg_ + inc_ * c0,
                    beg_ + inc_ * c1, inc_);
        }
    }

    /**
     * Reverses the order of the data block (from the last item to the first
     * item);
     *
     * @return A new data block (the current object is not modified).
     */
    public DataBlock reverse() {
        return new DataBlock(x_, end_ - inc_, beg_ - inc_, -inc_);
    }

    @Override
    public DataBlock rextract(int start, int count) {
        return extract(start, count, 1);
    }

    private double sbshift() {
        int imax = end_ - inc_;
        double s = x_[beg_];
        if (inc_ == 1) {
            for (int i = beg_; i != imax; ++i) {
                x_[i] = x_[i + 1];
                s += x_[i];
            }
        } else {
            for (int i = beg_; i < imax; i += inc_) {
                x_[i] = x_[i + inc_];
                s += x_[i];
            }
        }
        return s;
    }

    /**
     * Sets all the data of the block to a given value.
     *
     * @param d The value to be set.
     */
    public void set(double d) {
        if (inc_ == 1) {
            Arrays.fill(x_, beg_, end_, d);
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] = d;
            }
        }
    }

    /**
     * Sets the data at the idx-th position to a given value.
     *
     * @param idx The position of the data being changed
     * @param value The new value.
     */
    @Override
    public void set(int idx, double value) {
        x_[beg_ + inc_ * idx] = value;
    }

    private double sfshift() {
        double s = x_[beg_];
        if (inc_ == 1) {
            for (int i = end_ - 1; i != beg_; --i) {
                s += x_[i];
                x_[i] = x_[i - 1];
            }
        } else {
            for (int i = end_ - inc_; i != beg_; i -= inc_) {
                s += x_[i];
                x_[i] = x_[i - inc_];
            }
        }
        return s;
    }

    /**
     * Moves the current data block by del cells. If the "move" method takes
     * into account the increment of the data block, this method is actually a
     * move on the physical buffer (without considering the increment)
     *
     * @param del The length of the sliding.
     */
    public void slide(int del) {
        beg_ += del;
        end_ += del;
    }

    /**
     * Returns the sum of the squared items.
     *
     * @return The sum of the squares.
     */
    public double ssq() {
        if (beg_ == end_) {
            return 0;
        } else if (beg_ + inc_ == end_) {
            return x_[beg_] * x_[beg_];
        } else {
            double ssq = ZERO;
            for (int ix = beg_; ix != end_; ix += inc_) {
                double x = x_[ix];
                ssq += x * x;
            }
            return ssq;
        }
    }

    /**
     * Computes sum([x(i)-m]^2
     *
     * @param m The correction parameter
     * @return sum([x(i)-m]^2
     */
    public double ssqc(double m) {
        if (beg_ == end_) {
            return 0;
        } else if (beg_ + inc_ == end_) {
            double xc = x_[beg_] - m;
            return xc * xc;
        } else {
            double ssq = ZERO;
            for (int ix = beg_; ix != end_; ix += inc_) {
                double x = x_[ix] - m;
                ssq += x * x;
            }
            return ssq;
        }
    }

    /**
     * Subtracts the data block r to this object. this(i) = this(i) - r(i)
     *
     * @param data The subtracted data block. Its length must be &ge the length of
     * this object.
     */
    public void sub(DataBlock data) {
        if (inc_ == 1 && data.inc_ == 1) {
            for (int i = beg_, j = data.beg_; i != end_; ++i, ++j) {
                x_[i] -= data.x_[j];
            }
        } else {
            for (int i = beg_, j = data.beg_; i != end_; i += inc_, j += data.inc_) {
                x_[i] -= data.x_[j];
            }
        }
    }

    /**
     * Subtracts d to all the elements of this data block this(i) = this(i) - d
     *
     * @param d. The subtracted value.
     */
    public void sub(double d) {
        if (d == 0) {
            return;
        }
        if (inc_ == 1) {
            for (int i = beg_; i != end_; ++i) {
                x_[i] -= d;
            }
        } else {
            for (int i = beg_; i != end_; i += inc_) {
                x_[i] -= d;
            }
        }
    }

    /**
     * Computes the sum of the data in the block.
     *
     * @return The sum of the data block
     */
    public double sum() {
        double s = 0;
        for (int i = beg_; i != end_; i += inc_) {
            s += x_[i];
        }
        return s;
    }

    /**
     * Computes the sum of two data blocks. The results are stored in this
     * object. this[i] = l[i] + r[i]
     *
     * @param l The left operand.
     * @param r The right operand.
     */
    public void sum(DataBlock l, DataBlock r) {
        for (int i = beg_, j = l.beg_, k = r.beg_; i != end_; i += inc_, j += l.inc_, k += r.inc_) {
            x_[i] = l.x_[j] + r.x_[k];
        }
    }

    /**
     * Computes the product of the items of this data block, under a log (and
     * sign) form. Returns
     *
     * @return The log and signed result of the product.
     */
    public LogSign sumLog() {
        LogSign ls = new LogSign();
        ls.value = 0;
        ls.pos = true;
        for (int i = beg_; i != end_; i += inc_) {
            double x = x_[i];
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
        for (int i = beg_; i != end_; i += inc_) {
            if (ndec > 0) {
                x_[i] = Math.round(x_[i] * f) / f;
            } else {
                x_[i] = Math.round(x_[i]);
            }
        }
    }

    @Override
    public String toString() {
        return ReadDataBlock.convert(this);
    }

    /**
     * Sets random numbers in this data block
     */
    public void randomize() {
        for (int i = beg_; i != end_; i += inc_) {
            x_[i] = RNG.nextDouble() - .5;
        }
    }

    public void randomize(int seed) {
        Random rnd = new Random(seed);
        for (int i = beg_; i != end_; i += inc_) {
            x_[i] = rnd.nextDouble() - .5;
        }
    }

    /**
     * Checks that all the data in the block are (nearly) 0.
     *
     * @param zero A given zero
     * @return false if some data in the block are &gt zero in absolute value,
     * true otherwise.
     */
    public boolean isZero(double zero) {
        for (int i = beg_; i != end_; i += inc_) {
            if (Math.abs(x_[i]) > zero) {
                return false;
            }
        }
        return true;
    }
//<editor-fold defaultstate="collapsed" desc="iterator method">
    
    /**
     * The following methods can be used to create fast iterations.
     * They avoid the creation of unnecessary objects
     * 
     * example:
     * 
     * DataBlock data=...
     * DataBlock cur=data.start();
     * while (cur.getEndPosition() != data.getEndPosition()){
     *    cur.next(z);
     * }
     */
    
    
    /**
     * Moves the current DataBlock to the right by a given number of items.
     * The new starting position is the old ending position
     * and the ending position is incremented by the given number.
     * @param nitems The number of items in the block
     */
    public void next(int nitems){
        beg_=end_;
        end_+=inc_*nitems;
    }

    /**
     * Moves the current DataBlock to the left by a given number of items.
     * The new ending position is the old starting position
     * and the starting position is decremented by the given number.
     * @param nitems The number of items in the block
     */
    public void previous(int nitems){
        end_=beg_;
        beg_-=inc_*nitems;
    }
    
    /**
     * Creates an empty DataBlock positioned at the current start.
     * To be used with next/previous
     * @return The new DataBlock
     */
    public DataBlock start(){
        return new DataBlock(x_, beg_, beg_, inc_);
    }

    /**
     * Creates an empty DataBlock positioned at the current end.
     * To be used with next/previous
     * @return The new DataBlock
     */
    public DataBlock end(){
        return new DataBlock(x_, end_, end_, inc_);
    }

//</editor-fold>

}

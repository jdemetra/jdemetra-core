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
import ec.tstoolkit.utilities.IntList;
import java.util.Arrays;
import static ec.tstoolkit.data.DescriptiveStatistics.isFinite;
import ec.tstoolkit.utilities.Arrays2;

/**
 * Enriched implementation of an "IDataSet". The data belong to this object. The
 * size of a "Values" object cannot be changed after its creation. Any method
 * that yield a change in the size returns a new object.
 *
 * @author Jean Palate, Philippe Charles
 *
 */
@Development(status = Development.Status.Alpha)
public class Values implements IReadDataBlock, Cloneable {
    // / <summary>d + vals</summary>
    // / <returns>A new Values object (= d + vals).</returns>

    /**
     *
     * @param d
     * @param vals
     * @return
     */
    public static Values add(final double d, final Values vals) {
        return add(vals, d);
    }

    // / <summary>vals + d</summary>
    // / <returns>A new Values object (= vals + d).</returns>
    /**
     *
     * @param vals
     * @param d
     * @return
     */
    public static Values add(final Values vals, final double d) {
        Values nvals = new Values(vals.getLength());
        for (int i = 0; i < vals.getLength(); ++i) {
            if (isFinite(vals.get(i))) {
                nvals.set(i, vals.get(i) + d);
            }
        }
        return nvals;
    }

    // / <summary>vals / d</summary>
    // / <returns>A new Values object (= d / vals).</returns>
    // / <remarks>Invalid operations (x / 0 ) yield missing values.</remarks>
    /**
     *
     * @param d
     * @param vals
     * @return
     */
    public static Values divide(final double d, final Values vals) {
        Values nvals = new Values(vals.getLength());
        for (int i = 0; i < vals.getLength(); ++i) {
            if (isFinite(vals.get(i)) && (vals.get(i) != 0)) {
                nvals.set(i, d / vals.get(i));
            }
        }
        return nvals;
    }

    // / <summary>d / vals</summary>
    // / <returns>A new Values object (= vals / d).</returns>
    /**
     *
     * @param vals
     * @param d
     * @return
     */
    public static Values divide(final Values vals, final double d) {
        Values nvals = new Values(vals.getLength());
        if (d != 0) {
            for (int i = 0; i < vals.getLength(); ++i) {
                if (isFinite(vals.get(i))) {
                    nvals.set(i, vals.get(i) / d);
                }
            }
        }
        return nvals;
    }

    // / <summary>d * vals</summary>
    // / <returns>A new Values object (= d * vals).</returns>
    /**
     *
     * @param d
     * @param vals
     * @return
     */
    public static Values multiply(final double d, final Values vals) {
        return multiply(vals, d);
    }

    // / <summary>vals * d</summary>
    // / <returns>A new Values object (= vals * d).</returns>
    /**
     *
     * @param vals
     * @param d
     * @return
     */
    public static Values multiply(final Values vals, final double d) {
        Values nvals = new Values(vals.getLength());
        for (int i = 0; i < vals.getLength(); ++i) {
            if (isFinite(vals.get(i))) {
                nvals.set(i, vals.get(i) * d);
            }
        }
        return nvals;
    }

    /**
     *
     * @param d
     * @param vals
     * @return
     */
    public static Values subtract(final double d, final Values vals) {
        Values nvals = new Values(vals.getLength());
        for (int i = 0; i < vals.getLength(); ++i) {
            if (isFinite(vals.get(i))) {
                nvals.set(i, d - vals.get(i));
            }
        }
        return nvals;
    }

    // / <summary>vals - d</summary>
    // / <returns>A new Values object (= vals - d).</returns>
    /**
     *
     * @param vals
     * @param d
     * @return
     */
    public static Values subtract(final Values vals, final double d) {
        Values nvals = new Values(vals.getLength());
        for (int i = 0; i < vals.getLength(); ++i) {
            if (isFinite(vals.get(i))) {
                nvals.set(i, vals.get(i) - d);
            }
        }
        return nvals;
    }
    private double[] m_vals;
    private int m_nm;

    /**
     * Creates a new empty object
     */
    public Values() {
        m_vals = Arrays2.EMPTY_DOUBLE_ARRAY;
    }

    /**
     * Creates a new object that contains a copy of the given data.
     *
     * @param data The data being copied.
     */
    public Values(final double[] data) {
        m_vals = data.clone();
        m_nm = -1;
    }

    /**
     *
     * @param data
     * @param copydata
     */
    public Values(double[] data, final boolean copydata) {
        if (copydata) {
            m_vals = data.clone();
        } else {
            m_vals = data;
        }
        m_nm = -1;
    }

    /**
     * Creates a new object of n doubles. All the values are set to Double.NaN.
     *
     * @param n The number of items
     */
    public Values(final int n) {
        m_vals = new double[n];
        for (int i = 0; i < m_vals.length; ++i) {
            m_vals[i] = Double.NaN;
        }
        m_nm = n;
    }

    public Values(final int n, final double val) {
        m_vals = new double[n];
        Arrays.fill(m_vals, val);
        m_nm = Double.isNaN(val) ? n : 0;
    }

    /**
     *
     * @param data
     */
    public Values(final IReadDataBlock data) {
        m_vals = new double[data.getLength()];
        data.copyTo(m_vals, 0);
    }

    // / <summary>Takes the absolute value</summary>
    /**
     *
     */
    public void abs() {
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] = Math.abs(m_vals[i]);
            }
        }
    }

    // / <summary>Adds a given number to each value</summary>
    // / <param name="d">The added value.</param>
    /**
     *
     * @param d
     */
    public void add(final double d) {
        if (d == 0) {
            return;
        }
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] += d;
            }
        }
    }

    /**
     *
     * @param pos
     * @param d
     */
    public void add(final int pos, final double d) {
        if (isFinite(m_vals[pos])) {
            m_vals[pos] += d;
        }
    }

    // / <summary>Changes the signs of the data</summary>
    /**
     *
     */
    public void chs() {
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] = -m_vals[i];
            }
        }
    }

    @Override
    public Values clone() {
        try {
            Values vals = (Values) super.clone();
            vals.m_vals = m_vals.clone();
            return vals;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    // / <remarks>
    // / If the current object is longer than the array being copied, only the
    // first items
    // / are replaced by the new values. If it is smaller, the excess values are
    // ignored.
    // / </remarks>
    // / <summary>Copies a given "Values" object</summary>
    // / <param name="v">The object being copied.</param>
    /**
     *
     * @param v
     */
    public void copy(final Values v) {
        int n = m_vals.length;
        if (n > v.m_vals.length) {
            n = v.m_vals.length;
        }
        for (int i = 0; i < n; ++i) {
            m_vals[i] = v.m_vals[i];
        }
        m_nm = -1;
    }

    // / <summary>Copies an array of double.</summary>
    // / <remarks>
    // / If the current object is longer than the array being copied, only the
    // first items
    // / are replaced by the new values. If it is smaller, the excess values are
    // ignored.
    // / </remarks>
    // / <param name="vals">The copied values</param>
    /**
     *
     * @param vals
     * @param start
     */
    public void copyFrom(final double[] vals, int start) {
        int n = m_vals.length;
        if (n > vals.length - start) {
            n = vals.length - start;
        }
        for (int i = 0, j = start; i < n; ++i, ++j) {
            m_vals[i] = vals[j];
        }
        m_nm = -1;
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        for (int i = 0; i < m_vals.length; ++i) {
            buffer[start + i] = m_vals[i];
        }
    }

    // / <summary>Divides the data by a given number</summary>
    // / <remarks>Invalid operations (/0) yield missing values.</remarks>
    // / <param name="d">The divisor.</param>
    /**
     *
     * @param d
     */
    public void div(final double d) {
        if (d == 0) {
            m_nm = m_vals.length;
        } else if (d == 1) {
            return;
        }
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] /= d;
            }
        }
    }

    /**
     * Shortens a Values object. If nfirst and/or nlast are negative, the set is
     * extended.
     *
     * @param nfirst The number of items being dropped at the beginning of the
     * dataset.
     * @param nlast The number of items being dropped at the end of the dataset.
     * @return The shortened Values objects
     * @see extend
     */
    public Values drop(final int nfirst, final int nlast) {
        return extend(-nfirst, -nlast);
    }

    // / <summary>X=exp(X)</summary>
    /**
     *
     */
    public void exp() {
        m_nm = -1;
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] = Math.exp(m_vals[i]);
            }
        }

    }

    /**
     * Extends a values object. New items are set to Double.NaN (missing
     * values). If nb and/or ne are negative, the set is shortened.
     *
     * @param nb Number of new items at the beginning of the dataset.
     * @param ne Number of new items at the end of the dataset.
     * @return The extended object
     */
    public Values extend(final int nb, final int ne) {
        if (nb == 0 && ne == 0) {
            return clone();
        }
        int sz = m_vals.length + nb + ne;
        if (sz <= 0) {
            return new Values(0);
        }
        Values rslt = new Values(sz);

        int i0 = 0, i1 = m_vals.length;
        int j0 = -nb, j1 = i1 + ne;
        int beg = Math.max(i0, j0), end = Math.min(i1, j1);
        int ncopy = end - beg;

        // initial missing values
        int cur = nb > 0 ? nb : 0;
        for (int i = 0; i < ncopy; ++i) {
            rslt.set(cur++, m_vals[beg + i]);
        }
        rslt.m_nm = m_nm == 0 ? 0 : -1;
        return rslt;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double get(final int idx) {
        return m_vals[idx];
    }

    /**
     *
     * @return
     */
    @Override
    public int getLength() {
        return m_vals.length;
    }

    /**
     *
     * @return
     */
    public int getMissingValuesCount() {
        if (m_nm < 0) {
            m_nm = 0;
            for (double element : m_vals) {
                if (!isFinite(element)) {
                    ++m_nm;
                }
            }
        }
        return m_nm;
    }

    /**
     * The number of non missing values.
     *
     * @return Count-MissingValuesCount
     */
    public int getObsCount() {
        return m_vals.length - getMissingValuesCount();
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    public int getRepeatCount() {
        int i = 0;
        while ((i < m_vals.length) && !isFinite(m_vals[i])) {
            ++i;
        }
        if (i == m_vals.length) {
            return 0;
        }
        int c = 0;
        double prev = m_vals[i++];
        for (; i < m_vals.length; ++i) {
            double cur = m_vals[i];
            if (isFinite(cur)) {
                if (cur == prev) {
                    ++c;
                } else {
                    prev = cur;
                }
            }
        }
        return c;
    }

    /**
     *
     * @return
     */
    public boolean hasMissingValues() {
        return getMissingValuesCount() > 0;
    }

    /**
     *
     * @return
     */
    public double[] internalStorage() {
        m_nm = -1;
        return m_vals;
    }

    // / <summary>Takes the inverse of each value.</summary>
    // / <remarks>Invalid operations yield missing values.</remarks>
    /**
     *
     */
    public void inv() {
        m_nm = -1;
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                if (m_vals[i] != 0) {
                    m_vals[i] = 1 / m_vals[i];
                } else {
                    m_vals[i] = Double.NaN;
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return m_vals.length == 0;
    }

    // / <summary>Checks if a given item is missing (= Double.NaN)</summary>
    // / <returns>True if Item(i) = Double.NaN. False otherwise.</returns>
    // / <param name="i">The observed item</param>
    /**
     *
     * @param i
     * @return
     */
    public boolean isMissing(final int i) {
        return !isFinite(m_vals[i]);
    }

    // / <summary>X=log(X)</summary>
    // / <remarks>Invalid operations yield missing values.</remarks>
    /**
     *
     */
    public void log() {
        m_nm = -1;
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                if (m_vals[i] > 0) {
                    m_vals[i] = Math.log(m_vals[i]);
                } else {
                    m_vals[i] = Double.NaN;
                }
            }
        }
    }

    // / <summary>X=log(X)</summary>
    // / <remarks>Invalid operations yield missing values.</remarks>
    // / <param name="b">The base of the logarithmic transformation</param>
    /**
     *
     * @param b
     */
    public void log(final double b) {
        if (b <= 0) {
            return;
        }
        m_nm = -1;
        double c = Math.log(b);
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                if (m_vals[i] > 0) {
                    m_vals[i] = Math.log(m_vals[i]) / c;
                } else {
                    m_vals[i] = Double.NaN;
                }
            }
        }
    }

    /**
     * Searches the maximal value(s).
     *
     * @return The position(s) of the maximal value(s). Null if all the data are
     * missing.
     */
    public int[] maxIndexes() {
        int i = 0;
        while ((i < m_vals.length) && !isFinite(m_vals[i])) {
            ++i;
        }
        if (i == m_vals.length) {
            return null;
        }

        IntList indexes = new IntList(m_vals.length - i);
        double curmax = m_vals[i++];
        indexes.add(i);
        for (; i < m_vals.length; ++i) {
            if (!isFinite(m_vals[i])) {
                continue;
            }
            if (m_vals[i] > curmax) {
                curmax = m_vals[i];
                indexes.clear();
                indexes.add(i);
            } else if (m_vals[i] == curmax) {
                indexes.add(i);
            }
        }

        return indexes.toArray();
    }

    /**
     * Searches the minimal value(s).
     *
     * @return The position(s) of the minimal value(s). Null if all the data are
     * missing.
     */
    public int[] minIndexes() {
        int i = 0;
        while ((i < m_vals.length) && !isFinite(m_vals[i])) {
            ++i;
        }
        if (i == m_vals.length) {
            return null;
        }

        IntList indexes = new IntList(m_vals.length - i);
        double curmin = m_vals[i++];
        indexes.add(i);
        for (; i < m_vals.length; ++i) {
            if (!isFinite(m_vals[i])) {
                continue;
            }
            if (m_vals[i] < curmin) {
                curmin = m_vals[i];
                indexes.clear();
                indexes.add(i);
            } else if (m_vals[i] == curmin) {
                indexes.add(i);
            }
        }

        return indexes.toArray();
    }

    // / <summary>Multiplies all the values by a given factor</summary>
    // / <param name="d">The multiplying factor.</param>
    /**
     *
     * @param d
     */
    public void mul(final double d) {
        if (d == 1) {
            return;
        }
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] *= d;
            }
        }
    }

    /**
     *
     * @param pos
     * @param d
     */
    public void mul(final int pos, final double d) {
        if (isFinite(m_vals[pos])) {
            m_vals[pos] *= d;
        }
    }

    // / <summary>Raises the values to a given exponent.</summary>
    // / <remarks>Invalid operations yield missing values.</remarks>
    // / <param name="e">The exponent</param>
    /**
     *
     * @param e
     */
    public void pow(final double e) {
        m_nm = -1;
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] = Math.pow(m_vals[i], e);
            }
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
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                double v = m_vals[i];
                if (ndec > 0) {
                    m_vals[i] = Math.round(v * f) / f;
                } else {
                    m_vals[i] = Math.round(v);
                }
            }
        }

    }

    /**
     *
     * @param start
     * @param length
     * @return
     */
    @Override
    public IReadDataBlock rextract(int start, int length) {
        return new ReadDataBlock(m_vals, start, length);
    }

    /**
     * X=a*X+b
     *
     * @param a
     * @param b
     */
    public void scale(final double a, final double b) {
        if ((a != 1) && (b != 0)) {
            for (int i = 0; i < m_vals.length; ++i) {
                if (isFinite(m_vals[i])) {
                    m_vals[i] = a * m_vals[i] + b;
                }
            }
        } else if (b == 0) {
            for (int i = 0; i < m_vals.length; ++i) {
                if (isFinite(m_vals[i])) {
                    m_vals[i] *= a;
                }
            }
        } else {
            for (int i = 0; i < m_vals.length; ++i) {
                if (isFinite(m_vals[i])) {
                    m_vals[i] += b;
                }
            }
        }
    }

    /**
     *
     * @param d
     */
    public void set(double d) {
        m_nm = 0;
        for (int i = 0; i < m_vals.length; ++i) {
            m_vals[i] = d;
        }
    }

    /**
     *
     * @param idx
     * @param value
     */
    public void set(final int idx, final double value) {
        m_vals[idx] = value;
        m_nm = -1;
    }

    // modifiers
    // / <summary>Replace a given item by a missing value.</summary>
    // / <param name="i">The index of the replaced item.</param>
    /**
     *
     * @param i
     */
    public void setMissing(final int i) {
        m_vals[i] = Double.NaN;
        m_nm = -1;
    }

    // / <summary>Sets all the missing values to a given value.</summary>
    // / <param name="nv">The new value that replaces all the missing values (if
    // any).</param>
    /**
     *
     * @param nv
     */
    public void setMissingValues(final double nv) {
        if (hasMissingValues()) {
            for (int i = 0; i < m_vals.length; ++i) {
                if (!isFinite(m_vals[i])) {
                    m_vals[i] = nv;
                }
            }
            if (isFinite(nv)) {
                m_nm = 0;
            }
        }
    }

    // / <summary>Subtracts a given number to each value</summary>
    // / <param name="d">The subtracted value.</param>
    /**
     *
     * @param d
     */
    public void sub(final double d) {
        if (d == 0) {
            return;
        }
        for (int i = 0; i < m_vals.length; ++i) {
            if (isFinite(m_vals[i])) {
                m_vals[i] -= d;
            }
        }
    }

    /**
     *
     */
    public void sqrt() {
        m_nm = -1;
        for (int i = 0; i < m_vals.length; ++i) {
            if (DescriptiveStatistics.isFinite(m_vals[i])) {
                if (m_vals[i] >= 0) {
                    m_vals[i] = Math.sqrt(m_vals[i]);
                } else {
                    m_vals[i] = Double.NaN;
                }
            }
        }
    }

    @Override
    public String toString() {
        return ReadDataBlock.convert(this);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Values && equals((Values) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Arrays.hashCode(this.m_vals);
        return hash;
    }

    public boolean equals(Values other) {
        return Arrays.equals(m_vals, other.m_vals);
    }
}

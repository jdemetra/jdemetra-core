/*
 * Copyright 2017 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.data;

import demetra.design.Development;
import demetra.utilities.functions.DoubleBiPredicate;
import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

/**
 * Read only indexed collection ofFunction doubles
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface Doubles extends DoubleSequence {

    //<editor-fold defaultstate="collapsed" desc="Static factories">
    /**
     * Read only envelope around an array ofFunction doubles.
     *
     * @param data The array ofFunction doubles. the toArray are not copied
     * (they might be modified externally). Use the of method to protect your
     * code against such porblems, if need be.
     * @return
     */
    public static Doubles ofInternal(double[] data) {
        return data == null ? ArrayReader.EMPTY : new ArrayReader(data);
    }

    /**
     * Read only envelope around a part ofFunction an array ofFunction doubles.
     *
     * @param data The array ofFunction doubles. the toArray are not copied
     * (they might be modified externally). Use the of method to protect your
     * code against such porblems, if need be.
     * @param start The first item. get(0) will correspond to toArray[reader].
     * @param len The number ofFunction items
     * @return
     */
    public static Doubles ofInternal(double[] data, int start, int len) {
        return len == 0 ? ArrayReader.EMPTY : new PartialArrayReader(data, start, len);
    }

    public static Doubles ofFunction(int n, IntToDoubleFunction fn) {
        return n == 0 ? ArrayReader.EMPTY : new FnReader(n, fn);
    }

    public static Doubles transformation(Doubles source, DoubleUnaryOperator fn) {
        return new FnReader(source.length(), i->fn.applyAsDouble(source.get(i)));
    }
    /**
     * Read only envelope around a part ofFunction an array ofFunction doubles.
     *
     * @param data The array ofFunction doubles. the toArray are copied
     * (defensive solution)
     * @return
     */
    public static Doubles of(double... data) {
        return data == null ? ArrayReader.EMPTY : new ArrayReader(data.clone());
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Specific methods">
    /**
     * The cell reader at the beginning of this object. The first data will be
     * retrieved by "next".
     *
     * @return
     */
    CellReader reader();

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
    Doubles extract(int start, int length);

    /**
     * Drops some items at the beginning and/or at the end of the array
     * @param beg The number of items dropped at the beginning
     * @param end The number of items dropped at the end
     * @return The shortened array
     */
    default Doubles drop(int beg, int end) {
        return extract(beg, length() - beg - end);
    }

    /**
     * Returns a new array of doubles in reverse order
     * @return 
     */
    default Doubles reverse() {
        final int n = length();
        return Doubles.ofFunction(n, i -> get(n - 1 - i));
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Lambda operations (with default implementations)">
    @Override
    default boolean allMatch(DoublePredicate pred) {
        return DoublesUtility.allMatch(this, pred);
    }

    default boolean allMatch(Doubles r, DoubleBiPredicate pred) {
        return DoublesUtility.allMatch(this, r, pred);
    }

    default int count(DoublePredicate pred) {
        return DoublesUtility.count(this, pred);
    }

    default int first(DoublePredicate pred) {
        return DoublesUtility.first(this, pred);
    }

    default int last(DoublePredicate pred) {
        return DoublesUtility.last(this, pred);
    }

    default double computeIteratively(final double initial, DoubleBinaryOperator fn) {
        return DoublesUtility.computeIteratively(this, initial, fn);
    }

    default int[] search(DoublePredicate pred) {
        return DoublesUtility.search(this, pred);
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Descriptive statistics (with default implementations">
    default double sum() {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> s + x);
    }

    default double ssq() {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> s + x * x);
    }

    default double ssqc(double mean) {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> {
            x -= mean;
            return s + x * x;
        });
    }

    default double sumWithMissing() {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> Double.isFinite(x) ? s + x : s);
    }

    default double ssqWithMissing() {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> Double.isFinite(x) ? s + x * x : s);
    }

    default double ssqcWithMissing(double mean) {
        return DoublesUtility.computeIteratively(this, 0, (s, x) -> {
            if (Double.isFinite(x)) {
                x -= mean;
                return s + x * x;
            } else {
                return s;
            }
        });
    }

    default double average() {
        return sum() / length();
    }

    default double averageWithMissing() {
        return DoublesUtility.averageWithMissing(this);
    }

    default boolean isMissing(int idx) {
        return !Double.isFinite(get(idx));
    }

    /**
     * Computes the norm1 ofFunction this src block (sum ofFunction the absolute
     * values)
     *
     * @return Returns min{|src(i)|}
     */
    default double norm1() {
        return DoublesUtility.norm1(this);
    }

    /**
     * Computes the euclidian norm ofFunction the src block. Based on the
     * "dnrm2" Lapack function.
     *
     * @return The euclidian norm (&gt=0).
     */
    default double norm2() {
        return DoublesUtility.norm2(this);
    }

    default double fastNorm2() {
        return DoublesUtility.fastNorm2(this);
    }

    /**
     * Computes the infinite-norm ofFunction this src block
     *
     * @return Returns min{|src(i)|}
     */
    default double normInf() {
        return DoublesUtility.normInf(this);
    }

    /**
     * Counts the number ofFunction identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    default int getRepeatCount() {
        return DoublesUtility.getRepeatCount(this);
    }

    /**
     * Checks that all the src in the block are (nearly) 0.
     *
     * @param zero A given zero
     * @return false if some src in the block are &gt zero in absolute value,
     * true otherwise.
     */
    default boolean isZero(double zero) {
        return allMatch(x -> !Double.isFinite(x) || Math.abs(x) <= zero);
    }

    default boolean isConstant(double cnt) {
        return allMatch(x -> x == cnt);
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Others (with default implementations">
    default double dot(Doubles data) {
        return DoublesUtility.dot(this, data);
    }

    default double jdot(Doubles data, int pos) {
        return DoublesUtility.jdot(this, data, pos);
    }

    default double distance(Doubles data) {
        return DoublesUtility.distance(this, data);
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Static methods">
    //</editor-fold>
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

    public static boolean equals(double a, double b, double epsilon) {
        return a > b ? (a - epsilon <= b) : (b - epsilon <= a);
    }

    public static String toString(Doubles rd) {
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

    public static String toString(Doubles rd, String fmt) {
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

}

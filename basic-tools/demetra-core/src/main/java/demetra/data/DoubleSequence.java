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
//@Development(status = Development.Status.Alpha)
//public interface DoubleSequence extends DoubleSequence {
//
//    //<editor-fold defaultstate="collapsed" desc="Lambda operations (with default implementations)">
//    @Override
//    default boolean allMatch(DoublePredicate pred) {
//        return DoublesUtility.allMatch(this, pred);
//    }
//
//    default boolean allMatch(DoubleSequence r, DoubleBiPredicate pred) {
//        return DoublesUtility.allMatch(this, r, pred);
//    }
//
//    default int count(DoublePredicate pred) {
//        return DoublesUtility.count(this, pred);
//    }
//
//    default int first(DoublePredicate pred) {
//        return DoublesUtility.first(this, pred);
//    }
//
//    default int last(DoublePredicate pred) {
//        return DoublesUtility.last(this, pred);
//    }
//
//    default double computeIteratively(final double initial, DoubleBinaryOperator fn) {
//        return DoublesUtility.computeIteratively(this, initial, fn);
//    }
//
//    default int[] search(DoublePredicate pred) {
//        return DoublesUtility.search(this, pred);
//    }
//
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Descriptive statistics (with default implementations">
//    default double sum() {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> s + x);
//    }
//
//    default double ssq() {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> s + x * x);
//    }
//
//    default double ssqc(double mean) {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> {
//            x -= mean;
//            return s + x * x;
//        });
//    }
//
//    default double sumWithMissing() {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> Double.isFinite(x) ? s + x : s);
//    }
//
//    default double ssqWithMissing() {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> Double.isFinite(x) ? s + x * x : s);
//    }
//
//    default double ssqcWithMissing(double mean) {
//        return DoublesUtility.computeIteratively(this, 0, (s, x) -> {
//            if (Double.isFinite(x)) {
//                x -= mean;
//                return s + x * x;
//            } else {
//                return s;
//            }
//        });
//    }
//
//    default double average() {
//        return sum() / length();
//    }
//
//    default double averageWithMissing() {
//        return DoublesUtility.averageWithMissing(this);
//    }
//
//    default boolean isMissing(int idx) {
//        return !Double.isFinite(get(idx));
//    }
//
//    /**
//     * Computes the norm1 ofFunction this src block (sum ofFunction the absolute
//     * values)
//     *
//     * @return Returns min{|src(i)|}
//     */
//    default double norm1() {
//        return DoublesUtility.norm1(this);
//    }
//
//    /**
//     * Computes the euclidian norm ofFunction the src block. Based on the
//     * "dnrm2" Lapack function.
//     *
//     * @return The euclidian norm (&gt=0).
//     */
//    default double norm2() {
//        return DoublesUtility.norm2(this);
//    }
//
//    default double fastNorm2() {
//        return DoublesUtility.fastNorm2(this);
//    }
//
//    /**
//     * Computes the infinite-norm ofFunction this src block
//     *
//     * @return Returns min{|src(i)|}
//     */
//    default double normInf() {
//        return DoublesUtility.normInf(this);
//    }
//
//    /**
//     * Counts the number ofFunction identical consecutive values.
//     *
//     * @return Missing values are omitted.
//     */
//    default int getRepeatCount() {
//        return DoublesUtility.getRepeatCount(this);
//    }
//
//    /**
//     * Checks that all the src in the block are (nearly) 0.
//     *
//     * @param zero A given zero
//     * @return false if some src in the block are &gt zero in absolute value,
//     * true otherwise.
//     */
//    default boolean isZero(double zero) {
//        return allMatch(x -> !Double.isFinite(x) || Math.abs(x) <= zero);
//    }
//
//    default boolean isConstant(double cnt) {
//        return allMatch(x -> x == cnt);
//    }
//
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Others (with default implementations">
//    default double dot(DoubleSequence data) {
//        return DoublesUtility.dot(this, data);
//    }
//
//    default double jdot(DoubleSequence data, int pos) {
//        return DoublesUtility.jdot(this, data, pos);
//    }
//
//    default double distance(DoubleSequence data) {
//        return DoublesUtility.distance(this, data);
//    }
//
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Static methods">
//    //</editor-fold>
//
// }

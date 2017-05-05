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

import demetra.utilities.IntList;
import demetra.utilities.functions.DoubleBiPredicate;
import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DoublesUtility {
    
    boolean allMatch(Doubles d, DoublePredicate pred) {
        int n = d.length();
        CellReader cell = d.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(cell.next())) {
                return false;
            }
        }
        return true;
    }

    boolean allMatch(final Doubles d, final Doubles r, DoubleBiPredicate pred) {
        int n = d.length();
        CellReader cell = d.reader();
        CellReader rcell = r.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(cell.next(), rcell.next())) {
                return false;
            }
        }
        return true;
    }

    int count(final Doubles d, final DoublePredicate pred) {
        int n = d.length();
        int m = 0;
        CellReader cell = d.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(cell.next())) {
                m++;
            }
        }
        return m;
    }

    int first(final Doubles d, final DoublePredicate pred) {
        int n = d.length();
        CellReader cell = d.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(cell.next())) {
                return i;
            }
        }
        return n;
    }

    int last(final Doubles d, final DoublePredicate pred) {
        int n = d.length();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(d.get(i))) {
                return i;
            }
        }
        return -1;
    }

    double computeIteratively(final Doubles d, final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        int n = d.length();
        CellReader cell = d.reader();
        for (int i = 0; i < n; ++i) {
            cur = fn.applyAsDouble(cur, cell.next());
        }
        return cur;
    }

    int[] search(final Doubles d, final DoublePredicate pred) {
        IntList list = new IntList();
        int n = d.length();
        CellReader cell = d.reader();
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.next())) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Descriptive statistics (with default implementations">
    
    double sumWithMissing(final Doubles d) {
        int n = d.length();
        double s = 0;
        CellReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next();
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    double ssqWithMissing(final Doubles d) {
        int n = d.length();
        double s = 0;
        CellReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next();
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    double ssqcWithMissing(final Doubles d, final double mean) {
        int n = d.length();
        double s = 0;
        CellReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next() - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    double averageWithMissing(final Doubles d) {
        int n = d.length();
        int m = 0;
        double s = 0;
        CellReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next();
            if (Double.isFinite(cur)) {
                s += cur;
            } else {
                m++;
            }
        }
        return s / (n - m);
    }
    
    double norm1(final Doubles d) {
        int n = d.length();
        double nrm = 0;
        CellReader cur = d.reader();
        for (int i = 0; i < n; ++i) {
            nrm += Math.abs(cur.next());
        }
        return nrm;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    double norm2(final Doubles d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                double scale = 0;
                double ssq = 1;
                CellReader cell = d.reader();
                for (int i = 0; i < n; ++i) {
                    double cur = cell.next();
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

    double fastNorm2(final Doubles d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                CellReader cell = d.reader();
                double ssq = 0;
                for (int i = 0; i < n; ++i) {
                    double cur = cell.next();
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
    double normInf(final Doubles d) {
        int n = d.length();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(d.get(0));
            CellReader cell = d.reader();
            for (int i = 1; i < n; ++i) {
                double tmp = Math.abs(cell.next());
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
    int getRepeatCount(final Doubles d) {
        int i = 0;
        int n = d.length();
        CellReader cell = d.reader();
        double prev=0;
        while (i++ < n ) {
            prev=cell.next();
            if (Double.isFinite(prev))
                break;
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        for (; i < n; ++i) {
            double cur = cell.next();
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

    double dot(final Doubles d, Doubles data) {
        int n = d.length();
        double s = 0;
        CellReader cur = d.reader();
        CellReader xcur = data.reader();
        for (int i = 0; i < n; i++) {
            s += cur.next() * xcur.next();
        }
        return s;
    }

    double jdot(final Doubles doubles, Doubles data, int pos) {
        int n = doubles.length();
        double s = 0;
        CellReader cur = doubles.reader();
        CellReader xcur = data.reader();
        for (int i = 0; i < pos; i++) {
            s += cur.next() * xcur.next();
        }
        for (int i = pos; i < n; i++) {
            s -= cur.next() * xcur.next();
        }
        return s;
    }

    double distance(final Doubles doubles, Doubles data) {
        double scale = 0;
        double ssq = 1;
        CellReader cur = doubles.reader();
        CellReader xcur = data.reader();
        int n = doubles.length();
        for (int i = 0; i < n; ++i) {
            double x = cur.next(), y = xcur.next();
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
}

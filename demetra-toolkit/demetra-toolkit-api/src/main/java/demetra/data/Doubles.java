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

import demetra.data.DoubleSeqCursor;
import demetra.util.IntList;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Deprecated
public class Doubles {

    public int[] search(final DoubleSeq d, final DoublePredicate pred) {
        IntList list = new IntList();
        int n = d.length();
        DoubleSeqCursor cell = d.cursor();
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.getAndNext())) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    public int search(final DoubleSeq d, final DoublePredicate pred, final int[] first) {
        int n = d.length();
        DoubleSeqCursor cell = d.cursor();
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
    public double sum(final DoubleSeq d) {
        return d.reduce(0, (s, x) -> s + x);
    }

    public double average(final DoubleSeq d) {
        return d.reduce(0, (s, x) -> s + x) / d.length();
    }

    public double ssq(final DoubleSeq d) {
        return d.reduce(0, (s, x) -> s + x * x);
    }

    public double ssqc(final DoubleSeq d, double mean) {
        return d.reduce(0, (s, x) -> {
            x -= mean;
            return s + x * x;
        });
    }

    public double sumWithMissing(final DoubleSeq d) {
        int n = d.length();
        double s = 0;
        DoubleSeqCursor cell = d.cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    public double ssqWithMissing(final DoubleSeq d) {
        int n = d.length();
        double s = 0;
        DoubleSeqCursor cell = d.cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext();
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    public double ssqcWithMissing(final DoubleSeq d, final double mean) {
        int n = d.length();
        double s = 0;
        DoubleSeqCursor cell = d.cursor();
        for (int i = 0; i < n; i++) {
            double cur = cell.getAndNext() - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    public double averageWithMissing(final DoubleSeq d) {
        int n = d.length();
        int m = 0;
        double s = 0;
        DoubleSeqCursor cell = d.cursor();
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

    public double norm1(final DoubleSeq d) {
        int n = d.length();
        double nrm = 0;
        DoubleSeqCursor cur = d.cursor();
        for (int i = 0; i < n; ++i) {
            nrm += Math.abs(cur.getAndNext());
        }
        return nrm;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @param d
     * @return The euclidian norm (&gt=0).
     */
    public double norm2(final DoubleSeq d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                double scale = 0;
                double ssq = 1;
                DoubleSeqCursor cell = d.cursor();
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

    public double fastNorm2(final DoubleSeq d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                DoubleSeqCursor cell = d.cursor();
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
    public double normInf(final DoubleSeq d) {
        int n = d.length();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(d.get(0));
            DoubleSeqCursor cell = d.cursor();
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
    public int getRepeatCount(final DoubleSeq d) {
        int i = 0;
        int n = d.length();
        DoubleSeqCursor cell = d.cursor();
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

    public double dot(final DoubleSeq d, DoubleSeq data) {
        int n = d.length();
        double s = 0;
        DoubleSeqCursor cur = d.cursor();
        DoubleSeqCursor xcur = data.cursor();
        for (int i = 0; i < n; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    public double jdot(final DoubleSeq doubles, DoubleSeq data, int pos) {
        int n = doubles.length();
        double s = 0;
        DoubleSeqCursor cur = doubles.cursor();
        DoubleSeqCursor xcur = data.cursor();
        for (int i = 0; i < pos; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        for (int i = pos; i < n; i++) {
            s -= cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    public double distance(final DoubleSeq doubles, DoubleSeq data) {
        double scale = 0;
        double ssq = 1;
        DoubleSeqCursor cur = doubles.cursor();
        DoubleSeqCursor xcur = data.cursor();
        int n = doubles.length();
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

    public DoubleSeq select(DoubleSeq data, DoublePredicate pred) {
        double[] x = data.toArray();
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

    public static DoubleSeq removeMean(DoubleSeq x) {
        double[] y = x.toArray();
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

    public DoubleSeq fn(DoubleSeq s, DoubleUnaryOperator fn) {
        double[] data = s.toArray();
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(data[i]);
        }
        return DoubleSeq.of(data);
    }

    public DoubleSeq fastFn(DoubleSeq s, DoubleUnaryOperator fn) {
        return DoubleSeq.onMapping(s.length(), i -> fn.applyAsDouble(s.get(i)));
    }

    public DoubleSeq fn(DoubleSeq s, int lag, DoubleBinaryOperator fn) {
        int n = s.length() - lag;
        if (n <= 0) {
            return null;
        }
        double[] nvalues = new double[n];
        for (int j = 0; j < lag; ++j) {
            double prev = s.get(j);
            for (int i = j; i < n; i += lag) {
                double next = s.get(i + lag);
                nvalues[i] = fn.applyAsDouble(prev, next);
                prev = next;
            }
        }
        return DoubleSeq.of(nvalues);
    }

    public DoubleSeq extend(DoubleSeq s, @Nonnegative int nbeg, @Nonnegative int nend) {
        int n = s.length() + nbeg + nend;
        double[] nvalues = new double[n];
        for (int i = 0; i < nbeg; ++i) {
            nvalues[i] = Double.NaN;
        }
        s.copyTo(nvalues, nbeg);
        for (int i = n - nend; i < n; ++i) {
            nvalues[i] = Double.NaN;
        }
        return DoubleSeq.of(nvalues);
    }

    public DoubleSeq delta(DoubleSeq s, int lag) {
        return fn(s, lag, (x, y) -> y - x);
    }

    public DoubleSeq delta(DoubleSeq s, int lag, int pow) {
        DoubleSeq ns = s;
        for (int i = 0; i < pow; ++i) {
            ns = fn(ns, lag, (x, y) -> y - x);
        }
        return ns;
    }

    public DoubleSeq op(DoubleSeq a, DoubleSeq b, DoubleBinaryOperator op) {
        double[] data = a.toArray();
        DoubleSeqCursor reader = b.cursor();
        for (int i = 0; i < data.length; ++i) {
            data[i] = op.applyAsDouble(data[i], reader.getAndNext());
        }
        return DoubleSeq.of(data);
    }

    public DoubleSeq fastOp(DoubleSeq a, DoubleSeq b, DoubleBinaryOperator op) {
        int n = a.length();
        return DoubleSeq.onMapping(n, i -> a.get(i) + b.get(i));
    }

    public DoubleSeq commit(DoubleSeq s) {
        return DoubleSeq.of(s.toArray());
    }
}

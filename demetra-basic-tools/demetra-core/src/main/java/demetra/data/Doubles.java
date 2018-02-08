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
import java.util.function.DoublePredicate;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Doubles {

    public int[] search(final DoubleSequence d, final DoublePredicate pred) {
        IntList list = new IntList();
        int n = d.length();
        DoubleReader cell = d.reader();
        for (int j = 0; j < n; ++j) {
            if (pred.test(cell.next())) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Descriptive statistics (with default implementations">
    public double sum(final DoubleSequence d) {
        return d.reduce(0, (s, x) -> s + x);
    }

    public double average(final DoubleSequence d) {
        return d.reduce(0, (s, x) -> s + x) / d.length();
    }

    public double ssq(final DoubleSequence d) {
        return d.reduce(0, (s, x) -> s + x * x);
    }

    public double ssqc(final DoubleSequence d, double mean) {
        return d.reduce(0, (s, x) -> {
            x -= mean;
            return s + x * x;
        });
    }

    public double sumWithMissing(final DoubleSequence d) {
        int n = d.length();
        double s = 0;
        DoubleReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next();
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    public double ssqWithMissing(final DoubleSequence d) {
        int n = d.length();
        double s = 0;
        DoubleReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next();
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    public double ssqcWithMissing(final DoubleSequence d, final double mean) {
        int n = d.length();
        double s = 0;
        DoubleReader cell = d.reader();
        for (int i = 0; i < n; i++) {
            double cur = cell.next() - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    public double averageWithMissing(final DoubleSequence d) {
        int n = d.length();
        int m = 0;
        double s = 0;
        DoubleReader cell = d.reader();
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

    public double norm1(final DoubleSequence d) {
        int n = d.length();
        double nrm = 0;
        DoubleReader cur = d.reader();
        for (int i = 0; i < n; ++i) {
            nrm += Math.abs(cur.next());
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
    public double norm2(final DoubleSequence d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                double scale = 0;
                double ssq = 1;
                DoubleReader cell = d.reader();
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

    public double fastNorm2(final DoubleSequence d) {
        int n = d.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(d.get(0));
            default:
                DoubleReader cell = d.reader();
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
    public double normInf(final DoubleSequence d) {
        int n = d.length();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(d.get(0));
            DoubleReader cell = d.reader();
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
    public int getRepeatCount(final DoubleSequence d) {
        int i = 0;
        int n = d.length();
        DoubleReader cell = d.reader();
        double prev = 0;
        while (i++ < n) {
            prev = cell.next();
            if (Double.isFinite(prev)) {
                break;
            }
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

    public double dot(final DoubleSequence d, DoubleSequence data) {
        int n = d.length();
        double s = 0;
        DoubleReader cur = d.reader();
        DoubleReader xcur = data.reader();
        for (int i = 0; i < n; i++) {
            s += cur.next() * xcur.next();
        }
        return s;
    }

    public double jdot(final DoubleSequence doubles, DoubleSequence data, int pos) {
        int n = doubles.length();
        double s = 0;
        DoubleReader cur = doubles.reader();
        DoubleReader xcur = data.reader();
        for (int i = 0; i < pos; i++) {
            s += cur.next() * xcur.next();
        }
        for (int i = pos; i < n; i++) {
            s -= cur.next() * xcur.next();
        }
        return s;
    }

    public double distance(final DoubleSequence doubles, DoubleSequence data) {
        double scale = 0;
        double ssq = 1;
        DoubleReader cur = doubles.reader();
        DoubleReader xcur = data.reader();
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
    
    public DoubleSequence select(DoubleSequence data, DoublePredicate pred){
        double[] x=data.toArray();
        int cur=0;
        for (int i=0; i<x.length; ++i){
            if (pred.test(x[i])){
                if (cur <i)
                    x[cur]=x[i];
                ++cur;
            }
        }
        if (cur == x.length)
            return DoubleSequence.ofInternal(x);
        else{
            double[] xc=new double[cur];
            System.arraycopy(x, 0, xc, 0, cur);
            return DoubleSequence.ofInternal(xc);
        }
    }
    
    
}

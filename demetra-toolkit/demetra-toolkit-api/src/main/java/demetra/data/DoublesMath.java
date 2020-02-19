/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DoublesMath {

    public double sum(DoubleSeq src) {
        return src.reduce(0, (s, x) -> s + x);
    }

    public double average(DoubleSeq src) {
        return src.reduce(0, (s, x) -> s + x) / src.length();
    }

    public double ssq(DoubleSeq src) {
        return src.reduce(0, (s, x) -> s + x * x);
    }

    public double ssqc(DoubleSeq src, double mean) {
        return src.reduce(0, (s, x) -> {
            x -= mean;
            return s + x * x;
        });
    }

    public double sumWithMissing(DoubleSeq src) {
        int n = src.length();
        double result = 0;
        DoubleSeqCursor cursor = src.cursor();
        for (int i = 0; i < n; i++) {
            double value = cursor.getAndNext();
            if (Double.isFinite(value)) {
                result += value;
            }
        }
        return result;
    }

    public double ssqWithMissing(DoubleSeq src) {
        int n = src.length();
        double result = 0;
        DoubleSeqCursor cursor = src.cursor();
        for (int i = 0; i < n; i++) {
            double value = cursor.getAndNext();
            if (Double.isFinite(value)) {
                result += value * value;
            }
        }
        return result;
    }

    public double ssqcWithMissing(DoubleSeq src, final double mean) {
        int n = src.length();
        double result = 0;
        DoubleSeqCursor cursor = src.cursor();
        for (int i = 0; i < n; i++) {
            double value = cursor.getAndNext() - mean;
            if (Double.isFinite(value)) {
                result += value * value;
            }
        }
        return result;
    }

    public double averageWithMissing(DoubleSeq src) {
        int n = src.length();
        int m = 0;
        double s = 0;
        DoubleSeqCursor cursor = src.cursor();
        for (int i = 0; i < n; i++) {
            double value = cursor.getAndNext();
            if (Double.isFinite(value)) {
                s += value;
            } else {
                m++;
            }
        }
        return s / (n - m);
    }

    public double norm1(DoubleSeq src) {
        int n = src.length();
        double result = 0;
        DoubleSeqCursor cursor = src.cursor();
        for (int i = 0; i < n; ++i) {
            result += Math.abs(cursor.getAndNext());
        }
        return result;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function. Should be used to avoid possible overflow. Otherwise, consider
     * fastNorm2, which is sigificantly faster.
     *
     * @param src
     * @return The euclidian norm (&gt=0).
     */
    public double norm2(DoubleSeq src) {
        int n = src.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(src.get(0));
            default:
                double scale = 0;
                double ssq = 1;
                DoubleSeqCursor cursor = src.cursor();
                for (int i = 0; i < n; ++i) {
                    double value = cursor.getAndNext();
                    if (value != 0) {
                        double absxi = Math.abs(value);
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
     * Computes the euclidian norm of the src.
     *
     * @param src The data
     * @return The euclidian norm (&gt=0).
     */
    public double fastNorm2(DoubleSeq src) {
        int n = src.length();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(src.get(0));
            default:
                DoubleSeqCursor cursor = src.cursor();
                double ssq = 0;
                for (int i = 0; i < n; ++i) {
                    double value = cursor.getAndNext();
                    if (value != 0) {
                        ssq += value * value;
                    }
                }
                return Math.sqrt(ssq);
        }
    }

    /**
     * Computes the infinite-norm of this src
     *
     * @param src The source
     * @return Returns min{|src(i)|}
     */
    public double normInf(DoubleSeq src) {
        int n = src.length();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(src.get(0));
            DoubleSeqCursor cursor = src.cursor();
            for (int i = 1; i < n; ++i) {
                double value = Math.abs(cursor.getAndNext());
                if (value > nrm) {
                    nrm = value;
                }
            }
            return nrm;
        }
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @param src The source
     * @return Missing values are omitted.
     */
    public int getRepeatCount(DoubleSeq src) {
        int i = 0;
        int n = src.length();
        DoubleSeqCursor cursor = src.cursor();
        double prev = 0;
        while (i++ < n) {
            prev = cursor.getAndNext();
            if (Double.isFinite(prev)) {
                break;
            }
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        for (; i < n; ++i) {
            double value = cursor.getAndNext();
            if (Double.isFinite(value)) {
                if (value == prev) {
                    ++c;
                } else {
                    prev = value;
                }
            }
        }
        return c;
    }

    public double dot(DoubleSeq a, DoubleSeq b) {
        int n = a.length();
        double s = 0;
        DoubleSeqCursor cur = a.cursor();
        DoubleSeqCursor xcur = b.cursor();
        for (int i = 0; i < n; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    public double jdot(DoubleSeq a, DoubleSeq b, int pos) {
        int n = a.length();
        double s = 0;
        DoubleSeqCursor cur = a.cursor();
        DoubleSeqCursor xcur = b.cursor();
        for (int i = 0; i < pos; i++) {
            s += cur.getAndNext() * xcur.getAndNext();
        }
        for (int i = pos; i < n; i++) {
            s -= cur.getAndNext() * xcur.getAndNext();
        }
        return s;
    }

    public double distance(DoubleSeq a, DoubleSeq b) {
        double scale = 0;
        double ssq = 1;
        DoubleSeqCursor cur = a.cursor();
        DoubleSeqCursor xcur = b.cursor();
        int n = a.length();
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

    public DoubleSeq removeMean(DoubleSeq src) {
        double[] safeArray = src.toArray();
        double s = 0;
        for (int i = 0; i < safeArray.length; ++i) {
            s += safeArray[i];
        }
        s /= safeArray.length;
        for (int i = 0; i < safeArray.length; ++i) {
            safeArray[i] -= s;
        }
        return Doubles.ofInternal(safeArray);
    }

    public DoubleSeq delta(DoubleSeq src, int lag) {
        return src.fn(lag, (x, y) -> y - x);
    }

    public DoubleSeq delta(DoubleSeq src, int lag, int pow) {
        DoubleSeq result = src;
        for (int i = 0; i < pow; ++i) {
            result = result.fn(lag, (x, y) -> y - x);
        }
        return result;
    }

    public DoubleSeq log(DoubleSeq src) {
        return src.fn(Math::log);
    }

    public DoubleSeq exp(DoubleSeq src) {
        return src.fn(Math::exp);
    }

    public DoubleSeq add(DoubleSeq a, DoubleSeq... b) {
        int start = -1;
        if (b != null) {
            for (int i = 0; i < b.length; ++i) {
                if (b[i] != null) {
                    start = i;
                    break;
                }
            }
        }
        if (start == -1) {
            return a;
        }
        double[] tot;
        if (a != null) {
            tot = a.toArray();
        } else if (start == b.length - 1) {
            return b[start];
        } else {
            tot = b[start++].toArray();
        }

        for (int i = start; i < b.length; ++i) {
            if (tot.length != b[i].length()) {
                throw new IllegalArgumentException("wrong dimensions");
            }
            DoubleSeqCursor cursor = b[i].cursor();
            for (int j = 0; j < tot.length; ++j) {
                tot[j] += cursor.getAndNext();
            }
        }
        return Doubles.ofInternal(tot);
    }

    public DoubleSeq subtract(DoubleSeq a, DoubleSeq b) {
        if (a == null) {
            return b.fn(x -> -x);
        } else if (b == null) {
            return a;
        } else {
            if (a.length() != b.length()) {
                throw new IllegalArgumentException("wrong dimensions");
            }
            return a.fn(b, (x, y) -> x - y);
        }
    }

    public DoubleSeq multiply(DoubleSeq a, DoubleSeq b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            if (a.length() != b.length()) {
                throw new IllegalArgumentException("wrong dimensions");
            }
            return a.fn(b, (x, y) -> x * y);
        }
    }

    public DoubleSeq divide(DoubleSeq a, DoubleSeq b) {
        if (a == null) {
            return b.fn(x -> 1 / x);
        } else if (b == null) {
            return a;
        } else {
            if (a.length() != b.length()) {
                throw new IllegalArgumentException("wrong dimensions");
            }
            return a.fn(b, (x, y) -> x / y);
        }
    }
}

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
package demetra.stats;

import demetra.design.Development;
import demetra.data.Doubles;
import demetra.design.Immutable;
import demetra.maths.Constants;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class DescriptiveStatistics {

    /**
     * Computes the auto-covariance
     *
     * @param k Lag of the auto-variance
     * @param data Data on which the auto-covariance is computed
     * @return
     */
    public static double[] ac(int k, double[] data) {
        double[] c = new double[k];
        double var = cov(0, data);
        for (int i = 0; i < k; ++i) {
            c[i] = cov(i + 1, data) / var;
        }
        return c;
    }

    /**
     * Computes the covariance between two arrays of doubles, which are supposed
     * to have zero means; the arrays might contain missing values (Double.NaN);
     * those values are omitted in the computation the covariance (and the
     * number of observations are adjusted).
     *
     * @param x The first array
     * @param y The second array
     * @param t The delay between the two arrays
     * @return The covariance; cov = sum((x(i)*y(i+t)/(n-t))
     */
    public static double cov(double[] x, double[] y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return cov(y, x, -t);
        }
        double v = 0;
        int n = x.length - t;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i];
            double ycur = y[i + t];
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += xcur * ycur;
            } else {
                ++nm;
            }
        }
        int m = x.length - nm;
        if (m == 0) {
            return 0;
        }
        return v / m;
        //return v / x.length;
    }

    // compute the covariance of (x (from sx to sx+n), y(from sy to sy+n)
    /**
     *
     * @param x
     * @param sx
     * @param y
     * @param sy
     * @param n
     * @return
     */
    public static double cov(double[] x, int sx, double[] y, int sy, int n) {
        double v = 0;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i + sx];
            double ycur = y[i + sy];
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += xcur * ycur;
            } else {
                ++nm;
            }
        }
        n -= nm;
        if (n == 0) {
            return 0;
        }
        return v / n;
    }

    /**
     *
     * @param k
     * @param data
     * @return
     */
    public static double cov(int k, double[] data) {
        return cov(data, data, k);
    }

    public static final double DELTA = 3.834e-20;

    public static boolean isSmall(double val) {
        Constants.getEpsilon();
        return Math.abs(val) < DELTA;
    }

    /**
     *
     * @param ac
     * @return
     */
    public static double[] pac(double[] ac) {
        double[] pc = new double[ac.length];
        return pac(ac, pc);
    }

    /**
     *
     * @param ac
     * @param coeff
     * @return
     */
    public static double[] pac(double[] ac, double[] coeff) {
        int kmax = ac.length;
        double[] pac = new double[kmax];
        double[] tmp = new double[kmax];

        pac[0] = coeff[0] = ac[0]; // K = 1
        for (int K = 2; K <= kmax; ++K) {
            double n = 0, d = 0;
            for (int k = 1; k <= K - 1; ++k) {
                double x = coeff[k - 1];
                n += ac[K - k - 1] * x;
                d += ac[k - 1] * x;
            }
            pac[K - 1] = coeff[K - 1] = (ac[K - 1] - n) / (1 - d);

            for (int i = 0; i < K; ++i) {
                tmp[i] = coeff[i];
            }
            for (int j = 1; j <= K - 1; ++j) {
                coeff[j - 1] = tmp[j - 1] - tmp[K - 1] * tmp[K - j - 1];
            }
        }
        return pac;
    }

    /**
     *
     * @param k
     * @param data
     * @return
     */
    public static double[] pac(int k, double[] data) {
        return pac(ac(k, data));
    }

    /**
     * Computes the variance of x
     *
     * @param x Data for which we compute the variance
     * @param sx Starting position in the array
     * @param n Number of toArray.
     * @return
     */
    public static double var(double[] x, int sx, int n) {
        double v = 0;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i + sx];
            if (Double.isFinite(xcur)) {
                v += xcur * xcur;
            } else {
                ++nm;
            }
        }
        n -= nm;
        return v / n;
    }

    private final double[] data;
    private final double sx, sxx;
    private final int nmissings;
    private double skewness, kurtosis;
    private volatile double[] obs, sortedObs;

    public static DescriptiveStatistics of(Doubles data) {
        return new DescriptiveStatistics(data.toArray());
    }

    public static DescriptiveStatistics ofInternal(double[] data) {
        return new DescriptiveStatistics(data);
    }

    private double[] obs() {
        double[] tmp = this.obs;
        if (tmp == null) {
            synchronized (this) {
                tmp = this.obs;
                if (tmp == null) {
                    if (nmissings == 0) {
                        tmp = data;
                    } else {
                        tmp = new double[data.length - nmissings];
                        for (int i = 0, j = 0; i < data.length; ++i) {
                            double x = data[i];
                            if (Double.isFinite(x)) {
                                tmp[j++] = x;
                            }
                        }
                    }
                    this.obs = tmp;
                }
            }
        }
        return obs;
    }

    private double[] sortObs() {
        double[] tmp = this.sortedObs;
        if (tmp == null) {
            double[] cobs = obs();
            synchronized (this) {
                tmp = this.sortedObs;
                if (tmp == null) {
                    tmp = cobs.clone();
                    Arrays.sort(tmp);
                    sortedObs = tmp;
                }
            }
        }
        return sortedObs;
    }

    private DescriptiveStatistics(double[] data) {
        this.data = data;
        double s_x = 0, s_xx = 0;
        int nm = 0;
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if (!Double.isFinite(v)) {
                ++nm;
            } else {
                s_x += v;
                s_xx += v * v;
            }
        }
        sx = s_x;
        sxx = s_xx;
        nmissings = nm;
        calcMoments();
    }

    /**
     *
     */
    private void calcMoments() {
        double stdev = getStdev(), avg = getAverage();
        int n = data.length;

        // skweness...
        skewness = 0.0;
        kurtosis = 0.0;
        double stdev3 = stdev * stdev * stdev;
        for (int i = 0; i < n; i++) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                double m3 = (cur - avg) * (cur - avg) * (cur - avg);
                skewness += m3;
                kurtosis += m3 * (cur - avg);
            }
        }
        n -= nmissings;
        skewness /= stdev3 * n;
        kurtosis /= stdev3 * stdev * n;
    }

    /**
     * Counts the number of elements between two doubles
     *
     * @param a Lower bound (included)
     * @param b Upper bound (excluded)
     * @return The number of observations in [a,b[
     */
    public int countBetween(double a, double b) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            int n = tmp.length;
            if (n == 0) {
                return 0;
            }
            int i0 = 0;
            while (i0 < n && tmp[i0] < a) {
                ++i0;
            }
            int i1 = i0;
            while (i1 < n && tmp[i1] < b) {
                ++i1;
            }
            return i1 - i0;
        } else {
            int n = data.length;
            int m = 0;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v >= a && v < b) {
                    m++;
                }
            }
            return m;
        }
    }

    // / <Summary>
    // / A Read Only property representing the average of the toArray in the
    // dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getAverage() {
        return sx / (data.length - nmissings);
    }

    /**
     *
     * @return
     */
    public int getDataCount() {
        return data.length;
    }

    /**
     * @return The kurtosis of the white noise toArray
     */
    public double getKurtosis() {
        return kurtosis;
    }

    // / <Summary>
    // / A Read Only property representing the maximum value in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMax() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[tmp.length - 1];
        } else {
            int n = data.length;
            double sent = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v > sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    // / <Summary>
    // / A Read Only property representing the median of the toArray in the
    // dataset.
    // / Defined as toArray[n/2+1] for count is odd and (toArray[n/2]+toArray[n/2+1])/2
    // fo even count
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMedian() {
        double[] tmp = sortObs();
        if (tmp.length == 0) {
            return Double.NaN;
        }
        boolean even = tmp.length % 2 == 0;
        if (even) {
            return ((tmp[tmp.length / 2 - 1] + tmp[tmp.length / 2]) / 2.0);
        } else {
            return tmp[tmp.length / 2];
        }
    }

    // / <Summary>
    // / A Read Only property representing the minimum value in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMin() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0];
        } else {
            int n = data.length;
            double sent = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v < sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    /**
     *
     * @return
     */
    public int getMissingValuesCount() {
        return nmissings;
    }

    /**
     *
     * @return
     */
    public int getObservationsCount() {
        return data.length - getMissingValuesCount();
    }

    /**
     *
     * @return
     */
    public double getSkewness() {
        return skewness;
    }

    /**
     *
     * @return
     */
    public double getStdev() {
        return Math.sqrt(getVar());
    }

    /**
     *
     * @param df
     * @return
     */
    public double getStdevDF(int df) {
        return Math.sqrt(getVarDF(df));
    }

    // / <Summary>
    // / A Read Only property representing the sum of the values in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getSum() {
        return sx;
    }

    // / <Summary>
    // / A Read Only property representing the sum of the squared values in the
    // dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getSumSquare() {
        return sxx;
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     *
     * @return
     */
    public double getVar() {
        return getVarDF(0);
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     *
     * @param df
     * @return
     */
    public double getVarDF(int df) {
        int n = data.length - nmissings;
        // Could lead to numerical instabilities
        //return (sxx - sx * sx / n) / (n - df);
        double sxxc = 0;
        double m = sx / n;
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if (Double.isFinite(v)) {
                double e = v - m;
                sxxc += e * e;
            }
        }
        return sxxc / (n - df);
    }

    /**
     * Root mean square error
     *
     * @return
     */
    public double getRmse() {
        int n = data.length - nmissings;
        return Math.sqrt(sxx / n);
    }

    // / <Summary>
    // / A Read Only property that returns true if at least one element in the
    // set is equal to zero
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean hasZeroes() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            if (tmp[0] > 0 || tmp[tmp.length - 1] < 0) {
                return false;
            }
        }
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if (Double.isFinite(v) && v == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public double[] internalStorage() {
        return data;
    }

    // / <Summary>
    // / A Read Only property to see if the toArray in the dataset have a constant
    // value.
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean isConstant() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            if (tmp.length == 0) {
                return true;
            }
            return tmp[0] == tmp[tmp.length - 1];
        } else {
            double sent = Double.NaN;
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v)) {
                    if (Double.isNaN(sent)) {
                        sent = v;
                    } else if (v != sent) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // larger than some value
    // / </Summary>
    /**
     *
     * @param val
     * @return
     */
    public boolean isGreater(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] > val;
        } else {
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v <= val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // larger than or equal to some value
    // / </Summary>
    /**
     *
     * @param val
     * @return
     */
    public boolean isGreaterOrEqual(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] > val;
        } else {
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v < val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // strictly negative
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean isNegative() {
        return isSmaller(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // negative or zero
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean isNegativeOrNull() {
        return isSmallerOrEqual(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // strictly positive
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean isPositive() {

        return isGreater(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // positive or zero
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean isPositiveOrNull() {
        return isGreaterOrEqual(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // smaller than some value
    // / </Summary>
    /**
     *
     * @param val
     * @return
     */
    public boolean isSmaller(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] > val;
        } else {
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v >= val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // smaller than or equal to some value
    // / </Summary>
    /**
     *
     * @param val
     * @return
     */
    public boolean isSmallerOrEqual(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] > val;
        } else {
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v > val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all toArray in the set are
    // larger than some value
    // / </Summary>
    /**
     *
     * @param eps
     * @return
     */
    public boolean isZero(double eps) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] >= -eps && tmp[tmp.length - 1] <= eps;
        } else {
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v) && Math.abs(v) > eps) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     *
     * @param partitions
     * @return
     */
    public double[] quantiles(int partitions) {
        double[] tmp = sortObs();
        int n = tmp.length;
        if (n == 0) {
            return null;
        }

        if (partitions < 2 || n / partitions < 1) {
            return null;
        }

        int np = partitions - 1;
        double[] res = new double[np];

        int ns = ((n - 1) / partitions);

        if (ns * partitions == n - 1) // pas d'arrondis
        {
            for (int i = 0; i < np; i++) {
                res[i] = tmp[(i + 1) * ns];
            }
        } else // quelques complications. On fait la moyenne pondérée entre les
        // deux valeurs les plus proches du point de partition
        {
            double ds = ((double) (n - 1)) / partitions;
            for (int i = 0; i < np; i++) {
                double dindex = (i + 1) * ds;
                int lo = (int) dindex;
                double dlo = dindex - lo;
                res[i] = tmp[lo] * (1 - dlo) + tmp[lo + 1] * dlo;
            }
        }

        return res;
    }

    /**
     *
     * @return
     */
    public Doubles sortedObservations() {
        return Doubles.ofInternal(sortObs());
    }

    /**
     *
     * @return
     */
    public Doubles observations() {
        return Doubles.ofInternal(obs());
    }
}

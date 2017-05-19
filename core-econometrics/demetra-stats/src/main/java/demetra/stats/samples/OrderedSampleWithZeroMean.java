/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class OrderedSampleWithZeroMean implements OrderedSample {

    private final Doubles data;
    private final boolean checkMissing;

    public OrderedSampleWithZeroMean(Doubles data, boolean missing) {
        this.data = data;
        this.checkMissing=missing;
    }

    @Override
    public double mean() {
        return 0;
    }

    @Override
    public double autoCovariance(int lag) {
        if (checkMissing)
            return cov(data, data, lag);
        else
            return covNoMissing(data, data, lag);
    }

    /**
     *
     * @param k
     * @param data
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
    public static double cov(Doubles x, Doubles y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return cov(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x.get(i);
            double ycur = y.get(i + t);
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += xcur * ycur;
            } else {
                ++nm;
            }
        }
        int m = x.length() - nm;
        if (m == 0) {
            return 0;
        }
        return v / m;
    }

    public static double covNoMissing(Doubles x, Doubles y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covNoMissing(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        for (int i = 0; i < n; ++i) {
            v += x.get(i) * y.get(i + t);
        }
        return v / x.length();
    }
}

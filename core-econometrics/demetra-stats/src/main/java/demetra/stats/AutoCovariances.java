/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AutoCovariances {

    public static final double SMALL = 1e-38;

    public static double[] autoCovariancesWithZeroMean(DoubleSequence data, int maxLag) {
        return autoCovariances(data, 0, maxLag);
    }

    public static double[] autoCovariances(DoubleSequence data, double mean, int maxLag) {
        double[] cov = new double[maxLag + 1];
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            cov[0] = variance(data, mean);
            for (int i = 1; i <= maxLag; ++i) {
                cov[i] = cov(data, mean, i);
            }
        } else {
            cov[0] = varianceNoMissing(data, mean);
            for (int i = 1; i <= maxLag; ++i) {
                cov[i] = covNoMissing(data, mean, i);
            }
        }
        return cov;
    }

    /**
     * Computes the covariance between two arrays of doubles, which are supposed
     * to have zero means; the arrays might contain missing values (Double.NaN);
     * those values are omitted in the computation of the covariance (and the
     * number of observations are adjusted).
     *
     * @param x The first array
     * @param y The second array
     * @param t The delay between the two arrays
     * @return The covariance; covariance = sum((x(i)*y(i+t)/n)
     */
    public static double covarianceWithZeroMean(DoubleSequence x, DoubleSequence y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMean(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        int nm = 0;
        DoubleReader xr = x.reader();
        DoubleReader yr = y.reader();
        yr.setPosition(t);
        for (int i = 0; i < n; ++i) {
            double xcur = xr.next();
            double ycur = yr.next();
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

    public static double covarianceWithZeroMean(DoubleSequence x, DoubleSequence y) {
        return covarianceWithZeroMean(x, y, 0);
    }

    public static double covarianceWithZeroMeanAndNoMissing(DoubleSequence x, DoubleSequence y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMeanAndNoMissing(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        DoubleReader xr = x.reader();
        DoubleReader yr = y.reader();
        yr.setPosition(t);
        for (int i = 0; i < n; ++i) {
            v += xr.next() * yr.next();
        }
        return v / x.length();
    }

    public static IntToDoubleFunction autoCorrelationFunction(DoubleSequence data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            final double var = variance(data, mean);
            return i -> var < SMALL ? 0 : cov(data, mean, i) / var;
        } else {
            final double var = varianceNoMissing(data, mean);
            return i -> var < SMALL ? 0 : covNoMissing(data, mean, i) / var;
        }
    }

    public static IntToDoubleFunction autoCovarianceFunction(DoubleSequence data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            return i -> cov(data, mean, i);
        } else {
            return i -> covNoMissing(data, mean, i);
        }
    }

    private static double cov(DoubleSequence data, double mean, int lag) {
        double v = 0;
        int n = data.length() - lag;
        int nm = 0;
        DoubleReader xr = data.reader();
        DoubleReader yr = data.reader();
        yr.setPosition(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.next();
            double ycur = yr.next();
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += (xcur - mean) * (ycur - mean);
            } else {
                ++nm;
            }
        }
        int m = data.length() - nm;
        if (m == 0) {
            return 0;
        } else {
            return v / m;
        }
    }

    public static double variance(DoubleSequence data, double mean) {
        double v = 0;
        int n = data.length();
        int nm = 0;
        DoubleReader xr = data.reader();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.next();
            if (Double.isFinite(xcur)) {
                v += (xcur - mean) * (xcur - mean);
            } else {
                ++nm;
            }
        }
        int m = data.length() - nm;
        if (m == 0) {
            return 0;
        } else {
            return v / m;
        }
    }

    private static double covNoMissing(DoubleSequence data, double mean, int lag) {
        int n = data.length() - lag;
        if (n <= 0) {
            return 0;
        }
        double v = 0;
        DoubleReader xr = data.reader();
        DoubleReader yr = data.reader();
        yr.setPosition(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.next();
            double ycur = yr.next();
            v += (xcur - mean) * (ycur - mean);
        }
        return v / data.length();
    }

    public static double varianceNoMissing(DoubleSequence data, double mean) {
        int n = data.length();
        if (n == 0) {
            return 0;
        }
        double v = 0;
        DoubleReader xr = data.reader();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.next();
            v += (xcur - mean) * (xcur - mean);
        }
        return v / n;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.CellReader;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import java.util.stream.DoubleStream;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OrderedSampleWithZeroMean implements OrderedSample {

    private final Doubles data;
    private final boolean hasMissing;

    public static OrderedSampleWithZeroMean of(Doubles data) {
        return new OrderedSampleWithZeroMean(data, !data.allMatch(x -> Double.isFinite(x)));
    }

    public OrderedSampleWithZeroMean(Doubles data, boolean hasMissing) {
        this.data = data;
        this.hasMissing = hasMissing;
    }

    @Override
    public double mean() {
        return 0;
    }

    @Override
    public double autoCovariance(int lag) {
        if (hasMissing) {
            return covariance(data, data, lag);
        } else {
            return covarianceNoMissing(data, data, lag);
        }
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
     * @return The covariance; covariance = sum((x(i)*y(i+t)/(n-t))
     */
    public static double covariance(Doubles x, Doubles y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covariance(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        int nm = 0;
        CellReader xr = x.reader(), yr = y.reader();
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

    public static double covariance(Doubles x, Doubles y) {
        return covariance(x, y, 0);
    }

    public static double covarianceNoMissing(Doubles x, Doubles y) {
        return covarianceNoMissing(x, y, 0);
    }

    public static double covarianceNoMissing(Doubles x, Doubles y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceNoMissing(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        CellReader xr = x.reader(), yr = y.reader();
        yr.setPosition(t);
        for (int i = 0; i < n; ++i) {
            v += xr.next() * yr.next();
        }
        return v / x.length();
    }

    public static double varianceNoMissing(Doubles x) {
        return x.ssq() / x.length();
    }

    public static double variance(Doubles x) {
        return x.ssqWithMissing() / x.count(y -> Double.isFinite(y));
    }

    @Override
    public DoubleSequence data() {
        return data;
    }

    @Override
    public int size() {
        if (hasMissing) {
            return data.count(x -> Double.isFinite(x));
        } else {
            return data.length();
        }
    }
}

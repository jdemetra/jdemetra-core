/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import java.util.stream.DoubleStream;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class DefaultOrderedSample implements OrderedSample {

    private final Doubles data;
    private final double mean;
    private final boolean hasMissing;

    public DefaultOrderedSample(Doubles data) {
        this.data = data;
        this.hasMissing = !data.allMatch(x -> Double.isFinite(x));
        this.mean = computeMean();
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double autoCovariance(int lag) {
        // x and y must have the same Length...
        int t = lag < 0 ? -lag : lag;
        double v = 0;
        int n = data.length() - t;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = data.get(i) - mean;
            double ycur = data.get(i + t) - mean;
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += xcur * ycur;
            } else {
                ++nm;
            }
        }
        int m = data.length() - nm;
        if (m == 0) {
            return 0;
        }
        return v / m;
    }

    private double computeMean() {
        if (hasMissing) {
            return data.average();
        } else {
            return data.averageWithMissing();
        }
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

/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats;

import demetra.data.DoubleSeqCursor;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AutoCovariances {

    public static final double SMALL = 1e-38;

    public static double[] autoCovariancesWithZeroMean(DoubleSeq data, int maxLag) {
        return autoCovariances(data, 0, maxLag);
    }

    public static double[] autoCovariances(DoubleSeq data, double mean, int maxLag) {
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
    public static double covarianceWithZeroMean(DoubleSeq x, DoubleSeq y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMean(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        int nm = 0;
        DoubleSeqCursor xr = x.cursor();
        DoubleSeqCursor yr = y.cursor();
        yr.moveTo(t);
        for (int i = 0; i < n; ++i) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
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

    public static double covarianceWithZeroMean(DoubleSeq x, DoubleSeq y) {
        return covarianceWithZeroMean(x, y, 0);
    }

    public static double covarianceWithZeroMeanAndNoMissing(DoubleSeq x, DoubleSeq y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMeanAndNoMissing(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        DoubleSeqCursor xr = x.cursor();
        DoubleSeqCursor yr = y.cursor();
        yr.moveTo(t);
        for (int i = 0; i < n; ++i) {
            v += xr.getAndNext() * yr.getAndNext();
        }
        return v / x.length();
    }

    public static IntToDoubleFunction autoCorrelationFunction(DoubleSeq data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            final double var = variance(data, mean);
            return i -> var < SMALL ? 0 : cov(data, mean, i) / var;
        } else {
            final double var = varianceNoMissing(data, mean);
            return i -> var < SMALL ? 0 : covNoMissing(data, mean, i) / var;
        }
    }

    public static IntToDoubleFunction autoCovarianceFunction(DoubleSeq data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            return i -> cov(data, mean, i);
        } else {
            return i -> covNoMissing(data, mean, i);
        }
    }

    private static double cov(DoubleSeq data, double mean, int lag) {
        double v = 0;
        int n = data.length() - lag;
        int nm = 0;
        DoubleSeqCursor xr = data.cursor();
        DoubleSeqCursor yr = data.cursor();
        yr.moveTo(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
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

    public static double variance(DoubleSeq data, double mean) {
        double v = 0;
        int n = data.length();
        int nm = 0;
        DoubleSeqCursor xr = data.cursor();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
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

    private static double covNoMissing(DoubleSeq data, double mean, int lag) {
        int n = data.length() - lag;
        if (n <= 0) {
            return 0;
        }
        double v = 0;
        DoubleSeqCursor xr = data.cursor();
        DoubleSeqCursor yr = data.cursor();
        yr.moveTo(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
            v += (xcur - mean) * (ycur - mean);
        }
        return v / data.length();
    }

    /**
     * Computes the variance of a sample from a population with known mean
     * @param data The sample
     * @param mean Mean of the population
     * @return 
     */
    public static double varianceNoMissing(DoubleSeq data, double mean) {
        int n = data.length();
        if (n == 0) {
            return 0;
        }
        double v = 0;
        DoubleSeqCursor xr = data.cursor();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            v += (xcur - mean) * (xcur - mean);
        }
        return v / n;
    }
}

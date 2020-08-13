/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.samples;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Moments {

    /**
     * Two passes computation of the mean (no missing value)
     *
     * @param x
     * @return
     */
    public double mean(DoubleSeq x) {
        int n = x.length();
        switch (n) {
            case 0:
                return Double.NaN;
            case 1:
                return x.get(0);
            default:
                double m = 0;
                DoubleSeqCursor cursor = x.cursor();
                for (int i = 0; i < n; ++i) {
                    m += cursor.getAndNext();
                }
                m /= n;
                double e = 0;
                cursor = x.cursor();
                for (int i = 0; i < n; ++i) {
                    e += cursor.getAndNext() - m;
                }
                return m + e / n;
        }
    }

    public double variance(DoubleSeq x, double mean, boolean unbiased) {

        int n = x.length();
        switch (n) {
            case 0:
                return Double.NaN;
            case 1:
                return 0;
            default:
                DoubleSeqCursor cursor = x.cursor();
                double v = 0,
                 v2 = 0;
                for (int i = 0; i < n; ++i) {
                    double d = cursor.getAndNext() - mean;
                    v += d * d;
                    v2 += d;
                }

                if (unbiased) {
                    return (v - (v2 * v2 / n)) / (n - 1);
                } else {
                    return (v - (v2 * v2 / n)) / n;
                }
        }
    }

    public double skewness(DoubleSeq x, boolean unbiased) {
        double mean = mean(x), variance = variance(x, mean, unbiased);
        return skewness(x, mean, variance, unbiased);
    }

    public double skewness(DoubleSeq x, double mean, double var, boolean unbiased) {
        int n = x.length();
        if (n < 3) {
            return Double.NaN;
        }
        DoubleSeqCursor cursor = x.cursor();
        double m3 = 0;
        for (int i = 0; i < n; ++i) {
            double d = cursor.getAndNext() - mean;
            m3 += d * d * d;
        }
        double std3 = var * Math.sqrt(var);
        if (unbiased) {
            return (n * m3) / ((n - 1) * (n - 2) * std3);
        } else {
            return m3 / (std3 * n);
        }
    }

    public double excessKurtosis(DoubleSeq x, boolean unbiased) {
        double mean = mean(x), variance = variance(x, mean, unbiased);
        return excessKurtosis(x, mean, variance, unbiased);
    }

    public double excessKurtosis(DoubleSeq x, double mean, double var, boolean unbiased) {
        int n = x.length();
        if (n < 4) {
            return Double.NaN;
        }
        DoubleSeqCursor cursor = x.cursor();
        double m4 = 0;
        for (int i = 0; i < n; ++i) {
            double d = cursor.getAndNext() - mean, d2 = d * d;
            m4 += d2 * d2;
        }
        double std4 = var * var;
        if (unbiased) {
            double dcorr = (n - 2) * (n - 3);
            return (n * (n + 1) * m4) / ((n - 1) * dcorr * std4) - 3 * (n - 1) * (n - 1) / dcorr;
        } else {
            return m4 / (std4 * n) - 3;
        }
    }
}

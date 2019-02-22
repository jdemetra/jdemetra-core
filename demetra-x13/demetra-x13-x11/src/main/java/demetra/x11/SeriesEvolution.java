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
package demetra.x11;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.Development;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeriesEvolution {

    public static double[] calcAbsMeanVariations(DoubleSequence x, int nlags, boolean mul) {
        double[] mean = new double[nlags];

        int n = x.length();
        for (int lag = 1; lag <= nlags; ++lag) {
            double sum = 0;
            for (int i = lag; i < n; ++i) {
                double x1 = x.get(i), x0 = x.get(i - lag);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                sum += Math.abs(d);
            }
            mean[lag - 1] = sum / (n - lag);
        }
        return mean;
    }

    public static double calcAbsMeanVariation(DoubleSequence x, int lag, boolean mul) {
        double sum = 0;
        int n = x.length();
        for (int i = lag; i < n; ++i) {
            double x1 = x.get(i), x0 = x.get(i - lag);
            double d = Math.abs(x1 - x0);
            if (mul) {
                d /= x0;
            }
            sum += d;
        }
        return sum / (n - lag);
    }

    public static double[] calcMeanVariations(DoubleSequence x, int nlags, boolean mul) {
        double[] mean = new double[nlags];
        int n = x.length();

        for (int lag = 1; lag <= nlags; ++lag) {
            double sum = 0;
            for (int i = lag; i < n; ++i) {
                double x1 = x.get(i), x0 = x.get(i - lag);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                sum += d;
            }
            mean[lag - 1] = sum / (n - lag);
        }
        return mean;
    }

    public static double calcMeanVariation(DoubleSequence x, int lag, boolean mul) {
        int n = x.length();

        double sum = 0;
        for (int i = lag; i < n; ++i) {
            double x1 = x.get(i), x0 = x.get(i - lag);
            double d = x1 - x0;
            if (mul) {
                d /= x0;
            }
            sum += d;
        }
        return sum / (n - lag);
    }

    /**
     * average duration of run for MStatistics
     *
     * @param x
     * @param mul
     *
     * @return
     */
    public static double adr(DoubleSequence x, boolean mul) {
        if (x.length() < 2) {
            return 0;
        }

        int n = x.length() - 1;
        double[] d = new double[n];
        DoubleReader reader = x.reader();
        double s0 = reader.next();
        if (mul) {
            for (int i = 0; i < n; ++i) {
                double s1 = reader.next();
                d[i] = (s1 - s0) / s0;
                s0 = s1;
            }
        } else {
            for (int i = 0; i < n; ++i) {
                double s1 = reader.next();
                d[i] = s1 - s0;
                s0 = s1;
            }
        }
        int c = 0;
        int s = 0;
        for (int i = 0; i < n; ++i) {
            int cur = sign(d[i]);
            if (s != cur && cur != 0) {
                ++c;
                s = cur;
            }
        }
        double N = n;
        return N / c;
    }

    private static int sign(double val) {
        if (val < 0) {
            return -1;
        } else if (val > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}

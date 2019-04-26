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
package demetra.data.analysis;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import demetra.maths.Constants;
import demetra.util.IntList;

/**
 * Periodogram of a series of real-valued data The periodogram is defined at the
 * Fourier frequencies: if we have n data, the Fourier frequencies f(j) are
 * (2*pi)*j/n, j in [0,n[. p(k) is usually defined as
 * (1/n)*|sum(x(j)*e(i*k*f(j))|^2 As the x(j) are real-valued, we consider only
 * the Fourier frequencies with j in [0, n/2] We have: p(0) = (1/n)*|sum(x(j)|^2
 * = (1/n)*|sx|^2 p(j) = (2/n)*|sum(x(j)*e(i*k*f(j))|^2 if n is even, p(n/2) =
 * (1/n)*|(-1)^k*sum(x(j)|^2 This implementation will rescale the periodogram
 * with the factor n/sum(x(j)^2)=n/sx2, so that we will have: p(0) = |sx|^2/sx2
 * p(j) = 2*|sum(x(j)*e(i*k*f(j))|^2/sx2 if n is even, p(n/2) =
 * |(-1)^k*sum(x(j)|^2/sx2
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class Periodogram {

    public static Periodogram of(DoubleSeq y) {
        int T = y.length(), T1 = (1 + T) / 2, T2 = 1 + T / 2;
        double sy = 0, sy2 = 0;
        DoubleSeqCursor reader = y.cursor();
        int n = 0;
        for (int i = 0; i < T; ++i) {
            double cur = reader.getAndNext();
            if (!Double.isNaN(cur)) {
                ++n;
                sy += cur;
                sy2 += cur * cur;
            }
        }
        double[] p = new double[T2];
        double l = Constants.TWOPI / T;
        double cosl = Math.cos(l), sinl = Math.sin(l);
        double cos = 1, sin = 0; // current cos and sin...

        // the mean has been removed
        p[0] = sy * sy / sy2;
        double a = 0, b = 0;
        for (int i = 1; i < T1; ++i) {
            // compute next cos, sin
            double ctmp = cos, stmp = sin;
            sin = cosl * stmp + sinl * ctmp;
            cos = -sinl * stmp + cosl * ctmp;

            a = 0;
            b = 0;
            double c = 1, s = 0;
            reader.moveTo(0);
            for (int j = 0; j < T; ++j) {
                // compute next c, s ...
                ctmp = c;
                stmp = s;
                s = cos * stmp + sin * ctmp;
                c = -sin * stmp + cos * ctmp;
                double cur = reader.getAndNext();
                if (!Double.isNaN(cur)) {
                    a += c * cur;
                    b += s * cur;
                }
            }
            p[i] = 2 * (a * a + b * b) / sy2;
        }

        if (T1 != T2) // T even
        {
            a = 0;
            reader.moveTo(0);
            for (int i = 0; i < T; ++i) {
                double cur = reader.getAndNext();
                if (!Double.isNaN(cur)) {
                    if (i % 2 == 0) {
                        a += cur;
                    } else {
                        a -= cur;
                    }
                }
            }
            p[T2 - 1] = a * a / sy2;
        }

        return new Periodogram(p, n);
    }

    private double[] p;
    private int n;
    
    private Periodogram(double[] p, int n){
        this.p=p;
        this.n=n;
    }

    public static double[] getSeasonalFrequencies(int freq) {
        double[] dfreq = new double[freq / 2];
        for (int i = 1; i <= dfreq.length; ++i) {
            dfreq[i - 1] = Math.PI * 2 * i / freq;
        }
        return dfreq;
    }

    /**
     *
     * @param freq
     * @return
     */
    public static double[] getTradingDaysFrequencies(int freq) {
        double n = 365.25 / freq;
        double f = 2 * Math.PI / 7 * (n - 7 * Math.floor(n / 7));
        if (f > Math.PI) {
            f = 2 * Math.PI - f;
        }
        if (freq == 12) {
            return new double[]{f}; // , 2 * Math.PI * 0.432 };
        } else if (freq == 4) {
            return new double[]{f, 1.292, 1.850, 2.128};
        } else {
            return new double[]{f};
        }
    }

    /**
     *
     * @return
     */
    public double getIntervalInRadians() {
        return (2 * Math.PI) / (n - 1);
    }

    /**
     *
     * @param dMin
     * @param smoothed
     * @return
     */
    public int[] searchPeaks(double dMin, boolean smoothed) {
        IntList peaks = new IntList(p.length);
        for (int i = 0; i < p.length; ++i) {
            if (p[i] > dMin) {
                peaks.add(i);
            }
        }
        return peaks.toArray();
    }

}

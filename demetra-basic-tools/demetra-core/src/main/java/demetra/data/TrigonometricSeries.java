/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.maths.Constants;
import demetra.maths.matrices.Matrix;

/**
 * Computes cos(tw), sin(tw)
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TrigonometricSeries {

    private final double[] w;

    public static TrigonometricSeries regular(int periodicity) {
        int n = periodicity / 2;
        double[] freq = new double[n];
        double f = Constants.TWOPI / periodicity;
        for (int i = 1; i <= n; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricSeries(freq);
    }

    public static TrigonometricSeries regular(int periodicity, int[] seasfreq) {
        double[] freq = new double[seasfreq.length];
        double f = Constants.TWOPI / periodicity;
        for (int i = 0; i < seasfreq.length; ++i) {
            freq[i] = f * seasfreq[i];
        }
        return new TrigonometricSeries(freq);
    }

    /**
     * Creates trigonometric series for "non regular" series Example: For weekly
     * series, the periodicity is 365.25/7 = 52.1786 (52.1786 is the number of periods
     * for 1 year, 52.1786 = 2*pi).
     * If we are interested by monthly frequencies, we should consider the
     * frequencies k*2*pi/12 = k*pi/6. pi/6 corresponds to 52.1786/12 periods
     *
     *
     * We compute the trigonometric
     * variables for w= (k*2*pi)/52.1786, k=1,..., nfreq
     *
     * @param periodicity Annual periodicity
     * @param nfreq Number of "seasonal" frequencies of interest
     * @return
     *
     */
    public static TrigonometricSeries all(double periodicity, int nfreq) {
        double[] freq = new double[nfreq];
        double f = Constants.TWOPI / (periodicity);
        for (int i = 1; i <= nfreq; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricSeries(freq);
    }

    public static TrigonometricSeries specific(double periodicity) {
        return new TrigonometricSeries(new double[]{Constants.TWOPI / periodicity});
    }

    private TrigonometricSeries(double[] freq) {
        this.w = freq;
    }

    public Matrix matrix(int len) {
        return matrix(0, len);
    }

    public Matrix matrix(int len, int start) {
        int nlast = w.length - 1;
        int n = w.length * 2;
        boolean zero = false, pi = false;

        if (Math.abs(w[nlast] - Math.PI) < 1e-9) {
            pi = true;
            --n;
        }
        if (Math.abs(w[0]) < 1e-9) {
            zero = true;
            --n;
        }
        Matrix m = Matrix.make(len, n);
        int icur = 0, ccur = 0;
        if (zero) {
            m.column(ccur++).set(0);
            ++icur;
        }

        for (; ccur < n-1; ++icur, ccur += 2) {
            double v = w[icur];
            DataBlock c = m.column(ccur);
            DataBlock s = m.column(ccur + 1);
            for (int j = 0; j < len; ++j) {
                double wj = (start + j) * v;
                c.set(j, Math.cos(wj));
                s.set(j, Math.sin(wj));
            }
        }
        if (pi) {
            DataBlock c = m.column(ccur);
            c.extract(0, -1, 2).set(1);
            c.extract(1, -1, 2).set(-1);
        }

        return m;

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 * Computes cos(tw), sin(tw)
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TrigonometricSeries {

    /**
     * to be multiplied by pi
     */
    private final double[] w;

    public static TrigonometricSeries regular(int periodicity) {
        int n = periodicity / 2;
        double[] freq = new double[n];
        double f = 2.0 / periodicity;
        for (int i = 1; i <= n; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricSeries(freq);
    }

    public static TrigonometricSeries regular(int periodicity, int[] seasfreq) {
        double[] freq = new double[seasfreq.length];
        double f = 2.0 / periodicity;
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
        double f = 2.0 / (periodicity);
        for (int i = 1; i <= nfreq; ++i) {
            freq[i - 1] = f * i;
        }
        return new TrigonometricSeries(freq);
    }

    public TrigonometricSeries(double[] freq) {
        this.w = freq;
    }

    public Matrix matrix(int len) {
        return matrix(0, len);
    }

    public Matrix matrix(int start, int len) {
        int nlast = w.length - 1;
        int n = w.length * 2 - (w[nlast] == 1 ? 1 : 0);
        Matrix m = new Matrix(len, n);

        int nq = n / 2;
        for (int i = 0; i < nq; ++i) {
            double v = Math.PI * w[i];
            DataBlock c = m.column(i * 2);
            DataBlock s = m.column(i * 2 + 1);
            for (int j = 0; j < len; ++j) {
                double wj = (start + j) * v;
                c.set(j, Math.cos(wj));
                s.set(j, Math.sin(wj));
            }
        }
        if (n % 2 == 1) {
            DataBlock c = m.column(2 * nlast);
            double v = Math.PI * w[nlast];
             for (int j = 0; j < len; ++j) {
                double wj = (start + j) * v;
                c.set(j, Math.cos(wj));
            }
        }

        return m;

    }

}

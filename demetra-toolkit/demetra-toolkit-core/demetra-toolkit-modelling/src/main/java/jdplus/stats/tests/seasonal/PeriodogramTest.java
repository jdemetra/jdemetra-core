/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests.seasonal;

import jdplus.data.analysis.Periodogram;
import jdplus.dstats.Chi2;
import jdplus.dstats.F;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class PeriodogramTest {

    private static final double D = .01;

    private final DoubleSeq sample;
    private final int period;

    public PeriodogramTest(DoubleSeq sample, int period) {
        this.sample = sample;
        this.period = period;
    }

    public StatisticalTest buildChi2() {
        double[] seasfreqs = new double[(period - 1) / 2];
        // seas freq in radians...
        for (int i = 0; i < seasfreqs.length; ++i) {
            seasfreqs[i] = (i + 1) * 2 * Math.PI / period;
        }

        Periodogram periodogram = Periodogram.of(sample);
        double[] p = periodogram.getP();
        double xsum = 0;
        double dstep = periodogram.getIntervalInRadians(), estep = dstep * D;
        int nf = 0;
        for (int i = 0; i < seasfreqs.length; ++i) {
            double f = seasfreqs[i];
            int j = (int) (seasfreqs[i] / dstep);
            if (f - (j - 1) * dstep < estep) {
                nf += 2;
                xsum += p[j - 1];
            }
            if (f - j * dstep < estep) {
                nf += 2;
                xsum += p[j];
            }
            if ((j + 1) * dstep - f < estep) {
                nf += 2;
                xsum += p[j + 1];
            }
        }
        if (period % 2 == 0) {
            ++nf;
            xsum += p[p.length - 1];
        }
        Chi2 chi2 = new Chi2(nf);
        return new StatisticalTest(chi2, xsum, TestType.Upper, true);
    }

    private DoubleSeq expand(DoubleSeq data) {
        int n = data.length();
        if (n % period == 0) {
            return data;
        } else {
            double[] nd = new double[(1 + n / period) * period];
            data.copyTo(nd, 0);
            return DoubleSeq.of(nd);
        }
    }

    private DoubleSeq shrink(DoubleSeq data) {
        int n = data.length();
        if (n % period == 0) {
            return data;
        } else {
            int nc = n - n % period;
            double[] nd = new double[nc];
            data.extract(n - nc, nc).copyTo(nd, 0);
            return DoubleSeq.of(nd);
        }
    }

    /**
     * Computes a F test
     *
     * @return F test
     */
    public StatisticalTest buildF() {
        DoubleSeq data = shrink(sample);
        Periodogram periodogram = Periodogram.of(data);
        double[] p = periodogram.getP();
        double xsum = 0;
        int f2 = (period - 1) / 2;
        int nf = 2 * f2;
        int m = data.length() / period;
        for (int i = 1; i <= f2; ++i) {
            xsum += p[i * m];
        }
        if (period % 2 == 0) {
            ++nf;
            xsum += p[p.length - 1];
        }
        int n = data.length();
        F f = new F(nf, n - nf - 1);
        double val = (n - nf - 1) * xsum / (n - xsum - p[0]) / (nf);
        return new StatisticalTest(f, val, TestType.Upper, true);
    }

    public double computeMax() {
        double[] seasfreqs = new double[(period - 1) / 2];
        // seas freq in radians...
        for (int i = 0; i < seasfreqs.length; ++i) {
            seasfreqs[i] = (i + 1) * 2 * Math.PI / period;
        }

        Periodogram periodogram = Periodogram.of(sample);
        double[] p = periodogram.getP();
        double xmax = 0;
        double dstep = periodogram.getIntervalInRadians(), estep = dstep * D;
        int nf = 0;
        for (int i = 0; i < seasfreqs.length; ++i) {
            double f = seasfreqs[i];
            int j = (int) (seasfreqs[i] / dstep);
            if (f - (j - 1) * dstep < estep) {
                ++nf;
                xmax = Math.max(xmax, p[j - 1]);
            }
            if (f - j * dstep < estep) {
                ++nf;
                xmax = Math.max(xmax, p[j]);
            }
            if ((j + 1) * dstep - f < estep) {
                ++nf;
                xmax = Math.max(xmax, p[j + 1]);
            }
        }
        return 1 - Math.pow(1 - Math.exp(-xmax * .5), nf);
    }
}

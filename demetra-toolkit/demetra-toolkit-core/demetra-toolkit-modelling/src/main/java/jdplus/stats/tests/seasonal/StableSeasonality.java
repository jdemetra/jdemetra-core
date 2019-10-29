/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests.seasonal;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.dstats.F;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;

/**
 * One way ANOVA test on seasonality. The treatments are the different periods.
 * This implementation is identical to the ANOVA one (but faster)
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class StableSeasonality {

    private final double M, SSQ, SSR, SSM;
    private final int N, DFM, DFR;

    public StableSeasonality(DoubleSeq ts, int period) {
        // compute mean
        M = ts.average();
        N = ts.length();
        DFM = period - 1;
        DFR = N - period;
        // compute total SSQ
        double ssq = 0;
        int n = ts.length();
        DoubleSeqCursor reader = ts.cursor();
        for (int i = 0; i < n; i++) {
            double cur = reader.getAndNext();
            ssq += (cur - M) * (cur - M);
        }
        SSQ = ssq;
        // compute SS of seasonality factors

        double ssm = 0;
        for (int i = 0; i < period; ++i) {
            double s = 0;
            int nc = 0;
            for (int j = i; j < n; j += period) {
                s += ts.get(j);
                ++nc;
            }
            double mmj = s / nc;
            ssm += (mmj - M) * (mmj - M) * nc;
        }
        SSM = ssm;

        SSR = Math.max(0, SSQ - SSM);
    }

    /**
     * @return the M
     */
    public double getM() {
        return M;
    }

    /**
     * @return the SSQ
     */
    public double getSSQ() {
        return SSQ;
    }

    /**
     * @return the SSM
     */
    public double getSSM() {
        return SSM;
    }

    /**
     * @return the SSR
     */
    public double getSSR() {
        return SSR;
    }

    /**
     * @return the N
     */
    public int getN() {
        return N;
    }

    /**
     * @return the DFM
     */
    public int getDFM() {
        return DFM;
    }

    /**
     * @return the DFR
     */
    public int getDFR() {
        return DFR;
    }

    public StatisticalTest build() {
        F f = new F(DFM, DFR);
        return new StatisticalTest(f, (SSM / DFM) * (DFR / SSR), TestType.Upper, true);
    }
}

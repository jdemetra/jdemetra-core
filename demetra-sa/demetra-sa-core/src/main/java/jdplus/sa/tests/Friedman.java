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
package jdplus.sa.tests;

import jdplus.data.DataBlockIterator;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.dstats.Chi2;
import jdplus.dstats.F;
import jdplus.math.matrices.Matrix;
import demetra.stats.TestType;
import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import jdplus.stats.tests.TestsUtility;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class Friedman {

    private boolean useChi2 = true;

    private final int n;
    private final int period;
    private double sst;
    private final double sse;
    private final double t;

    public Friedman(DoubleSeq all, final int period) {
        // gets complete years:
        int nall = all.length();
        this.period = period;
        n = nall / period;

        DoubleSeq x = all.drop(nall - n * this.period, 0);
        DoubleSeq y = x.extract(0, period);
        Matrix R = Matrix.make(n, this.period);

        // computes the ranks on each year:
        int row = 0;
        for (int i = 0, start = 0; i < n; ++i) {
            Ranking.sort(y, R.row(row++));
            start += period;
            y = x.extract(start, period);
        }

        // computes mean of the ranks:
        double rmean = R.sum() / (n * this.period);
        // remove mean from R:
        sst = 0;
        DataBlockIterator cols = R.columnsIterator();
        while (cols.hasNext()) {
            double tmp = cols.next().sum() / n - rmean;
            sst += tmp * tmp;
        }
        sst *= n;

        R.sub(rmean);
        sse = R.ssq() / (n * (this.period - 1));
        t = sst / sse;
    }

    public Friedman useChi2(boolean chi2) {
        this.useChi2 = chi2;
        return this;
    }

    /**
     *
     * @return
     */
    public int getPeriod() {
        return period;
    }

    /**
     *
     * @return
     */
    public int getN() {
        return n;
    }

    /**
     *
     * @return
     */
    public double getSse() {
        return sse;
    }

    /**
     *
     * @return
     */
    public double getSst() {
        return sst;
    }

    /**
     *
     * @return
     */
    public double getT() {
        return t;
    }

//    private void process(final DataBlock all, int freq, boolean f) {
//
//    }
    public StatisticalTest build() {
        int nk = n * (period - 1);
        if (!useChi2 && t < nk) {
            F ftest = new F(period - 1, (period - 1) * (n - 1));
            return TestsUtility.testOf((n - 1) * t / (nk - t), ftest, TestType.Upper);
        } else {
            Chi2 chi2 = new Chi2(period - 1);
            return TestsUtility.testOf(t, chi2, TestType.Upper);
        }
    }

}

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
package demetra.stats.tests.seasonal;

import demetra.data.DataBlockIterator;
import static demetra.data.DeprecatedDoubles.sum;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.dstats.Chi2;
import demetra.dstats.F;
import demetra.maths.matrices.FastMatrix;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class Friedman {

    private boolean useChi2 = true;

    private int n;
    private int period;
    private double sst;
    private double sse;
    private double t;

    public Friedman(DoubleSeq all, final int period) {
        // gets complete years:
        int nall = all.length();
        this.period = period;
        n = nall / period;

        DoubleSeq x = all.drop(nall - n * this.period, 0);
        DoubleSeq y = x.extract(0, period);
        FastMatrix R = FastMatrix.make(n, this.period);

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
            double tmp = sum(cols.next()) / n - rmean;
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
            return new StatisticalTest(ftest, (n - 1) * t / (nk - t), TestType.Upper, false);
        } else {
            Chi2 chi2 = new Chi2(period - 1);
            return new StatisticalTest(chi2, t, TestType.Upper, true);
        }
    }

}

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
package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.YearIterator;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FriedmanTest extends StatisticalTest {

    private int m_n;

    private int m_k;
    private double m_sst;
    private double m_sse;
    private double m_t;

    /**
     * Chi2 Test
     *
     * @param ts Analysed time series
     */
    public FriedmanTest(TsData ts) {
        DataBlock x=TsDataBlock.all(ts).data;
        process(x, ts.getFrequency().intValue(), false);
    }

    /**
     * F or Chi2 tests. Both tests give very similar results
     *
     * @param ts Analysed time series
     * @param f Computes F test (true) or Chi2 test (false);
     */
    public FriedmanTest(TsData ts, boolean f) {
        DataBlock x=TsDataBlock.all(ts).data;
        process(x, ts.getFrequency().intValue(), f);
    }

    /**
     *
     * @return
     */
    public int getK() {
        return m_k;
    }

    /**
     *
     * @return
     */
    public int getN() {
        return m_n;
    }

    /**
     *
     * @return
     */
    public double getSse() {
        return m_sse;
    }

    /**
     *
     * @return
     */
    public double getSst() {
        return m_sst;
    }

    /**
     *
     * @return
     */
    public double getT() {
        return m_t;
    }

    private void process(final DataBlock all, int freq, boolean f) {
        // gets complete years:
        int nall = all.getLength();
        m_k = freq;
        m_n = nall / freq;

        DataBlock x = all.drop(nall - m_n * m_k, 0);
        DataBlock y = x.range(0, freq);
        Matrix R = new Matrix(m_n, m_k);

        // computes the ranks on each year:
        int row = 0;
        for (int i = 0; i < m_n; ++i) {
            Ranking.sort(y, R.row(row++));
            y.move(freq);
        }

        // computes mean of the ranks:
        double rmean = R.sum() / (m_n * m_k);
        // remove mean from R:
        m_sst = 0;
        DataBlockIterator cols = R.columns();
        DataBlock col = cols.getData();
        do {
            double tmp = col.sum() / m_n - rmean;
            m_sst += tmp * tmp;
        } while (cols.next());
        m_sst *= m_n;

        R.sub(rmean);
        m_sse = R.ssq() / (m_n * (m_k - 1));
        m_type = TestType.Upper;
        m_t = m_sst / m_sse;
        int nk = m_n * (m_k - 1);

        if (f && m_t < nk) {
            F ftest = new F();
            ftest.setDFNum(m_k - 1);
            ftest.setDFDenom((m_k - 1) * (m_n - 1));
            m_dist = ftest;
            m_val = (m_n - 1) * m_t / (nk - m_t);
        } else {
            Chi2 chi2 = new Chi2();
            chi2.setDegreesofFreedom(m_k - 1);
            m_dist = chi2;
            m_val = m_t;
        }
    }
}

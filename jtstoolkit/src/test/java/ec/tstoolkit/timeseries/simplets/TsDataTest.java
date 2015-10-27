/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package ec.tstoolkit.timeseries.simplets;

import data.Data;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TsDataTest {

    private TsData X, Y;

    public TsDataTest() {
        X = new TsData(TsFrequency.Monthly, 1980, 0, 120);
        Y = new TsData(TsFrequency.Monthly, 1982, 0, 120);
        for (int i = 0; i < X.getLength(); ++i) {
            X.set(i, i + 1);
        }
        for (int i = Y.getLength(); i > 0; --i) {
            Y.set(i - 1, i);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testCreation() {
        double[] values = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        // The monthly series will start in January 2012
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 2012, 0);
        // Creates directly the series. The last parameter indicates that the data are cloned.
        TsData s1 = new TsData(start, values, true);

        TsDomain dom = new TsDomain(start, values.length);
        // Creates a series from its domain
        TsData s2 = new TsData(dom);
        // All the data are missing
        assertTrue(s2.getObsCount() == 0);
        // Initialize the data
        for (int i = 0; i < values.length; ++i) {
            s2.set(i, values[i]);
        }
        // The series are of course identical
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testOperations() {
    }

    @Test
    public void testDrop() {
        boolean end = false;
        TsData s = X.clone();
        while (!s.isEmpty()) {
//            System.out.print(s.getLength());
            testIterators(s);
            if (end) {
                end = false;
                s = s.drop(0, 1);
            } else {
                end = true;
                s = s.drop(1, 0);
            }
        }

    }

    private void testIterators(TsData s) {
        PeriodIterator pf = PeriodIterator.fullYears(s);
        YearIterator yf = YearIterator.fullYears(s);
        double p = 0, y = 0;
        while (pf.hasMoreElements()) {
            p += pf.nextElement().data.sum();
        }
        while (yf.hasMoreElements()) {
            y += yf.nextElement().data.sum();
        }
        assertTrue(p == y);
//        System.out.print(" ok");
        pf = new PeriodIterator(s);
        yf = new YearIterator(s);
        p = 0;
        y = 0;
        while (pf.hasMoreElements()) {
            p += pf.nextElement().data.sum();
        }
        while (yf.hasMoreElements()) {
            y += yf.nextElement().data.sum();
        }
//        System.out.print(" ");
//        System.out.print(y);
//        System.out.print(" ");
//        System.out.println(p);
        assertTrue(p == y);
    }

    @Test
    public void testChainLinking() {
        // Example taken from the IMF manual
        // Initialization of the figures
        double[] qa = new double[]{67.4, 69.4, 71.5, 73.7, 76, 78.3, 80.6, 83.1, 85.5, 88.2, 90.8, 93.5};
        double[] pa = new double[]{6.1, 5.7, 5.3, 5, 4.5, 4.3, 3.8, 3.5, 3.4, 3.1, 2.8, 2.7};
        double[] qb = new double[]{57.6, 57.1, 56.5, 55.8, 55.4, 54.8, 54.2, 53.6, 53.2, 52.7, 52.1, 52};
        double[] pb = new double[]{8.0, 8.6, 9.4, 10.0, 10.7, 11.5, 11.7, 12.1, 12.5, 13, 13.8, 14.7};

        // Q for quantities, P for price, V for values. Products A and B
        // Previous year
        TsData QAy = new TsData(TsFrequency.Yearly, 1997, 0, 1);
        TsData PAy = new TsData(TsFrequency.Yearly, 1997, 0, 1);
        TsData QBy = new TsData(TsFrequency.Yearly, 1997, 0, 1);
        TsData PBy = new TsData(TsFrequency.Yearly, 1997, 0, 1);
        QAy.set(0, 251.0);
        PAy.set(0, 7.0);
        QBy.set(0, 236.0);
        PBy.set(0, 6.0);
        TsData Vy = TsData.add(TsData.multiply(QAy, PAy), TsData.multiply(QBy, PBy));

        // Quarterly series 
        TsData QAq = new TsData(TsFrequency.Quarterly, 1998, 0, qa, true);
        TsData PAq = new TsData(TsFrequency.Quarterly, 1998, 0, pa, true);
        TsData QBq = new TsData(TsFrequency.Quarterly, 1998, 0, qb, true);
        TsData PBq = new TsData(TsFrequency.Quarterly, 1998, 0, pb, true);

        // Chain-linking  by annual overlap
        // STEP 1. Computes the annual (weighted) price index for A and B. 
        TsData VAq = TsData.multiply(QAq, PAq);
        TsData VAy = VAq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        QAy = QAy.update(QAq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));
        PAy = PAy.update(TsData.divide(VAy, QAy));

        TsData VBq = TsData.multiply(QBq, PBq);
        TsData VBy = VBq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        QBy = QBy.update(QBq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));
        PBy = PBy.update(TsData.divide(VBy, QBy));

        // Total value (quarterly)
        TsData Vq = TsData.add(VAq, VBq);
        // Total value (annual)
        Vy = Vy.update(Vq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));

        // STEP2. Compute quantities expressed in the average prices of previous year.
        // Create first Qq, an empty series (with the same time domain as the values)
        // that will contain the data 
        TsData Qq = new TsData(Vq.getDomain());

        // Iterates through the years for computing the series at the price of previous year
        YearIterator yq = new YearIterator(Qq);
        YearIterator yqa = new YearIterator(QAq);
        YearIterator yqb = new YearIterator(QBq);
        while (yq.hasMoreElements()) {
            TsDataBlock qcur = yq.nextElement();
            TsDataBlock qacur = yqa.nextElement();
            TsDataBlock qbcur = yqb.nextElement();
            TsPeriod prev = TsPeriod.year(qcur.start.getYear() - 1);
            // Qq=PAy(y-1)*QAq
            // Qq=Qq + PBy(y-1)*QBq
            qcur.data.setAY(PAy.get(prev), qacur.data);
            qcur.data.addAY(PBy.get(prev), qbcur.data);
        }

        TsData Qy = Qq.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);

        // STEP 3. Chain the indexes.
        // The current index is stored in idx. 
        double idx = 100;
        int ifreq = Qq.getFrequency().intValue();
        yq.reset();
        while (yq.hasMoreElements()) {
            // the index is applied to the quantities Qq, divided by the average value of
            // previous year. It is increased by the growth of the year 
            // (value(t), expressed in price of (t-1) divided by value in t-1)
            TsDataBlock qcur = yq.nextElement();
            TsPeriod prev = TsPeriod.year(qcur.start.getYear() - 1);
            double val0 = Vy.get(prev);
            double val1 = qcur.data.sum();
            qcur.data.mul(idx / (val0 / ifreq));
            // increase the index by the growth of this year
            idx *= val1 / val0;
        }
//        System.out.println(Qq);

        //////////////////////////////////////
        // Fixed year index is computed in a trivial way
        TsPeriod ybase = TsPeriod.year(1997);
        TsData Qa = QAq.times(PAy.get(ybase));
        TsData Qb = QBq.times(PBy.get(ybase));

        TsData Qq2 = TsData.add(Qa, Qb).index(ybase, 100);
//        System.out.println(Qq2);

    }

    @Test
    public void testSelection() {
        TsData s = new TsData(TsFrequency.Monthly, 1980, 0, 0);
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.from(Day.BEG);
        assertTrue(s.select(sel).isEmpty());
        sel.to(Day.END);
        assertTrue(s.select(sel).isEmpty());

    }

    @Test
    public void testExtendTo() {
        TsData s = new TsData(TsFrequency.Monthly, 1980, 0, 12);
        Day d0 = new Day(1980, Month.December, 30);
        assertTrue(s.extendTo(d0).getLastPeriod().lastday().equals(d0));
        Day d1 = new Day(1980, Month.December, 29);
        assertTrue(s.extendTo(d1).getLastPeriod().lastday().isBefore(d1));
        Day d2 = new Day(1981, Month.January, 1);
        assertTrue(s.extendTo(d2).getLastPeriod().lastday().equals(d0));
    }
    
    @Test
    public void testChangeFreq(){
        TsData s = new TsData(TsFrequency.Monthly, 1946, 5, 825);
        s.randomAirline();
        for (int i=0; i<12; ++i){
            TsData l=s.drop(i, 0).changeFrequency(TsFrequency.Quarterly, TsAggregationType.Last, true);
            for (int j=0; j<l.getLength(); ++j){
                TsPeriod q=l.getDomain().get(j);
                assertTrue(l.get(j) == s.get(q.lastPeriod(TsFrequency.Monthly)));
            }
        }
    }
}

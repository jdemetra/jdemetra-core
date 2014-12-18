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

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class AutoRegressiveSpectrumTestTest {


    public AutoRegressiveSpectrumTestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void test() {
//        ArimaModelBuilder builder = new ArimaModelBuilder();
//        int n = 240;
//        double[] x = builder.generate(builder.createModel(Polynomial.of(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -.3}), Polynomial.of(new double[]{1, .3}), 1), n);

        TsData ts = data.Data.X.clone();
        AutoRegressiveSpectrumTest ar = new AutoRegressiveSpectrumTest();
        ar.test(ts.delta(12));

        int tdp = ar.tdPeaksCount(), sp = ar.seasonalPeaksCount();
        assertTrue(tdp > 0 && sp == 0);
        ar = new AutoRegressiveSpectrumTest();
        ar.test(ts.delta(1));

        tdp = ar.tdPeaksCount();
        sp = ar.seasonalPeaksCount();
        assertTrue(sp > 0 && tdp == 0);
    }

    //@Test
    public void testRandom() {
        ArimaModelBuilder builder = new ArimaModelBuilder();
        int n = 240, m = 1000;
        int tdp = 0, sp = 0;
        for (int i = 0; i < m; ++i) {
            double[] x = builder.generate(builder.createModel(Polynomial.of(new double[]{1}), Polynomial.of(new double[]{1}), 1), n);

            TsData ts = new TsData(TsFrequency.Monthly, 1995, 0, x, false);
            AutoRegressiveSpectrumTest ar = new AutoRegressiveSpectrumTest();
            ar.test(ts);
            tdp += ar.tdPeaksCount();
            sp += ar.seasonalPeaksCount()> 0 ? 1 : 0;
        }
        
        System.out.println(1.0/m*tdp);
        System.out.println(1.0/m*sp);
    }
}

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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FTestTest {

    public FTestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testTs() {

        TsData s = data.Data.X.delta(1);
        FTest f = new FTest();
        f.test(s);
        assertTrue(f.getFTest().isSignificant());
        s = s.delta(12);
        f.test(s);
        assertFalse(f.getFTest().isSignificant());
    }

    @Test
    public void testWhiteNoise() {
        SarimaSpecification spec = new SarimaSpecification(12);
        SarimaModel wn = new SarimaModel(spec);
        ArimaModelBuilder builder = new ArimaModelBuilder();
        double[] x = builder.generate(wn, 240);
        TsData s = new TsData(TsFrequency.Monthly, 1980, 0, x, false);
        ModelDescription desc = new ModelDescription(s, null);
        SarimaComponent cmp = new SarimaComponent();
        cmp.setModel(wn);
        desc.setArimaComponent(cmp);
        FTest f = new FTest();
        f.test(desc);
        StatisticalTest ftest = f.getFTest();
        StatisticalTest ptest = PeriodogramTest.computeSum2(s, 12);
        assertTrue(Math.abs(ftest.getValue() - ptest.getValue()) < 1e-6);
    }

}

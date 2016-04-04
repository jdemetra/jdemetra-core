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
package ec.tstoolkit.arima;

import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;
import java.util.Random;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class AutoCovarianceFunctionTest {

    private SarimaModel arma1010, arma3101;
    private double EPS = 1e-8;

    public AutoCovarianceFunctionTest() {
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setP(1);
        spec.setBP(1);
        spec.setQ(0);
        spec.setBQ(0);
        arma1010 = new SarimaModel(spec);
        spec.setP(3);
        spec.setBP(0);
        spec.setQ(1);
        spec.setBQ(1);
        arma3101 = new SarimaModel(spec);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testARMA1010() {
        Random rn = new Random(0);
        for (int i = 0; i < 1000; ++i) {
            arma1010.setPhi(1, 1.99 * rn.nextDouble() - .995);
            arma1010.setBPhi(1, 1.99 * rn.nextDouble() - .995);
            if (arma1010.isValid(true)) {
                process(arma1010);
            }
        }
    }

    @Test
    public void testARMA3111() {
        Random rn = new Random(0);
        for (int i = 0; i < 1000; ++i) {
            arma3101.setTheta(1, 1.99 * rn.nextDouble() - .995);
            arma3101.setPhi(1, 1.99 * rn.nextDouble() - .995);
            arma3101.setPhi(2, 1.99 * rn.nextDouble() - .995);
            arma3101.setPhi(3, 1.99 * rn.nextDouble() - .995);
            arma3101.setBTheta(1, 1.99 * rn.nextDouble() - .995);
            if (arma3101.isValid(true)) {
                process(arma3101);
            }
        }
    }

    private void process(SarimaModel arma) {
        AutoCovarianceFunction acf = arma.getAutoCovarianceFunction();
        acf.setMethod(AutoCovarianceFunction.Method.Default2);
        double[] a1 = acf.values(36);
        acf.setMethod(AutoCovarianceFunction.Method.SymmetricFilterDecomposition2);
        double[] a2 = acf.values(36);
        AutoCovarianceFunction acf3 = new AutoCovarianceFunction(SymmetricFilter.createFromFilter(arma.getMA()), arma.getAR());
        double[] a3 = acf3.values(36);
        for (int i = 0; i < a1.length; ++i) {
            assertTrue(Math.abs(a1[i] - a2[i]) < EPS);
            assertTrue(Math.abs(a1[i] - a3[i]) < EPS);
        }
    }

    @Test
    public void testPrepare() {
        arma3101.setDefault(-.6, -.4);
        AutoCovarianceFunction acf = arma3101.getAutoCovarianceFunction();
        acf.prepare(36);
    }
}

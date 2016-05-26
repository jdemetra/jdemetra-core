/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.data;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class BurgAlgorithmTest {

    public BurgAlgorithmTest() {
    }

    @Test
    public void test() {
        ArimaModelBuilder builder = new ArimaModelBuilder();
        int n = 240, m = 10;
        for (int i = 0; i < m; ++i) {
            double[] x = builder.generate(builder.createModel(Polynomial.of(new double[]{1, -.7, .5}), Polynomial.ONE, 1), n);
            BurgAlgorithm burg = new BurgAlgorithm();
            //Peaks peaks=new Peaks(new TsData(TsFrequency.Monthly, 1980, 0, x, true), 240, false);
            assertTrue(burg.solve(new ReadDataBlock(x), 30));
        }
    }

    @Test
    @Ignore
    public void testCompare() {
        ArimaModelBuilder builder = new ArimaModelBuilder();
        int n = 240;
        double[] x = builder.generate(builder.createModel(Polynomial.of(new double[]{1, -.7, .5}), Polynomial.ONE, 1), n);
        BurgAlgorithm burg = new BurgAlgorithm();
        burg.solve(new ReadDataBlock(x), 30);
        //Peaks peaks=new Peaks(new TsData(TsFrequency.Monthly, 1980, 0, x, true), 240, false);
        System.out.println(burg.getCoefficients());
        DurbinAlgorithm durbin = new DurbinAlgorithm();
        durbin.solve(new ReadDataBlock(x), 30);
        //Peaks peaks=new Peaks(new TsData(TsFrequency.Monthly, 1980, 0, x, true), 240, false);
        System.out.println(new ReadDataBlock(durbin.getCoefficients()));

    }
}

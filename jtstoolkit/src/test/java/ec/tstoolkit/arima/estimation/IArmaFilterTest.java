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

package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class IArmaFilterTest {

    private SarimaModel ar11, ma11, arma3111;
    private AnsleyFilter af = new AnsleyFilter();
    private KalmanFilter kf = new KalmanFilter();
    private LjungBoxFilter lf = new LjungBoxFilter();
    private ModifiedLjungBoxFilter mlf = new ModifiedLjungBoxFilter();
    private double EPS = 1e-9;

    public IArmaFilterTest() {
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setQ(1);
        spec.setBQ(1);
        ma11 = new SarimaModel(spec);

        spec = new SarmaSpecification(12);
        spec.setP(1);
        spec.setBP(1);
        ar11 = new SarimaModel(spec);

        spec = new SarmaSpecification(12);
        spec.setP(3);
        spec.setBP(1);
        spec.setQ(1);
        spec.setBQ(1);
        arma3111 = new SarimaModel(spec);
    }

    private void process(SarimaModel sarima) {
        double d0 = 0, d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 0, d6=0, d7=0;

        //try {
            DataBlock Y = new DataBlock(360);
            Y.randomize(0);
            
            ArmaKF rf=new ArmaKF(sarima);
            Likelihood ll=new Likelihood();
            rf.process(Y, ll);
            double D=ll.getLogDeterminant();
            double S=ll.getSsqErr();

            int n = af.initialize(sarima, Y.getLength());
            double ad = af.getLogDeterminant();
            DataBlock aE = new DataBlock(n);
            af.filter(Y, aE);
            double as = aE.ssq();

            n = kf.initialize(sarima, Y.getLength());
            double kd = kf.getLogDeterminant();
            DataBlock kE = new DataBlock(n);
            kf.filter(Y, kE);
            double ks = kE.ssq();

            n = lf.initialize(sarima, Y.getLength());
            double ld = lf.getLogDeterminant();
            DataBlock lE = new DataBlock(n);
            lf.filter(Y, lE);
            double ls = lE.ssq();

            n = mlf.initialize(sarima, Y.getLength());
            double mld = mlf.getLogDeterminant();
            DataBlock mlE = new DataBlock(n);
            mlf.filter(Y, mlE);
            double mls = mlE.ssq();


            d0 = Math.abs((S - as) / (1 + S));
            d1 = Math.abs((D - ad) / (1 + D));
            d2 = Math.abs((S - ks) / (1 + S));
            d3 = Math.abs((D - kd) / (1 + D));
            d4 = Math.abs((S - ls) / (1 + S));
            d5 = Math.abs((D - ld) / (1 + D));
            d6 = Math.abs((S - mls) / (1 + S));
            d7 = Math.abs((D - mld) / (1 + D));

        //} catch (Exception err) {
        //    return;
        //}
//        System.out.print(sarima.getParameters());
//        System.out.print('\t');
//        System.out.print(d0);
//        System.out.print('\t');
//        System.out.print(d1);
//        System.out.print('\t');
//        System.out.print(d2);
//        System.out.print('\t');
//        System.out.print(d3);
//        System.out.print('\t');
//        System.out.print(d4);
//        System.out.print('\t');
//        System.out.print(d5);
//        System.out.print('\t');
//        System.out.print(d6);
//        System.out.print('\t');
//        System.out.println(d7);
        //assertTrue(d0 < EPS);
        //assertTrue(d1 < EPS);
        assertTrue(d2 < EPS);
        assertTrue(d3 < EPS);
        //assertTrue(d4 < EPS);
        //assertTrue(d5 < EPS);
        //assertTrue(d6 < EPS); // X12
        assertTrue(d7 < EPS);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testFilter_MA_1_1() {
        for (int i = -9; i < 10; ++i) {
            for (int j = -9; j < 10; ++j) {
                ma11.setTheta(1, i * .1);
                ma11.setBTheta(1, j * .1);
                process(ma11);
            }
        }
    }

    @Test
    public void testFilter_AR_1_1() {
        for (int i = -9; i < 10; ++i) {
            for (int j = -9; j < 10; ++j) {
                ar11.setPhi(1, i * .1);
                ar11.setBPhi(1, j * .1);
                process(ar11);
            }
        }
    }

    //@Ignore(value = "CHARPHI@2012-09-10: this test failure blocks the next tests")
    @Test
    public void testFilter_ARMA_3_1_1_1() {
        Random rn =new Random(0);
        for (int i = 0; i < 1000; ++i) {
            arma3111.setPhi(1, 2*rn.nextDouble()-1);
            arma3111.setPhi(2, 2*rn.nextDouble()-1);
            arma3111.setPhi(3, 2*rn.nextDouble()-1);
            arma3111.setBPhi(1, 2*rn.nextDouble()-1);
            arma3111.setTheta(1, 2*rn.nextDouble()-1);
            arma3111.setBTheta(1, 2*rn.nextDouble()-1);
            if (arma3111.isValid(true)) {
                process(arma3111);
            }
        }
    }
}

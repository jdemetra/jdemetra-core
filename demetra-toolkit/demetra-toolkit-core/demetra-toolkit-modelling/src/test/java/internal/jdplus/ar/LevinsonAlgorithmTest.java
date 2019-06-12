/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.jdplus.ar;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import jdplus.ar.AutoRegressiveEstimation;
import demetra.data.DoubleSeq;
import internal.jdplus.ar.LevinsonAlgorithm;

/**
 *
 * @author Jean Palate
 */
public class LevinsonAlgorithmTest {

    public static final DoubleSeq X;

    static {
        double[] x = new double[120];
        Random rnd = new Random(0);
        x[0] = rnd.nextGaussian();
        x[1] = rnd.nextGaussian();
        for (int i = 2; i < 120; ++i) {
            x[i] = x[i - 1] * .8 - x[i-2] * .4 + rnd.nextGaussian();
        }
        X = DoubleSeq.of(x);
    }

    public LevinsonAlgorithmTest() {
    }

    @Test
    public void testSomeMethod() {
        AutoRegressiveEstimation ar = new LevinsonAlgorithm();
        ar.estimate(X, 30);
//        System.out.println(ar.coefficients());
//        System.out.println(ar.residuals());
        
        double[] x=new double[X.length()];
        X.copyTo(x, 0);
        double[] ac=ec.tstoolkit.data.DescriptiveStatistics.ac(30, x);
        double[] coeff=new double[ac.length];
        double[] pac = ec.tstoolkit.data.DescriptiveStatistics.pac(ac, coeff);
        double[] a=x.clone();
        for (int i = 0; i < a.length; ++i) {
            int jmax = pac.length > i ? i : pac.length;
            for (int j = 1; j <= jmax; ++j) {
                a[i] -= coeff[j - 1] * x[i - j];
            }
        }
        assertTrue(ar.coefficients().allMatch(DoubleSeq.of(coeff), (y,z)->Math.abs(y-z)<1e-9));
        assertTrue(ar.residuals().allMatch(DoubleSeq.of(a), (y,z)->Math.abs(y-z)<1e-9));
   }

}

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
package demetra.stats;

import demetra.data.DataBlock;
import demetra.data.WindowFunction;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class RobustCovarianceComputerTest {

    public RobustCovarianceComputerTest() {
    }

    @Test
    public void testArray() {
        DataBlock s = DataBlock.make(1000);
        Random rnd = new Random(0);
        s.set(() -> -.5 + rnd.nextDouble());
        s.applyRecursively(1, (a, b) -> .5 * a + .5 * b);

//        double sxx = s.ssq(), sx = s.sum();
//        System.out.println((sxx - sx * sx / s.length()) / s.length());
        for (int i = 5; i < 100; ++i) {
            assertTrue(0.03<RobustCovarianceComputer.covariance(s, WindowFunction.Bartlett, i));
            assertTrue(0.03<RobustCovarianceComputer.covariance(s, WindowFunction.Tukey, i));
        }
    }

}

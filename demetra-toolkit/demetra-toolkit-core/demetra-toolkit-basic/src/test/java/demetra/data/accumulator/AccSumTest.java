/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data.accumulator;

import jdplus.data.accumulator.AccSum;
import demetra.data.DoubleSeq;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AccSumTest {

    public AccSumTest() {
    }

    @Test
    public void testRounding() {
        double e = 1e-30;
        assertTrue(1 + e == 1 - e);
    }

    @Test
    public void testSum() {
        double[] values = new double[10000];
        DoubleSeq.Mutable seq = DoubleSeq.Mutable.of(values);
        seq.set(i -> (i + 1) / 7999.0);
        double m = values.length;
        double t = m * (m + 1) / (2 * 7999.0);
        double t0 = seq.sum();
        double t1 = AccSum.fastAccurateSum(seq);
//        System.out.println(t-t0);
//        System.out.println(t-t1);
        assertTrue(Math.abs(t - t1) <= Math.abs(t - t0));
    }

    @Test
    public void testProduct() {
        AccSum.AccurateDouble ad = new AccSum.AccurateDouble();
        AccSum.twoProduct(8.01, 8.01, ad);
        System.out.println(ad);
    }

}

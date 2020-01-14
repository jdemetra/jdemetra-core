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
package jdplus.math.highprecision;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DoubleDoubleTest {

    public DoubleDoubleTest() {
    }

    @Test
    public void testSqrt() {
        final double A = 500000000.123, B = 71.00001;

        LegacyDoubleDouble ldd1 = LegacyDoubleDouble.valueOf(A);
        LegacyDoubleDouble ldd2 = LegacyDoubleDouble.valueOf(B);
        LegacyDoubleDouble lm = ldd1.multiply(ldd2);
        LegacyDoubleDouble ls = lm.sqrt();
        //System.out.println(ls);

        DoubleDoubleComputer dd = new DoubleDoubleComputer(A);
        DoubleDouble s = dd.mul(B, 0).sqrt().result();
        //System.out.println(s);
        assertTrue(ls.doubleValue() == s.asDouble());

    }

    public static void main(String[] arg) {
        final double A = 50000.123, B = 0.71;
        final long K = 100000000;
        long t0 = System.currentTimeMillis();
        LegacyDoubleDouble ls = LegacyDoubleDouble.valueOf(0);
        for (long i = 0; i < K; ++i) {
            LegacyDoubleDouble ldd1 = LegacyDoubleDouble.valueOf(A);
            LegacyDoubleDouble ldd2 = LegacyDoubleDouble.valueOf(B);
            LegacyDoubleDouble lm = ldd1.multiply(ldd2);
            ls = lm.add(ldd1).add(ldd2);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(ls);
        t0 = System.currentTimeMillis();
        DoubleDouble s = new DoubleDouble(0, 0);
        for (long i = 0; i < K; ++i) {
            DoubleDoubleComputer dd = new DoubleDoubleComputer(A);
            s = dd.mul(B, 0).add(A).add(B).result();
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(s);
        t0 = System.currentTimeMillis();
        double ds = 0;
        for (long i = 0; i < K; ++i) {
            ds = A * B+ A + B;
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(ds);
        
        System.out.println(ls.doubleValue()-ds);

    }

}

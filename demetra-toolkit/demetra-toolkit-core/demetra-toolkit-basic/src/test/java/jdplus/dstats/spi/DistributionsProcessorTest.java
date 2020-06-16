/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.dstats.spi;

import demetra.dstats.spi.Distributions;
import demetra.stats.ProbabilityType;
import jdplus.data.DataBlock;
import jdplus.dstats.Chi2;
import jdplus.random.MersenneTwister;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class DistributionsProcessorTest {

    public DistributionsProcessorTest() {
    }

    @Test
    public void testNormal() {
        Distributions.Processor.Distribution normal = Distributions.normal();
        DataBlock Z = DataBlock.make(1001);
        Z.set(i -> normal.density((i - 501) * .01));
        assertTrue(!Z.isZero(0));
    }

    @Test
    public void testT() {
        Distributions.Processor.Distribution t = Distributions.t(2);
        DataBlock Z = DataBlock.make(1001);
        Z.set(i -> t.density((i - 501) * .01));
        assertTrue(!Z.isZero(0));
    }

    public static void main(String[] arg) {
        MersenneTwister rnd = MersenneTwister.fromSystemNanoTime();
        long t0 = System.currentTimeMillis();
        Chi2 d = new Chi2(16);
        for (int i = 0; i < 10000000; ++i) {
            double random = d.random(rnd);
            double p = d.getProbability(random, ProbabilityType.Lower);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        Distributions.Processor.Distribution d2 = demetra.dstats.spi.Distributions.chi2(16);
        for (int i = 0; i < 10000000; ++i) {
            double random = d2.random();
            double p = d2.probability(random, ProbabilityType.Lower);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}

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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.random.MersenneTwister;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ADFTestTest {

    static final int K = 50;

    public ADFTestTest() {
    }

    @Test
    public void testSomeMethod() {
        int N = 100000;
        double[] T = new double[N];
        for (int i = 0; i < N; ++i) {
            IReadDataBlock data = test(1200);
            ADFTest adf = new ADFTest();
            adf.setK(1);

            adf.test(data);
            T[i] = adf.getT();
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(new DataBlock(T));
        double[] quantiles = stats.quantiles(99);
        double n=N;
        for (int i = 0; i < 12; ++i) {
            System.out.println(stats.countBetween(-3 + i * .5, -3 + (i + 1) * .5)*100/n);
        }
    }

    public static IReadDataBlock test(int n) {

        Normal N = new Normal();
        MersenneTwister rnd = MersenneTwister.fromSystemNanoTime();
        DataBlock data = new DataBlock(n + K);
        data.set(() -> N.random(rnd));
        data.cumul();
        return data.drop(K, 0);
    }
}

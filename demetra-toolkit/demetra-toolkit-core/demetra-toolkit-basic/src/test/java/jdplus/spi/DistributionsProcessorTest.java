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
package jdplus.spi;

import demetra.dstats.spi.Distributions;
import jdplus.data.DataBlock;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate
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
}

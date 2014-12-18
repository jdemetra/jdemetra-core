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

package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Extension of a smoothing by copying of the last smoothed values
 * @author Frank Osaer, Jean Palate
 */
public class CopyEndPointsTest {

    public CopyEndPointsTest() {
    }

    /**
     * Test of process method, of class CopyEndPoints.
     */
    @Test
    public void testProcess() {
        int N = 19;
        //int P=5;
        DataBlock in = new DataBlock(N);
        in.set(1);
        in.cumul();
        for (int P = 0; P < 10; ++P) {
            DataBlock out = in.deepClone();
            CopyEndPoints cnp = new CopyEndPoints(P);
            cnp.process(in, out);
            assertTrue(out.range(0, P + 1).isConstant());
            assertTrue(out.range(N - P - 1, N).isConstant());
        }
    }
}

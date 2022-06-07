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
package jdplus.data.analysis;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodogramTest {
    
    double[] data;
    
    public PeriodogramTest() {
        data=new double[Data.ABS_RETAIL.length-1];
        for (int i=0; i<data.length; ++i){
            data[i]=Math.log(Data.ABS_RETAIL[i+1])-Math.log(Data.ABS_RETAIL[i]);
        }
    }

    @Test
    public void testDFT() {
        Periodogram p1 = Periodogram.of(DoubleSeq.of(data));
        ec.tstoolkit.data.Periodogram p2=new ec.tstoolkit.data.Periodogram(new ec.tstoolkit.data.ReadDataBlock(data), false);
        for (int i=0; i<p1.getP().length; ++i){
            assertEquals(p1.getP()[i], p2.getP()[i], 1e-9);
        }
    }
    
}

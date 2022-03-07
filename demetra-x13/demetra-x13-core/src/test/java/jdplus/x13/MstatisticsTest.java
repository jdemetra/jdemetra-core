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
package jdplus.x13;

import demetra.data.Data;
import demetra.processing.DefaultProcessingLog;
import demetra.x13.X13Spec;
import ec.tstoolkit.algorithm.CompositeResults;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class MstatisticsTest {

    public MstatisticsTest() {
    }

    @Test
    public void testMStats() {
        
        process(0,0);
        process(0,1);
        process(0,11);
        process(1,0);
        process(10,0);
        process(9,1);
    }
    
    private void process(int bdrop, int edrop){
        X13Kernel x13=X13Kernel.of(X13Spec.RSA4, null);
        DefaultProcessingLog log=new DefaultProcessingLog();
        X13Results rslt = x13.process(Data.TS_PROD.drop(bdrop, edrop), log);
        Mstatistics m=Mstatistics.of(rslt.getPreadjustment(), rslt.getDecomposition(), rslt.getFinals());
        
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true).drop(bdrop, edrop);
        CompositeResults orslt = ec.satoolkit.algorithm.implementation.X13ProcessingFactory.process(s, ec.satoolkit.x13.X13Specification.RSA4);
        ec.satoolkit.x11.Mstatistics om=orslt.get("m-statistics", ec.satoolkit.x11.Mstatistics.class);
        compare(m, om);
    }

    private void compare(Mstatistics m, ec.satoolkit.x11.Mstatistics oldm) {
        for (int i = 1; i <= 11; ++i) {
            assertEquals(m.getM(i), oldm.getM(i), 1e-5);
        }
    }

}

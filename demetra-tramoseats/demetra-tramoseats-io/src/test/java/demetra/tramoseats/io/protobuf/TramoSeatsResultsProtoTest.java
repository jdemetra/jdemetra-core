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
package demetra.tramoseats.io.protobuf;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsResultsProtoTest {
    
    public TramoSeatsResultsProtoTest() {
    }

    @Test
    public void testFull() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log=ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        TramoSeatsResultsProtos.TramoSeatsResults pb = TramoSeatsResultsProto.convert(rslt);
        assertTrue(pb.toByteArray() != null);
   }
    
    @Test
    public void test0() {
        TsPeriod start=TsPeriod.monthly(1992,1);
        TsData s=TsData.ofInternal(start, Data.RETAIL_BEERWINEANDLIQUORSTORES);
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log=ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(s, log);
        TramoSeatsResultsProtos.TramoSeatsResults pb = TramoSeatsResultsProto.convert(rslt);
        assertTrue(pb.toByteArray() != null);
   }
}

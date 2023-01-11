/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.modelling.r;

import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class InterpolationTest {
    
    public InterpolationTest() {
    }

    @Test
    public void testAirline() {
        TsData ts = Data.TS_ABS_RETAIL;
        double[] s=ts.getValues().toArray();
        Random rnd=new Random(0);
        for (int i=0; i<300; ++i){
            s[rnd.nextInt(s.length)]=Double.NaN;
        } 
        
        TsData tsc = Interpolation.airlineInterpolation(TsData.ofInternal(ts.getStart(), s));
        TsDataTable table=TsDataTable.of(Arrays.asList(ts,tsc));
        System.out.println(table);
    }
    
}

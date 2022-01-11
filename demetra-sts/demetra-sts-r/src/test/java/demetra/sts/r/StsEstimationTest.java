/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.sts.r;

import demetra.data.Data;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StsEstimationTest {

    public StsEstimationTest() {
    }

    @Test
    public void testABS() {
        StsEstimation.Results rslt = StsEstimation.process(Data.TS_ABS_RETAIL, 1, 1, -1, 1, "Crude");
//        System.out.println(rslt.getBsm());

        Map<String, Class> dictionary = new LinkedHashMap<>();
        StsEstimation.Results.getMapping().fillDictionary(null, dictionary, true);

//        dictionary.keySet().forEach(s -> System.out.println(s));

//        System.out.println(rslt.getData("levelvar", Double.class));
//        System.out.println(rslt.getData("t", TsData.class));
    }

}

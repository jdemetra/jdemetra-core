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
package demetra.timeseries.regression.modelling;

import demetra.arima.SarimaModel;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class LinearModelEstimationTest {

    public LinearModelEstimationTest() {
    }

    @Test
    public void testSomeMethod() {
        String[] keys=new String[]{"test1","test10","test0","test5","test3","test11",
            "test41","test61","test51","test91","test81","test14","test17",
            "test35","test36","test31","test19","test18"};
        LinearModelEstimation.Builder<SarimaModel> builder = LinearModelEstimation.<SarimaModel>builder();
        for (int i=0; i<keys.length; ++i)
            builder.addtionalResult(keys[i], null);
                
        LinearModelEstimation<SarimaModel> lme = builder.build();

        String[] keys2 = lme.getAddtionalResults().keySet().stream().toArray(n->new String[n]);
        assertTrue(Arrays.equals(keys, keys2));

        boolean ok = true;
        try {
            lme.getAddtionalResults().clear();
            ok = false;
        } catch (UnsupportedOperationException err) {
        }
        assertTrue(ok);
    }

}

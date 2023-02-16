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
package jdplus.ssf.sts.splines;

import demetra.data.DoubleSeq;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class WeeklySplineTest {

    public WeeklySplineTest() {
    }

    @Test
    public void testLp() {

        WeeklySpline ds = new WeeklySpline(2003, 3, new int[]{100, 200, 300});
        for (int i = 0; i < 29; ++i) {
            DoubleSeq obs = ds.observations(1);
            assertTrue(obs.length() <= 53);
            double last=obs.get(obs.length() - 1);
            assertTrue(last < ds.getPeriod());
            assertTrue(last + 1 > ds.getPeriod());
        }
    }

}

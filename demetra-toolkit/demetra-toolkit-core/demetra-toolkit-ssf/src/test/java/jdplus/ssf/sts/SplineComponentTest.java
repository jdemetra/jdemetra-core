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
package jdplus.ssf.sts;

import jdplus.ssf.sts.splines.SplineComponent;
import demetra.data.DoubleSeq;
import jdplus.ssf.sts.splines.SplineData;
import jdplus.ssf.sts.splines.WeeklySpline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SplineComponentTest {

    public SplineComponentTest() {
    }

    @Test
    public void testWeekly() {
        int[] xi = new int[]{50, 100, 150, 200, 300, 350};
        WeeklySpline ws = new WeeklySpline(1980, 0, xi);
        SplineData data = SplineData.of(ws, 20);

        assertTrue(data != null);
    }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            int[] xi = new int[]{50, 100, 150, 200, 300, 350};
            WeeklySpline ws = new WeeklySpline(1980, 0, xi);
            SplineData data = SplineData.of(ws, 20);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}

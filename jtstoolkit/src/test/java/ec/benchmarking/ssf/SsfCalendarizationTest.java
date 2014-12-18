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
package ec.benchmarking.ssf;

import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfCalendarizationTest {

    public SsfCalendarizationTest() {
    }

    @Test
    public void testSsfCompare() {
        double[] x = new double[96];
        double[] xc = new double[96];
        Random rnd = new Random(0);
        for (int i = 0; i < 96; ++i) {
            if ((i + 1) % 12 == 0) {
                x[i] = rnd.nextDouble();
                if (i >= 12) {
                    xc[i] = x[i] + xc[i - 12];
                } else {
                    xc[i] = x[i];
                }
            } else {
                x[i] = Double.NaN;
                xc[i] = Double.NaN;
            }
        }

        SsfData sx = new SsfData(x, null);
        SsfData sxc = new SsfData(xc, null);

        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setSsf(new SsfDenton(12, null));
        smoother.process(sx);
        SmoothingResults sstates1 = smoother.calcSmoothedStates();
        double[] c1 = sstates1.component(1);
        smoother.setSsf(new SsfCalendarizationC(null));
        smoother.process(sxc);
        SmoothingResults sstates2 = smoother.calcSmoothedStates();
        double[] c2 = sstates2.component(1);

        for (int i = 0; i < c1.length; ++i) {
            assertTrue(Math.abs(c1[i] - c2[i]) < 1e-9);
        }
    }

//    @Test
    public void demoSsfCalendarization() {
        // 
        double[] x = new double[134];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }

        x[0] = 0;
        x[28] = 9000;
        x[59] = 14000;
        x[84] = 23500;
        x[112] = 30500;

        SsfData sx = new SsfData(x, null);

        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setSsf(new SsfCalendarizationC(null));
        smoother.process(sx);
        SmoothingResults sstates = smoother.calcSmoothedStates();
        double[] c1 = sstates.component(1);

        double[] w = new double[140];
        double[] d = new double[]{0, .6, .8, 1, 1.2, 1.8, 1.6};

        Day start = new Day(2009, Month.February, 17);
        int j = start.getDayOfWeek().intValue();
        for (int i = 0; i < w.length; ++i) {
            w[i] = d[j];
            if (++j == 7) {
                j = 0;
            }
        }
        smoother.setSsf(new SsfCalendarizationC(w));
        smoother.process(sx);
        sstates = smoother.calcSmoothedStates();
        double[] c2 = sstates.component(1);
        double[] c0 = sstates.component(0);
    }
}
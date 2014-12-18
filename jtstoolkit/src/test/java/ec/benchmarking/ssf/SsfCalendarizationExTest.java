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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfCalendarizationExTest {

    public SsfCalendarizationExTest() {
    }

    @Test
    public void testSomeMethod() {
        // 
        double[] x = new double[140];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }

        x[27] = 9000;
        x[55] = 5000;
        x[83] = 9500;
        x[111] = 7000;

        SsfData sx = new SsfData(x, null);
        int[] starts = new int[]{0, 28, 56, 84, 112};
        int[] astarts = new int[]{0, 11, 42, 72, 103, 133};
        Smoother smoother = new Smoother();
        smoother.setSsf(new SsfCalendarizationEx(starts, astarts, null));
        SmoothingResults sstates = new SmoothingResults(true, true);
        smoother.setCalcVar(true);
        smoother.process(sx, sstates);
        double[] c1 = sstates.component(1);
        double[] e1 = sstates.componentStdev(2);
        double[] q1 = sstates.componentStdev(1);
        DataBlock Z = new DataBlock(3);
        Z.set(1, 1);
        Z.set(2, 1);
        for (int i = 0; i < c1.length; ++i) {
            c1[i] = sstates.zcomponent(i, Z);
            q1[i] = Math.sqrt(sstates.zvariance(i, Z));
        }

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
        SsfCalendarizationEx ssf = new SsfCalendarizationEx(starts, astarts, w);
        smoother.setSsf(ssf);
        sstates = new SmoothingResults(true, true);
        smoother.process(sx, sstates);
        double[] c2 = sstates.component(1);
        double[] e2 = sstates.componentStdev(1);
        Z.set(1, 1);
        Z.set(2, 1);
        for (int i = 0; i < c2.length; ++i) {
            Z.set(2, w[i]);
            c2[i] = sstates.zcomponent(i, Z);
            e2[i] = Math.sqrt(sstates.zvariance(i, Z));
        }
    }
}
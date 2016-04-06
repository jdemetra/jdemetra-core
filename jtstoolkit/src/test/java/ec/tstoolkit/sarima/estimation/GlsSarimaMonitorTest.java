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

package ec.tstoolkit.sarima.estimation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class GlsSarimaMonitorTest {

    public GlsSarimaMonitorTest() {
    }

 //   @Test
    public void demo311011() {
        process311011(1, data.Data.P.internalStorage());
        long span=process311011(100, data.Data.P.internalStorage());
        System.out.println(span);
    }

    private long process311011(int N, double[] data) {
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        spec.setP(3);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            ec.tstoolkit.sarima.estimation.GlsSarimaMonitor monitor = new ec.tstoolkit.sarima.estimation.GlsSarimaMonitor();
            ec.tstoolkit.arima.estimation.RegArimaModel model = new ec.tstoolkit.arima.estimation.RegArimaModel(new ec.tstoolkit.sarima.SarimaModel(spec), new ec.tstoolkit.data.DataBlock(data));
            ec.tstoolkit.arima.estimation.RegArimaEstimation rslt = monitor.process(model);
        }
        long t1 = System.currentTimeMillis();
        return t1 - t0;
    }
}
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

package ec.satoolkit.diagnostics;

import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class QSTest  {

    private QSTest() {
    }
    
    public static StatisticalTest test(TsData data) {
        return compute(data.getValues().internalStorage(), data.getFrequency().intValue(),2);
    }

    public static StatisticalTest compute(double[] data, int lag, int k) {
        double qs = LjungBoxTest.calc(data, lag, k, true);
        Chi2 chi2 = new Chi2();
        chi2.setDegreesofFreedom(k);
        StatisticalTest test = new StatisticalTest(chi2, qs, TestType.Upper, true);
        test.setSignificanceThreshold(.01);
        if (!test.isValid()) {
            return null;
        }
        else {
            return test;
        }
    }

}

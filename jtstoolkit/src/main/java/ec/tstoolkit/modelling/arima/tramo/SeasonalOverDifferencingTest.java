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

package ec.tstoolkit.modelling.arima.tramo;

import ec.satoolkit.diagnostics.FTest;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SeasonalOverDifferencingTest {

    private static double THRESHOLD = -.7;

    public int test(ModellingContext context) {
        
        SarimaModel arima = context.estimation.getArima();
        SarimaSpecification spec = arima.getSpecification();
        if (spec.getFrequency() == 1)
            return 0;
        if (spec.getBP() != 0 || spec.getBD() != 1 || spec.getBQ() != 1 || arima.btheta(1) >= THRESHOLD) {
            return 0;
        }
        TsData lin = context.current(false).linearizedSeries(false);
        SeasonalityTests tests = SeasonalityTests.seasonalityTest(lin, 1, true, true);
        FTest ftest = new FTest();
        ftest.test(context.description);
        int score = tests.getScore();
        boolean fsign = ftest.getFTest().isSignificant();
        if (fsign) {
            ++score;
        }
        if (score >= 2 || fsign || tests.getQs().isSignificant()) {
            return 1;
        }
        else {
            return 2;
        }
    }
}

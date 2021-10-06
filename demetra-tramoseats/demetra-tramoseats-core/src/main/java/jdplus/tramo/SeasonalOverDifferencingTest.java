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
package jdplus.tramo;

import jdplus.sa.tests.SeasonalityTests;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.regular.SeasonalFTest;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
class SeasonalOverDifferencingTest {

    private static final double THRESHOLD = -.7;
    private static final double SIGNIF = 0.01;

    public int test(RegSarimaModelling context) {

        SarimaModel arima = context.getDescription().arima();
        SarimaOrders spec = arima.orders();
        if (spec.getPeriod() == 1) {
            return 0;
        }
        if (spec.getBp() == 1 || spec.getBd() == 0 || spec.getBq() == 0 || arima.btheta(1) >= THRESHOLD) {
            return 0;
        }
        DoubleSeq lin = context.getEstimation().linearizedSeries();
        SeasonalityTests tests = SeasonalityTests.seasonalityTest(lin, spec.getPeriod(), 1, true, true);
        SeasonalFTest ftest = new SeasonalFTest();
        ftest.test(context.getDescription());
        int score = tests.getScore();
        boolean fsign = ftest.getFTest().getPvalue() < SIGNIF;
        if (fsign) {
            ++score;
        }
        if (score >= 2 || fsign || tests.getQs().getPvalue()<SIGNIF) {
            return 1;
        } else {
            return 2;
        }
    }
}

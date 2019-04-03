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

package demetra.tramo;

import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaModelling;
import demetra.stats.tests.NiidTests;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public class ModelVerifier {

    private static final double OUT = .03, NORMAL = 6, SKEWNESS = 2.576, RUNS = 2.576, QSTAT = .05, QS = 6, MEAN = .01;

    public boolean accept(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        ModelEstimation estimation = context.getEstimation();
        int nz = desc.getSeries().getValues().count(x->Double.isFinite(x));
        if (desc.variables().filter(var->var.isOutlier(false)).count() > OUT * nz) {
            return false;
        }
        NiidTests niid = estimation.getTests();
        //join test on normality
        if (niid.normalityTest().getValue() > NORMAL) {
            return false;
        }
        // QStat
        if (niid.ljungBox().getPValue() < QSTAT) {
            return false;
        }
        // skewness
        if (niid.skewness().getValue() > SKEWNESS) {
            return false;
        }
        // runs
        if (niid.runsNumber().getValue() > RUNS) {
            return false;
        }
        // mean
        if (niid.meanTest().getPValue() < .01) {
            return false;
        }
        // qs
        int period = desc.getAnnualFrequency();
        if (period > 1) {
            if (niid.seasonalLjungBox().getValue() > QS) {
                return false;
            }
            // Seasonality
            DoubleSeq res = estimation.getConcentratedLikelihood().e();
            SeasonalityTests stests = SeasonalityTests.residualSeasonalityTest(res, period);
            if (stests.getScore() > 1) {
                return false;
            }
        }
        return true;
    }
}

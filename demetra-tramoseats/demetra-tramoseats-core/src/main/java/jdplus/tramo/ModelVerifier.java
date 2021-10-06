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
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.stats.tests.NiidTests;
import demetra.data.DoubleSeq;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.sarima.SarimaModel;


/**
 *
 * @author Jean Palate
 */
public class ModelVerifier {

    private static final double OUT = .03, NORMAL = 6, SKEWNESS = 2.576, RUNS = 2.576, QSTAT = .05, QS = 6, MEAN = .01;

    public boolean accept(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        RegArimaEstimation<SarimaModel> estimation = context.getEstimation();
        int nz = desc.getSeries().getValues().count(x->Double.isFinite(x));
        if (desc.variables().filter(var->ModellingUtility.isOutlier(var, true)).count() > OUT * nz) {
            return false;
        }
        int period=desc.getAnnualFrequency();
        NiidTests niid = NiidTests.builder()
                    .data(estimation.getConcentratedLikelihood().e())
                    .period(period)
                    .k(RegArimaUtility.defaultLjungBoxLength(period))
                    .ks(2)
                    .seasonal(period > 1)
                    .hyperParametersCount(estimation.parametersCount())
                    .build();
        //join test on normality
        if (niid.normalityTest().getValue() > NORMAL) {
            return false;
        }
        // QStat
        if (niid.ljungBox().getPvalue() < QSTAT) {
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
        if (niid.meanTest().getPvalue() < .01) {
            return false;
        }
        // qs
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

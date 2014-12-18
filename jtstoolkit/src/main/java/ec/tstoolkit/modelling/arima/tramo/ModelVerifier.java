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

import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class ModelVerifier {

    private static final double OUT = .03, NORMAL = 6, SKEWNESS = 2.576, RUNS = 2.576, QSTAT = .05, QS = 6, MEAN = .01;
    NiidTests niid;

    public boolean accept(ModellingContext context) {
        int nz = context.estimation.getRegArima().getObsCount();
        if (context.description.getOutliers().size() > OUT * nz) {
            return false;
        }
        niid = context.estimation.getNiidTests();
        //join test on normality
        if (niid.getNormalityTest().getValue() > NORMAL) {
            return false;
        }
        // QStat
        if (niid.getLjungBox().getPValue() < QSTAT) {
            return false;
        }
        // skewness
        if (niid.getSkewness().getValue() > SKEWNESS) {
            return false;
        }
        // runs
        if (niid.getRuns().getValue() > RUNS) {
            return false;
        }
        // mean
        if (niid.getMeanTest().getPValue() < .01) {
            return false;
        }
        // qs
        int ifreq = context.description.getFrequency();
        if (ifreq > 1) {
            if (niid.getSeasonalLjungBox().getValue() > QS) {
                return false;
            }
            // Seasonality
            SeasonalityTests stests = SeasonalityTests.residualSeasonalityTest(context.estimation.getLikelihood().getResiduals(),
                    TsFrequency.valueOf(ifreq));
            if (stests.getScore() > 1) {
                return false;
            }
        }
        return true;
    }
}

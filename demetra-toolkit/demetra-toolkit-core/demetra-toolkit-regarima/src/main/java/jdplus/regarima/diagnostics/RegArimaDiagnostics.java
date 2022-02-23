/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.regarima.diagnostics;

import demetra.data.DoubleSeq;
import demetra.timeseries.regression.ModellingUtility;
import demetra.timeseries.regression.Variable;
import java.util.Arrays;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.tests.OneStepAheadForecastingTest;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.sarima.SarimaModel;
import jdplus.stats.tests.NiidTests;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegArimaDiagnostics {

    private final int OUTOFSAMPLE_MIN = 5;

    /**
     *
     * @param regarima
     * @param forecastLength If 0, default length defined in
     * OutOfSampleDiagnostics is used
     * @return
     */
    public OneStepAheadForecastingTest oneStepAheadForecastingTest(final RegArimaModel<SarimaModel> regarima, final double forecastLength) {
        double len = forecastLength;
        if (len == 0) {
            len = OutOfSampleDiagnosticsConfiguration.getDefault().getOutOfSampleLength();
        }
        int ifreq = regarima.arima().getPeriod();
        int nback = (int) Math.round(len * ifreq);
        if (nback < OUTOFSAMPLE_MIN) {
            nback = OUTOFSAMPLE_MIN;
        }
        RegSarimaComputer processor = RegSarimaComputer.builder().build();
        return OneStepAheadForecastingTest.of(regarima, processor, nback);
    }

    public double outliersRatio(final RegArimaModel<SarimaModel> regarima, Variable[] variables, boolean all) {
        int n = regarima.getY().length() - regarima.getMissingValuesCount();
        double o;
        if (all) {
            o = Arrays.stream(variables)
                    .filter(var -> ModellingUtility.isOutlier(var))
                    .count();
        } else {
            o = Arrays.stream(variables)
                    .filter(var -> ModellingUtility.isOutlier(var))
                    .filter(var -> ModellingUtility.isAutomaticallyIdentified(var))
                    .count();
        }
        return o / n;
    }

    public NiidTests residualTests(DoubleSeq res, int period, int nhyperparameters) {
        try {
            return NiidTests.builder()
                    .data(res)
                    .period(period)
                    .k(RegArimaUtility.defaultLjungBoxLength(period))
                    .ks(2)
                    .seasonal(period > 1)
                    .hyperParametersCount(nhyperparameters)
                    .build();
        } catch (Exception ex) {
            return null;
        }
    }

}

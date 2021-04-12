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
package jdplus.regarima.diagnostics;

import jdplus.regarima.tests.OneStepAheadForecastingTest;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import java.util.Collections;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.sarima.SarimaModel;
import java.util.List;
import jdplus.regarima.RegArimaModel;

/**
 *
 * @author Jean Palate
 */
public class OutOfSampleDiagnostics implements Diagnostics {

    private final double ub;
    private final double bb;
    private double mpval, vpval;

    static OutOfSampleDiagnostics create(OutOfSampleDiagnosticsConfiguration config, RegArimaModel<SarimaModel> regarima) {
        try {
            if (regarima == null) {
                return null;
            } else {
                return new OutOfSampleDiagnostics(config, regarima);
            }

        } catch (Exception ex) {
            return null;
        }
    }

    private OutOfSampleDiagnostics(OutOfSampleDiagnosticsConfiguration config, RegArimaModel<SarimaModel> regarima) {
        // set the boundaries...
        bb = config.getBadThreshold();
        ub = config.getUncertainThreshold();
        test(regarima, config.getOutOfSampleLength(), config.isDiagnosticOnMean(), config.isDiagnosticOnVariance());
    }

    private void test(RegArimaModel<SarimaModel> regarima, double len, boolean m, boolean v) {
        int ifreq = regarima.arima().getPeriod();
        int nback = (int) (len * ifreq);
        if (nback < 5) {
            nback = 5;
        }
        RegSarimaComputer processor = RegSarimaComputer.builder().build();
        OneStepAheadForecastingTest<SarimaModel> xtest = new OneStepAheadForecastingTest<>(processor, nback);
        xtest.test(regarima);
        if (m) {
            mpval = xtest.outOfSampleMeanTest().getPvalue();
        } else {
            mpval = Double.NaN;
        }
        if (v) {
            vpval = xtest.sameVarianceTest().getPvalue();
        } else {
            vpval = Double.NaN;
        }
    }

    @Override
    public String getName() {
        return OutOfSampleDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return OutOfSampleDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double r = getValue(test);
        if (Double.isNaN(r)) {
            return ProcQuality.Undefined;
        }
        if (r < bb) {
            return ProcQuality.Bad;
        } else if (r < ub) {
            return ProcQuality.Uncertain;
        } else {
            return ProcQuality.Good;
        }
    }

    @Override
    public double getValue(String test) {
        switch (test) {
            case OutOfSampleDiagnosticsFactory.MEAN:
                return mpval;
            case OutOfSampleDiagnosticsFactory.MSE:
                return vpval;
            default:
                return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }
}

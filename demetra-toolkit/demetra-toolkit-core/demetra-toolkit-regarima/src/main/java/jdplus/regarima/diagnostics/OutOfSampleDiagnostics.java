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
import demetra.processing.ProcQuality;
import java.util.Collections;
import java.util.List;
import demetra.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
public class OutOfSampleDiagnostics implements Diagnostics {

    private final double ub;
    private final double bb;
    private double mpval, vpval;

    static OutOfSampleDiagnostics create(OutOfSampleDiagnosticsConfiguration config, OneStepAheadForecastingTest test) {
        try {
            if (test == null) {
                return null;
            } else {
                return new OutOfSampleDiagnostics(config, test);
            }

        } catch (Exception ex) {
            return null;
        }
    }

    private OutOfSampleDiagnostics(OutOfSampleDiagnosticsConfiguration config, OneStepAheadForecastingTest test) {
        // set the boundaries...
        bb = config.getBadThreshold();
        ub = config.getUncertainThreshold();
        test(test, config.isDiagnosticOnMean(), config.isDiagnosticOnVariance());
    }

    private void test(OneStepAheadForecastingTest xtest, boolean m, boolean v) {
        mpval = Double.NaN;
        vpval = Double.NaN;
        if (xtest != null) {
            if (m) {
                mpval = xtest.outOfSampleMeanTest().getPvalue();
            }
            if (v) {
                vpval = xtest.sameVarianceTest().getPvalue();
            }
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

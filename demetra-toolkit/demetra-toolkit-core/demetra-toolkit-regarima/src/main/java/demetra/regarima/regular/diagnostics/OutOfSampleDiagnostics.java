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
package demetra.regarima.regular.diagnostics;

import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.regarima.regular.PreprocessingModel;
import demetra.sarima.RegSarimaProcessor;
import demetra.sarima.SarimaModel;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class OutOfSampleDiagnostics implements Diagnostics {

    private final double ub;
    private final double bb;
    private double mpval, vpval;

    static OutOfSampleDiagnostics create(OutOfSampleDiagnosticsConfiguration config, PreprocessingModel pp) {
        try {
            if (pp == null) {
                return null;
            } else {
                return new OutOfSampleDiagnostics(config, pp);
            }

        } catch (Exception ex) {
            return null;
        }
    }

    private OutOfSampleDiagnostics(OutOfSampleDiagnosticsConfiguration config, PreprocessingModel rslts) {
        // set the boundaries...
        bb = config.getBadThreshold();
        ub = config.getUncertainThreshold();
        test(rslts, config.getOutOfSampleLength(), config.isDiagnosticOnMean(), config.isDiagnosticOnVariance());
    }

    private void test(PreprocessingModel rslts, double len, boolean m, boolean v) {
        int ifreq = rslts.getDescription().getAnnualFrequency();
        int nback = (int) (len * ifreq);
        if (nback < 5) {
            nback = 5;
        }
        RegSarimaProcessor processor = RegSarimaProcessor.builder().build();
        OneStepAheadForecastingTest<SarimaModel> xtest = new OneStepAheadForecastingTest<>(processor, nback);
        xtest.test(rslts.getDescription().regarima());
        if (m) {
            mpval = xtest.outOfSampleMeanTest().getPValue();
        } else {
            mpval = Double.NaN;
        }
        if (v) {
            vpval = xtest.sameVarianceTest().getPValue();
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
        return null;
    }
}

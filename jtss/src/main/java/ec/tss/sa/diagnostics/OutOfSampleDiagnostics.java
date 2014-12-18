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

package ec.tss.sa.diagnostics;

import ec.satoolkit.GenericSaResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class OutOfSampleDiagnostics implements IDiagnostics {

    public static final String MEAN = "mean", MSE = "mse";
    public static final String NAME = "out-of-sample";
    public static final List<String> DEF_TESTS = Arrays.asList(MEAN, MSE);
    private double ub_ = OutOfSampleDiagnosticsConfiguration.UNC;
    private double bb_ = OutOfSampleDiagnosticsConfiguration.BAD;
    private double mpval_, vpval_;

    static OutOfSampleDiagnostics create(OutOfSampleDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            if (rslts == null) {
                return null;
            } else {
                PreprocessingModel pp = GenericSaResults.getPreprocessingModel(rslts);
                if (pp == null ) {
                    return null;
                }
                return new OutOfSampleDiagnostics(config, pp);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private OutOfSampleDiagnostics(OutOfSampleDiagnosticsConfiguration config, PreprocessingModel rslts) {
        // set the boundaries...
        bb_ = config.getBad();
        ub_ = config.getUncertain();
        test(rslts, config.getForecastingLength(), config.isMeanTestEnabled(), config.isMSETestEnabled());
    }

    private void test(PreprocessingModel rslts, double len, boolean m, boolean v) {
        int ifreq = rslts.description.getFrequency();
        int nback = (int) (len * ifreq);
        if (nback < 5) {
            nback = 5;
        }
        OneStepAheadForecastingTest xtest = new OneStepAheadForecastingTest(nback);
        xtest.test(rslts.estimation.getRegArima());
        if (m) {
            mpval_ = xtest.outOfSampleMeanTest().getPValue();
        } else {
            mpval_ = Double.NaN;
        }
        if (v) {
            vpval_ = xtest.mseTest().getPValue();
        } else {
            vpval_ = Double.NaN;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTests() {
        return DEF_TESTS;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double r = getValue(test);
        if (Double.isNaN(r)) {
            return ProcQuality.Undefined;
        }
        if (r < bb_) {
            return ProcQuality.Bad;
        } else if (r < ub_) {
            return ProcQuality.Uncertain;
        } else {
            return ProcQuality.Good;
        }
    }

    @Override
    public double getValue(String test) {
        if (test.equals(DEF_TESTS.get(0))) {
            return mpval_;
        } else if (test.equals(DEF_TESTS.get(1))) {
            return vpval_;
        } else {
            return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }
}

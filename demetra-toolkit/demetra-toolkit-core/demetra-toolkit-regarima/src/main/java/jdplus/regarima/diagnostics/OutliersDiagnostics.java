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

import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import java.util.Arrays;
import java.util.List;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author Kristof Bayens
 */
public final class OutliersDiagnostics implements Diagnostics {

    private final OutliersDiagnosticsConfiguration config;
    private int n, prespecifiedOutliers, detectedOutliers;

    static OutliersDiagnostics create(ModelEstimation model, OutliersDiagnosticsConfiguration config) {
        try {
            if (model == null) {
                return null;
            } else {
                return new OutliersDiagnostics(model, config);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private OutliersDiagnostics(ModelEstimation model, OutliersDiagnosticsConfiguration config) {
        // set the boundaries...
        this.config = config;
        test(model);
    }

    private void test(ModelEstimation rslts) {
        TsData y = rslts.getOriginalSeries();
        if (y == null) {
            return;
        }
        n = y.length();
        
        prespecifiedOutliers = (int) Arrays.stream(rslts.getVariables()).filter(var -> var.isOutlier(true)).count();
        detectedOutliers = (int) Arrays.stream(rslts.getVariables()).filter(var -> var.isOutlier(false)).count();
    }

    @Override
    public String getName() {
        return OutliersDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return OutliersDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double r = getValue(test);
        if (Double.isNaN(r)) {
            return ProcQuality.Undefined;
        }
        if (r > config.getSevereThreshold()) {
            return ProcQuality.Severe;
        } else if (r > config.getBadThreshold()) {
            return ProcQuality.Bad;
        } else if (r > config.getUncertainThreshold()) {
            return ProcQuality.Uncertain;
        } else {
            return ProcQuality.Good;
        }
    }

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        if (!test.equals(OutliersDiagnosticsFactory.NUMBER)) {
            return val;
        }
        if (Double.isNaN(n)) {
            return val;
        }
        double r = prespecifiedOutliers + detectedOutliers;
        r /= n;
        val = r;
        return val;
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }
}

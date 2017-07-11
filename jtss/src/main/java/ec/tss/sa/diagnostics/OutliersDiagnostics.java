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
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public final class OutliersDiagnostics implements IDiagnostics {

    private double ub_ = OutliersDiagnosticsConfiguration.UNC;
    private double bb_ = OutliersDiagnosticsConfiguration.BAD;
    private double sb_ = OutliersDiagnosticsConfiguration.SEV;
    private int n_, p_, o_;

    static OutliersDiagnostics create(OutliersDiagnosticsConfiguration config, CompositeResults rslts)
    {
        try {
            if (rslts == null)
                return null;
            else{
                PreprocessingModel pp=GenericSaResults.getPreprocessingModel(rslts);
                if (pp == null)
                    return null;
                return new OutliersDiagnostics(config, pp);
            }
        }
        catch(Exception ex) {
            return null;
        }
    }

    private OutliersDiagnostics(OutliersDiagnosticsConfiguration config, PreprocessingModel rslts) {
        // set the boundaries...
        sb_ = config.getSevere();
        bb_ = config.getBad();
        ub_ = config.getUncertain();
        test(rslts);
    }

    private void test(PreprocessingModel rslts)
    {
        TsData y = rslts.description.getOriginal();
        if (y == null)
            return;
        n_ = y.getObsCount();
        p_ = rslts.description.getPrespecifiedOutliers().size();
        o_ = rslts.description.getOutliers().size();
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
        if (Double.isNaN(r))
            return ProcQuality.Undefined;
        if (r > sb_)
            return ProcQuality.Severe;
        else if (r > bb_)
            return ProcQuality.Bad;
        else if (r > ub_)
            return ProcQuality.Uncertain;
        else
            return ProcQuality.Good;
    }

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        if (!test.equals(OutliersDiagnosticsFactory.NUMBER))
            return val;
        if (Double.isNaN(n_))
            return val;
        double r = p_ + o_;
        r /= n_;
        val = r;
        return val;
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }
}

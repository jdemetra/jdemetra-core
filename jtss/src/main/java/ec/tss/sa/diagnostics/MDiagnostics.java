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
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class MDiagnostics implements IDiagnostics {

    public double[] stats_;
    private double bad_ = MDiagnosticsConfiguration.BAD, severe_ = MDiagnosticsConfiguration.SEVERE;
    private boolean all_ = true;

    public static MDiagnostics create(MDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            X11Results drslts = GenericSaResults.getDecomposition(rslts, X11Results.class);
            if (drslts == null) {
                return null;
            }

            return new MDiagnostics(config, rslts);
        } catch (Exception ex) {
            return null;
        }
    }

    protected MDiagnostics(MDiagnosticsConfiguration config, CompositeResults rslts) {
        bad_ = config.getBad();
        severe_ = config.getSevere();
        all_ = config.isUseAll();

        ISeriesDecomposition decomposition=GenericSaResults.getFinalDecomposition(rslts);
        X11Results x11=GenericSaResults.getDecomposition(rslts, X11Results.class);
        Mstatistics mstats = Mstatistics.computeFromX11(decomposition.getMode(), x11.getInformation());

        int nm = mstats.getMCount();
        stats_ = new double[nm + 2];
        for (int i = 0; i < nm; ++i) {
            stats_[i] = mstats.getM(i + 1);
        }
        stats_[nm++] = mstats.getQ();
        stats_[nm] = mstats.getQm2();
    }

    @Override
    public String getName() {
        return MDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return MDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double pval = getValue(test);
        if (Double.isNaN(pval)) {
            return ProcQuality.Undefined;
        }
        if (pval >= severe_) {
            return ProcQuality.Severe;
        } else if (pval >= bad_) {
            return ProcQuality.Bad;
        } else {
            return ProcQuality.Good;
        }
    }

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        if (test.equals(Mstatistics.Q)) {
            val = stats_[stats_.length - 2];
        }
        if (test.equals(Mstatistics.Q2)) {
            val = stats_[stats_.length - 1];
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }

    public double getBadThreshold() {
        return bad_;
    }

    public double getSevereThreshold() {
        return severe_;
    }

    public double[] getMStatistics() {
        return stats_;
    }
}

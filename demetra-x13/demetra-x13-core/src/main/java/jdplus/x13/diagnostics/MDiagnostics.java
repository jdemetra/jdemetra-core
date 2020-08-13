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
package jdplus.x13.diagnostics;

import jdplus.x13.Mstatistics;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import java.util.Collections;
import java.util.List;
import jdplus.x13.X13Results;

/**
 *
 * @author Jean Palate
 */
public class MDiagnostics implements Diagnostics {

    public double[] stats;
    private double bad = MDiagnosticsConfiguration.BAD, severe = MDiagnosticsConfiguration.SEVERE;
    private boolean all = true;

    public static MDiagnostics of(MDiagnosticsConfiguration config, X13Results rslts) {
        try {
            Mstatistics stats = rslts.getMstatistics();
            if (stats == null) {
                return null;
            }

            return new MDiagnostics(config, stats);
        } catch (Exception ex) {
            return null;
        }
    }

    private MDiagnostics(MDiagnosticsConfiguration config, Mstatistics mstats) {
        bad = config.getBadThreshold();
        severe = config.getSevereThreshold();
        all = config.isAll();

        int nm = mstats.getMCount();
        stats = new double[nm + 2];
        for (int i = 0; i < nm; ++i) {
            stats[i] = mstats.getM(i + 1);
        }
        stats[nm++] = mstats.getQ();
        stats[nm] = mstats.getQm2();
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
        if (pval >= severe) {
            return ProcQuality.Severe;
        } else if (pval >= bad) {
            return ProcQuality.Bad;
        } else {
            return ProcQuality.Good;
        }
    }

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        if (test.equals(MDiagnosticsFactory.Q)) {
            val = stats[stats.length - 2];
        }
        if (test.equals(MDiagnosticsFactory.Q2)) {
            val = stats[stats.length - 1];
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }

    public double getBadThreshold() {
        return bad;
    }

    public double getSevereThreshold() {
        return severe;
    }

    public double[] getMStatistics() {
        return stats;
    }
}

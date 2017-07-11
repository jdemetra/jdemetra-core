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
import ec.satoolkit.ISaResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.analysis.SpectralDiagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SpectralDiagnostics implements IDiagnostics {

    private boolean sorig_, ssa_, sirr_;
    private boolean tdsa_, tdirr_;
    private boolean strict_;
    private boolean processed_;

    protected static SpectralDiagnostics create(SpectralDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            if (rslts == null) {
                return null;
            } else {
                return new SpectralDiagnostics(config, rslts);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public SpectralDiagnostics(SpectralDiagnosticsConfiguration config, CompositeResults rslts) {
        processed_=test(rslts, config.getSensitivity(), config.getLength(), config.isStrict());
    }

    private boolean test(CompositeResults rslt, double sens, int len, boolean strict) {
         strict_ = strict;
        try {
            if (rslt == null || GenericSaResults.getDecomposition(rslt, ISaResults.class) == null) {
                return false;
            }
            boolean r=false;
            TsPeriodSelector sel = new TsPeriodSelector();
            SpectralDiagnostic diag = new SpectralDiagnostic();
            diag.setSensitivity(sens);
            TsData s = rslt.getData(ModellingDictionary.Y_LIN, TsData.class);
            if (s != null) {
                s = s.delta(1);
                if (len != 0) {
                    sel.last(len * s.getFrequency().intValue());
                    s = s.select(sel);
                }
                diag.setARLength(s.getFrequency() == TsFrequency.Monthly ? 30 : 3 * s.getFrequency().intValue());
                if (diag.test(s)) {
                     sorig_ = diag.hasSeasonalPeaks();
                }
            }
            s = rslt.getData(ModellingDictionary.SA, TsData.class);
            if (s != null) {
                int del = Math.max(1, s.getFrequency().intValue() / 4);
                s = s.delta(del);
                if (len != 0) {
                    sel.last(len * s.getFrequency().intValue());
                    s = s.select(sel);
                }
                diag.setARLength(s.getFrequency() == TsFrequency.Monthly ? 30 : 3 * s.getFrequency().intValue());
                if (diag.test(s)) {
                    r=true;
                    ssa_ = diag.hasSeasonalPeaks();
                    if (s.getFrequency() == TsFrequency.Monthly) {
                        tdsa_ = diag.hasTradingDayPeaks();
                    }
                }
            }
            s = rslt.getData(ModellingDictionary.I, TsData.class);
            if (s != null) {
                if (len != 0) {
                    r=true;
                    sel.last(len * s.getFrequency().intValue());
                    s = s.select(sel);
                }
                diag.setARLength(s.getFrequency() == TsFrequency.Monthly ? 30 : 3 * s.getFrequency().intValue());
                if (diag.test(s)) {
                    r=true;
                    sirr_ = diag.hasSeasonalPeaks();
                    if (s.getFrequency() == TsFrequency.Monthly) {
                        tdirr_ = diag.hasTradingDayPeaks();
                    }
                }
            }
            return r;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getName() {
        return SpectralDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return SpectralDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (! processed_)
            return ProcQuality.Undefined;
        if (test.equals(SpectralDiagnosticsFactory.SEAS)) {
            if (!sirr_ && !ssa_) {
                return ProcQuality.Good;
            } else if (sirr_ && ssa_) {
                return ProcQuality.Severe;
            } else {
                return strict_ ? ProcQuality.Severe : ProcQuality.Bad;
            }
        }
        if (test.equals(SpectralDiagnosticsFactory.TD)) {
            if (!tdirr_ && !tdsa_) {
                return ProcQuality.Good;
            } else if (tdirr_ && tdsa_) {
                return ProcQuality.Severe;
            } else {
                return strict_ ? ProcQuality.Severe : ProcQuality.Bad;
            }
        }
        return ProcQuality.Undefined;
    }

    @Override
    public double getValue(String test) {
        return 0;
    }

    @Override
    public List<String> getWarnings() {
        if (!sorig_) {
            return Collections.singletonList("No seasonal peak in the original differenced series");
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}

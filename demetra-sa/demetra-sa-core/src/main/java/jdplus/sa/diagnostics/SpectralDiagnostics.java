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
package jdplus.sa.diagnostics;

import demetra.information.Explorable;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.Dictionary;
import java.util.Collections;
import java.util.List;
import jdplus.modelling.regular.tests.SpectralAnalysis;

/**
 *
 */
public class SpectralDiagnostics implements Diagnostics {

    private boolean sorig, ssa, sirr;
    private boolean tdsa, tdirr;
    private boolean strict;

    protected static SpectralDiagnostics of(SpectralDiagnosticsConfiguration config, Explorable rslts) {
        try {
            if (rslts == null) {
                return null;
            }
            SpectralDiagnostics diags = new SpectralDiagnostics();
            if (diags.test(rslts, config.getSensibility(), config.getLength(), config.isStrict())) {
                return diags;
            } else {
                return null;
            }

        } catch (Exception ex) {
            return null;
        }
    }

    private String decompositionItem(String key) {
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    private boolean test(Explorable rslts, double sens, int len, boolean strict) {
        this.strict = strict;
        try {
            boolean r = false;
            TsData s = rslts.getData(decompositionItem(SaDictionaries.Y_LIN), TsData.class);
            if (s != null) {
                int sfreq = s.getAnnualFrequency();
                s = s.delta(1);
                if (len != 0) {
                    TimeSelector sel = TimeSelector.last(len * sfreq);
                    s = s.select(sel);
                }
                SpectralAnalysis diag = SpectralAnalysis.test(s)
                        .sensibility(sens)
                        .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                        .build();

                if (diag != null) {
                    sorig = diag.hasSeasonalPeaks();
                    r = true;
                }
            }
            s = rslts.getData(SaDictionaries.SA, TsData.class);
            if (s != null) {
                int sfreq = s.getAnnualFrequency();
                int del = Math.max(1, sfreq / 4);
                s = s.delta(del);
                if (len != 0) {
                    TimeSelector sel = TimeSelector.last(len * sfreq);
                    s = s.select(sel);
                }
                SpectralAnalysis diag = SpectralAnalysis.test(s)
                        .sensibility(sens)
                        .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                        .build();
                if (diag != null) {
                    r = true;
                    ssa = diag.hasSeasonalPeaks();
                    if (sfreq == 12) {
                        tdsa = diag.hasTradingDayPeaks();
                    }
                }
            }
            s = rslts.getData(SaDictionaries.SA, TsData.class);
            if (s != null) {
                int sfreq = s.getAnnualFrequency();
                if (len != 0) {
                    r = true;
                    TimeSelector sel = TimeSelector.last(len * sfreq);
                    s = s.select(sel);
                }
                SpectralAnalysis diag = SpectralAnalysis.test(s)
                        .sensibility(sens)
                        .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                        .build();
                if (diag != null) {
                    r = true;
                    sirr = diag.hasSeasonalPeaks();
                    if (sfreq == 12) {
                        tdirr = diag.hasTradingDayPeaks();
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
        if (test.equals(SpectralDiagnosticsFactory.SEAS)) {
            if (!sirr && !ssa) {
                return ProcQuality.Good;
            } else if (sirr && ssa) {
                return ProcQuality.Severe;
            } else {
                return strict ? ProcQuality.Severe : ProcQuality.Bad;
            }
        }
        if (test.equals(SpectralDiagnosticsFactory.TD)) {
            if (!tdirr && !tdsa) {
                return ProcQuality.Good;
            } else if (tdirr && tdsa) {
                return ProcQuality.Severe;
            } else {
                return strict ? ProcQuality.Severe : ProcQuality.Bad;
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
        if (!sorig) {
            return Collections.singletonList("No seasonal peak in the original differenced series");
        } else {
            return Collections.emptyList();
        }
    }
}

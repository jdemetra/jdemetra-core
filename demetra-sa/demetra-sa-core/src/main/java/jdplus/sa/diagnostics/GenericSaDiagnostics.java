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
package jdplus.sa.diagnostics;

import demetra.information.Explorable;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import jdplus.regarima.diagnostics.RegArimaDiagnostics;
import jdplus.regarima.tests.OneStepAheadForecastingTest;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.sa.tests.SeasonalityTests;

/**
 *
 * @author palatej
 */
@lombok.Getter
public class GenericSaDiagnostics implements Explorable{

    private final RegSarimaModel regarima;
    private final boolean mul;

    private final TsData linearized, residuals;
    private final TsData sa, irr, si, s, t;

    @lombok.Builder(builderClassName = "Builder")
    private GenericSaDiagnostics(RegSarimaModel regarima, boolean mul,
            TsData lin, TsData res, TsData sa, TsData irr, TsData si, TsData s, TsData t) {
        this.regarima = regarima;
        this.mul = mul;
        this.linearized = lin;
        this.residuals = res;
        this.sa = sa;
        this.irr = irr;
        this.si = si;
        this.s = s;
        this.t = t;
    }

    private volatile SeasonalityTests ytests, rtests, satests, itests;
    private volatile OneStepAheadForecastingTest outOfSampleTest;
    private volatile CombinedSeasonality seasSI, seasSa, seasI, seasRes, seasSa3, seasI3, seasRes3;
    
    public int annualFrequency(){
        return linearized.getAnnualFrequency();
    }

    public SeasonalityTests resTests() {
        if (residuals == null) {
            return null;
        }
        SeasonalityTests tests = rtests;
        if (tests == null) {
            synchronized (this) {
                tests = rtests;
                if (tests == null) {
                    tests = SeasonalityTests.seasonalityTest(residuals.getValues(), residuals.getAnnualFrequency(), 0, false, true);
                    rtests = tests;
                }
            }
        }
        return tests;
    }

    public SeasonalityTests yTests() {
        SeasonalityTests tests = ytests;
        if (tests == null) {
            synchronized (this) {
                tests = ytests;
                if (tests == null) {
                    tests = SeasonalityTests.seasonalityTest(linearized.getValues(), linearized.getAnnualFrequency(), 1, true, true);
                    ytests = tests;
                }
            }
        }
        return tests;
    }

    public SeasonalityTests saTests() {
        SeasonalityTests tests = satests;
        if (tests == null) {
            synchronized (this) {
                tests = satests;
                if (tests == null) {
                    tests = SeasonalityTests.seasonalityTest(sa.getValues(), sa.getAnnualFrequency(), 1, true, true);
                    satests = tests;
                }
            }
        }
        return tests;
    }

    public SeasonalityTests irrTests() {
        SeasonalityTests tests = itests;
        if (tests == null) {
            synchronized (this) {
                tests = itests;
                if (tests == null) {
                    tests = SeasonalityTests.seasonalityTest(irr.getValues(), irr.getAnnualFrequency(), 0, true, true);
                    itests = tests;
                }
            }
        }
        return tests;
    }

    public CombinedSeasonality csiTest() {
        CombinedSeasonality cs = seasSI;
        if (cs == null) {
            synchronized (this) {
                cs = seasSI;
                if (cs == null) {
                    cs = CombinedSeasonality.of(si, mul);
                    seasSI = cs;
                }
            }
        }
        return cs;
    }

    public CombinedSeasonality csaTest(boolean last) {
        if (last) {
            CombinedSeasonality cs = seasSa3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasSa3;
                    if (cs == null) {
                        int freq = sa.getAnnualFrequency();
                        TsData ts = sa.delta(Math.max(1, freq / 4));
                        TimeSelector sel = TimeSelector.last(freq * 3);
                        cs = CombinedSeasonality.of(ts.select(sel), mul);
                        seasSa3 = cs;
                    }
                }
            }
            return cs;
        } else {
            CombinedSeasonality cs = seasSa;
            if (cs == null) {
                synchronized (this) {
                    cs = seasSa;
                    if (cs == null) {
                        int freq = sa.getAnnualFrequency();
                        TsData ts = sa.delta(Math.max(1, freq / 4));
                        cs = CombinedSeasonality.of(ts, mul);
                        seasSa = cs;
                    }
                }
            }
            return cs;
        }
    }

    public CombinedSeasonality cresTest(boolean last) {
        if (residuals == null) {
            return null;
        }
        int freq = residuals.getAnnualFrequency();
        if (residuals.length() < 3 * freq) {
            return null;
        }
        if (last) {
            CombinedSeasonality cs = seasRes3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasRes3;
                    if (cs == null) {
                        TimeSelector sel = TimeSelector.last(freq * 3);
                        cs = CombinedSeasonality.of(residuals.select(sel), mul);
                        seasRes3 = cs;
                    }
                }
            }
            return cs;
        } else {
            CombinedSeasonality cs = seasRes;
            if (cs == null) {
                synchronized (this) {
                    cs = seasRes;
                    if (cs == null) {
                        cs = CombinedSeasonality.of(residuals, mul);
                        seasRes = cs;
                    }
                }
            }
            return cs;
        }
    }

    public CombinedSeasonality ciTest(boolean last) {
        if (last) {
            CombinedSeasonality cs = seasI3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasI3;
                    if (cs == null) {
                        int freq = irr.getAnnualFrequency();
                        TimeSelector sel = TimeSelector.last(freq * 3);
                        cs = CombinedSeasonality.of(irr.select(sel), mul);
                        seasI3 = cs;
                    }
                }
            }
            return cs;
        } else {
            CombinedSeasonality cs = seasI;
            if (cs == null) {
                synchronized (this) {
                    cs = seasI;
                    if (cs == null) {
                        cs = CombinedSeasonality.of(irr, mul);
                        seasI = cs;
                    }
                }
            }
            return cs;
        }
    }

    public OneStepAheadForecastingTest forecastingTest() {
        if (regarima == null) {
            return null;
        }
        OneStepAheadForecastingTest os = outOfSampleTest;
        if (os == null) {
            synchronized (this) {
                os = outOfSampleTest;
                if (os == null) {
                    try {
                        os = RegArimaDiagnostics.oneStepAheadForecastingTest(regarima.regarima(), 0);
                        outOfSampleTest = os;
                    } catch (Exception err) {

                    }
                }
            }
        }
        return os;
    }

}

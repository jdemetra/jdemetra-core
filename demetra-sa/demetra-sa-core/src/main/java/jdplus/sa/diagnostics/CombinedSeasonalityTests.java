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

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.linearfilters.SymmetricFiltersFactory;
import jdplus.sa.tests.CombinedSeasonality;
import lombok.AccessLevel;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
public class CombinedSeasonalityTests {

    private final boolean mul;
    private final CombinedSeasonalityOptions options;
    /**
     * Linearized series (in levels  =  not log-transformed)
     */
    private final TsData y, sa, irr, si;
    private final TsData residuals;

    @lombok.Builder(builderClassName = "Builder")
    private CombinedSeasonalityTests(TsData y, TsData sa, TsData irr, TsData si, TsData residuals, boolean mul, CombinedSeasonalityOptions options) {
        this.mul = mul;
        this.y = y;
        this.sa = sa;
        this.irr = irr;
        this.si = si;
        this.residuals = residuals;
        this.options = options;
    }

    private TsData detrend(TsData s) {
        SymmetricFilter filter = SymmetricFiltersFactory.makeSymmetricFilter(s.getAnnualFrequency());
        int ndrop = filter.length() / 2;

        double[] x = new double[s.length() - 2 * ndrop];
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(s.getValues(), out);
        if (mul) {
            out.apply(s.getValues().drop(ndrop, ndrop), (a, b) -> b / a);
        } else {
            out.apply(s.getValues().drop(ndrop, ndrop), (a, b) -> b - a);
        }
        return TsData.of(s.getStart().plus(ndrop), out);
    }

    @lombok.Getter(AccessLevel.PRIVATE)
    private volatile CombinedSeasonality seasLinearized, seasSI, seasSa, seasI, seasRes, seasSI3, seasSa3, seasI3, seasRes3;

    public CombinedSeasonality linearizedTest() {
        CombinedSeasonality cs = seasLinearized;
        if (cs == null) {
            synchronized (this) {
                cs = seasLinearized;
                if (cs == null) {
                    // we apply a de-trending similar to the X11 one (freq*2)
                    TsData osi = detrend(y);
                    cs = SaDiagnosticsUtility.combinedSeasonalityTest(osi, 0, mul ? 1 : 0, false);
                    seasLinearized = cs;
                }
            }
        }
        return cs;
    }

    public CombinedSeasonality siTest(boolean last) {
        if (last) {
            CombinedSeasonality cs = seasSI3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasSI3;
                    if (cs == null) {
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(si, options.getLastYears(), mul ? 1 : 0, false);
                        seasSI3 = cs;
                    }
                }
            }
            return cs;
        } else {
            CombinedSeasonality cs = seasSI;
            if (cs == null) {
                synchronized (this) {
                    cs = seasSI;
                    if (cs == null) {
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(si, 0, mul ? 1 : 0, false);
                        seasSI = cs;
                    }
                }
            }
            return cs;
        }
    }

    public CombinedSeasonality saTest(boolean last) {
        if (last) {
            CombinedSeasonality cs = seasSa3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasSa3;
                    if (cs == null) {
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(sa, options.getLastYears(), 0, true);
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
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(sa, 0, 0, true);
                        seasSa = cs;
                    }
                }
            }
            return cs;
        }
    }

    public CombinedSeasonality residualsTest(boolean last) {
        if (residuals == null) {
            return null;
        }
        if (last) {
            CombinedSeasonality cs = seasRes3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasRes3;
                    if (cs == null) {
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(residuals, options.getLastYears(), 0, false);
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
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(residuals, 0, 0, false);
                        seasRes = cs;
                    }
                }
            }
            return cs;
        }
    }

    public CombinedSeasonality irrTest(boolean last) {
        if (last) {
            CombinedSeasonality cs = seasI3;
            if (cs == null) {
                synchronized (this) {
                    cs = seasI3;
                    if (cs == null) {
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(irr, options.getLastYears(), mul ? 1 : 0, false);
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
                        cs = SaDiagnosticsUtility.combinedSeasonalityTest(irr, 0, mul ? 1 : 0, false);
                        seasI = cs;
                    }
                }
            }
            return cs;
        }
    }
}

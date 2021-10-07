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

import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import jdplus.modelling.regular.tests.TradingDaysTest;
import jdplus.sa.tests.FTest;

/**
 *
 * @author palatej
 */
@lombok.Getter
public class ResidualTradingDaysTests {

    private final TsData sa, irr, residuals;

    private final ResidualTradingDaysTestsOptions options;

    @lombok.Builder(builderClassName = "Builder")
    private ResidualTradingDaysTests(TsData sa, TsData irr, TsData residuals, ResidualTradingDaysTestsOptions options) {
        this.sa = sa;
        this.irr = irr;
        this.residuals = residuals;
        this.options = options;
    }

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile StatisticalTest saTest, irrTest, residualsTest, saLastTest, irrLastTest, residualsLastTest;

    public int annualFrequency() {
        return sa.getAnnualFrequency();
    }

    public StatisticalTest saTest(boolean last) {
        if (last && options.getFlast() > 0) {
            StatisticalTest test = saLastTest;
            if (test == null) {
                synchronized (this) {
                    test = saLastTest;
                    if (test == null) {
                        test = td(sa, options.getFlast(), 1);
                        saLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = saTest;
            if (test == null) {
                synchronized (this) {
                    test = saTest;
                    if (test == null) {
                        test = td(sa, 0, 1);
                        saTest = test;
                    }
                }
            }
            return test;
        }
    }

    public StatisticalTest irrTest(boolean last) {
        if (last && options.getFlast() > 0) {
            StatisticalTest test = irrLastTest;
            if (test == null) {
                synchronized (this) {
                    test = irrLastTest;
                    if (test == null) {
                        test = td(irr, options.getFlast(), 1);
                        irrLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = irrTest;
            if (test == null) {
                synchronized (this) {
                    test = irrTest;
                    if (test == null) {
                        test = td(irr, 0, 1);
                        saTest = test;
                    }
                }
            }
            return test;
        }
    }

    public StatisticalTest residualsTest(boolean last) {
        if (last && options.getFlast() > 0) {
            StatisticalTest test = residualsLastTest;
            if (test == null) {
                synchronized (this) {
                    test = residualsLastTest;
                    if (test == null) {
                        test = td(irr, options.getFlast(), 0);
                        residualsLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = residualsTest;
            if (test == null) {
                synchronized (this) {
                    test = residualsTest;
                    if (test == null) {
                        test = td(irr, 0, 0);
                        residualsTest = test;
                    }
                }
            }
            return test;
        }
    }

    private StatisticalTest td(TsData s, int ny, int lag) {
        int ifreq = annualFrequency();
        if (ny > 0) {
            s = s.drop(Math.max(0, s.length() - ifreq * ny), 0);
        }
        return TradingDaysTest.olsTest(s, lag);
    }
}

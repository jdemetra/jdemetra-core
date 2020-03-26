/*
 * Copyright 2013-2014 National Bank of Belgium
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

import demetra.data.DoubleSeq;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.processing.ProcResults;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.modelling.regular.tests.TradingDaysTest;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.tests.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public class ResidualTradingDaysDiagnostics implements Diagnostics {

    private StatisticalTest f_sa, f_i;
    private double sev, bad, unc;

    private static final double E_LIMIT = .005;

    private static boolean isMultiplicative(ProcResults rslts) {
        DecompositionMode mul = rslts.getData(SaDictionary.MODE, DecompositionMode.class);
        return mul != null && mul.isMultiplicative();
    }

    private static boolean isSignificant(DoubleSeq s, DoubleSeq ref) {
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        DescriptiveStatistics refdesc = DescriptiveStatistics.of(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > E_LIMIT;
    }

    private static boolean isSignificant(DoubleSeq i) {
        if (i == null) {
            return false;
        }
        DescriptiveStatistics idesc = DescriptiveStatistics.of(i);
        double se = idesc.getStdev();
        return se > E_LIMIT;
    }

    static Diagnostics create(ProcResults rslts, ResidualTradingDaysDiagnosticsConfiguration config) {
        try {
            ResidualTradingDaysDiagnostics test = new ResidualTradingDaysDiagnostics();
            TsData sa = rslts.getData(SaDictionary.SA_CMP, TsData.class);
            TsData i = rslts.getData(SaDictionary.I_CMP, TsData.class);
            if (sa == null && i == null) {
                return null;
            }
            int ny = config.getSpanInYears();
            boolean ar = config.isArModel();
            boolean mul = isMultiplicative(rslts);
            boolean isignif = mul ? isSignificant(i.getValues()) : (sa != null && i != null) ? isSignificant(i.getValues(), sa.getValues()) : true;
            if (sa != null) {
                if (mul) {
                    sa = sa.log();
                }
                int ifreq = sa.getAnnualFrequency();
                if (ar) {
                    TsData salast = sa;
                    if (ny != 0) {
                        salast = sa.drop(Math.max(0, sa.length() - ifreq * ny - 1), 0);
                    }
                    test.f_sa = TradingDaysTest.olsTest2(salast);
                } else {
                    TsData salast = sa.delta(1);
                    if (ny != 0) {
                        salast = sa.drop(Math.max(0, sa.length() - ifreq * ny), 0);
                    }
                    test.f_sa = TradingDaysTest.olsTest(salast);
                }
            }
            if (i != null && isignif) {
                if (mul) {
                    i = i.log();
                }
                int ifreq = i.getAnnualFrequency();
                TsData ilast = i;
                if (ar) {
                    if (ny != 0) {
                        ilast = i.drop(Math.max(0, i.length() - ifreq * ny - 1), 0);
                    }
                    test.f_i = TradingDaysTest.olsTest2(ilast);

                } else {
                    if (ny != 0) {
                        ilast = i.drop(Math.max(0, i.length() - ifreq * ny), 0);
                    }
                    test.f_i = TradingDaysTest.olsTest(ilast);
                }
            }
            test.sev = config.getSevereThreshold();
            test.bad = config.getBadThreshold();
            test.unc = config.getUncertainThreshold();
            return test;
        } catch (Exception err) {
            return null;
        }
    }

    @Override
    public String getName() {
        return ResidualTradingDaysDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        ArrayList<String> tests = new ArrayList<>();
        if (f_sa != null) {
            tests.add(ResidualTradingDaysDiagnosticsFactory.FTEST_SA);
        }
        if (f_i != null) {
            tests.add(ResidualTradingDaysDiagnosticsFactory.FTEST_I);
        }
        return tests;
    }

    @Override
    public ProcQuality getDiagnostic(String test
    ) {
        switch (test) {
            case ResidualTradingDaysDiagnosticsFactory.FTEST_SA:
                return quality(f_sa);

            case ResidualTradingDaysDiagnosticsFactory.FTEST_I:
                return quality(f_i);

            default:
                return ProcQuality.Undefined;
        }
    }

    @Override
    public double getValue(String test) {

        switch (test) {

            case ResidualTradingDaysDiagnosticsFactory.FTEST_SA:
                return pvalue(f_sa);
            case ResidualTradingDaysDiagnosticsFactory.FTEST_I:
                return pvalue(f_i);

            default:
                return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }

    private ProcQuality quality(StatisticalTest test) {
        if (test == null) {
            return ProcQuality.Undefined;
        }
        double pval = test.getPValue();
        if (pval < sev) {
            return ProcQuality.Severe;
        } else if (pval < bad) {
            return ProcQuality.Bad;
        } else if (pval < unc) {
            return ProcQuality.Uncertain;
        } else {
            return ProcQuality.Good;
        }
    }

    private double pvalue(StatisticalTest test) {
        return test == null ? Double.NaN : test.getPValue();
    }


}

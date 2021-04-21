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
import nbbrd.design.Development;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.modelling.DifferencingResults;
import jdplus.stats.DescriptiveStatistics;
import jdplus.sa.tests.FTest;
import jdplus.sa.tests.Qs;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class AdvancedResidualSeasonalityDiagnostics implements Diagnostics {
    
    @lombok.Value
    public static class Input{
        
        /**
         * Multiplicative decomposition
         */
        boolean multiplicative;
        
        /**
         * Seasonally adjusted series, linearized, level
         */
        TsData sa;
        
        /**
         * Irregular component, linearized, level (around 0 or 1)
         */
        TsData irregular;
    }

    private static final double E_LIMIT = .005;
    private StatisticalTest qs_sa, qs_i, f_sa, f_i;
    private double sev, bad, unc;

    private static boolean isSignificant(DoubleSeq s, DoubleSeq ref, double limit) {
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        DescriptiveStatistics refdesc = DescriptiveStatistics.of(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > limit;
    }

    private static boolean isSignificant(DoubleSeq i) {
        if (i == null) {
            return false;
        }
        DescriptiveStatistics idesc = DescriptiveStatistics.of(i);
        double se = idesc.getStdev();
        return se > E_LIMIT;
    }

    public static AdvancedResidualSeasonalityDiagnostics of(AdvancedResidualSeasonalityDiagnosticsConfiguration config, Input data) {
        try {
            AdvancedResidualSeasonalityDiagnostics test = new AdvancedResidualSeasonalityDiagnostics();
            TsData sa = data.getSa();
            TsData i = data.getIrregular();
            boolean mul=data.isMultiplicative();
            if (sa == null && i == null) {
                return null;
            }
            boolean isignif = mul ? isSignificant(i.getValues()) : (sa != null && i != null) ? isSignificant(i.getValues(), sa.getValues(), E_LIMIT) : true;
            if (config.isQs()) {
                int ny = config.getQslast();
                if (sa != null) {
                    TsData sac = sa;
                    if (mul) {
                        sac = sac.log();
                    }
                    int ifreq = sac.getAnnualFrequency();
                    TsData salast = sac;
                    if (ny != 0) {
                        salast = sac.drop(Math.max(0, sac.length() - ifreq * ny - 1), 0);
                    }
                    DifferencingResults dsa = DifferencingResults.of(salast.getValues(), salast.getAnnualFrequency(), -1, true);
                    if (mul ? isSignificant(dsa.getDifferenced()) : isSignificant(dsa.getDifferenced(), salast.getValues(), E_LIMIT)) {
                        test.qs_sa = new Qs(dsa.getDifferenced(), ifreq)
                                .autoCorrelationsCount(2)
                                .build();
                    }
                }
                if (i != null && isignif) {
                    TsData ic = i;
                    if (mul) {
                        ic = ic.log();
                    }
                    int ifreq = ic.getAnnualFrequency();
                    TsData ilast = ic;
                    if (ny != 0) {
                        ilast = ic.drop(Math.max(0, ic.length() - ifreq * ny), 0);
                    }
                    DifferencingResults di = DifferencingResults.of(ilast.getValues(), ilast.getAnnualFrequency(), -1, true);
                    test.qs_i = new Qs(di.getDifferenced(), ifreq)
                            .autoCorrelationsCount(2)
                            .build();
                }
            }
            if (config.isFtest()) {
                int ny = config.getFlast();
                if (sa != null) {
                    TsData sac = sa;
                    if (mul) {
                        sac = sac.log();
                    }
                    int ifreq = sac.getAnnualFrequency();
                    test.f_sa=new FTest(sac.getValues(), ifreq)
                            .model(FTest.Model.AR)
                            .ncycles(ny)
                            .build();
                }
                if (i != null && isignif) {
                    TsData ic = i;
                    if (mul) {
                        ic = ic.log();
                    }
                    int ifreq = ic.getAnnualFrequency();
                    test.f_i=new FTest(ic.getValues(), ifreq)
                            .model(FTest.Model.AR)
                            .ncycles(ny)
                            .build();
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
        return AdvancedResidualSeasonalityDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        ArrayList<String> tests = new ArrayList<>();
        if (qs_sa != null) {
            tests.add(AdvancedResidualSeasonalityDiagnosticsFactory.QS_SA);
        }
        if (f_sa != null) {
            tests.add(AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_SA);
        }
        if (qs_i != null) {
            tests.add(AdvancedResidualSeasonalityDiagnosticsFactory.QS_I);
        }
        if (f_i != null) {
            tests.add(AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_I);
        }
        return tests;
    }

    @Override
    public ProcQuality getDiagnostic(String test
    ) {
        switch (test) {
            case AdvancedResidualSeasonalityDiagnosticsFactory.QS_SA:
                return quality(qs_sa);
            case AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_SA:
                return quality(f_sa);
            case AdvancedResidualSeasonalityDiagnosticsFactory.QS_I:
                return quality(qs_i);
            case AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_I:
                return quality(f_i);

            default:
                return ProcQuality.Undefined;
        }
    }

    @Override
    public double getValue(String test
    ) {
        switch (test) {
            case AdvancedResidualSeasonalityDiagnosticsFactory.QS_SA:
                return pvalue(qs_sa);
            case AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_SA:
                return pvalue(f_sa);
            case AdvancedResidualSeasonalityDiagnosticsFactory.QS_I:
                return pvalue(qs_i);
            case AdvancedResidualSeasonalityDiagnosticsFactory.FTEST_I:
                return pvalue(f_i);
            default:
                return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }
    
    public StatisticalTest QsTestOnSa(){
        return qs_sa;
    }

    public StatisticalTest QsTestOnI(){
        return qs_i;
    }

    public StatisticalTest FTestOnSa(){
        return f_sa;
    }

    public StatisticalTest FTestOnI(){
        return f_i;
    }

    private ProcQuality quality(StatisticalTest test) {
        if (test == null) {
            return ProcQuality.Undefined;
        }
        double pval = test.getPvalue();
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
        return test == null ? Double.NaN : test.getPvalue();
    }

}

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

import nbbrd.design.Development;
import demetra.processing.ProcQuality;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import demetra.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class AdvancedResidualSeasonalityDiagnostics implements Diagnostics {
    
    private StatisticalTest qs_sa, qs_i, f_sa, f_i;
    private double sev, bad, unc;

    public static AdvancedResidualSeasonalityDiagnostics of(AdvancedResidualSeasonalityDiagnosticsConfiguration config, GenericSaTests data) {
        try {
            AdvancedResidualSeasonalityDiagnostics test = new AdvancedResidualSeasonalityDiagnostics();
            ResidualSeasonalityTests rsa = data.residualSeasonalityTestsOnSa();
            ResidualSeasonalityTests ri = data.residualSeasonalityTestsOnIrregular();
            if (rsa == null || ri == null)
                return null;
            TsData sa = rsa.getSeries();
            TsData i = ri.getSeries();
            if (sa == null || i == null) {
                return null;
            }
            boolean isignif = SaDiagnosticsUtility.isSignificant(i.getValues(), sa.getValues());
            if (config.isQs()) {
                test.qs_sa = rsa.qsTest();
                if (isignif) {
                    test.qs_i = ri.qsTest();
                }
            }
            if (config.isFtest()) {
                test.f_sa = rsa.fTest();
                if (isignif) {
                    test.f_i = ri.fTest();
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

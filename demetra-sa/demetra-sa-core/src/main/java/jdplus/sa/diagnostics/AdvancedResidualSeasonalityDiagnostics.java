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
import demetra.modelling.ComponentInformation;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.PeriodicDummies;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.linearmodel.JointTest;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.DifferencingResults;
import jdplus.modelling.regression.Regression;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.seasonal.Qs;

/**
 *
 * @author Jean Palate
 */
public class AdvancedResidualSeasonalityDiagnostics implements Diagnostics {

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

    public static Diagnostics create(AdvancedResidualSeasonalityDiagnosticsConfiguration config, SeriesDecomposition rslts) {
        try {
            AdvancedResidualSeasonalityDiagnostics test = new AdvancedResidualSeasonalityDiagnostics();
            TsData sa = rslts.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            TsData i = rslts.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            if (sa == null && i == null) {
                return null;
            }
            boolean mul = rslts.getMode().isMultiplicative();
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
                    TsData salast = sac;
                    if (ny != 0) {
                        salast = sac.drop(Math.max(0, sac.length() - ifreq * ny - 1), 0);
                    }
//                    FTest ftest = new FTest();
//                    if (ftest.test(salast)) {
//                        test.f_sa = ftest.getFTest();
//                    }
                    test.f_sa = processAr(salast);
                }
                if (i != null && isignif) {
                    TsData ic = i;
                    if (mul) {
                        ic = ic.log();
                    }
                    int ifreq = ic.getAnnualFrequency();
                    TsData ilast = ic;
                    if (ny != 0) {
                        ilast = ic.drop(Math.max(0, ic.length() - ifreq * ny - 1), 0);
                    }
//                    FTest ftest = new FTest();
//                    if (ftest.test(ilast)) {
//                        test.f_i = ftest.getFTest();
//                    }
                    test.f_i = processAr(ilast);
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

    private static StatisticalTest processAr(TsData s) {
        try {
            PeriodicDummies dummies = new PeriodicDummies(s.getAnnualFrequency());
            TsDomain edomain = s.getDomain().drop(1, 0);
            DoubleSeq y=s.getValues();
            Matrix matrix = Regression.matrix(edomain, dummies);
            LinearModel reg=LinearModel.builder()
                    .y(y.drop(1, 0))
                    .meanCorrection(true)
                    .addX(y.drop(0, 1))
                    .addX(matrix)
                    .build();
            int nseas = dummies.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(2, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

    private static StatisticalTest process(TsData s) {
        try {
            PeriodicDummies dummies = new PeriodicDummies(s.getAnnualFrequency());
            Matrix matrix = Regression.matrix(s.getDomain(), dummies);
            LinearModel reg=LinearModel.builder()
                    .y(s.getValues().plus(-s.getValues().average()))
                    .addX(matrix)
                    .build();
            int nseas = dummies.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(0, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }
}

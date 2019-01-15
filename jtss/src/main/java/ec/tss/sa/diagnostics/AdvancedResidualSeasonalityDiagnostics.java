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
package ec.tss.sa.diagnostics;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaResults;
import ec.satoolkit.diagnostics.QSTest;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class AdvancedResidualSeasonalityDiagnostics implements IDiagnostics {

    private static final double E_LIMIT = .005;
    private StatisticalTest qs_sa, qs_i, f_sa, f_i;
    private double sev, bad, unc;

    private static boolean isMultiplicative(CompositeResults rslts) {
        DecompositionMode mul = rslts.getData(ModellingDictionary.MODE, DecompositionMode.class);
        return mul != null && mul.isMultiplicative();
    }

    private static boolean isSignificant(TsData s, TsData ref, double limit) {
        DescriptiveStatistics sdesc = new DescriptiveStatistics(s);
        DescriptiveStatistics refdesc = new DescriptiveStatistics(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > limit;
    }

    private static boolean isSignificant(TsData i) {
        if (i == null) {
            return false;
        }
        DescriptiveStatistics idesc = new DescriptiveStatistics(i);
        double se = idesc.getStdev();
        return se > E_LIMIT;
    }

    public static IDiagnostics create(CompositeResults rslts, AdvancedResidualSeasonalityDiagnosticsConfiguration config) {
        try {
            if (rslts == null || GenericSaResults.getDecomposition(rslts, ISaResults.class) == null) {
                return null;
            }
            AdvancedResidualSeasonalityDiagnostics test = new AdvancedResidualSeasonalityDiagnostics();
            TsData sa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
            TsData i = rslts.getData(ModellingDictionary.I_CMP, TsData.class);
            if (sa == null && i == null) {
                return null;
            }
            boolean mul = isMultiplicative(rslts);
            boolean isignif = mul ? isSignificant(i) : (sa != null && i != null) ? isSignificant(i, sa, E_LIMIT) : true;
            if (config.isQsTest()) {
                int ny = config.getQsTestLastYears();
                if (sa != null) {
                    TsData sac = sa;
                    if (mul) {
                        sac = sac.log();
                    }
                    int ifreq = sac.getFrequency().intValue();
                    TsData salast = sac;
                    if (ny != 0) {
                        salast = sac.drop(Math.max(0, sac.getLength() - ifreq * ny - 1), 0);
                    }
                    DifferencingResults dsa = DifferencingResults.create(salast, -1, true);
                    if (mul ? isSignificant(dsa.getDifferenced()) : isSignificant(dsa.getDifferenced(), salast, E_LIMIT)) {
                        test.qs_sa = QSTest.compute(dsa.getDifferenced().internalStorage(), ifreq, 2);
                    }
                }
                if (i != null && isignif) {
                    TsData ic = i;
                    if (mul) {
                        ic = ic.log();
                    }
                    int ifreq = ic.getFrequency().intValue();
                    TsData ilast = ic;
                    if (ny != 0) {
                        ilast = ic.drop(Math.max(0, ic.getLength() - ifreq * ny), 0);
                    }
                    DifferencingResults di = DifferencingResults.create(ilast, -1, true);
                    test.qs_i = QSTest.compute(di.getDifferenced().internalStorage(), ifreq, 2);
                }
            }
            if (config.isFTest()) {
                int ny = config.getFTestLastYears();
                if (sa != null) {
                    TsData sac = sa;
                    if (mul) {
                        sac = sac.log();
                    }
                    int ifreq = sac.getFrequency().intValue();
                    TsData salast = sac;
                    if (ny != 0) {
                        salast = sac.drop(Math.max(0, sac.getLength() - ifreq * ny - 1), 0);
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
                    int ifreq = ic.getFrequency().intValue();
                    TsData ilast = ic;
                    if (ny != 0) {
                        ilast = ic.drop(Math.max(0, ic.getLength() - ifreq * ny - 1), 0);
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
        return Collections.EMPTY_LIST;
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
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            TsDomain edomain = s.getDomain().drop(1, 0);
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            reg.addX(y.drop(0, 1));
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            reg.setMeanCorrection(true);
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private static StatisticalTest process(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            y.add(-y.average());
            reg.setY(y);
            TsDomain edomain = s.getDomain();
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 0, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }
}

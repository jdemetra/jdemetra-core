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
import ec.satoolkit.diagnostics.FTest;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ResidualTradingDaysDiagnostics implements IDiagnostics {

    static final String FTEST_SA = "F-Test on SA", FTEST_I = "F-Test on I";
    static final String[] ALL = new String[]{FTEST_SA, FTEST_I};
    private StatisticalTest f_sa, f_i;
    private double sev, bad, unc;

    private static final double E_LIMIT = .01;

    private static boolean isMultiplicative(CompositeResults rslts) {
        DecompositionMode mul = rslts.getData(ModellingDictionary.MODE, DecompositionMode.class);
        return mul != null && mul.isMultiplicative();
    }

    private static boolean isSignificant(TsData s, TsData ref) {
        DescriptiveStatistics sdesc = new DescriptiveStatistics(s);
        DescriptiveStatistics refdesc = new DescriptiveStatistics(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > E_LIMIT;
    }

    static IDiagnostics create(CompositeResults rslts, ResidualTradingDaysDiagnosticsConfiguration config) {
        try {
            ResidualTradingDaysDiagnostics test = new ResidualTradingDaysDiagnostics();
            TsData sa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
            TsData i = rslts.getData(ModellingDictionary.I_CMP, TsData.class);
            if (sa == null && i == null) {
                return null;
            }
            int ny = config.getSpan();
            boolean ar = config.isArModelling();
            boolean mul = isMultiplicative(rslts);
            boolean isignif = (sa != null && i != null) ? isSignificant(i, sa) : true;
            if (sa != null) {
                if (mul) {
                    sa = sa.log();
                }
                int ifreq = sa.getFrequency().intValue();
                if (ar) {
                    TsData salast = sa;
                    if (ny != 0) {
                        salast = sa.drop(Math.max(0, sa.getLength() - ifreq * ny - 1), 0);
                    }
                    test.f_sa = processAr(salast);
                } else {
                    TsData salast = sa.delta(1);
                    if (ny != 0) {
                        salast = sa.drop(Math.max(0, sa.getLength() - ifreq * ny), 0);
                    }
                    test.f_sa = process(salast);
                }
            }
            if (i != null && isignif) {
                if (mul) {
                    i = i.log();
                }
                int ifreq = i.getFrequency().intValue();
                TsData ilast = i;
                if (ar) {
                    if (ny != 0) {
                        ilast = i.drop(Math.max(0, i.getLength() - ifreq * ny - 1), 0);
                    }
                    test.f_i = processAr(ilast);

                } else {
                    if (ny != 0) {
                        ilast = i.drop(Math.max(0, i.getLength() - ifreq * ny), 0);
                    }
                    test.f_i = process(ilast);
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
            tests.add(FTEST_SA);
        }
        if (f_i != null) {
            tests.add(FTEST_I);
        }
        return tests;
    }

    @Override
    public ProcQuality getDiagnostic(String test
    ) {
        switch (test) {
            case FTEST_SA:
                return quality(f_sa);

            case FTEST_I:
                return quality(f_i);

            default:
                return ProcQuality.Undefined;
        }
    }

    @Override
    public double getValue(String test
    ) {

        switch (test) {

            case FTEST_SA:
                return pvalue(f_sa);
            case FTEST_I:
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

    private static StatisticalTest process(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            y.add(-y.average());
            reg.setY(y);
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain();
            List<DataBlock> bvars = new ArrayList<DataBlock>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
 //           reg.setMeanCorrection(true);
            Ols ols = new Ols();
            
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 0, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private static StatisticalTest processAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain().drop(1, 0);
            List<DataBlock> bvars = new ArrayList<DataBlock>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            reg.addX(y.drop(0, 1));
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
            Ols ols = new Ols();
//            reg.setMeanCorrection(true);
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 1, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

}

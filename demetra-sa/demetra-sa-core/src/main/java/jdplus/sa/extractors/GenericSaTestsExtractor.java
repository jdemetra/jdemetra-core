/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.data.DoubleSeq;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.stats.StatisticalTest;
import java.util.function.IntToDoubleFunction;
import jdplus.regarima.tests.OneStepAheadForecastingTest;
import jdplus.sa.diagnostics.CombinedSeasonalityTests;
import jdplus.sa.diagnostics.GenericSaTests;
import jdplus.sa.diagnostics.ResidualSeasonalityTests;
import jdplus.sa.diagnostics.ResidualTradingDaysTests;
import jdplus.sa.tests.SpectralPeaks;
import demetra.stats.AutoCovariances;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class GenericSaTestsExtractor extends InformationMapping<GenericSaTests> {

    @Override
    public Class getSourceClass() {
        return GenericSaTests.class;
    }

    public static final String SEAS_LIN_QS = "seas-lin-qs",
            SEAS_LIN_F = "seas-lin-f",
            SEAS_LIN_FRIEDMAN = "seas-lin-friedman",
            SEAS_LIN_KW = "seas-lin-kw",
            SEAS_LIN_PERIODOGRAM = "seas-lin-periodogram",
            SEAS_LIN_SP = "seas-lin-spectralpeaks",
            SEAS_RES_QS = "seas-res-qs",
            SEAS_RES_F = "seas-res-f",
            SEAS_RES_FRIEDMAN = "seas-res-friedman",
            SEAS_RES_KW = "seas-res-kw",
            SEAS_RES_PERIODOGRAM = "seas-res-periodogram",
            SEAS_RES_SP = "seas-res-spectralpeaks",
            SEAS_SA_QS = "seas-sa-qs",
            SEAS_SA_F = "seas-sa-f",
            SEAS_SA_FRIEDMAN = "seas-sa-friedman",
            SEAS_SA_KW = "seas-sa-kw",
            SEAS_SA_PERIODOGRAM = "seas-sa-periodogram",
            SEAS_SA_SP = "seas-sa-spectralpeaks",
            SEAS_SA_AC1 = "seas-sa-ac1",
            SEAS_I_QS = "seas-i-qs",
            SEAS_I_F = "seas-i-f",
            SEAS_I_FRIEDMAN = "seas-i-friedman",
            SEAS_I_KW = "seas-i-kw",
            SEAS_I_PERIODOGRAM = "seas-i-periodogram",
            SEAS_I_SP = "seas-i-spectralpeaks",
            LOG_STAT = "logstat",
            LEVEL_STAT = "levelstat",
            TD_RES_ALL = "td-res-all",
            TD_RES_LAST = "td-res-last",
            TD_I_ALL = "td-i-all",
            TD_I_LAST = "td-i-last",
            TD_SA_ALL = "td-sa-all",
            TD_SA_LAST = "td-sa-last",
            IC_RATIO = "ic-ratio",
            IC_RATIO_HENDERSON = "ic-ratio-henderson",
            MSR_GLOBAL = "msr-global",
            MSR = "msr";

    public GenericSaTestsExtractor() {

        delegate(null, CombinedSeasonalityTests.class, source -> source.combinedSeasonalityTests());
        delegate(null, OneStepAheadForecastingTest.class, source -> source.forecastingTest());

        //////////  Residuals 
        set(SEAS_RES_F, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                StatisticalTest t = test.fTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_RES_QS, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                StatisticalTest t = test.qsTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_RES_KW, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                StatisticalTest t = test.kruskallWallisTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_RES_FRIEDMAN, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                StatisticalTest t = test.friedmanTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_RES_PERIODOGRAM, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                StatisticalTest t = test.periodogramTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_RES_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });

        /////////////////// Irregular
        set(SEAS_I_F, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                StatisticalTest t = test.fTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_I_QS, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                StatisticalTest t = test.qsTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_I_KW, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                StatisticalTest t = test.kruskallWallisTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_I_PERIODOGRAM, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                StatisticalTest t = test.periodogramTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_I_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });

        /////////////////////// SA
        set(SEAS_SA_F, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                StatisticalTest t = test.fTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_SA_QS, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                StatisticalTest t = test.qsTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_SA_KW, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                StatisticalTest t = test.kruskallWallisTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_SA_FRIEDMAN, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                StatisticalTest t = test.friedmanTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_SA_PERIODOGRAM, Double.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                StatisticalTest t = test.periodogramTest();
                if (t != null) {
                    return t.getPvalue();
                }
            }
            return null;
        });

        set(SEAS_SA_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });

        set(SEAS_SA_AC1, Double.class, source -> {
            DoubleSeq dsa = source.getLsa().delta(1).getValues();
            IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(dsa, dsa.average());
            return ac.applyAsDouble(1);
        });

        set(TD_SA_ALL, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.saTest(false);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(TD_SA_LAST, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.saTest(true);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(TD_I_ALL, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.irrTest(false);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(TD_I_LAST, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.irrTest(true);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(TD_RES_ALL, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.residualsTest(false);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(TD_RES_LAST, Double.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            StatisticalTest test = td.residualsTest(true);
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });
    }
}

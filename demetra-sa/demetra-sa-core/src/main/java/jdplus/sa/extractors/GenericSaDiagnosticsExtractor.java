/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import jdplus.regarima.tests.OneStepAheadForecastingTest;
import jdplus.sa.diagnostics.GenericSaDiagnostics;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.sa.tests.FTest;
import jdplus.sa.tests.KruskalWallis;
import jdplus.sa.tests.SeasonalityTests;
import jdplus.sa.tests.SpectralPeaks;
import jdplus.stats.tests.TestsUtility;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class GenericSaDiagnosticsExtractor extends InformationMapping<GenericSaDiagnostics> {

    @Override
    public Class getSourceClass() {
        return GenericSaDiagnostics.class;
    }

    public static final String SEAS_LIN_QS = "seas-lin-qs",
            SEAS_LIN_F = "seas-lin-f",
            SEAS_LIN_FRIEDMAN = "seas-lin-friedman",
            SEAS_LIN_KW = "seas-lin-kw",
            SEAS_LIN_PERIODOGRAM = "seas-lin-periodogram",
            SEAS_LIN_SP = "seas-lin-spectralpeaks",
            SEAS_SI_COMBINED = "seas-si-combined",
            SEAS_SI_EVOLUTIVE = "seas-si-evolutive",
            SEAS_SI_STABLE = "seas-si-stable",
            SEAS_RES_QS = "seas-res-qs",
            SEAS_RES_F = "seas-res-f",
            SEAS_RES_FRIEDMAN = "seas-res-friedman",
            SEAS_RES_KW = "seas-res-kw",
            SEAS_RES_PERIODOGRAM = "seas-res-periodogram",
            SEAS_RES_COMBINED = "seas-res-combined",
            SEAS_RES_COMBINED3 = "seas-res-combined3",
            SEAS_RES_EVOLUTIVE = "seas-res-evolutive",
            SEAS_RES_STABLE = "seas-res-stable",
            SEAS_RES_SP = "seas-res-spectralpeaks",
            SEAS_SA_QS = "seas-sa-qs",
            SEAS_SA_F = "seas-sa-f",
            SEAS_SA_FRIEDMAN = "seas-sa-friedman",
            SEAS_SA_KW = "seas-sa-kw",
            SEAS_SA_PERIODOGRAM = "seas-sa-periodogram",
            SEAS_SA_COMBINED = "seas-sa-combined",
            SEAS_SA_COMBINED3 = "seas-sa-combined3",
            SEAS_SA_STABLE = "seas-sa-stable",
            SEAS_SA_EVOLUTIVE = "seas-sa-evolutive",
            SEAS_SA_SP = "seas-sa-spectralpeaks",
            SEAS_SA_AC1 = "seas-sa-ac1",
            SEAS_I_QS = "seas-i-qs",
            SEAS_I_F = "seas-i-f",
            SEAS_I_FRIEDMAN = "seas-i-friedman",
            SEAS_I_KW = "seas-i-kw",
            SEAS_I_PERIODOGRAM = "seas-i-periodogram",
            SEAS_I_COMBINED = "seas-i-combined",
            SEAS_I_COMBINED3 = "seas-i-combined3",
            SEAS_I_STABLE = "seas-i-stable",
            SEAS_I_EVOLUTIVE = "seas-i-evolutive",
            SEAS_I_SP = "seas-i-spectralpeaks",
            FCAST_INSAMPLE_MEAN = "fcast-insample-mean",
            FCAST_OUTSAMPLE_MEAN = "fcast-outsample-mean",
            FCAST_OUTSAMPLE_VARIANCE = "fcast-outsample-variance",
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

    public GenericSaDiagnosticsExtractor() {

////////////////////// FCASTS
        set(FCAST_INSAMPLE_MEAN, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ftest.inSampleMeanTest().getPvalue();
        });

        set(FCAST_OUTSAMPLE_MEAN, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ftest.outOfSampleMeanTest().getPvalue();
        });

        set(FCAST_OUTSAMPLE_VARIANCE, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ftest.sameVarianceTest().getPvalue();
        });

        //////////  Y
        set(SEAS_LIN_F, Double.class, source -> {
            TsData lin = source.getLin();
            StatisticalTest test = new FTest(lin.getValues(), lin.getAnnualFrequency())
                    .model(FTest.Model.AR)
                    .build();
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(SEAS_LIN_QS, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ytests.getQs().getPvalue();
            }
        });

        set(SEAS_LIN_KW, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            StatisticalTest test = new KruskalWallis(ytests.getDifferencing().getDifferenced(), source.annualFrequency())
                    .build();
            return test == null ? null : test.getPvalue();
        });

        set(SEAS_LIN_FRIEDMAN, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ytests.getNonParametricTest().getPvalue();
            }
        });

        set(SEAS_LIN_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                StatisticalTest ptest = ytests.getPeriodogramTest();
                return ptest == null ? null : ptest.getPvalue();
            }
        });

        set(SEAS_LIN_SP, String.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                SpectralPeaks[] spectralPeaks = ytests.getSpectralPeaks();
                if (spectralPeaks == null) {
                    return null;
                } else {
                    return SpectralPeaks.format(spectralPeaks);
                }
            }
        });

        set(SEAS_SI_COMBINED, String.class, source -> {
            CombinedSeasonality sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });

        set(SEAS_SI_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_SI_STABLE, Double.class, source -> {
            CombinedSeasonality sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getStableSeasonalityTest()).getPvalue();
            }
        });

        //////////  Residuals 
        set(SEAS_RES_F, Double.class, source -> {
            TsData res = source.getRes();
            if (res == null) {
                return null;
            }
            StatisticalTest test = new FTest(res.getValues(), res.getAnnualFrequency())
                    .model(FTest.Model.WN)
                    .build();
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(SEAS_RES_QS, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return rtests.getQs().getPvalue();
            }
        });

        set(SEAS_RES_KW, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            StatisticalTest test = new KruskalWallis(rtests.getDifferencing().getDifferenced(), source.annualFrequency())
                    .build();
            return test == null ? null : test.getPvalue();
        });

        set(SEAS_RES_FRIEDMAN, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return rtests.getNonParametricTest().getPvalue();
            }
        });

        set(SEAS_RES_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                StatisticalTest ptest = rtests.getPeriodogramTest();
                return ptest == null ? null : ptest.getPvalue();
            }
        });

        set(SEAS_RES_SP, String.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return SpectralPeaks.format(rtests.getSpectralPeaks());
            }
        });

        set(SEAS_RES_COMBINED, String.class, source -> {
            CombinedSeasonality rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SEAS_RES_COMBINED3, String.class, source -> {
            CombinedSeasonality rtest = source.cresTest(true);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SEAS_RES_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_RES_STABLE, Double.class, source -> {
            CombinedSeasonality rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getStableSeasonalityTest()).getPvalue();
            }
        });

        /////////////////// Irregular
        set(SEAS_I_F, Double.class, source -> {
            TsData irr = source.getIrr();
            StatisticalTest test = new FTest(irr.getValues(), irr.getAnnualFrequency())
                    .model(FTest.Model.WN)
                    .build();
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(SEAS_I_QS, Double.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return itests.getQs().getPvalue();
            }
        });

        set(SEAS_I_KW, Double.class, source -> {
            SeasonalityTests itests = source.irrTests();
            StatisticalTest test = new KruskalWallis(itests.getDifferencing().getDifferenced(), source.annualFrequency())
                    .build();
            return test == null ? null : test.getPvalue();
        });

        set(SEAS_I_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                StatisticalTest ptest = itests.getPeriodogramTest();
                return ptest == null ? null : ptest.getPvalue();
            }
        });

        set(SEAS_I_SP, String.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return SpectralPeaks.format(itests.getSpectralPeaks());
            }
        });

        set(SEAS_I_COMBINED, String.class, source -> {
            CombinedSeasonality itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SEAS_I_COMBINED3, String.class, source -> {
            CombinedSeasonality itest = source.ciTest(true);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SEAS_I_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_I_STABLE, Double.class, source -> {
            CombinedSeasonality itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getStableSeasonalityTest()).getPvalue();
            }
        });

        /////////////////////// SA
        set(SEAS_SA_F, Double.class, source -> {
            TsData sa = source.getSa();
            StatisticalTest test = new FTest(sa.getValues(), sa.getAnnualFrequency())
                    .model(FTest.Model.AR)
                    .build();
            if (test != null) {
                return test.getPvalue();
            } else {
                return null;
            }
        });

        set(SEAS_SA_QS, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return satests.getQs().getPvalue();
            }
        });

        set(SEAS_SA_KW, Double.class, source -> {
            SeasonalityTests rtests = source.saTests();
            StatisticalTest test = new KruskalWallis(rtests.getDifferencing().getDifferenced(), source.annualFrequency())
                    .build();
            return test == null ? null : test.getPvalue();
        });

        set(SEAS_SA_FRIEDMAN, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return satests.getNonParametricTest().getPvalue();
            }
        });

        set(SEAS_SA_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                StatisticalTest ptest = satests.getPeriodogramTest();
                return ptest == null ? null : ptest.getPvalue();
            }
        });

        set(SEAS_SA_SP, String.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return SpectralPeaks.format(satests.getSpectralPeaks());
            }
        });

        set(SEAS_SA_COMBINED, String.class, source -> {
            CombinedSeasonality satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SEAS_SA_COMBINED3, String.class, source -> {
            CombinedSeasonality satest = source.csaTest(true);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SEAS_SA_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(satest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_SA_STABLE, Double.class, source -> {
            CombinedSeasonality satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
               return TestsUtility.ofAnova(satest.getStableSeasonalityTest()).getPvalue();
             }
        });

//        set(SEAS_SA_AC1, Double.class, source -> {
//            TsData dsa = source.getSa().delta(1);
//            AutoCorrelations ac = new AutoCorrelations(dsa);
//            return ac.autoCorrelation(1);
//        });
//
//        MAPPING.set(TD_SA_ALL, Double.class, source -> {
//            TsData s = source.sa;
//            if (source.mul) {
//                s = s.log();
//            }
//            StatisticalTest test = tdAr(s);
//            return test == null ? null : test.getPvalue();
//        });
//
//        MAPPING.set(TD_SA_LAST, Double.class, source -> {
//            TsData s = source.sa;
//            if (source.mul) {
//                s = s.log();
//            }
//            int ifreq = s.getFrequency().intValue();
//            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
//            StatisticalTest test = tdAr(s);
//            return test == null ? null : test.getPvalue();
//        });
//
//        MAPPING.set(TD_I_ALL, Double.class, source -> {
//            TsData s = source.irr;
//            if (source.mul) {
//                s = s.log();
//            }
//            StatisticalTest test = tdAr(s);
//            return test == null ? null : test.getPvalue();
//        });
//
//        MAPPING.set(TD_I_LAST, Double.class, source -> {
//            TsData s = source.irr;
//            if (source.mul) {
//                s = s.log();
//            }
//            int ifreq = s.getFrequency().intValue();
//            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
//            StatisticalTest test = tdAr(s);
//            return test == null ? null : test.getPvalue();
//        });
//
//        MAPPING.set(TD_RES_ALL, Double.class, source -> {
//            TsData s = source.res;
//            if (s == null) {
//                return null;
//            }
//            StatisticalTest test = td(s);
//            return test == null ? null : test.getPvalue();
//        });
//
//        MAPPING.set(TD_RES_LAST, Double.class, source -> {
//            TsData s = source.res;
//            if (s == null) {
//                return null;
//            }
//            int ifreq = s.getFrequency().intValue();
//            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
//            StatisticalTest test = td(s);
//            return test == null ? null : test.getPvalue();
//        });

    }
}


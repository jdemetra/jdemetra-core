/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit;

import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.AutoCorrelations;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.modelling.arima.tramo.SpectralPeaks;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author palatej
 */
public class GenericSaDiagnostics implements IProcResults {

    public static GenericSaDiagnostics of(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        if (decomposition == null || finals == null) {
            return null;
        }
        return new GenericSaDiagnostics(regarima, decomposition, finals);
    }

    private final PreprocessingModel regarima;
    private final ISaResults decomposition;
    private final ISeriesDecomposition finals;
    private final boolean mul;

    private final TsData lin, res, sa, irr, si, s;

    private SeasonalityTests ytests, rtests, satests, itests;
    private CombinedSeasonalityTest seasSI, seasSa, seasI, seasRes, seasSa3, seasI3, seasRes3;
    private OneStepAheadForecastingTest outOfSampleTest;

    private GenericSaDiagnostics(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        this.regarima = regarima;
        this.decomposition = decomposition;
        this.finals = finals;
        sa = decomposition.getData(ModellingDictionary.SA_CMP, TsData.class);
        s = decomposition.getData(ModellingDictionary.S_CMP, TsData.class);
        irr = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);
        mul = decomposition.getSeriesDecomposition().getMode().isMultiplicative();
        if (regarima == null) {
            lin = decomposition.getData(ModellingDictionary.Y_CMP, TsData.class);
            res = null;
        } else {
            lin = regarima.linearizedSeries();
            res = regarima.getFullResiduals();
        }
        if (decomposition instanceof X11Results) {
            si = ((X11Results) decomposition).getData(InformationSet.concatenate(X11Kernel.D, X11Kernel.D8), TsData.class);
        } else if (mul) {
            si = TsData.multiply(s, irr);
        } else {
            si = TsData.add(s, irr);
        }
    }

    private SeasonalityTests resTests() {
        if (res == null) {
            return null;
        }
        if (rtests == null) {
            rtests = SeasonalityTests.seasonalityTest(res, 0, false, true);
        }
        return rtests;
    }

    private SeasonalityTests yTests() {
        if (ytests == null) {
            ytests = SeasonalityTests.seasonalityTest(lin, 1, true, true);
        }
        return ytests;
    }

    private SeasonalityTests saTests() {
        if (satests == null) {
            satests = SeasonalityTests.seasonalityTest(sa, 1, true, true);
        }
        return satests;
    }

    private SeasonalityTests irrTests() {
        if (itests == null) {
            itests = SeasonalityTests.seasonalityTest(irr, 0, true, true);
        }
        return itests;
    }

    private CombinedSeasonalityTest csiTest() {
        if (seasSI == null) {
            seasSI = new CombinedSeasonalityTest(si, mul);
        }
        return seasSI;
    }

    private CombinedSeasonalityTest csaTest(boolean last) {
        if (last) {
            if (seasSa3 == null) {
                int freq = sa.getFrequency().intValue();
                TsData s = sa.delta(Math.max(1, freq / 4));
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasSa3 = new CombinedSeasonalityTest(s.select(sel), mul);
            }
            return seasSa3;
        } else {
            if (seasSa == null) {
                int freq = sa.getFrequency().intValue();
                TsData s = sa.delta(Math.max(1, freq / 4));
                seasSa = new CombinedSeasonalityTest(s, mul);
            }
            return seasSa;
        }
    }

    private CombinedSeasonalityTest cresTest(boolean last) {
        if (res == null) {
            return null;
        }
        if (last) {
            if (seasRes3 == null) {
                int freq = res.getFrequency().intValue();
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasRes3 = new CombinedSeasonalityTest(res.select(sel), false);
            }
            return seasRes3;
        } else {
            if (seasRes == null) {
                seasRes = new CombinedSeasonalityTest(res, false);
            }
            return seasRes;
        }
    }

    private CombinedSeasonalityTest ciTest(boolean last) {
        if (last) {
            if (seasI3 == null) {
                int freq = irr.getFrequency().intValue();
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasI3 = new CombinedSeasonalityTest(irr.select(sel), mul);
            }
            return seasI3;
        } else {
            if (seasI == null) {
                seasI = new CombinedSeasonalityTest(irr, mul);
            }
            return seasI;
        }
    }

    private static final double FLEN = 1.5;

    private OneStepAheadForecastingTest forecastingTest() {
        if (regarima == null) {
            return null;
        }
        if (outOfSampleTest == null) {
            int ifreq = regarima.description.getFrequency();
            int nback = (int) (FLEN * ifreq);
            if (nback < 5) {
                nback = 5;
            }
            outOfSampleTest = new OneStepAheadForecastingTest(nback);
            outOfSampleTest.test(regarima.estimation.getRegArima());
        }
        return outOfSampleTest;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
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
            FCAST_OUTSAMPLE_VARIANCE = "fcast-outsample-variance";

    // MAPPING
    public static InformationMapping<GenericSaDiagnostics> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<GenericSaDiagnostics, T> extractor) {
        synchronized (MAPPING) {
            MAPPING.set(name, tclass, extractor);
        }
    }

    private static final InformationMapping<GenericSaDiagnostics> MAPPING = new InformationMapping<>(GenericSaDiagnostics.class);

    static {

        //////////  Y
        MAPPING.set(SEAS_LIN_F, Double.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.lin)) {
                return ftest.getFTest().getPValue();
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_LIN_QS, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ytests.getQs().getPValue();
            }
        });

        MAPPING.set(SEAS_LIN_KW, Double.class, source -> {
            KruskalWallisTest kw = new KruskalWallisTest(source.lin);
            return kw.getPValue();
        });

        MAPPING.set(SEAS_LIN_FRIEDMAN, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ytests.getNonParametricTest().getPValue();
            }
        });

        MAPPING.set(SEAS_LIN_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ytests.getPeriodogramTest().getPValue();
            }
        });

        MAPPING.set(SEAS_LIN_SP, String.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return SpectralPeaks.format(ytests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_SI_COMBINED, String.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SI_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return sitest.getEvolutiveSeasonality().getPValue();
            }
        });

        MAPPING.set(SEAS_SI_STABLE, Double.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest();
            if (sitest == null) {
                return null;
            } else {
                return sitest.getStableSeasonality().getPValue();
            }
        });

        //////////  Residuals 
        MAPPING.set(SEAS_RES_F, Double.class, source -> {
            if (source.res == null) {
                return null;
            }
            FTest ftest = new FTest();
            if (ftest.test(source.res)) {
                return ftest.getFTest().getPValue();
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_RES_QS, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return rtests.getQs().getPValue();
            }
        });

        MAPPING.set(SEAS_RES_KW, Double.class, source -> {
            if (source.res == null) {
                return null;
            }
            KruskalWallisTest kw = new KruskalWallisTest(source.res);
            return kw.getPValue();
        });

        MAPPING.set(SEAS_RES_FRIEDMAN, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return rtests.getNonParametricTest().getPValue();
            }
        });
        MAPPING.set(SEAS_RES_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return rtests.getPeriodogramTest().getPValue();
            }
        });

        MAPPING.set(SEAS_RES_SP, String.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return SpectralPeaks.format(rtests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_RES_COMBINED, String.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_RES_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(true);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_RES_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getEvolutiveSeasonality().getPValue();
            }
        });

        MAPPING.set(SEAS_RES_STABLE, Double.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getStableSeasonality().getPValue();
            }
        });

        /////////////////// Irregular
        MAPPING.set(SEAS_I_F, Double.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.irr)) {
                return ftest.getFTest().getPValue();
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_I_QS, Double.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return itests.getQs().getPValue();
            }
        });

        MAPPING.set(SEAS_I_KW, Double.class, source -> {
            KruskalWallisTest kw = new KruskalWallisTest(source.irr);
            return kw.getPValue();
        });

        MAPPING.set(SEAS_I_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return itests.getPeriodogramTest().getPValue();
            }
        });

        MAPPING.set(SEAS_I_SP, String.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return SpectralPeaks.format(itests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_I_COMBINED, String.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_I_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(true);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_I_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getEvolutiveSeasonality().getPValue();
            }
        });

        MAPPING.set(SEAS_I_STABLE, Double.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getStableSeasonality().getPValue();
            }
        });

        /////////////////////// SA
        MAPPING.set(SEAS_SA_F, Double.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.sa)) {
                return ftest.getFTest().getPValue();
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_SA_QS, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return satests.getQs().getPValue();
            }
        });

        MAPPING.set(SEAS_SA_KW, Double.class, source -> {
            KruskalWallisTest kw = new KruskalWallisTest(source.sa);
            return kw.getPValue();
        });

        MAPPING.set(SEAS_SA_FRIEDMAN, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return satests.getNonParametricTest().getPValue();
            }
        });

        MAPPING.set(SEAS_SA_PERIODOGRAM, Double.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return satests.getPeriodogramTest().getPValue();
            }
        });

        MAPPING.set(SEAS_SA_SP, String.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return SpectralPeaks.format(satests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_SA_COMBINED, String.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SA_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(true);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SA_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getEvolutiveSeasonality().getPValue();
            }
        });

        MAPPING.set(SEAS_SA_STABLE, Double.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getStableSeasonality().getPValue();
            }
        });

        MAPPING.set(SEAS_SA_AC1, Double.class, source -> {
            TsData dsa = source.sa.delta(1);
            AutoCorrelations ac = new AutoCorrelations(dsa);
            return ac.autoCorrelation(1);
        });

////////////////////// FCASTS

        MAPPING.set(FCAST_INSAMPLE_MEAN, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            return ftest.inSampleMeanTest().getPValue();
        });
        
        MAPPING.set(FCAST_OUTSAMPLE_MEAN, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            return ftest.outOfSampleMeanTest().getPValue();
        });

        MAPPING.set(FCAST_OUTSAMPLE_VARIANCE, Double.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            return ftest.mseTest().getPValue();
        });
    }
}

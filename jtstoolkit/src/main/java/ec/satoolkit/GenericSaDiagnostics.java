/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit;

import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
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

    private final TsData res, sa, irr;

    private SeasonalityTests rtests, satests, itests;

    private CombinedSeasonalityTest seasSA, seasI;

    private GenericSaDiagnostics(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        this.regarima = regarima;
        this.decomposition = decomposition;
        this.finals = finals;
        if (regarima == null) {
            res = null;
        } else {
            res = regarima.getFullResiduals();
        }
        sa = decomposition.getData(ModellingDictionary.SA_CMP, TsData.class);
        irr = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);
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

    public static final String COMBINED = "combined",
            SEAS_RES_QS = "seas-res-qs",
            SEAS_RES_F = "seas-res-f",
            SEAS_RES_FRIEDMAN = "seas-res-friedman",
            SEAS_RES_KW = "seas-res-kw",
            SEAS_RES_PERIODOGRAM = "seas-res-periodogram",
            SEAS_RES_COMBINED = "seas-res-combined",
            SEAS_RES_SP = "seas-res-spectralpeaks",
            SEAS_SA_QS = "seas-sa-qs",
            SEAS_SA_F = "seas-sa-f",
            SEAS_SA_FRIEDMAN = "seas-sa-friedman",
            SEAS_SA_KW = "seas-sa-kw",
            SEAS_SA_PERIODOGRAM = "seas-sa-periodogram",
            SEAS_SA_COMBINED = "seas-sa-combined",
            SEAS_SA_SP = "seas-sa-spectralpeaks",
            SEAS_I_QS = "seas-i-qs",
            SEAS_I_F = "seas-i-f",
            SEAS_I_FRIEDMAN = "seas-i-friedman",
            SEAS_I_KW = "seas-i-kw",
            SEAS_I_PERIODOGRAM = "seas-i-periodogram",
            SEAS_I_COMBINED = "seas-i-combined",
            SEAS_I_SP = "seas-i-spectralpeaks";

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
        MAPPING.set(SEAS_RES_F, Double.class, source -> {
            if (source.res == null)
                return null;
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
            if (source.res == null)
                return null;
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

    }
}

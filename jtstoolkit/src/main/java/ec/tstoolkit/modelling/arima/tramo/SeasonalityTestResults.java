/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Nina Gonschorreck
 */
public class SeasonalityTestResults implements IProcResults {

    public static final String VALUE = "value", PVALUE = "pvalue", LOG = "log";

    private TsData data;
    private boolean mul;

    public TsData getData() {
        return data;
    }

    public TsData getLogData() {
        return data.log();
    }

    public void setData(TsData tsData) {
        this.data = tsData;
    }

    private boolean isMultiplicative() {
        return mul;
    }

    public void setMode(boolean m) {
        mul = m;
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> dictionary = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dictionary, false);
        return dictionary;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
        }
        return null;
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static InformationMapping<SeasonalityTestResults> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<SeasonalityTestResults, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<SeasonalityTestResults, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<SeasonalityTestResults> MAPPING = new InformationMapping<>(SeasonalityTestResults.class);

    static {
        //<editor-fold defaultstate="collapsed" desc="TESTS">
        MAPPING.set("int", Integer.class,
                source -> 17
        );
        MAPPING.set("bool", Boolean.class,
                source -> true
        );
        MAPPING.set("double", Double.class,
                source -> 17.1
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="DATA">
        MAPPING.set("data",
                source -> source.getData()
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="QS">
        MAPPING.set(InformationSet.item("qs", PVALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getQs().getPValue();
                }
        );
        MAPPING.set(InformationSet.item("qs", VALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getQs().getValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("qs", PVALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getQs().getPValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("qs", VALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getQs().getValue();
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="FRIEDMAN">
        MAPPING.set(InformationSet.item("friedman", VALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getNonParametricTest().getValue();
                }
        );
        MAPPING.set(InformationSet.item("friedman", PVALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getNonParametricTest().getPValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("friedman", VALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getNonParametricTest().getValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("friedman", PVALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getNonParametricTest().getPValue();
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="PEAKS">
        MAPPING.set("peaks", String.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return SpectralPeaks.format(tests.getSpectralPeaks());
                }
        );
        MAPPING.set(InformationSet.item(LOG, "peaks"), String.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return SpectralPeaks.format(tests.getSpectralPeaks());
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="KW">
        MAPPING.set(InformationSet.item("kw", PVALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return StatisticalTest.create(new KruskalWallisTest(tests.getDifferencing().getDifferenced())).pvalue;
                }
        );
        MAPPING.set(InformationSet.item("kw", VALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return StatisticalTest.create(new KruskalWallisTest(tests.getDifferencing().getDifferenced())).value;
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("kw", PVALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return StatisticalTest.create(new KruskalWallisTest(tests.getDifferencing().getDifferenced())).pvalue;
                });
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("kw", VALUE)), Double.class,
                source -> {

                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return StatisticalTest.create(new KruskalWallisTest(tests.getDifferencing().getDifferenced())).value;
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="STABLE">
        MAPPING.set(InformationSet.item("stable", VALUE), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.stableSeasonality(source.getData());
                    return tests.getValue();
                }
        );

        MAPPING.set(InformationSet.item("stable", PVALUE), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.stableSeasonality(source.getData());
                    return tests.getPValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("stable", VALUE)), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.stableSeasonality(source.getLogData());
                    return tests.getValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("stable", PVALUE)), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.stableSeasonality(source.getLogData());

                    return tests.getPValue();
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="EVOLUTIVE">
        MAPPING.set(InformationSet.item("evolutive", PVALUE), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.evolutiveSeasonality(source.getData(), source.isMultiplicative());
                    return tests.getPValue();
                }
        );
        MAPPING.set(InformationSet.item("evolutive", VALUE), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.evolutiveSeasonality(source.getData(), source.isMultiplicative());
                    return tests.getValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("evolutive", PVALUE)), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.evolutiveSeasonality(source.getLogData(), source.isMultiplicative());
                    return tests.getPValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("evolutive", VALUE)), Double.class,
                source -> {
                    SeasonalityTest tests = SeasonalityTest.evolutiveSeasonality(source.getLogData(), source.isMultiplicative());
                    return tests.getValue();
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="PERIODOGRAM">
        MAPPING.set(InformationSet.item("periodogram", PVALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getPeriodogramTest().getPValue();
                }
        );
        MAPPING.set(InformationSet.item("periodogram", VALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    return tests.getPeriodogramTest().getValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("periodogram", PVALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getPeriodogramTest().getPValue();
                }
        );
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("periodogram", VALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    return tests.getPeriodogramTest().getValue();
                }
        );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="DUMMIES">
        MAPPING.set(InformationSet.item("dummies", PVALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    FTest ftest = new FTest();
                    ftest.test(tests.getDifferencing().getOriginal());
                    return StatisticalTest.create(ftest.getFTest()).pvalue;
                }
        );

        MAPPING.set(
                InformationSet.item("dummies", VALUE), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getData(), 1, true, true);
                    FTest ftest = new FTest();
                    ftest.test(tests.getDifferencing().getOriginal());

                    return StatisticalTest.create(ftest.getFTest()).value;
                }
        );
        
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("dummies", PVALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    FTest ftest = new FTest();
                    ftest.test(tests.getDifferencing().getOriginal());

                    return StatisticalTest.create(ftest.getFTest()).pvalue;
                }
        );
        
        MAPPING.set(InformationSet.item(LOG, InformationSet.item("dummies", VALUE)), Double.class,
                source -> {
                    SeasonalityTests tests = SeasonalityTests.seasonalityTest(source.getLogData(), 1, true, true);
                    FTest ftest = new FTest();
                    ftest.test(tests.getDifferencing().getOriginal());

                    return StatisticalTest.create(ftest.getFTest()).value;
                }
        );
        //</editor-fold>
    }
}

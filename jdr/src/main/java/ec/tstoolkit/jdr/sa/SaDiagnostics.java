/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.satoolkit.diagnostics.StationaryVarianceDecomposition;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.jdr.tests.CombinedSeasonalityTestInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class SaDiagnostics implements IProcResults {
    
    static final InformationMapping<SaDiagnostics> MAPPING = new InformationMapping<>(SaDiagnostics.class);

    private StatisticalTest qs;
    private StatisticalTest ftest;
    private CombinedSeasonalityTest combinedSeasonality, combinedSeasonalityOnEnd;
    private SeasonalityTest residualSeasonality, residualSeasonalityOnEnd;
    private StatisticalTest residualTradingDays;
    private StationaryVarianceDecomposition varDecomposition;
    
    double[] allVariances(){
        return new double[]{
            varDecomposition.getVarC(),
            varDecomposition.getVarS(),
            varDecomposition.getVarI(),
            varDecomposition.getVarTD(),
            varDecomposition.getVarP(),
            varDecomposition.getVarTotal()
        };
    }
    
    static {
        MAPPING.set("qs", StatisticalTest.class, source->source.qs);
        MAPPING.set("ftest", StatisticalTest.class, source->source.ftest);
        MAPPING.delegate("combined.all", CombinedSeasonalityTestInfo.getMapping(), source->source.combinedSeasonality);
        MAPPING.delegate("combined.end", CombinedSeasonalityTestInfo.getMapping(), source->source.combinedSeasonalityOnEnd);
        MAPPING.set("residual.all", StatisticalTest.class, source->StatisticalTest.create(source.residualSeasonality));
        MAPPING.set("residual.end", StatisticalTest.class, source->StatisticalTest.create(source.residualSeasonalityOnEnd));
        MAPPING.set("residualtd", StatisticalTest.class, source->source.residualTradingDays);
        MAPPING.set("variancedecomposition", double[].class, source->source.allVariances());
    }

    public static InformationMapping<SaDiagnostics> getMapping() {
        return MAPPING;
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }
    
    
}

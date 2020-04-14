/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.highfreq.extractors.FractionalAirlineDecompositionExtractor;
import demetra.highfreq.extractors.FractionalAirlineModelExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.processing.ProcResults;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Low-level results. Should be refined
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class FractionalAirlineDecomposition implements ProcResults {

    FractionalAirline model;

    private double[] parameters, score;
    private MatrixType parametersCovariance;

    LikelihoodStatistics likelihood;
    double[] y, t, s, i, n;
    demetra.arima.UcarimaModel ucarima;

    public double[] getSa() {
        double[] sa = y.clone();
        if (s != null) {
            for (int i = 0; i < sa.length; ++i) {
                sa[i] -= s[i];
            }
        }
        return sa;
    }
    
    @Override
    public boolean contains(String id) {
        return FractionalAirlineDecompositionExtractor.getMapping().contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        FractionalAirlineDecompositionExtractor.getMapping().fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return FractionalAirlineDecompositionExtractor.getMapping().getData(this, id, tclass);
    }
    
    public static InformationMapping<FractionalAirlineDecomposition> getMapping(){
        return FractionalAirlineDecompositionExtractor.getMapping();
    }
    
}

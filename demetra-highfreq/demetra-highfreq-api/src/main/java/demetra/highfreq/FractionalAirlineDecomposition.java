/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import demetra.information.Explorable;
import java.util.List;

/**
 * Low-level results. Should be refined
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class FractionalAirlineDecomposition implements Explorable {

    DoubleSeq y;
    FractionalAirline model;

    private DoubleSeq parameters, score;
    private Matrix parametersCovariance;
    
    LikelihoodStatistics likelihood;
    @lombok.Singular
    List<SeriesComponent> components;
    
    demetra.arima.UcarimaModel ucarima;
    
    public SeriesComponent component(String name){
        for (SeriesComponent cmp : components){
            if (cmp.getName().equalsIgnoreCase(name))
                return cmp;
        }
        return null;
    }

}

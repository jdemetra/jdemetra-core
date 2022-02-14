/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import jdplus.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import demetra.information.GenericExplorable;
import java.util.List;

/**
 * Low-level results. Should be refined
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class FractionalAirlineDecomposition implements GenericExplorable {

    DoubleSeq y;
    FractionalAirline model;

    private DoubleSeq parameters, score;
    private Matrix parametersCovariance;

    LikelihoodStatistics likelihood;
    @lombok.Singular
    List<SeriesComponent> components;

    demetra.arima.UcarimaModel ucarima;

    public SeriesComponent component(String name) {
        for (SeriesComponent cmp : components) {
            if (cmp.getName().equalsIgnoreCase(name)) {
                return cmp;
            }
        }
        return null;
    }

}

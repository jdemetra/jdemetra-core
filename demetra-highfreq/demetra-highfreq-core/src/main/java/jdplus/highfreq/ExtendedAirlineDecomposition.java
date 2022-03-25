/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq;

import demetra.data.DoubleSeq;
import demetra.highfreq.ExtendedAirline;
import demetra.highfreq.SeriesComponent;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import demetra.information.GenericExplorable;
import java.util.List;
import jdplus.ucarima.UcarimaModel;

/**
 * Low-level results. Should be refined
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineDecomposition implements GenericExplorable {

    DoubleSeq y;
    ExtendedAirline model;

    private DoubleSeq parameters, score;
    private Matrix parametersCovariance;

    LikelihoodStatistics likelihood;
    @lombok.Singular
    List<SeriesComponent> components;

    UcarimaModel ucarima;

    public SeriesComponent component(String name) {
        for (SeriesComponent cmp : components) {
            if (cmp.getName().equalsIgnoreCase(name)) {
                return cmp;
            }
        }
        return null;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.highfreq.extractors.FractionalAirlineDecompositionExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.information.Explorable;

/**
 * Low-level results. Should be refined
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class FractionalAirlineDecomposition implements Explorable {

    FractionalAirline model;

    private DoubleSeq parameters, score;
    private MatrixType parametersCovariance;

    LikelihoodStatistics likelihood;
    double[] y, t, s, i, n, stdeT, stdeS, stdeI, stdeN;
    demetra.arima.UcarimaModel ucarima;

    public double[] getSa() {
        double[] sa = y.clone();
        if (s != null) {
            for (int j = 0; j < sa.length; ++j) {
                sa[j] -= s[j];
            }
        }
        return sa;
    }
   
}

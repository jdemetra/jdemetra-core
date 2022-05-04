/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.timeseries.TsData;
import nbbrd.service.ServiceProvider;
import demetra.math.matrices.Matrix;
import jdplus.tempdisagg.univariate.TemporalDisaggregationIResults;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class TemporalDisaggregationIExtractor extends InformationMapping<TemporalDisaggregationIResults> {

    public final String LIKELIHOOD = "likelihood", DISAGG = "disagg", A = "a", B="b",
            RES = "residuals", ML = "ml", PARAMETER = "parameter", EPARAMETER = "eparameter";

    public TemporalDisaggregationIExtractor() {
        set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        set(A, Double.class, source -> source.getA());
        set(B, Double.class, source -> source.getB());
        set(PARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            double[] p = source.getMaximum().getParameters();
            return p.length == 0 ? Double.NaN : p[0];
        });
        delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getLikelihood());
    }

    @Override
    public Class getSourceClass() {
        return TemporalDisaggregationIResults.class;
    }

}

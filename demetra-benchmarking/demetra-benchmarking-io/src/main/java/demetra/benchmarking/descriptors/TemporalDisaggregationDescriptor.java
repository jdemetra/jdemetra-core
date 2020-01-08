/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.descriptors;

import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import demetra.descriptors.stats.MaximumLikelihoodDescriptor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.linearmodel.Coefficient;
import demetra.linearmodel.LinearModelEstimation;
import demetra.math.matrices.MatrixType;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TemporalDisaggregationDescriptor{

    public static final String LIKELIHOOD = "likelihood", DISAGG = "disagg", EDISAGG = "edisagg",
            RES = "residuals", ML = "ml", COEFF="coeff", NX="nx", COVAR="covar", C="c", REGEFFECT = "regeffect";

    private static final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        MAPPING.set(REGEFFECT, TsData.class, source -> source.getRegressionEffects());
        MAPPING.delegate(ML, MaximumLikelihoodDescriptor.getMapping(), source -> source.getMaximum());
        MAPPING.setArray(COEFF, 1, 9, Coefficient.class, (TemporalDisaggregationResults source, Integer i)->
        {
            LinearModelEstimation estimation = source.getEstimation();
            Coefficient[] coefficients = estimation.getCoefficients();
            if (i>=1 && i<=coefficients.length){
                return coefficients[i-1];
            }
            return null;
        });
        MAPPING.set(NX, Integer.class, source->source.getEstimation().nx());
        MAPPING.set(C, double[].class, source->source.getEstimation().values().toArray());
        MAPPING.set(COVAR, MatrixType.class, source->source.getEstimation().getCovariance());
        MAPPING.delegate(LIKELIHOOD, LikelihoodStatisticsDescriptor.getMapping(), source -> source.getLikelihood());
        MAPPING.delegate(RES, ResidualsDiagnosticsDescriptor.getMapping(), source -> source.getResidualsDiagnostics());
    }

    public static InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }
   
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.extractors;

import demetra.benchmarking.extractors.ResidualsDiagnosticsExtractor;
import demetra.data.ParameterEstimation;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.linearmodel.LinearModelEstimation;
import demetra.math.matrices.MatrixType;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TemporalDisaggregationExtractor{

    public final String LIKELIHOOD = "likelihood", DISAGG = "disagg", EDISAGG = "edisagg",
            RES = "residuals", ML = "ml", COEFF="coeff", NX="nx", COVAR="covar", C="c", REGEFFECT = "regeffect";

    private final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        MAPPING.set(REGEFFECT, TsData.class, source -> source.getRegressionEffects());
        MAPPING.setArray(COEFF, 1, 9, ParameterEstimation.class, (TemporalDisaggregationResults source, Integer i)->
        {
            LinearModelEstimation estimation = source.getEstimation();
            ParameterEstimation[] coefficients = estimation.getCoefficients();
            if (i>=1 && i<=coefficients.length){
                return coefficients[i-1];
            }
            return null;
        });
        MAPPING.set(NX, Integer.class, source->source.getEstimation().nx());
        MAPPING.set(C, double[].class, source->source.getEstimation().values().toArray());
        MAPPING.set(COVAR, MatrixType.class, source->source.getEstimation().getCovariance());
        MAPPING.delegate(LIKELIHOOD, LikelihoodStatisticsExtractor.getMapping(), source -> source.getLikelihood());
        MAPPING.delegate(RES, ResidualsDiagnosticsExtractor.getMapping(), source -> source.getResidualsDiagnostics());
    }

    public InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }
   
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.univariate;

import demetra.likelihood.DiffuseConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.linearmodel.LinearModelEstimation;
import demetra.linearmodel.LinearModelType;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class TemporalDisaggregationResults {
    private @lombok.NonNull LinearModelType regressionModel;
    private @lombok.NonNull LinearModelEstimation estimation;
    private @lombok.NonNull TsData disaggregatedSeries;
    private @lombok.NonNull TsData stdevDisaggregatedSeries;
    private @lombok.NonNull TsData residuals, whiteNoiseResiduals;
    private DiffuseConcentratedLikelihood concentratedLikelihood;
    private LikelihoodStatistics likelihoodStatistics;
    private MaximumLogLikelihood maximum;
}

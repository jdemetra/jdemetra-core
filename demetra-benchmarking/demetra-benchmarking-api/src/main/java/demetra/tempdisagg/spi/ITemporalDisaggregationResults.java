/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.spi;

import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MaximumLogLikelihood;
import demetra.linearmodel.LinearModelEstimation;
import demetra.linearmodel.LinearModelType;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ITemporalDisaggregationResults {
    LinearModelType getRegressionModel();
    LinearModelEstimation getEstimation();
    TsData getDisaggregatedSeries();
    TsData getStdevDisaggregatedSeries();
    TsData getResiduals();
    TsData getWhiteNoiseResiduals();
    LikelihoodStatistics getLikelihoodStatistics();
    MaximumLogLikelihood getMaximum();
}

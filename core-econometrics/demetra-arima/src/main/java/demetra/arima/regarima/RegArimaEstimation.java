/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.arima.regarima;

import demetra.arima.IArimaModel;
import demetra.arima.estimation.FastKalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.Likelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.linearmodel.LinearModel;

/**
 *
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class RegArimaEstimation<M extends IArimaModel> {

    public static <M extends IArimaModel> RegArimaEstimation<M> compute(RegArimaModel<M> model) {
        ConcentratedLikelihoodEstimation estimation=new ConcentratedLikelihoodEstimation(null, null);
        if (estimation.estimate(model)){
            return new RegArimaEstimation<>(model, estimation.getLikelihood());
        }else
            return null;
    }

    /**
     *
     */
    RegArimaModel<M> model;
    /**
     *
     */
    ConcentratedLikelihood likelihood;

    /**
     *
     * @param nparams
     * @param adj
     * @return
     */
    public LikelihoodStatistics statistics(int nparams, double adj) {
        return LikelihoodStatistics.statistics(likelihood.logLikelihood(), model.getObservationsCount()-model.getMissingValuesCount())
                .llAdjustment(adj)
                .differencingOrder(model.getDifferencingOrder())
                .parametersCount(nparams+model.getVariablesCount()+1)
                .ssq(likelihood.ssq())
                .build();

    }

    public DoubleSequence fullResiduals() {
        // compute the residuals...
        if (model.getVariablesCount() == 0) {
            return likelihood.e();
        }
        LinearModel lm = model.differencedModel().getLinearModel();
        DataBlock e = lm.calcResiduals(likelihood.coefficients());
        FastKalmanFilter kf = new FastKalmanFilter(model.arma());
        Likelihood ll = kf.process(e);
        return ll.e();
    }
}

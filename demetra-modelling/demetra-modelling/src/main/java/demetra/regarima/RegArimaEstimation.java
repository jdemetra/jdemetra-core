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
package demetra.regarima;

import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.internal.ConcentratedLikelihoodEstimation;
import demetra.regarima.internal.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;

/**
 *
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class RegArimaEstimation<M extends IArimaModel> {

    public static <M extends IArimaModel> RegArimaEstimation<M> compute(RegArimaModel<M> model) {
        
        ConcentratedLikelihoodComputer computer = new ConcentratedLikelihoodComputer(null, null, true);
        return new RegArimaEstimation<>(model, computer.compute(model));
    }

//    public static <M extends IArimaModel> RegArimaEstimation<M> compute(RegArmaModel<M> model) {
//        
//        ConcentratedLikelihoodComputer computer = new ConcentratedLikelihoodComputer(null, null, true);
//        return new RegArimaEstimation<>(null, computer.compute(model));
//    }
    /**
     *
     */
    RegArimaModel<M> model;
    /**
     *
     */
    ConcentratedLikelihoodEstimation<M> concentratedLikelihood;

    /**
     *
     * @param nparams
     * @param adj
     * @return
     */
    public LikelihoodStatistics statistics(int nparams, double adj) {
        ConcentratedLikelihood ll = concentratedLikelihood.getLikelihood();
        return LikelihoodStatistics.statistics(ll.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(adj)
                .differencingOrder(model.arima().getNonStationaryAROrder())
                .parametersCount(nparams + model.getVariablesCount() + 1)
                .ssq(ll.ssq())
                .build();

    }
}

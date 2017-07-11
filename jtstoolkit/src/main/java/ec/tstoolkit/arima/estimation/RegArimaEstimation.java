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

package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Likelihood;

/**
 * 
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RegArimaEstimation<M extends IArimaModel> {

    /**
     *
     */
    public final RegArimaModel<M> model;
    /**
     *
     */
    public final ConcentratedLikelihood likelihood;

    /**
     * 
     * @param model
     * @param computeLikelihood
     */
    public RegArimaEstimation(RegArimaModel<M> model,
	    ConcentratedLikelihood likelihood) {
	this.model = model;
	this.likelihood = likelihood;
    }

    /**
     * 
     * @param nparams
     * @param adj
     * @return
     */
    public LikelihoodStatistics statistics(int nparams, double adj) {
	LikelihoodStatistics stats = new LikelihoodStatistics();
	stats.observationsCount = model.getObsCount();
	stats.effectiveObservationsCount = stats.observationsCount
		- model.getArima().getNonStationaryARCount();
	stats.logLikelihood = likelihood.getLogLikelihood();
	stats.estimatedParametersCount = model.getVarsCount() + nparams + 1;
	if (Double.isNaN(adj))
	    adj = 0;
	stats.transformationAdjustment = adj;
	stats.adjustedLogLikelihood = adj == 0 ? stats.logLikelihood
		: stats.logLikelihood + stats.transformationAdjustment;
	stats.SsqErr = likelihood.getSsqErr();
        stats.adjustForMissing(model.getMissingsCount());
	stats.calc();
	return stats;
    }

    public double[] fullResiduals(){
        // compute the residuals...
        if (model.getVarsCount() == 0)
            return likelihood.getResiduals();
        DataBlock e=model.getDModel().calcRes(new DataBlock(likelihood.getB()));
        ArmaKF kf=new ArmaKF(model.getArma());
        Likelihood tmp=new Likelihood();
        kf.process(e, tmp);
        return tmp.getResiduals();
    }
}

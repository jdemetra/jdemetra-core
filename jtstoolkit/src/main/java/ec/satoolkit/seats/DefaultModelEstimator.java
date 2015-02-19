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


package ec.satoolkit.seats;

import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaMapping2;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultModelEstimator implements IModelEstimator {
    
    private final IModelValidator validator;
    
    public DefaultModelEstimator(IModelValidator validator){
        this.validator=validator;
    }

    /**
     *
     * @param ml
     * @param model
     * @param info
     * @return
     */
    @Override
    public boolean estimate(boolean ml, SeatsModel model, InformationSet info) {
	GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        monitor.setPrecision(1e-7);
	monitor.useMaximumLikelihood(ml);

	RegArimaEstimation<SarimaModel> estimation = monitor.process(model
		.getRegarima());
	if (estimation == null)
	    return false;

        SarimaModel arima=estimation.model.getArima();
        if (validator != null && ModelStatus.Changed == validator.validate(arima, info))
            arima=validator.getNewModel();
	model.setModel(arima);
        LikelihoodStatistics stat = estimation.statistics(arima.getParametersCount(), 0);
        model.setSer(Math.sqrt(stat.SsqErr/(stat.effectiveObservationsCount-stat.estimatedParametersCount)));
	info.subSet("gls").set("likelihood", estimation.likelihood);
	return true;
    }

}

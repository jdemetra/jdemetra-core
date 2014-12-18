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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.arima.IModelEstimator;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class X13Estimator implements IModelEstimator {

    private double eps_ = 1e-7;

    public X13Estimator(double eps) {
        eps_ = eps;
    }

    @Override
    public boolean estimate(ModellingContext context) {

        IParametricMapping<SarimaModel> mapping = X13Preprocessor.createDefaultMapping(context.description);
        RegArimaEstimator monitor = new RegArimaEstimator(mapping);
        monitor.setMinimizer(new LevenbergMarquardtMethod());
        monitor.setPrecision(eps_);

        ModelEstimation estimation = new ModelEstimation(context.description.buildRegArima(),
                context.description.getLikelihoodCorrection());
        try {
            estimation.compute(monitor, mapping.getDim());
            estimation.updateParametersCovariance(monitor.getParametersCovariance());
            if (mapping.getDim() > 0) {
                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, monitor.getScore());
            }
            context.estimation = estimation;

            return true;
        } catch (RuntimeException err) {
            context.estimation = null;
            return false;
        }
    }
}

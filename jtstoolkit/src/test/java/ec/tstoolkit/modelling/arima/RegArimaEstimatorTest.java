/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.bfgs.Bfgs;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class RegArimaEstimatorTest {

    public RegArimaEstimatorTest() {
    }

//    @Test
    public void demoAirline() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        SarimaModel model = new SarimaModel(spec);
        RegArimaModel<SarimaModel> reg = new RegArimaModel<>();
        reg.setArima(model);
        reg.setY(new DataBlock(data.Data.M1));
        RegArimaEstimator estimator = new RegArimaEstimator(new SarimaMapping(spec, true));
        estimator.optimize(reg);
        Matrix M = estimator.getParametersCovariance();
        System.out.println(M);
        estimator.setMinimizer(new Bfgs());
        estimator.setLogLikelihood(true);
        estimator.optimize(reg);
        M = estimator.getParametersCovariance();
        System.out.println(M);

    }

}

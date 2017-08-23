/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.arima.regarima.internals;

import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ssq.ISsqFunctionDerivatives;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class RegArmaProcessor {

    private final boolean ml, mt;

    public RegArmaProcessor(boolean ml, boolean mt) {
        this.ml = ml;
        this.mt = mt;
    }

    public <S extends IArimaModel> RegArmaEstimation<S> compute(RegArmaModel<S> model, IParametricMapping<S> mapping, ISsqFunctionMinimizer minimizer, int ndf) {
        // step 1. Build the function
        RegArmaSsqFunction fn = RegArmaSsqFunction.builder(model.getY())
                .variables(model.getX())
                .missingCount(model.getMissingCount())
                .mapping(mapping)
                .maximumLikelihood(ml)
                .parallelProcessing(mt)
                .build();

        boolean ok = minimizer.minimize(fn);
        RegArmaSsqFunction.Evaluation<S> rslt = (RegArmaSsqFunction.Evaluation<S>) minimizer.getResult();
        ISsqFunctionDerivatives derivatives = rslt.ssqDerivatives();
        double objective = rslt.getSsqE();
        Matrix hessian = derivatives.hessian();
        double[] score = derivatives.gradient().toArray();
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < score.length; ++i) {
            score[i] *= (-.5 * ndf) / objective;
        }
        RegArmaModel<S> nmodel = new RegArmaModel<>(model.getY(), rslt.arma, model.getX(), model.getMissingCount());
        return new RegArmaEstimation<>(nmodel, objective, ok, score, hessian, ndf);
    }
}

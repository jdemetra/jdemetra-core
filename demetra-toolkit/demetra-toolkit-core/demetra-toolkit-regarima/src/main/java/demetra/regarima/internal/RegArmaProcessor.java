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
package demetra.regarima.internal;

import demetra.regarima.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.maths.functions.IFunctionDerivatives;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.NumericalDerivatives;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.SsqProxyFunctionPoint;
import demetra.maths.matrices.FastMatrix;
import demetra.arima.estimation.IArimaMapping;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class RegArmaProcessor {

    private final boolean ml, mt, fast;

    public RegArmaProcessor(boolean ml, boolean mt, boolean fastDerivatives) {
        this.ml = ml;
        this.mt = mt;
        this.fast = fastDerivatives;
    }

    public <S extends IArimaModel> RegArmaEstimation<S> compute(@Nonnull RegArmaModel<S> model, @Nonnull DoubleSeq start, IArimaMapping<S> mapping, ISsqFunctionMinimizer minimizer, int ndf) {
        // step 1. Build the function
        RegArmaSsqFunction fn = RegArmaSsqFunction.builder(model.getY())
                .variables(model.getX())
                .missingCount(model.getMissingCount())
                .mapping(mapping)
                .maximumLikelihood(ml)
                .parallelProcessing(mt)
                .build();

        boolean ok = minimizer.minimize(fn.ssqEvaluate(start));
        RegArmaSsqFunction.Evaluation<S> rslt = (RegArmaSsqFunction.Evaluation<S>) minimizer.getResult();
        double objective = rslt.getSsqE();
        FastMatrix hessian;
        double[] gradient;
        if (fast) {
            gradient = minimizer.gradientAtMinimum().toArray();
            hessian = minimizer.curvatureAtMinimum();
        } else {
            IFunctionDerivatives derivatives = new NumericalDerivatives(new SsqProxyFunctionPoint(rslt), false);
            hessian = derivatives.hessian();
            gradient = derivatives.gradient().toArray();
        }
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < gradient.length; ++i) {
            gradient[i] *= (-.5 * ndf) / objective;
        }
        RegArmaModel<S> nmodel = RegArmaModel.of(model, rslt.arma);
        return new RegArmaEstimation<>(nmodel, objective, ok, rslt.getParameters().toArray(), gradient, hessian, ndf);
    }
}

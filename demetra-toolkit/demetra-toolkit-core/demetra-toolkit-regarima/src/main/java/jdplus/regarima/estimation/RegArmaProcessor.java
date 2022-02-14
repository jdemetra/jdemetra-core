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
package jdplus.regarima.estimation;

import jdplus.regarima.RegArmaModel;
import jdplus.arima.IArimaModel;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.matrices.FastMatrix;
import jdplus.arima.estimation.IArimaMapping;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.stats.likelihood.DefaultLikelihoodEvaluation;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;

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

    public <S extends IArimaModel> RegArmaEstimation<S> compute(@NonNull RegArmaModel<S> model, @NonNull DoubleSeq start, IArimaMapping<S> mapping, SsqFunctionMinimizer minimizer, int ndf) {
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
        double objective;
        FastMatrix hessian;
        double[] gradient;
        if (fast) {
            gradient = minimizer.gradientAtMinimum().toArray();
            hessian = minimizer.curvatureAtMinimum();
            objective = rslt.getSsqE();
        } else {
            // we have to compute the Hessian of the Arma model build on the residuals
            // otherwise, we might under-estimate the T-Stats.
            // the differences are usually very small;
            // it is normal: coeff and params are asymptotically independent...
            DataBlock res = model.asLinearModel().calcResiduals(rslt.allCoefficients());
            int nm = model.getMissingCount();
            FastMatrix xm = FastMatrix.EMPTY;
            if (nm > 0) {
                FastMatrix x = model.getX();
                xm = x.extract(0, x.getRowsCount(), 0, nm);
            }
            RegArmaFunction fnr = RegArmaFunction.builder(res)
                    .variables(xm)
                    .missingCount(nm)
                    .mapping(mapping)
                    .likelihoodEvaluation(DefaultLikelihoodEvaluation.deviance())
                    .build();
            RegArmaFunction.Evaluation eval = fnr.evaluate(rslt.getParameters());
            IFunctionDerivatives derivatives = eval.derivatives();
            gradient = derivatives.gradient().toArray();
            hessian = derivatives.hessian();
            objective = eval.getValue();
        }
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < gradient.length; ++i) {
            gradient[i] *= (-.5 * ndf) / objective;
        }
        RegArmaModel<S> nmodel = RegArmaModel.of(model, rslt.arma);
        return new RegArmaEstimation<>(rslt.getParameters(), DoubleSeq.of(gradient), hessian);
    }

}

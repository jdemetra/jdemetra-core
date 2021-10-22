/*
* Copyright 2020 National Bank of Belgium
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
package jdplus.regarima;

import demetra.data.DoubleSeq;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import nbbrd.design.Development;
import demetra.modelling.DeprecatedLinearModel;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import java.util.List;
import java.util.function.Function;
import jdplus.arima.IArimaModel;
import jdplus.dstats.T;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class ApiUtility {

//    public <S extends IArimaModel, R> demetra.modelling.StochasticLinearModel<R> toApi(RegArimaEstimation<S> regarima, Function<S, R> fn) {
//        RegArimaModel<S> model = regarima.getModel();
//        double[] y = model.getY().toArray();
//        int[] missing = model.missing();
//        for (int i = 0; i < missing.length; ++i) {
//            y[missing[i]] = Double.NaN;
//        }
//
//        Matrix X;
//        List<DoubleSeq> x = model.getX();
//        if (x.isEmpty()) {
//            X = null;
//        } else {
//            double[] all = new double[y.length * x.size()];
//            int pos = 0;
//            for (DoubleSeq xcur : x) {
//                xcur.copyTo(all, pos);
//                pos += y.length;
//            }
//            X = Matrix.of(all, y.length, x.size());
//        }
//
//        ConcentratedLikelihoodWithMissing ll = regarima.getConcentratedLikelihood();
//        LogLikelihoodFunction.Point<RegArimaModel<S>, ConcentratedLikelihoodWithMissing> max = regarima.getMax();
//        int nhp = max.getParameters().length();
//        ParametersEstimation p = null;
//        if (nhp > 0) {
//            // TODO: adjust the computation of the covariance of the parameters
//            p = new ParametersEstimation(
//                    max.getParameters(), max.asymptoticCovariance(), max.getScore(), null);
//        }
//
//        T t = new T(ll.degreesOfFreedom() - nhp);
//        DoubleSeq b = ll.coefficients();
//        Matrix cov = ll.covariance(nhp, true).unmodifiable();
//        ParametersEstimation all = new ParametersEstimation(b, cov, null, null);
//
//        MissingValueEstimation[] me = null;
//        if (missing.length > 0) {
//            me = new MissingValueEstimation[missing.length];
//            int ndf = ll.degreesOfFreedom() - nhp;
//            double sig2 = ll.ssq() / ndf;
//            DoubleSeq mvar = ll.missingUnscaledVariances();
//            DoubleSeq mval = ll.missingCorrections();
//            for (int i = 0; i < missing.length; ++i) {
//                me[i] = new MissingValueEstimation(missing[i], mval.get(i), Math.sqrt(mvar.get(i) / sig2));
//            }
//        }
//        
//        LinearModel lm = LinearModel.builder()
//                .y(DoubleSeq.of(y))
//                .X(X)
//                .missing(me)
//                .coefficients(all)
//                .build();
//        StochasticModel<R> sm=new StochasticModel<>(fn.apply(model.arima()), p);        
//
//        return new StochasticLinearModel<>(lm, sm, regarima.statistics());
//
//    }

//    public GeneralLinearModel toApi(ModelEstimation estimation) {
//        ConcentratedLikelihoodWithMissing cll = estimation.getConcentratedLikelihood();
//        RegArimaModel<SarimaModel> lm = estimation.getModel();
//        
//        return GeneralLinearModelEstimation.<demetra.arima.SarimaModel>builder()
//                .originalSeries(estimation.getOriginalSeries())
//                .estimationDomain(estimation.getEstimationDomain())
//                .logTransformation(estimation.isLogTransformation())
//                .lpTransformation(estimation.getLpTransformation())
//                .variables(estimation.getVariables())
//                .y(lm.getY())
//                .X(lm.variables())
//                .stochasticComponent(jdplus.modelling.ApiUtility.toApi(estimation.getModel().arima(), null))
//                .coefficients(new ParametersEstimation(cll.coefficients(), cll.covariance(estimation.getFreeArimaParametersCount(), true), null, null))
//                .parameters(new ParametersEstimation(DoubleSeq.of(estimation.getArimaParameters()), estimation.getArimaCovariance().unmodifiable(), 
//                        DoubleSeq.of(estimation.getArimaScore()), "arima"))
//                .missing(missing(estimation))
//                .statistics(estimation.getStatistics())
//                .build();
//    }

//    public MissingValueEstimation[] missing(ModelEstimation estimation) {
//        MissingValueEstimation[] missing = null;
//        int nmissing = estimation.getConcentratedLikelihood().nmissing();
//        if (nmissing > 0) {
//            int[] missingPos = estimation.getMissing();
//            double[] missingVal = estimation.getConcentratedLikelihood().missingCorrections().toArray();
//            double[] missingErr = estimation.getConcentratedLikelihood().missingCorrections().toArray();
//            missing = new MissingValueEstimation[nmissing];
//            DoubleSeq y = estimation.getModel().getY();
//            for (int i = 0; i < nmissing; ++i) {
//                missing[i] = new MissingValueEstimation(missingPos[i], y.get(missingPos[i]) - missingVal[i], missingErr[i]);
//            }
//        }
//        return missing;
//    }
}

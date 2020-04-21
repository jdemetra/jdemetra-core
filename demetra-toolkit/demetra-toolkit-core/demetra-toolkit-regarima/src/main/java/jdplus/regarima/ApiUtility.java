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
import demetra.data.MissingValueEstimation;
import demetra.data.ParameterEstimation;
import demetra.data.ParametersEstimation;
import demetra.design.Development;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import java.util.List;
import java.util.function.Function;
import jdplus.arima.IArimaModel;
import jdplus.dstats.T;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class ApiUtility {

    public <S extends IArimaModel, R> demetra.modelling.regarima.RegArimaEstimation<R> toApi(RegArimaEstimation<S> regarima, Function<S, R> fn) {
        RegArimaModel<S> model = regarima.getModel();
        double[] y = model.getY().toArray();
        int[] missing = model.missing();
        for (int i = 0; i < missing.length; ++i) {
            y[missing[i]] = Double.NaN;
        }

        MatrixType X;
        List<DoubleSeq> x = model.getX();
        if (x.isEmpty()) {
            X = null;
        } else {
            double[] all = new double[y.length * x.size()];
            int pos = 0;
            for (DoubleSeq xcur : x) {
                xcur.copyTo(all, pos);
                pos += y.length;
            }
            X = MatrixType.of(all, y.length, x.size());
        }

        ConcentratedLikelihoodWithMissing ll = regarima.getConcentratedLikelihood();
        LogLikelihoodFunction.Point<RegArimaModel<S>, ConcentratedLikelihoodWithMissing> max = regarima.getMax();
        int nhp = max.getParameters().length;
        ParametersEstimation p = null;
        if (nhp > 0) {
            // TODO: adjust the computation of the covariance of the parameters
            p = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), null);
        }

        ParameterEstimation mean = null;
        T t = new T(ll.degreesOfFreedom() - nhp);
        int x0 = 0, x1 = ll.nx();
        if (model.isMean()) {
            x0++;
            double m = ll.coefficient(0);
            double ser = ll.ser(0, nhp, true);
            double tval = Math.abs(m / ser);
            mean = new ParameterEstimation(m, ser, 1 - t.getProbabilityForInterval(-tval, tval), "mean");
        }
        double[] b = null;
        MatrixType cov = MatrixType.EMPTY;
        if (x1 > x0) {
            b = ll.coefficients().range(x0, x1).toArray();
            cov = ll.covariance(nhp, true).extract(x0, b.length, x0, b.length).unmodifiable();
        }
        ParametersEstimation all = new ParametersEstimation(b, cov, null);

        MissingValueEstimation[] me = null;
        if (missing.length > 0) {
            me = new MissingValueEstimation[missing.length];
            int ndf = ll.degreesOfFreedom() - nhp;
            double sig2 = ll.ssq() / ndf;
            DoubleSeq mvar = ll.missingUnscaledVariances();
            DoubleSeq mval = ll.missingCorrections();
            for (int i = 0; i < missing.length; ++i) {
                me[i] = new MissingValueEstimation(missing[i], mval.get(i), Math.sqrt(mvar.get(i) / sig2));
            }
        }
        return demetra.modelling.regarima.RegArimaEstimation.<R>builder()
                .arima(fn.apply(model.arima()))
                .y(y)
                .X(X)
                .meanCorrection(mean)
                .coefficients(all)
                .likelihood(regarima.statistics())
                .missing(me)
                .parameters(p)
                .build();

    }

    public LinearModelEstimation<demetra.arima.SarimaModel> toApi(ModelEstimation estimation) {
        return LinearModelEstimation.<demetra.arima.SarimaModel>builder()
                .originalSeries(estimation.getOriginalSeries())
                .estimationDomain(estimation.getEstimationDomain())
                .logTransformation(estimation.isLogTransformation())
                .lpTransformation(estimation.getLpTransformation())
                .meanCorrection(estimation.getModel().isMean())
                .variables(estimation.getVariables())
                .stochasticComponent(jdplus.modelling.ApiUtility.toApi(estimation.getModel().arima(), null))
                .coefficients(estimation.getConcentratedLikelihood().coefficients().toArray())
                .coefficientsCovariance(estimation.getConcentratedLikelihood().covariance(estimation.getFreeParametersCount(), true))
                .parameters(estimation.getParameters())
                .score(estimation.getScore())
                .parametersCovariance(estimation.getParametersCovariance().unmodifiable())
                .missing(missing(estimation))
                .statistics(estimation.getStatistics())
                .freeParametersCount(estimation.getFreeParametersCount())
                .build();
    }

    private MissingValueEstimation[] missing(ModelEstimation estimation) {
        MissingValueEstimation[] missing = null;
        int nmissing = estimation.getConcentratedLikelihood().nmissing();
        if (nmissing > 0) {
            int[] missingPos = estimation.getMissing();
            double[] missingVal = estimation.getConcentratedLikelihood().missingCorrections().toArray();
            double[] missingErr = estimation.getConcentratedLikelihood().missingCorrections().toArray();
            missing = new MissingValueEstimation[nmissing];
            DoubleSeq y = estimation.getModel().getY();
            for (int i = 0; i < nmissing; ++i) {
                missing[i] = new MissingValueEstimation(missingPos[i], y.get(missingPos[i]) - missingVal[i], missingErr[i]);
            }
        }
        return missing;
    }
}

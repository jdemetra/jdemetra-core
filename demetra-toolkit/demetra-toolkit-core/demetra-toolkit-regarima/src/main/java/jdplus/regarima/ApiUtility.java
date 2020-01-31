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
import java.util.List;
import java.util.function.Function;
import jdplus.arima.IArimaModel;
import jdplus.dstats.T;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;

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
            p=new ParametersEstimation(max.getParameters(), max.asymptoticCovariance());
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
        ParametersEstimation all = new ParametersEstimation(b, cov);

        MissingValueEstimation[] me = null;
        if (missing.length > 0) {
            me = new MissingValueEstimation[missing.length];
            int ndf = ll.degreesOfFreedom() - nhp;
            double sig2 = ll.ssq() / ndf;
            DoubleSeq mvar = ll.missingUnscaledVariances();
            DoubleSeq mval = ll.missingEstimates();
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

}

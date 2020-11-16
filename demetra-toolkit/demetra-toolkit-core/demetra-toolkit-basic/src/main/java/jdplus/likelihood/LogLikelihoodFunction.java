/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.likelihood;

import nbbrd.design.Development;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import java.util.function.Function;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 * @param <T>
 * @param <L>
 */
@Development(status = Development.Status.Release)
public class LogLikelihoodFunction<T, L extends Likelihood> implements IFunction {

    /**
     * Mapping from T to the parameters Theta
     */
    private final IParametricMapping<T> mapping;

    /**
     * Function that computes the log-likelihood
     */
    private final Function<T, L> function;

    public LogLikelihoodFunction(final IParametricMapping<T> mapping, final Function<T, L> function) {
        this.mapping = mapping;
        this.function = function;
    }

    public IParametricMapping<T> getMapping() {
        return mapping;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new llPoint(parameters);
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    private class llPoint implements IFunctionPoint {

        private final DoubleSeq parameters;
        private final T t;

        llPoint(DoubleSeq parameters) {
            this.parameters = parameters;
            this.t = mapping.map(parameters);
        }

        @Override
        public IFunction getFunction() {
            return LogLikelihoodFunction.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return parameters;
        }

        @Override
        public double getValue() {
            Likelihood l = function.apply(t);
            if (l == null) {
                return Double.NaN;
            } else {
                return l.logLikelihood();
            }
        }
    }

    public Point point(DoubleSeq parameters) {
        IFunctionDerivatives d = this.evaluate(parameters).derivatives();
        Matrix H = d.hessian();
        H.chs();
        return new Point(this, parameters.toArray(), d.gradient().toArray(), H);
    }

    @lombok.Value
    public static class Point<T, L extends Likelihood> {

        public static <T, L extends Likelihood> Point ofNegativeLogLikelihood(LogLikelihoodFunction<T, L> function,
                double[] p, double[] grad, Matrix hessian) {
            double[] score;
            if (grad.length == 0) {
                score = grad;
            } else {
                score = grad.clone();
            }
            for (int i = 0; i < score.length; ++i) {
                score[i] = -score[i];
            }
            return new Point<>(function, p, score, hessian);
        }

        private LogLikelihoodFunction<T, L> function;
        /**
         * Parameters of the log-likelihood function at this point
         */
        private double[] parameters;
        /**
         * Score (or gradient) of the log-likelihood function at this point.
         */
        private double[] score;

        /**
         * Observed Information matrix at this point.
         */
        private Matrix information;

        public Matrix asymptoticCovariance() {
            return SymmetricMatrix.inverse(information);
        }

    }
}

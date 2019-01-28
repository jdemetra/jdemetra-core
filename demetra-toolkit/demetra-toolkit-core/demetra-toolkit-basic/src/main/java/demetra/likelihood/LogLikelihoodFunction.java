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
package demetra.likelihood;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionDerivatives;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 * @param <T>
 * @param <L>
 */
@Development(status = Development.Status.Release)
public class LogLikelihoodFunction<T, L extends ILikelihood> implements IFunction {

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
   
    public IParametricMapping<T> getMapping(){
        return mapping;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSequence parameters) {
        return new llPoint(parameters);
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    private class llPoint implements IFunctionPoint {

        private final DoubleSequence parameters;
        private final T t;

        llPoint(DoubleSequence parameters) {
            this.parameters = parameters;
            this.t = mapping.map(parameters);
        }

        @Override
        public IFunction getFunction() {
            return LogLikelihoodFunction.this;
        }

        @Override
        public DoubleSequence getParameters() {
            return parameters;
        }

        @Override
        public double getValue() {
            ILikelihood l = function.apply(t);
            if (l == null) {
                return Double.NaN;
            } else {
                return l.logLikelihood();
            }
        }
    }
    
    public Point point(DoubleSequence parameters){
        IFunctionDerivatives d = this.evaluate(parameters).derivatives();
        return new Point(this, parameters.toArray(), d.gradient().toArray(), SymmetricMatrix.inverse(d.hessian()));
    }

    @lombok.Value
    public static class Point<T, L extends ILikelihood> {

        private LogLikelihoodFunction<T, L> function;
        private double[] parameters, gradient;
        private Matrix hessian;

    }
}

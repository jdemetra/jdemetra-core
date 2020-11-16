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
package jdplus.math.functions.ssq;

import nbbrd.design.Development;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.matrices.Matrix;
import demetra.data.DoubleSeq;
import jdplus.math.functions.FunctionMinimizer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ProxyMinimizer implements FunctionMinimizer {

    public static Builder builder(final SsqFunctionMinimizer.Builder builder) {
        return new ProxyBuilder(builder);
    }

    public static class ProxyBuilder implements Builder {

        private final SsqFunctionMinimizer.Builder builder;

        private ProxyBuilder(final SsqFunctionMinimizer.Builder builder) {
            this.builder = builder;
        }

        @Override
        public Builder functionPrecision(double eps) {
            builder.functionPrecision(eps);
            return this;
        }

        @Override
        public Builder maxIter(int niter) {
            builder.maxIter(niter);
            return this;
        }

        @Override
        public FunctionMinimizer build() {
            return new ProxyMinimizer(builder.build());
        }

    }

    private final SsqFunctionMinimizer minimizer;

    /**
     *
     * @param min
     */
    public ProxyMinimizer(SsqFunctionMinimizer min) {
        minimizer = min;
    }

    public SsqFunctionMinimizer getCore() {
        return minimizer;
    }

    @Override
    public Matrix curvatureAtMinimum() {
        return minimizer.curvatureAtMinimum();
    }

    @Override
    public DoubleSeq gradientAtMinimum() {
        return minimizer.gradientAtMinimum();
    }

    @Override
    public double getObjective() {
        return minimizer.getObjective();
    }

    @Override
    public IFunctionPoint getResult() {
        return (IFunctionPoint) minimizer.getResult();
    }

    @Override
    public boolean minimize(IFunctionPoint start) {
        IFunction function = start.getFunction();
        if (!(function instanceof ISsqFunction)) {
            return false;
        }
        ISsqFunction fn = (ISsqFunction) function;
        ISsqFunctionPoint s = fn.ssqEvaluate(start.getParameters());
        return minimizer.minimize(s);
    }
}

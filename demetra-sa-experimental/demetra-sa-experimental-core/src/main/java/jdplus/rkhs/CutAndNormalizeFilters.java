/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.maths.functions.GridSearch;
import jdplus.maths.functions.IFunction;
import jdplus.maths.functions.IFunctionPoint;
import jdplus.maths.functions.IParametersDomain;
import jdplus.maths.functions.ParametersRange;
import jdplus.maths.linearfilters.AsymmetricFilters;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CutAndNormalizeFilters {

    public FiniteFilter of(DoubleUnaryOperator kernel, double bandwidth, int m, int q) {
        return FiniteFilter.ofInternal(asymmetricWeights(kernel, bandwidth, m, q), -m);
    }

    private double[] asymmetricWeights(DoubleUnaryOperator kernel, double bandwidth, int m, int q) {
        double[] c = new double[m + q + 1];
        double s = 0;
        for (int i = -m; i <= q; ++i) {
            double w = kernel.applyAsDouble(i / bandwidth);
            c[m + i] = w;
            s += w;
        }
        for (int i = 0; i < c.length; ++i) {
            c[i] /= s;
        }
        return c;
    }

    static interface AsymmetricFilterProvider {

        FiniteFilter provide(double bandWitdth);
    }

    @lombok.Value
    static class D implements IFunction {

        private int m;
        private SymmetricFilter target;
        private AsymmetricFilters.Distance distance;
        private AsymmetricFilterProvider provider;

        @Override
        public IFunctionPoint evaluate(DoubleSeq parameters) {
            return new Point(parameters.get(0));
        }

        @Override
        public IParametersDomain getDomain() {
            return new ParametersRange(m, 3 * m, false);
        }

        class Point implements IFunctionPoint {

            final double bandWidth;

            Point(double bandWidth) {
                this.bandWidth = bandWidth;
            }

            @Override
            public IFunction getFunction() {
                return D.this;
            }

            @Override
            public DoubleSeq getParameters() {
                return DoubleSeq.of(bandWidth);
            }

            @Override
            public double getValue() {
                FiniteFilter f = provider.provide(bandWidth);
                return distance.compute(target, f);
            }

        }
    }

    public double optimalBandWidth(int m, int q, AsymmetricFilters.Distance distance) {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
        
         D fn = new D(m, H, distance, bandWidth->of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q));
        GridSearch grid = GridSearch.builder()
                .bounds(m, 3 * m)
                .build();
        grid.minimize(fn.evaluate(DoubleSeq.of(m + 1)));
        return ((D.Point) grid.getResult()).bandWidth;
    }

}

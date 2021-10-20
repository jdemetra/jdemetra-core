/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.math.functions.GridSearch;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.ParametersRange;
import jdplus.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
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
        private AsymmetricFiltersFactory.Distance distance;
        private AsymmetricFilterProvider provider;
        private double lbound;
        private double ubound;

        @Override
        public IFunctionPoint evaluate(DoubleSeq parameters) {
            return new Point(parameters.get(0));
        }

        @Override
        public IParametersDomain getDomain() {
            return new ParametersRange(lbound, ubound, false);
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

    public double optimalBandWidth(int m, int q, AsymmetricFiltersFactory.Distance distance) {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
        
        D fn = new D(m, H, distance, bandWidth->of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q),
        		m, 3*m);
        GridSearch grid = GridSearch.builder()
                .bounds(m, 3 * m)
                .build();
        grid.minimize(fn.evaluate(DoubleSeq.of(m + 1)));
        return ((D.Point) grid.getResult()).bandWidth;
    }
    public double optimalBandWidth(int m, int q, AsymmetricFiltersFactory.Distance distance,
    		DoubleUnaryOperator kernel, double lbound, double ubound) {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
        
        D fn = new D(m, H, distance, bandWidth->of(kernel, bandWidth, m, q),
        		lbound, ubound);
        GridSearch grid = GridSearch.builder()
                .bounds(lbound, ubound)
                .build();
        grid.minimize(fn.evaluate(DoubleSeq.of(Double.max(m + 1, lbound))));
        return ((D.Point) grid.getResult()).bandWidth;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import jdplus.maths.functions.GridSearch;
import jdplus.maths.functions.IFunction;
import jdplus.maths.functions.IFunctionPoint;
import jdplus.maths.functions.IParametersDomain;
import jdplus.maths.functions.ParametersRange;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.HendersonFilters;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.stats.Kernels;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AsymmetricFiltersUtilityTest {

    public AsymmetricFiltersUtilityTest() {
    }

    @Test
    public void testDistance() {
        int M = 6;
        for (int q = 0; q < M; ++q) {
            D1 fn = new D1(M, q);
            GridSearch grid = GridSearch.builder()
                    .bounds(M, 2 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M + 1)));
            double bandWidth = ((D1.Point) grid.getResult()).bandWidth;
            System.out.println(bandWidth);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            System.out.println("");
            for (int k=0; k<100; ++k){
                System.out.println(f.phaseFunction().applyAsDouble((k+1)*Math.PI/100));
            }
            System.out.println("");
        }
    }

    @Test
    public void testDistance2() {
        int M = 6;
        for (int q = 0; q < M; ++q) {
            D2 fn = new D2(M, q);
            GridSearch grid = GridSearch.builder()
                    .bounds(M, 2.5 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            double bandWidth = ((D2.Point) grid.getResult()).bandWidth;
            System.out.println(bandWidth);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            System.out.println("");
            for (int k=0; k<100; ++k){
                System.out.println(f.phaseFunction().applyAsDouble((k+1)*Math.PI/100));
            }
            System.out.println("");
        }
    }

    @Test
    public void testDistance3() {
        int M = 6;
        for (int q = 0; q < M; ++q) {
            D3 fn = new D3(M, q, 0, 1 / 16.0);
            GridSearch grid = GridSearch
                    .builder()
                    .bounds(M, 2 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            System.out.println(((D3.Point) grid.getResult()).bandWidth);
        }
    }
}

class D1 implements IFunction {

    final int m, q;
    final SymmetricFilter H;

    D1(int m, int q) {
        this.m = m;
        this.q = q;
        this.H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
//        this.H = HendersonFilters.ofLength(2 * m + 1);
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new Point(parameters.get(0));
    }

    @Override
    public IParametersDomain getDomain() {
        return new ParametersRange(m, 2 * m, false);
    }

    class Point implements IFunctionPoint {

        final double bandWidth;

        Point(double bandWidth) {
            this.bandWidth = bandWidth;
        }

        @Override
        public IFunction getFunction() {
            return D1.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(bandWidth);
        }

        @Override
        public double getValue() {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q);
            return AsymmetricFiltersUtility.distance(H, f);
        }

    }

}

class D2 implements IFunction {

    final int m, q;
    final SymmetricFilter H;

    D2(int m, int q) {
        this.m = m;
        this.q = q;
        this.H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
//        this.H = HendersonFilters.ofLength(2 * m + 1);
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new Point(parameters.get(0));
    }

    @Override
    public IParametersDomain getDomain() {
        return new ParametersRange(m, 2.5 * m, false);
    }

    class Point implements IFunctionPoint {

        final double bandWidth;

        Point(double bandWidth) {
            this.bandWidth = bandWidth;
        }

        @Override
        public IFunction getFunction() {
            return D2.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(bandWidth);
        }

        @Override
        public double getValue() {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q);
            return AsymmetricFiltersUtility.distance2(H, f);
        }

    }

}

class D3 implements IFunction {

    final int m, q;
    final double a, b;

    D3(int m, int q, double a, double b) {
        this.m = m;
        this.q = q;
        this.a = a;
        this.b = b;
    }

    D3(int m, int q) {
        this.m = m;
        this.q = q;
        this.a = 0;
        this.b = 1.0 / 16;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new Point(parameters.get(0));
    }

    @Override
    public IParametersDomain getDomain() {
        return new ParametersRange(1, 2.5 * m, false);
    }

    class Point implements IFunctionPoint {

        final double bandWidth;

        Point(double bandWidth) {
            this.bandWidth = bandWidth;
        }

        @Override
        public IFunction getFunction() {
            return D3.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(bandWidth);
        }

        @Override
        public double getValue() {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q);
            return AsymmetricFiltersUtility.distance3(f, a, b);
        }

    }

}

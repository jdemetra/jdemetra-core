/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.filters.MSEDecomposition;
import jdplus.maths.functions.GridSearch;
import jdplus.maths.functions.IFunction;
import jdplus.maths.functions.IFunctionPoint;
import jdplus.maths.functions.IParametersDomain;
import jdplus.maths.functions.ParametersRange;
import jdplus.maths.linearfilters.FiniteFilter;
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

    public static void frDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            D1 fn = new D1(sd, M, q);
            GridSearch grid = GridSearch.builder()
                    .bounds(M, 2 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M + 1)));
            double bandWidth = ((D1.Point) grid.getResult()).bandWidth;
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void accuracyDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            D2 fn = new D2(sd, M, q, true);
            GridSearch grid = GridSearch.builder()
                    .bounds(M, 2.5 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            double bandWidth = ((D2.Point) grid.getResult()).bandWidth;
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    private static void output(SymmetricFilter H, FiniteFilter f, double bandWidth, int output) {
        switch (output) {
            case 0:
                System.out.println(bandWidth);
                break;
            case 1:
                MSEDecomposition e = MSEDecomposition.of(null, H.frequencyResponseFunction(), f.frequencyResponseFunction(), 2 * Math.PI / 16.0);
                System.out.print('\t');
                System.out.print(e.getAccuracy());
                System.out.print('\t');
                System.out.print(e.getTimeliness());
                System.out.print('\t');
                System.out.print(e.getSmoothness());
                System.out.print('\t');
                System.out.println(e.getResidual());
                break;
            case 2:
                System.out.println(DoubleSeq.of(f.weightsToArray()));
                break;
            case 3:
                DoubleUnaryOperator g = f.squaredGainFunction();
                System.out.println(DoubleSeq.onMapping(200, i -> g.applyAsDouble(i * Math.PI / 200)));
                break;
            case 4:
                DoubleUnaryOperator p = f.phaseFunction();
                System.out.println(DoubleSeq.onMapping(200, i -> p.applyAsDouble(i * Math.PI / 1600)));
                break;
        }

    }

    public static void smoothnessDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            D2 fn = new D2(sd, M, q, false);
            GridSearch grid = GridSearch.builder()
                    .bounds(M, 2.5 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            double bandWidth = ((D2.Point) grid.getResult()).bandWidth;
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void timelinessDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            D3 fn = new D3(sd, M, q);
            GridSearch grid = GridSearch
                    .builder()
                    .bounds(M, 2 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            double bandWidth = ((D3.Point) grid.getResult()).bandWidth;
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void distance3bis(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            D3bis fn = new D3bis(sd, M, q, 0, 1 / 16.0);
            GridSearch grid = GridSearch
                    .builder()
                    .bounds(M, 2 * M)
                    .build();
            grid.minimize(fn.evaluate(DoubleSeq.of(M)));
            double bandWidth = ((D3bis.Point) grid.getResult()).bandWidth;
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    @Test
    public void testTimeliness() {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), 12, 11);
        for (double bandWidth = 11; bandWidth <= 100; bandWidth += 1) {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, 11, 7);
            double d = AsymmetricFiltersUtility.timelinessDistance(x -> 1/(1-Math.cos(x)), H, f, Math.PI/8);
            System.out.println(d);
//                System.out.println(DoubleSeq.of(f.weightsToArray()));
        }
    }

    public static void main(String[] arg) {
        int out = 0;
        DoubleUnaryOperator sd = x -> 1 ;/// (1.81 - 1.8 * Math.cos(x));
        System.out.println("Length=4");
        frDistance(sd, 4, out);
        System.out.println();
        accuracyDistance(sd, 4, out);
        System.out.println();
        smoothnessDistance(sd, 4, out);
        System.out.println();
        timelinessDistance(sd, 4, out);
        System.out.println();
        System.out.println("Length=6");
        frDistance(sd, 6, out);
        System.out.println();
        accuracyDistance(sd, 6, out);
        System.out.println();
        smoothnessDistance(sd, 6, out);
        System.out.println();
        timelinessDistance(sd, 6, out);
        System.out.println();
        System.out.println("Length=11");
        frDistance(sd, 11, out);
        System.out.println();
        accuracyDistance(sd, 11, out);
        System.out.println();
        smoothnessDistance(sd, 11, out);
        System.out.println();
        timelinessDistance(sd, 11, out);
    }
}

class D1 implements IFunction {

    final int m, q;
    final SymmetricFilter H;
    final DoubleUnaryOperator sd;

    D1(DoubleUnaryOperator sd, int m, int q) {
        this.m = m;
        this.q = q;
        this.sd = sd;
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
            return AsymmetricFiltersUtility.frequencyResponseDistance(sd, H, f);
        }

    }

}

class D2 implements IFunction {

    final int m, q;
    final SymmetricFilter H;
    final DoubleUnaryOperator sd;
    final double passBand = 2 * Math.PI / 16.0;
    final boolean accuracy;

    D2(DoubleUnaryOperator sd, int m, int q, boolean accuracy) {
        this.m = m;
        this.q = q;
        this.H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
//        this.H = HendersonFilters.ofLength(2 * m + 1);
        this.accuracy = accuracy;
        this.sd = sd;
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
            return accuracy ? AsymmetricFiltersUtility.accuracyDistance(sd, H, f, passBand)
                    : AsymmetricFiltersUtility.smoothnessDistance(sd, H, f, passBand);
        }

    }

}

class D3 implements IFunction {

    final int m, q;
    final double b;
    final SymmetricFilter H;
    final DoubleUnaryOperator sd;

    D3(DoubleUnaryOperator sd, int m, int q, double b) {
        this.m = m;
        this.q = q;
        this.b = b;
        this.sd = sd;
        this.H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
    }

    D3(DoubleUnaryOperator sd, int m, int q) {
        this.m = m;
        this.q = q;
        this.b = 2 * Math.PI / 16;
        this.sd = sd;
        this.H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
    }

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
            return D3.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(bandWidth);
        }

        @Override
        public double getValue() {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q);
            return AsymmetricFiltersUtility.timelinessDistance(sd, H, f, b);
        }

    }

}

class D3bis implements IFunction {

    final int m, q;
    final double a, b;
    final DoubleUnaryOperator sd;

    D3bis(DoubleUnaryOperator sd, int m, int q, double a, double b) {
        this.m = m;
        this.q = q;
        this.a = a;
        this.b = b;
        this.sd = sd;
    }

    D3bis(DoubleUnaryOperator sd, int m, int q) {
        this.m = m;
        this.q = q;
        this.a = 0;
        this.b = 2 * Math.PI / 16;
        this.sd = sd;
    }

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
            return D3bis.this;
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(bandWidth);
        }

        @Override
        public double getValue() {
            FiniteFilter f = KernelsUtility.cutAndNormalizeAsymmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q);
            return AsymmetricFiltersUtility.timelinessDistance2(sd, f, a, b);
        }

    }

}

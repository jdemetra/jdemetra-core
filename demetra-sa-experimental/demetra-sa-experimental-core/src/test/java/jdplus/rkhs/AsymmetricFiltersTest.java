/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.dfa.MSEDecomposition;
import jdplus.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
public class AsymmetricFiltersTest {

    public AsymmetricFiltersTest() {
    }

    public static void frDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            double bandWidth = CutAndNormalizeFilters.optimalBandWidth(M, q,
                    AsymmetricFiltersFactory.frequencyResponseDistance(sd));
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = CutAndNormalizeFilters.of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void accuracyDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            double bandWidth = CutAndNormalizeFilters.optimalBandWidth(M, q,
                    AsymmetricFiltersFactory.accuracyDistance(sd, 2 * Math.PI / 16.0));
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = CutAndNormalizeFilters.of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
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
                System.out.print(e.getSmoothness());
                System.out.print('\t');
                System.out.print(e.getTimeliness());
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
            double bandWidth = CutAndNormalizeFilters.optimalBandWidth(M, q,
                    AsymmetricFiltersFactory.smoothnessDistance(sd, 2 * Math.PI / 16.0));
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = CutAndNormalizeFilters.of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void timelinessDistance(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            double bandWidth = CutAndNormalizeFilters.optimalBandWidth(M, q,
                    AsymmetricFiltersFactory.timelinessDistance(sd, 2 * Math.PI / 16.0));
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = CutAndNormalizeFilters.of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void distance3bis(DoubleUnaryOperator sd, int M, int output) {
        for (int q = 0; q < M; ++q) {
            double bandWidth = CutAndNormalizeFilters.optimalBandWidth(M, q,
                    AsymmetricFiltersFactory.timelinessDistance2(sd, 0, 2 * Math.PI / 16.0));
            SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), M + 1, M);
            FiniteFilter f = CutAndNormalizeFilters.of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, M, q);
            output(H, f, bandWidth, output);
        }
    }

    public static void main(String[] arg) {
        int out = 1;
        DoubleUnaryOperator sd = x -> {
            double y = 2 - 2 * Math.cos(x);
            double z = 1.64 - 1.6 * Math.cos(x);
            return 1 / y;
        };
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

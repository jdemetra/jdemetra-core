/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import java.util.function.DoubleUnaryOperator;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.FastMatrix;
import jdplus.stats.Kernels;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class KernelsUtilityTest {

    public KernelsUtilityTest() {
    }

    @Test
    public void testSymmetricFilter() {
        int m = 11;
        double bandwidth = m + 1;
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(Kernels.BIWEIGHT, 3);
        SymmetricFilter s1 = KernelsUtility.symmetricFilter(kernel, bandwidth, m);
        SymmetricFilter s2 = LocalPolynomialFilterFactory.of(m, 3, DiscreteKernel.biweight(m));
        SymmetricFilter s3 = LocalPolynomialFilterFactory.of(m, 3, DiscreteKernel.henderson(m));
        FastMatrix S = FastMatrix.make(2 * m + 1, 3);
        S.column(0).copyFrom(s1.weightsToArray(), 0);
        S.column(1).copyFrom(s2.weightsToArray(), 0);
        S.column(2).copyFrom(s3.weightsToArray(), 0);
//        System.out.println(S);
    }

    @Test
    public void testBandWidth() {
        double b1 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.BIWEIGHT, 3), 4, 2);
        double b2 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.BIWEIGHT, 3), 6, 2);
        double b3 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.BIWEIGHT, 3), 11, 2);
        double c1 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.TRIWEIGHT, 3), 4, 2);
        double c2 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.TRIWEIGHT, 3), 6, 2);
        double c3 = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.TRIWEIGHT, 3), 11, 2);
        assertEquals(b1, 4.927, 1e-3);
        assertEquals(b2, 6.951, 1e-3);
        assertEquals(b3, 11.973, 1e-3);
        assertEquals(c1, 5.102, 1e-3);
        assertEquals(c2, 7.122, 1e-3);
        assertEquals(c3, 12.139, 1e-3);
    }

    public static void main(String[] args) {
        FastMatrix M = FastMatrix.make(100, 2);
        for (int i = 0; i < 100; ++i) {
            double b = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.BIWEIGHT, 3), i + 4, 2);
            double c = KernelsUtility.optimalBandWidth(HighOrderKernels.kernel(Kernels.TRIWEIGHT, 3), i + 4, 2);
            M.set(i, 0, b);
            M.set(i, 1, c);
        }
        System.out.println(M);
    }

}

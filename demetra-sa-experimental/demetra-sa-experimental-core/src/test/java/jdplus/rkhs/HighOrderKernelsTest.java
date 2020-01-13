/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.rkhs;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.filters.LocalPolynomialFilterFactory;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.polynomials.Polynomial;
import jdplus.stats.Kernel;
import jdplus.stats.Kernels;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HighOrderKernelsTest {

    public HighOrderKernelsTest() {
    }

    @Test
    public void testBiWeight() {
        Matrix H = HighOrderKernels.hankel(jdplus.stats.Kernels.BIWEIGHT, 0, 4);
        assertEquals(SymmetricMatrix.determinant(H), 2.243734e-05, 1e-12);
    }

    @Test
    public void testBiWeight1() {
        Kernel K = Kernels.BIWEIGHT;
//        System.out.println("BiWeight");
        for (int i = 1; i <= 12; ++i) {
            Polynomial p = HighOrderKernels.oldP(K, i), fp = HighOrderKernels.p(K, i);
            assertTrue(p.equals(fp, 1e-6));
        }
        int m = 11;
        SymmetricFilter sf = LocalPolynomialFilterFactory.of(m, 3, DiscreteKernel.biweight(m));
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 3);
        double step = 1.0 / (m + 1);
        Polynomial p = HighOrderKernels.p(K, 3);
        for (int i = 0; i <= m; ++i) {
//            System.out.print(sf.weights().applyAsDouble(i));
//            System.out.print('\t');
//            System.out.println(kernel.applyAsDouble(step * i));
            double z = p.evaluateAt(step * i),
                    f = K.asFunction().applyAsDouble(step * i),
                    k = kernel.applyAsDouble(step * i);
            assertEquals(z * f, k, 1e-9);
        }
    }

    @Test
    public void testTriWeight() {
        Matrix H = HighOrderKernels.hankel(jdplus.stats.Kernels.TRIWEIGHT, 0, 4);
        assertEquals(SymmetricMatrix.determinant(H), 6.765031e-06, 1e-12);
    }

    @Test
    public void testTriWeight1() {
        Kernel K = Kernels.TRIWEIGHT;
//        System.out.println("TriWeight");
        for (int i = 1; i <= 12; ++i) {
            Polynomial p = HighOrderKernels.oldP(K, i), fp = HighOrderKernels.p(K, i);
            assertTrue(p.equals(fp, 1e-6));
        }
        int m = 11;
        SymmetricFilter sf = LocalPolynomialFilterFactory.of(m, 3, DiscreteKernel.triweight(m));
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 3);
        double step = 1.0 / (m + 1);
        Polynomial p = HighOrderKernels.p(K, 3);
        for (int i = 0; i <= m; ++i) {
//            System.out.print(sf.weights().applyAsDouble(i));
//            System.out.print('\t');
//            System.out.println(kernel.applyAsDouble(step * i));
            double z = p.evaluateAt(step * i),
                    f = K.asFunction().applyAsDouble(step * i),
                    k = kernel.applyAsDouble(step * i);
            assertEquals(z * f, k, 1e-9);
        }
    }

    @Test
    public void testParabolic() {
        Kernel K = Kernels.EPANECHNIKOV;
//        System.out.println("Epanechnikov");
        for (int i = 1; i <= 12; ++i) {
            Polynomial p = HighOrderKernels.oldP(K, i), fp = HighOrderKernels.p(K, i);
            assertTrue(p.equals(fp, 1e-6));
//            System.out.println(p.times(Kernels.epanechnikovAsPolynomial()));
        }
        int m = 11;
        SymmetricFilter sf = LocalPolynomialFilterFactory.of(m, 3, DiscreteKernel.epanechnikov(m));
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 3);
        double step = 1.0 / (m + 1);
        Polynomial p = HighOrderKernels.p(K, 3);
        for (int i = 0; i <= m; ++i) {
//            System.out.print(sf.weights().applyAsDouble(i));
//            System.out.print('\t');
//            System.out.println(kernel.applyAsDouble(step * i));
            double z = p.evaluateAt(step * i),
                    f = K.asFunction().applyAsDouble(step * i),
                    k = kernel.applyAsDouble(step * i);
            assertEquals(z * f, k, 1e-9);
        }
    }

    @Test
    public void testEpanechnikov5() {
        Kernel K = Kernels.EPANECHNIKOV;
        int R = 5;
        int m = 51;
        SymmetricFilter sf = LocalPolynomialFilterFactory.of(m, R, DiscreteKernel.epanechnikov(m));
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, R);
        double step = 1.0 / (m + 1);
        Polynomial p = HighOrderKernels.p(K, R);
        for (int i = 0; i <= m; ++i) {
//            System.out.print(sf.weights().applyAsDouble(i));
//            System.out.print('\t');
//            System.out.println(kernel.applyAsDouble(step * i));
            double z = p.evaluateAt(step * i),
                    f = K.asFunction().applyAsDouble(step * i),
                    k = kernel.applyAsDouble(step * i);
            assertEquals(z * f, k, 1e-6);
        }
    }

    @Test
    public void testHenderson() {
        for (int m = 6; m <= 20; ++m) {
            Kernel K = Kernels.henderson(m);
            for (int i = 0; i <= Math.min(m, 10); ++i) {
                assertTrue(HighOrderKernels.oldP(K, i).equals(HighOrderKernels.p(K, i), 1e-6));
            }
            Polynomial p = HighOrderKernels.p(K, 3);
            DoubleUnaryOperator kernel = HighOrderKernels.kernel(K, 3);
            double step = 1.0 / (m + 1);
            for (int i = 0; i <= m; ++i) {
//            System.out.print(sf.weights().applyAsDouble(i));
//            System.out.print('\t');
//            System.out.print(p.evaluateAt(step * i));
//            System.out.print('\t');
//            System.out.print(K.asFunction().applyAsDouble(step * i));
//            System.out.print('\t');
//            System.out.println(kernel.applyAsDouble(step * i));
                double z = p.evaluateAt(step * i),
                        f = K.asFunction().applyAsDouble(step * i),
                        k = kernel.applyAsDouble(step * i);
                assertEquals(z * f, k, 1e-9);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Henderson-9 hierarchy");
        Kernel H = Kernels.henderson(4);
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Henderson-13 hierarchy");
        H = Kernels.henderson(6);
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.hendersonKernel(i, 6).coefficients());
        }
        System.out.println("Henderson-23 hierarchy");
        H = Kernels.henderson(11);
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Biweight hierarchy");
        H = Kernels.BIWEIGHT;
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Triweight hierarchy");
        H = Kernels.TRIWEIGHT;
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Epanechnikov hierarchy");
        H = Kernels.EPANECHNIKOV;
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Triangular hierarchy");
        H = Kernels.TRIANGULAR;
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
        System.out.println("Uniform hierarchy");
        H = Kernels.UNIFORM;
        for (int i = 0; i <= 10; ++i) {
            System.out.println(HighOrderKernels.p(H, i).coefficients());
        }
    }

    @Test
    public void testUnequallySpaced() {
        DoubleUnaryOperator kernel = HighOrderKernels.kernel(Kernels.TRIWEIGHT, 4);
        Matrix M = randomize(x -> 10 * Math.sin(x), 1000, 0, Math.PI * 16);
        double step = Math.PI * 16 / 1000;
        for (int j = 0; j < 900; ++j) {
            double a = j * step, b = a + step * 100;
            int start = 0;
            DataBlock col = M.column(0);
            while (col.get(start) < a && start < col.length()) {
                ++start;
            }
            if (start == col.length()) {
                return;
            }
            int end = start + 1;
            while (col.get(end) < b && end < col.length()) {
                ++end;
            }
            double num = 0, denom = 0;
            for (int i = start; i < end; ++i) {
                double x = (col.get(i) - (a + b) / 2) / (50 * step);
                double k = kernel.applyAsDouble(x);
                denom += k;
                num += M.get(i, 1) * k;
            }
            System.out.print(M.get((start + end) / 2, 1));
            System.out.print('\t');
            System.out.println(num / denom);
        }
    }

    public static Matrix randomize(DoubleUnaryOperator fn, int n, double a, double b) {
        Matrix M = Matrix.make(n, 2);
        Random rnd = new Random(0);
        double[] array = rnd.doubles(n, a, b).toArray();
        Arrays.sort(array);
        M.column(0).copyFrom(array, 0);
        M.column(1).set(i -> fn.applyAsDouble(array[i]) + rnd.nextGaussian());
        return M;
    }

}

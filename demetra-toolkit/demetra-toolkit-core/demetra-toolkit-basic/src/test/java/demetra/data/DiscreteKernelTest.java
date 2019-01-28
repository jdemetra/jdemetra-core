/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import static demetra.data.DiscreteKernel.biweight;
import static demetra.data.DiscreteKernel.henderson;
import static demetra.data.DiscreteKernel.parabolic;
import static demetra.data.DiscreteKernel.triangular;
import static demetra.data.DiscreteKernel.tricube;
import static demetra.data.DiscreteKernel.triweight;
import static demetra.data.DiscreteKernel.uniform;
import demetra.maths.matrices.Matrix;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;
import static demetra.data.DiscreteKernel.distance;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DiscreteKernelTest {

    private final int K = 12;

    public DiscreteKernelTest() {
    }

    @Test
    public void testHenderson() {
        IntToDoubleFunction kernel = DiscreteKernel.Henderson.asFunction(K);
//        System.out.println("Henderson");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            //assertTrue(q>0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTricube() {
        IntToDoubleFunction kernel = DiscreteKernel.Tricube.asFunction(K);
//        System.out.println("Tricube");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//           System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTriweight() {
        IntToDoubleFunction kernel = DiscreteKernel.Triweight.asFunction(K);
//        System.out.println("Triweight");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testBiweight() {
        IntToDoubleFunction kernel = DiscreteKernel.Biweight.asFunction(K);
//        System.out.println("Biweight");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testParabolic() {
        IntToDoubleFunction kernel = DiscreteKernel.Parabolic.asFunction(K);
//        System.out.println("Parabolic");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTriangular() {
        IntToDoubleFunction kernel = DiscreteKernel.Triangular.asFunction(K);
//        System.out.println("Triangular");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

//    @Test
//    public void testGaussian() {
//        IntToDoubleFunction kernel = gaussian(4 * K);
////        System.out.println("Gaussian");
//        for (int i = -K; i <= K; ++i) {
//            assertTrue(kernel.applyAsDouble(i) > 0);
////            System.out.println(kernel.applyAsDouble(i));
//        }
//    }

    @Test
    public void testDistance() {
        IntToDoubleFunction[] k = new IntToDoubleFunction[7];
        k[0] = uniform(K);
        k[1] = triangular(K);
        k[2] = parabolic(K);
        k[3] = biweight(K);
        k[4] = triweight(K);
        k[5] = tricube(K);
        k[6] = henderson(K);
        Matrix D = Matrix.square(k.length);

        for (int i = 0; i < k.length; ++i) {
            for (int j = 0; j < k.length; ++j) {
                D.set(i, j, distance(k[i], k[j], K));
            }
        }
 //       System.out.println(D);
    }
}

/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.data.analysis;

import static jdplus.data.analysis.DiscreteKernel.biweight;
import static jdplus.data.analysis.DiscreteKernel.henderson;
import static jdplus.data.analysis.DiscreteKernel.parabolic;
import static jdplus.data.analysis.DiscreteKernel.triangular;
import static jdplus.data.analysis.DiscreteKernel.tricube;
import static jdplus.data.analysis.DiscreteKernel.triweight;
import static jdplus.data.analysis.DiscreteKernel.uniform;
import jdplus.maths.matrices.CanonicalMatrix;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;
import static jdplus.data.analysis.DiscreteKernel.distance;
import static jdplus.data.analysis.DiscreteKernel.distance;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DiscreteKernelTest {

    private final int K = 22;

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
            System.out.println(kernel.applyAsDouble(i));
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
        CanonicalMatrix D = CanonicalMatrix.square(k.length);

        for (int i = 0; i < k.length; ++i) {
            for (int j = 0; j < k.length; ++j) {
                D.set(i, j, distance(k[i], k[j], K));
            }
        }
 //       System.out.println(D);
    }
}

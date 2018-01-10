/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters;

import static demetra.maths.linearfilters.DiscreteKernels.biweight;
import static demetra.maths.linearfilters.DiscreteKernels.gaussian;
import static demetra.maths.linearfilters.DiscreteKernels.henderson;
import static demetra.maths.linearfilters.DiscreteKernels.parabolic;
import static demetra.maths.linearfilters.DiscreteKernels.triangular;
import static demetra.maths.linearfilters.DiscreteKernels.tricube;
import static demetra.maths.linearfilters.DiscreteKernels.triweight;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DiscreteKernelsTest {
    
    private final int K=7;
    
    public DiscreteKernelsTest() {
    }

    @Test
    public void testHenderson() {
        IntToDoubleFunction kernel = henderson(K);
//        System.out.println("Henderson");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }
    
    @Test
    public void testTricube() {
        IntToDoubleFunction kernel = tricube(K);
//        System.out.println("Tricube");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }

    @Test
    public void testTriweight() {
        IntToDoubleFunction kernel = triweight(K);
//        System.out.println("Triweight");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }

    @Test
    public void testBiweight() {
        IntToDoubleFunction kernel = biweight(K);
//        System.out.println("Biweight");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }

    @Test
    public void testParabolic() {
        IntToDoubleFunction kernel = parabolic(K);
//        System.out.println("Parabolic");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }

    @Test
    public void testTriangular() {
        IntToDoubleFunction kernel = triangular(K);
//        System.out.println("Triangular");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }
    
    @Test
    public void testGaussian() {
        IntToDoubleFunction kernel = gaussian(4*K);
//        System.out.println("Gaussian");
        for (int i=-K; i<=K; ++i){
            assertTrue(kernel.applyAsDouble(i)>0);
//            System.out.println(kernel.applyAsDouble(i));
        }
    }
}

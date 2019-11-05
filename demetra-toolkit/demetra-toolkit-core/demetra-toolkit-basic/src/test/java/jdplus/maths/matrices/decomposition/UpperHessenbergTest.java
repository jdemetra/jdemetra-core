/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.decomposition;

import java.util.Random;
import jdplus.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class UpperHessenbergTest {

    public UpperHessenbergTest() {
    }

    @Test
    public void testRandom() {
        Matrix A = Matrix.square(100);
        Random rnd = new Random(0);
        A.set(rnd::nextDouble);
        long t0 = System.currentTimeMillis();
        UpperHessenberg uh;
        for (int j = 0; j < 100; ++j) {
            uh = UpperHessenberg.of(A);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(uh.getH());

        t0 = System.currentTimeMillis();
        Matrix B;
        for (int j = 0; j < 100; ++j) {
            B = A.deepClone();
            EigenRoutines.hessenberg(B.getStorage(), B.getColumnsCount());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(B);
    }

}

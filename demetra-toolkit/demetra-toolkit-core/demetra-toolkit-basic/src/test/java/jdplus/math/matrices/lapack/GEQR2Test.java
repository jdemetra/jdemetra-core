/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import java.util.Random;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class GEQR2Test {
    
    public GEQR2Test() {
    }
    
    @Test
    public void testRandom() {
        Matrix A = Matrix.make(100, 20);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        Matrix B = A.deepClone();
        double[] tau = new double[20];
        GEQR2.apply(A, tau);
        System.out.println(A.top(20));
        Householder hous = new Householder(B);
        Matrix r = hous.r(false);
        System.out.println("");
        System.out.println(r);
    }
    
    public static void main(String[] arg) {
        int M = 300, N = 10, K = 500000;
        Matrix A = Matrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            double[] tau = new double[A.getColumnsCount()];
            GEQR2.apply(A.deepClone(), tau);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            Householder hous = new Householder(A);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            HouseholderWithPivoting hous = new HouseholderWithPivoting(A, 1e-16);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
    
}
